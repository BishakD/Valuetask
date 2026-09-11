package com.valuetask.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valuetask.data.TodoItem
import com.valuetask.data.TodoItem.Companion.FAILED_CATEGORY
import com.valuetask.data.TodoItem.Companion.KNOWN_CATEGORIES
import com.valuetask.data.TodoItem.Companion.PENDING_CATEGORY
import com.valuetask.network.CategorizeService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodoViewModel : ViewModel() {

    // ── Raw item list ────────────────────────────────────────────────────────
    private val _items = MutableStateFlow<List<TodoItem>>(emptyList())

    /**
     * Items grouped for display, in a stable order:
     *  1. Known categories (Market, Chores, Work, …) — only if non-empty
     *  2. Unknown categories (should not happen in practice)
     *  3. "Uncategorized" (failed items)
     *  4. Pending (loading) items shown under the sentinel key [PENDING_CATEGORY]
     */
    val groupedItems: StateFlow<Map<String, List<TodoItem>>> = _items
        .map { list ->
            val groups = linkedMapOf<String, MutableList<TodoItem>>()

            // Seed with known categories in the correct order so sections
            // appear even before items arrive (they'll be filtered out if empty).
            KNOWN_CATEGORIES.forEach { groups[it] = mutableListOf() }
            groups[FAILED_CATEGORY] = mutableListOf()
            groups[PENDING_CATEGORY] = mutableListOf()

            list.forEach { item ->
                when {
                    item.isPending -> groups[PENDING_CATEGORY]!!.add(item)
                    else -> groups.getOrPut(item.category) { mutableListOf() }.add(item)
                }
            }

            // Drop empty sections — the UI only renders non-empty ones.
            groups.filterValues { it.isNotEmpty() }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    // ── Input text ───────────────────────────────────────────────────────────
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    // ── Send ─────────────────────────────────────────────────────────────────
    fun sendTodo() {
        val text = _inputText.value.trim()
        if (text.isBlank()) return
        _inputText.value = ""

        val pending = TodoItem(text = text, isPending = true)
        addOrUpdate(pending)
        categorize(pending)
    }

    // ── Retry ────────────────────────────────────────────────────────────────
    fun retry(item: TodoItem) {
        val retrying = item.copy(
            category = PENDING_CATEGORY,
            isPending = true,
            isFailed = false,
        )
        addOrUpdate(retrying)
        categorize(retrying)
    }

    // ── Internal helpers ─────────────────────────────────────────────────────
    private fun addOrUpdate(item: TodoItem) {
        val existing = _items.value.indexOfFirst { it.id == item.id }
        _items.value = if (existing == -1) {
            _items.value + item
        } else {
            _items.value.toMutableList().also { it[existing] = item }
        }
    }

    private fun categorize(item: TodoItem) {
        viewModelScope.launch {
            try {
                val category = CategorizeService.categorize(item.text)
                addOrUpdate(item.copy(category = category, isPending = false, isFailed = false))
            } catch (e: Exception) {
                addOrUpdate(
                    item.copy(category = FAILED_CATEGORY, isPending = false, isFailed = true)
                )
            }
        }
    }
}

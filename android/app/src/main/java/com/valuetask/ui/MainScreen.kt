package com.valuetask.ui

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.valuetask.R
import com.valuetask.data.TodoItem
import com.valuetask.ui.components.CategorySection
import com.valuetask.ui.components.InputBar
import com.valuetask.viewmodel.TodoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(vm: TodoViewModel = viewModel()) {

    val groupedItems by vm.groupedItems.collectAsStateWithLifecycle()
    val inputText by vm.inputText.collectAsStateWithLifecycle()

    // Sections are expanded by default; collapses are remembered across recompositions.
    val expandedState = remember { mutableStateMapOf<String, Boolean>() }
    fun isExpanded(cat: String) = expandedState.getOrPut(cat) { true }

    // ── Voice recognition ────────────────────────────────────────────────────
    // RecognizerIntent opens the system speech dialog — no RECORD_AUDIO
    // permission needed because the system app owns the microphone session.
    val context = LocalContext.current
    var isListening by remember { androidx.compose.runtime.mutableStateOf(false) }

    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            spoken?.let { vm.updateInputText(it) }
        }
    }

    fun launchSpeechRecognizer() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
            )
            putExtra(RecognizerIntent.EXTRA_PROMPT, context.getString(R.string.speech_prompt))
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        isListening = true
        speechLauncher.launch(intent)
    }

    // ── Scaffold ─────────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
        bottomBar = {
            InputBar(
                text = inputText,
                onTextChange = vm::updateInputText,
                onSend = vm::sendTodo,
                onMicTap = ::launchSpeechRecognizer,
                isListening = isListening,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding(),
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (groupedItems.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(
                        items = groupedItems.keys.toList(),
                        key = { it },
                    ) { category ->
                        val sectionItems = groupedItems[category] ?: return@items
                        CategorySection(
                            category = category,
                            items = sectionItems,
                            isExpanded = isExpanded(category),
                            onToggle = {
                                expandedState[category] = !(expandedState[category] ?: true)
                            },
                            onRetry = vm::retry,
                        )
                    }
                }
            }
        }
    }
}

// ── Empty state ──────────────────────────────────────────────────────────────

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Tap the mic or type below to add your first to-do ✨",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .align(androidx.compose.ui.Alignment.Center),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

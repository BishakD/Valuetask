package com.valuetask.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valuetask.R
import com.valuetask.data.TodoItem
import com.valuetask.data.TodoItem.Companion.PENDING_CATEGORY

/**
 * A collapsible section showing all items in one category.
 *
 * @param category    Category name (or [PENDING_CATEGORY] for in-flight items).
 * @param items       The items to display inside this section.
 * @param isExpanded  Whether the body is currently visible.
 * @param onToggle    Called when the header row is tapped.
 * @param onRetry     Called when the user taps "Retry" on a failed item.
 */
@Composable
fun CategorySection(
    category: String,
    items: List<TodoItem>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onRetry: (TodoItem) -> Unit,
) {
    val isPendingSection = category == PENDING_CATEGORY
    val displayName = if (isPendingSection) stringResource(R.string.categorizing) else category
    val emoji = if (isPendingSection) "⏳" else TodoItem.emoji(category)

    Column {
        // ── Section header ───────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isPendingSection, onClick = onToggle)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, fontSize = 20.sp)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "(${items.size})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }

            if (!isPendingSection) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                )
            }
        }

        // ── Section body (animated expand/collapse) ──────────────────────────
        AnimatedVisibility(
            visible = isExpanded || isPendingSection,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column {
                items.forEach { item ->
                    TodoItemRow(item = item, onRetry = onRetry)
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

// ── Single item row ──────────────────────────────────────────────────────────

@Composable
private fun TodoItemRow(item: TodoItem, onRetry: (TodoItem) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = item.text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )

            when {
                item.isPending -> CircularProgressIndicator(
                    modifier = Modifier
                        .size(18.dp)
                        .padding(start = 8.dp),
                    strokeWidth = 2.dp,
                )

                item.isFailed -> AssistChip(
                    onClick = { onRetry(item) },
                    label = { Text(stringResource(R.string.retry)) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(AssistChipDefaults.IconSize),
                        )
                    },
                )
            }
        }
    }
}

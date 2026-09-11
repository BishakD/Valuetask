package com.valuetask.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valuetask.R

/**
 * The chat-style input bar pinned to the bottom of the screen.
 *
 * Layout (left → right):
 *   [🎤 mic button]  [text field ─── fills remaining space]  [➤ send button]
 *
 * The mic button pulses red while [isListening] is true.
 */
@Composable
fun InputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onMicTap: () -> Unit,
    isListening: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // ── Mic button ───────────────────────────────────────────────────
            MicButton(isListening = isListening, onClick = onMicTap)

            // ── Text field ───────────────────────────────────────────────────
            TextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.input_hint)) },
                maxLines = 5,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { if (text.isNotBlank()) onSend() }),
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),
            )

            // ── Send button ──────────────────────────────────────────────────
            val sendColor by animateColorAsState(
                targetValue = if (text.isNotBlank()) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                label = "sendColor",
            )
            IconButton(
                onClick = onSend,
                enabled = text.isNotBlank(),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(R.string.send),
                    tint = sendColor,
                    modifier = Modifier.size(26.dp),
                )
            }
        }
    }
}

// ── Mic button with pulse animation ─────────────────────────────────────────

@Composable
private fun MicButton(isListening: Boolean, onClick: () -> Unit) {
    // Infinite alpha pulse while listening
    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 0.3f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "micAlpha",
    )

    val tint by animateColorAsState(
        targetValue = if (isListening) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.primary,
        label = "micTint",
    )

    IconButton(
        onClick = onClick,
        modifier = Modifier.size(56.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = stringResource(R.string.mic),
            tint = tint,
            modifier = Modifier
                .size(32.dp)
                .graphicsLayer { this.alpha = if (isListening) alpha else 1f },
        )
    }

    // Label under the mic when active
    if (isListening) {
        Text(
            text = "Listening…",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
            fontSize = 9.sp,
        )
    }
}

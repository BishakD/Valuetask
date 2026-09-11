package com.valuetask.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun ValueTaskTheme(content: @Composable () -> Unit) {
    // Use Material You dynamic colors on Android 12+; fall back to a teal scheme.
    val colorScheme = try {
        dynamicLightColorScheme(LocalContext.current)
    } catch (_: Exception) {
        lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}

package com.valuetask

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.valuetask.ui.MainScreen
import com.valuetask.ui.theme.ValueTaskTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Draw content behind system bars (status bar + nav bar).
        // Compose handles insets via WindowInsetsPadding modifiers.
        enableEdgeToEdge()
        setContent {
            ValueTaskTheme {
                MainScreen()
            }
        }
    }
}

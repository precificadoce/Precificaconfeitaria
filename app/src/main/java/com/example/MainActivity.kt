package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.preferences.ThemeMode
import com.example.ui.PrecificaDoceApp
import com.example.ui.theme.PrecificaDoceTheme
import com.example.viewmodel.PrecificaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: PrecificaViewModel = viewModel(
                factory = PrecificaViewModel.provideFactory(application)
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val darkTheme = when (uiState.settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            PrecificaDoceTheme(darkTheme = darkTheme) {
                PrecificaDoceApp(viewModel = viewModel)
            }
        }
    }
}

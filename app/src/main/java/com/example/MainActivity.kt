package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.MainAppScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ElectricityViewModel
import com.example.ui.viewmodel.ElectricityViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: ElectricityViewModel by viewModels {
        ElectricityViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsState by viewModel.settings.collectAsStateWithLifecycle()
            
            MyApplicationTheme(darkTheme = settingsState.darkMode) {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

package com.example.emergencysupport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.emergencysupport.ui.navigation.AppNavGraph
import com.example.emergencysupport.ui.theme.EmergencySupportTheme
import com.example.emergencysupport.ui.viewmodel.MainViewModel
import com.example.emergencysupport.ui.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(application as EmergencySupportApp)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EmergencySupportTheme {
                AppNavGraph(viewModel = viewModel)
            }
        }
    }
}

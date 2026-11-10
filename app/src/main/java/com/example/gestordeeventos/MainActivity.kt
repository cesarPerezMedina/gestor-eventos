package com.example.gestordeeventos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.gestordeeventos.navegacion.AppNavHost
import com.example.gestordeeventos.ui.theme.GestorDeEventosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GestorDeEventosTheme {
                AppNavHost()
            }
        }
    }
}
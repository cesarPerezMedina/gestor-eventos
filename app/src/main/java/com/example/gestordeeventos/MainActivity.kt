package com.example.gestordeeventos

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gestordeeventos.data.repositorio.AjustesRepositorio
import com.example.gestordeeventos.navegacion.AppNavHost
import com.example.gestordeeventos.ui.theme.GestorDeEventosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val ajustes = remember { AjustesRepositorio.obtener(applicationContext) }
            val modoTema by ajustes.modoTema.collectAsStateWithLifecycle()
            val oscuro = modoTema.esOscuro(isSystemInDarkTheme())

            // Los iconos de las barras del sistema deben verse bien con el tema elegido (no solo con el del móvil)
            DisposableEffect(oscuro) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { oscuro },
                    navigationBarStyle = SystemBarStyle.auto(CLARO_SCRIM, OSCURO_SCRIM) { oscuro },
                )
                onDispose {}
            }

            GestorDeEventosTheme(darkTheme = oscuro) {
                AppNavHost()
            }
        }
    }

    private companion object {
        val CLARO_SCRIM = Color.argb(0xE6, 0xFF, 0xFF, 0xFF)
        val OSCURO_SCRIM = Color.argb(0x80, 0x1B, 0x1B, 0x1B)
    }
}

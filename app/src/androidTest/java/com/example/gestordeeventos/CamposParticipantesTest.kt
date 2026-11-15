package com.example.gestordeeventos

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.gestordeeventos.ui.componentes.CamposParticipantes
import com.example.gestordeeventos.ui.theme.GestorDeEventosTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Comprueba los campos de participantes en pantalla (necesita emulador o móvil). */
@RunWith(AndroidJUnit4::class)
class CamposParticipantesTest {

    @get:Rule
    val regla = createComposeRule()

    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext

    private fun mostrar(inicialMinimo: String = "", inicialMaximo: String = "", mostrarErrores: Boolean = false) {
        regla.setContent {
            GestorDeEventosTheme {
                var minimo by remember { mutableStateOf(inicialMinimo) }
                var maximo by remember { mutableStateOf(inicialMaximo) }
                CamposParticipantes(
                    minimo = minimo,
                    maximo = maximo,
                    onMinimoChange = { minimo = it },
                    onMaximoChange = { maximo = it },
                    mostrarErrores = mostrarErrores
                )
            }
        }
    }

    @Test
    fun muestraLosCamposMinimoYMaximo() {
        mostrar()
        regla.onNodeWithText(contexto.getString(R.string.participantes_minimo)).assertIsDisplayed()
        regla.onNodeWithText(contexto.getString(R.string.participantes_maximo)).assertIsDisplayed()
    }

    @Test
    fun soloAceptaNumeros() {
        mostrar()
        regla.onNodeWithText(contexto.getString(R.string.participantes_minimo)).performTextInput("a1b2")
        regla.onNodeWithText("12").assertIsDisplayed()
    }

    @Test
    fun avisaSiElMaximoEsMenorQueElMinimo() {
        mostrar("10", "4", true)
        regla.onNodeWithText(contexto.getString(R.string.participantes_error_rango)).assertIsDisplayed()
    }

    @Test
    fun avisaCuandoLosCamposNoSonValidos() {
        mostrar("", "", true)
        regla.onNodeWithText(
            contexto.getString(
                R.string.participantes_error_numero,
                1,
                1000
            )
        ).assertIsDisplayed()
    }
}

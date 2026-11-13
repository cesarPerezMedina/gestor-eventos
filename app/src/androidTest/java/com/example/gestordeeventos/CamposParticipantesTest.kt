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
import com.example.gestordeeventos.ui.componentes.ParticipantesUiState
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

    private fun mostrar(inicial: ParticipantesUiState) {
        regla.setContent {
            GestorDeEventosTheme {
                var estado by remember { mutableStateOf(inicial) }
                CamposParticipantes(
                    estado = estado,
                    onMinimoChange = { estado = estado.copy(minimo = it) },
                    onMaximoChange = { estado = estado.copy(maximo = it) }
                )
            }
        }
    }

    @Test
    fun muestraLosCamposMinimoYMaximo() {
        mostrar(ParticipantesUiState())

        regla.onNodeWithText(contexto.getString(R.string.participantes_minimo)).assertIsDisplayed()
        regla.onNodeWithText(contexto.getString(R.string.participantes_maximo)).assertIsDisplayed()
    }

    @Test
    fun soloAceptaNumeros() {
        mostrar(ParticipantesUiState())

        regla.onNodeWithText(contexto.getString(R.string.participantes_minimo)).performTextInput("a1b2")

        regla.onNodeWithText("12").assertIsDisplayed()
    }

    @Test
    fun avisaSiElMaximoEsMenorQueElMinimo() {
        mostrar(ParticipantesUiState(minimo = "10", maximo = "4"))

        regla.onNodeWithText(contexto.getString(R.string.participantes_error_maximo_menor)).assertIsDisplayed()
    }

    @Test
    fun avisaDeLosCamposVaciosSoloCuandoSePide() {
        mostrar(ParticipantesUiState(mostrarErrores = true))

        regla.onNodeWithText(contexto.getString(R.string.participantes_error_minimo_vacio)).assertIsDisplayed()
        regla.onNodeWithText(contexto.getString(R.string.participantes_error_maximo_vacio)).assertIsDisplayed()
    }
}

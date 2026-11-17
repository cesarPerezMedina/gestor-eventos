package com.example.gestordeeventos

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.example.gestordeeventos.data.model.ModoTema
import com.example.gestordeeventos.ui.ajustes.AjustesContenido
import com.example.gestordeeventos.ui.theme.GestorDeEventosTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Comprueba el selector de tema de Ajustes (necesita emulador o móvil). */
class AjustesScreenTest {

    @get:Rule
    val reglaCompose = createComposeRule()

    private fun texto(id: Int) = InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    private fun mostrar(inicial: ModoTema = ModoTema.SISTEMA, alCambiar: (ModoTema) -> Unit = {}) {
        reglaCompose.setContent {
            var modo by remember { mutableStateOf(inicial) }
            GestorDeEventosTheme {
                AjustesContenido(
                    modoTema = modo,
                    onModoTemaChange = {
                        modo = it
                        alCambiar(it)
                    },
                    onBackClick = {},
                    onCerrarSesion = {},
                )
            }
        }
    }

    @Test
    fun muestraLasTresOpciones() {
        mostrar()

        reglaCompose.onNodeWithText(texto(R.string.ajustes_tema_sistema)).assertExists()
        reglaCompose.onNodeWithText(texto(R.string.ajustes_tema_claro)).assertExists()
        reglaCompose.onNodeWithText(texto(R.string.ajustes_tema_oscuro)).assertExists()
    }

    @Test
    fun laOpcionActualApareceSeleccionada() {
        mostrar(inicial = ModoTema.OSCURO)

        reglaCompose.onNodeWithText(texto(R.string.ajustes_tema_oscuro)).assertIsSelected()
    }

    @Test
    fun pulsarUnaOpcionAvisaConElModoElegido() {
        var elegido: ModoTema? = null
        mostrar(alCambiar = { elegido = it })

        reglaCompose.onNodeWithText(texto(R.string.ajustes_tema_oscuro)).performClick()

        assertEquals(ModoTema.OSCURO, elegido)
    }
}

package com.example.gestordeeventos

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.gestordeeventos.ui.componentes.SelectorVisibilidad
import com.example.gestordeeventos.ui.theme.GestorDeEventosTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Comprueba el selector de visibilidad en pantalla (necesita emulador o móvil). */
@RunWith(AndroidJUnit4::class)
class SelectorVisibilidadTest {

    @get:Rule
    val regla = createComposeRule()

    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private val textoPublico = contexto.getString(R.string.visibilidad_publico)
    private val textoPrivado = contexto.getString(R.string.visibilidad_privado)

    @Test
    fun muestraLasDosOpciones() {
        regla.setContent { GestorDeEventosTheme { SelectorVisibilidad(publico = true, onCambiar = {}) } }

        regla.onNodeWithText(textoPublico).assertIsDisplayed()
        regla.onNodeWithText(textoPrivado).assertIsDisplayed()
    }

    @Test
    fun marcaSoloLaOpcionActual() {
        regla.setContent { GestorDeEventosTheme { SelectorVisibilidad(publico = false, onCambiar = {}) } }

        regla.onNodeWithText(textoPrivado).assertIsSelected()
        regla.onNodeWithText(textoPublico).assertIsNotSelected()
    }

    @Test
    fun alPulsarPrivadoAvisaConFalse() {
        var publico: Boolean? = null
        regla.setContent {
            GestorDeEventosTheme { SelectorVisibilidad(publico = true, onCambiar = { publico = it }) }
        }

        regla.onNodeWithText(textoPrivado).performClick()

        assertEquals(false, publico)
    }

    @Test
    fun alPulsarPublicoAvisaConTrue() {
        var publico: Boolean? = null
        regla.setContent {
            GestorDeEventosTheme { SelectorVisibilidad(publico = false, onCambiar = { publico = it }) }
        }

        regla.onNodeWithText(textoPublico).performClick()

        assertEquals(true, publico)
    }
}

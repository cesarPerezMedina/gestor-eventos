package com.example.gestordeeventos

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.gestordeeventos.data.model.Deporte
import com.example.gestordeeventos.ui.componentes.SelectorDeporte
import com.example.gestordeeventos.ui.theme.GestorDeEventosTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Comprueba el selector de deporte en pantalla (necesita emulador o móvil). */
@RunWith(AndroidJUnit4::class)
class SelectorDeporteTest {

    @get:Rule
    val regla = createComposeRule()

    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext

    private fun nombre(deporte: Deporte): String = contexto.getString(deporte.etiqueta)

    @Test
    fun muestraLosSeisDeportes() {
        regla.setContent { GestorDeEventosTheme { SelectorDeporte(seleccionado = null, onSeleccionar = {}) } }

        Deporte.entries.forEach { regla.onNodeWithText(nombre(it)).assertIsDisplayed() }
    }

    @Test
    fun alPulsarUnDeporteAvisaConEseDeporte() {
        var elegido: Deporte? = null
        regla.setContent {
            GestorDeEventosTheme { SelectorDeporte(seleccionado = null, onSeleccionar = { elegido = it }) }
        }

        regla.onNodeWithText(nombre(Deporte.PADEL)).performClick()

        assertEquals(Deporte.PADEL, elegido)
    }

    @Test
    fun marcaSoloElDeporteSeleccionado() {
        regla.setContent {
            GestorDeEventosTheme { SelectorDeporte(seleccionado = Deporte.TENIS, onSeleccionar = {}) }
        }

        regla.onNodeWithText(nombre(Deporte.TENIS)).assertIsSelected()
        regla.onNodeWithText(nombre(Deporte.FUTBOL)).assertIsNotSelected()
    }
}

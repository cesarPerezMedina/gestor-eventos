package com.example.gestordeeventos

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.gestordeeventos.ui.componentes.ConfirmarSalirEventoDialog
import com.example.gestordeeventos.ui.theme.GestorDeEventosTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Pruebas de los cuadros de confirmación al gestionar un evento (Issues 67 y 68). */
@RunWith(AndroidJUnit4::class)
class DialogosEventoTest {

    @get:Rule
    val regla = createComposeRule()

    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext

    private fun texto(id: Int, vararg argumentos: Any) = contexto.getString(id, *argumentos)

    @Test
    fun salirMuestraElNombreDelEventoYLosDosBotones() {
        regla.setContent {
            GestorDeEventosTheme(darkTheme = false) {
                ConfirmarSalirEventoDialog(nombreEvento = "Fútbol 7", onConfirmar = {}, onCancelar = {})
            }
        }

        regla.onNodeWithText(texto(R.string.evento_salir_titulo)).assertIsDisplayed()
        regla.onNodeWithText(texto(R.string.evento_salir_texto, "Fútbol 7")).assertIsDisplayed()
        regla.onNodeWithText(texto(R.string.evento_salir_confirmar)).assertIsDisplayed()
        regla.onNodeWithText(texto(R.string.evento_cancelar)).assertIsDisplayed()
    }

    @Test
    fun confirmarYCancelarAvisanPorSeparado() {
        var confirmado = 0
        var cancelado = 0
        regla.setContent {
            GestorDeEventosTheme(darkTheme = false) {
                ConfirmarSalirEventoDialog(
                    nombreEvento = "Fútbol 7",
                    onConfirmar = { confirmado++ },
                    onCancelar = { cancelado++ },
                )
            }
        }

        regla.onNodeWithText(texto(R.string.evento_cancelar)).performClick()
        assertEquals(0, confirmado)
        assertEquals(1, cancelado)

        regla.onNodeWithText(texto(R.string.evento_salir_confirmar)).performClick()
        assertEquals(1, confirmado)
        assertEquals(1, cancelado)
    }
}

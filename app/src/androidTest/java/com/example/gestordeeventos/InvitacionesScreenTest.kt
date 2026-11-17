package com.example.gestordeeventos

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.gestordeeventos.data.model.Deporte
import com.example.gestordeeventos.data.model.InvitacionPendiente
import com.example.gestordeeventos.ui.invitaciones.InvitacionesScreen
import com.example.gestordeeventos.ui.theme.GestorDeEventosTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de interfaz de la pantalla de Invitaciones (Issue 71).
 * Comprueban lo que ve y toca el usuario; la lógica de aceptar y rechazar se prueba aparte.
 */
@RunWith(AndroidJUnit4::class)
class InvitacionesScreenTest {

    @get:Rule
    val regla = createComposeRule()

    private val futbol = InvitacionPendiente(
        invitacionId = 1, eventoId = 10, eventoNombre = "Fútbol 7", deporte = Deporte.FUTBOL,
        fechaHora = 1_795_300_000_000L, deUsuarioId = 2, deNombreUsuario = "luis_9",
    )
    private val tenis = InvitacionPendiente(
        invitacionId = 2, eventoId = 11, eventoNombre = "Tenis dobles", deporte = Deporte.TENIS,
        fechaHora = 1_795_400_000_000L, deUsuarioId = 3, deNombreUsuario = "marta",
    )

    private fun mostrar(
        invitaciones: List<InvitacionPendiente>,
        onAceptar: (InvitacionPendiente) -> Unit = {},
        onRechazar: (InvitacionPendiente) -> Unit = {},
        onBack: () -> Unit = {},
    ) {
        regla.setContent {
            GestorDeEventosTheme(darkTheme = false) {
                InvitacionesScreen(
                    invitaciones = invitaciones,
                    onAceptarClick = onAceptar,
                    onRechazarClick = onRechazar,
                    onBackClick = onBack,
                )
            }
        }
    }

    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext

    private fun texto(id: Int, vararg argumentos: Any) = contexto.getString(id, *argumentos)

    @Test
    fun muestraCadaInvitacionConSuEventoYQuienInvita() {
        mostrar(listOf(futbol, tenis))

        regla.onNodeWithText("Fútbol 7").assertIsDisplayed()
        regla.onNodeWithText("Tenis dobles").assertIsDisplayed()
        regla.onNodeWithText(texto(R.string.invitaciones_de, "luis_9")).assertIsDisplayed()
        regla.onNodeWithText(texto(R.string.invitaciones_de, "marta")).assertIsDisplayed()
        regla.onNodeWithText(texto(R.string.invitaciones_contador_varias, 2)).assertIsDisplayed()
    }

    @Test
    fun conUnaSolaInvitacionUsaElSingular() {
        mostrar(listOf(futbol))

        regla.onNodeWithText(texto(R.string.invitaciones_contador_una)).assertIsDisplayed()
    }

    @Test
    fun sinInvitacionesMuestraElMensajeVacio() {
        mostrar(emptyList())

        regla.onNodeWithText(texto(R.string.invitaciones_vacio_titulo)).assertIsDisplayed()
        regla.onAllNodesWithText(texto(R.string.invitaciones_aceptar)).assertCountEquals(0)
    }

    @Test
    fun aceptarAvisaConLaInvitacionPulsada() {
        var recibida: InvitacionPendiente? = null
        mostrar(listOf(futbol, tenis), onAceptar = { recibida = it })

        regla.onNodeWithContentDescription(texto(R.string.invitaciones_aceptar_descripcion, "Tenis dobles"))
            .performClick()

        assertEquals(tenis, recibida)
    }

    @Test
    fun rechazarAvisaConLaInvitacionPulsada() {
        var recibida: InvitacionPendiente? = null
        mostrar(listOf(futbol, tenis), onRechazar = { recibida = it })

        regla.onNodeWithContentDescription(texto(R.string.invitaciones_rechazar_descripcion, "Fútbol 7"))
            .performClick()

        assertEquals(futbol, recibida)
    }

    @Test
    fun elBotonVolverAvisa() {
        var volvio = false
        mostrar(listOf(futbol), onBack = { volvio = true })

        regla.onNodeWithContentDescription(texto(R.string.invitaciones_volver)).performClick()

        assertEquals(true, volvio)
    }
}

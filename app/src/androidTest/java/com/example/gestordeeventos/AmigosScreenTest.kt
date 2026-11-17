package com.example.gestordeeventos

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gestordeeventos.ui.amigos.AmigoItem
import com.example.gestordeeventos.ui.amigos.AmigosScreen
import com.example.gestordeeventos.ui.amigos.FiltroBusqueda
import com.example.gestordeeventos.ui.amigos.InvitacionRecibida
import com.example.gestordeeventos.ui.theme.GestorDeEventosTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de interfaz de la sección de Amigos (Issue 37).
 *
 * Comprueban la interacción visible del usuario con AmigosScreen.
 * La lógica de persistencia y de amistad se prueba por separado en los
 * tests de repositorio/ViewModel.
 */
@RunWith(AndroidJUnit4::class)
class AmigosScreenTest {

    @get:Rule
    val regla = createComposeRule()

    private val amigo = AmigoItem(
        id = "10",
        usuario = "ana_p",
        deportes = "Tenis",
        idTag = "#1234",
    )

    private val invitacion = InvitacionRecibida(
        id = "1",
        titulo = "Partido de tenis",
        etiqueta = "Privado",
        de = "@luis_g",
        fecha = "Hoy 19:00",
        lugar = "Pistas Canal",
    )

    private fun mostrarPantalla(
        amigos: List<AmigoItem> = listOf(amigo),
        invitaciones: List<InvitacionRecibida> = listOf(invitacion),
        onBuscarClick: (String, FiltroBusqueda) -> Unit = { _, _ -> },
        onEscanearQrClick: () -> Unit = {},
        onAceptarInvitacion: (InvitacionRecibida) -> Unit = {},
        onRechazarInvitacion: (InvitacionRecibida) -> Unit = {},
        onEliminarClick: (AmigoItem) -> Unit = {},
        onBloquearClick: (AmigoItem) -> Unit = {},
    ) {
        regla.setContent {
            GestorDeEventosTheme {
                AmigosScreen(
                    miId = "#EVA-0001",
                    amigos = amigos,
                    invitaciones = invitaciones,
                    onBuscarClick = onBuscarClick,
                    onEscanearQrClick = onEscanearQrClick,
                    onAceptarInvitacion = onAceptarInvitacion,
                    onRechazarInvitacion = onRechazarInvitacion,
                    onEliminarClick = onEliminarClick,
                    onBloquearClick = onBloquearClick,
                )
            }
        }
    }

    @Test
    fun muestraLaSeccionDeAmigosYLosDatosPrincipales() {
        mostrarPantalla()

        regla.onNodeWithText("Comunidad & Amigos").assertIsDisplayed()
        regla.onNodeWithText("Mis Amigos").assertIsDisplayed()
        regla.onNodeWithText("Buscar / ID").assertIsDisplayed()
        regla.onNodeWithText("Solicitudes").assertIsDisplayed()
        regla.onNodeWithText("Amigos agregados").assertIsDisplayed()
        regla.onNodeWithText("ana_p").assertIsDisplayed()
        regla.onNodeWithText("#1234").assertIsDisplayed()
        regla.onNodeWithText("Invitaciones recibidas").assertIsDisplayed()
        regla.onNodeWithText("Partido de tenis").assertIsDisplayed()
    }

    @Test
    fun alBuscarEntregaLaConsultaSinEspaciosYElFiltroSeleccionado() {
        var consultaRecibida: String? = null
        var filtroRecibido: FiltroBusqueda? = null

        mostrarPantalla(
            onBuscarClick = { consulta, filtro ->
                consultaRecibida = consulta
                filtroRecibido = filtro
            },
        )

        regla.onNodeWithText("#ID").performClick()

        val campo = regla.onAllNodes(hasSetTextAction())[0]
        campo.performTextInput("  #1234  ")

        regla.onNodeWithText("Añadir").performClick()

        assertEquals("#1234", consultaRecibida)
        assertEquals(FiltroBusqueda.ID_TAG, filtroRecibido)
    }

    @Test
    fun noEjecutaLaBusquedaConElCampoVacio() {
        var seHaBuscado = false

        mostrarPantalla(onBuscarClick = { _, _ -> seHaBuscado = true })

        regla.onNodeWithText("Añadir").performClick()

        assertTrue(!seHaBuscado)
    }

    @Test
    fun alPulsarEscanearCodigoQrEjecutaLaAccion() {
        var pulsado = false

        mostrarPantalla(onEscanearQrClick = { pulsado = true })

        regla.onNodeWithText("Escanear código QR en pista").performClick()

        assertTrue(pulsado)
    }

    @Test
    fun alAceptarUnaInvitacionEntregaLaInvitacionCorrecta() {
        var recibida: InvitacionRecibida? = null

        mostrarPantalla(onAceptarInvitacion = { recibida = it })

        regla.onNodeWithText("Aceptar").performClick()

        assertEquals(invitacion, recibida)
    }

    @Test
    fun alRechazarUnaInvitacionEntregaLaInvitacionCorrecta() {
        var recibida: InvitacionRecibida? = null

        mostrarPantalla(onRechazarInvitacion = { recibida = it })

        regla.onNodeWithText("Rechazar").performClick()

        assertEquals(invitacion, recibida)
    }

    @Test
    fun alPulsarEliminarMuestraElDialogoDeConfirmacion() {
        mostrarPantalla()

        regla.onNodeWithText("Eliminar").performClick()

        regla.onNodeWithText("Eliminar lo quita de tu lista de amigos. Bloquear además impide que te vuelva a enviar solicitudes.")
            .assertIsDisplayed()
        regla.onNodeWithText("Cancelar").assertIsDisplayed()
        regla.onNodeWithText("Bloquear").assertIsDisplayed()
    }

    @Test
    fun alConfirmarEliminarEntregaElAmigoSeleccionado() {
        var eliminado: AmigoItem? = null

        mostrarPantalla(onEliminarClick = { eliminado = it })

        regla.onNodeWithText("Eliminar").performClick()
        regla.onNodeWithText("Eliminar lo quita de tu lista de amigos. Bloquear además impide que te vuelva a enviar solicitudes.")
            .assertIsDisplayed()

        // Hay dos textos "Eliminar": el de la fila y el del botón de confirmación.
        regla.onAllNodesWithText("Eliminar")[1].performClick()

        assertEquals(amigo, eliminado)
    }

    @Test
    fun alConfirmarBloquearEntregaElAmigoSeleccionado() {
        var bloqueado: AmigoItem? = null

        mostrarPantalla(onBloquearClick = { bloqueado = it })

        regla.onNodeWithText("Eliminar").performClick()
        regla.onNodeWithText("Bloquear").performClick()

        assertEquals(amigo, bloqueado)
    }
}

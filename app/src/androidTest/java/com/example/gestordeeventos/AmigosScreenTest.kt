package com.example.gestordeeventos

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gestordeeventos.ui.amigos.AmigoItem
import com.example.gestordeeventos.ui.amigos.AmigosScreen
import com.example.gestordeeventos.ui.amigos.FiltroBusqueda
import com.example.gestordeeventos.ui.amigos.SolicitudItem
import com.example.gestordeeventos.ui.theme.GestorDeEventosTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de interfaz de la sección de Amigos (Issue 37, revisadas en la Issue de correcciones).
 *
 * Comprueban la interacción visible del usuario con AmigosScreen: las tres pestañas (Mis Amigos, Buscar / ID y
 * Solicitudes), el envío de solicitudes y aceptar / rechazar. La lógica de persistencia y de amistad se prueba
 * por separado en los tests de repositorio/ViewModel.
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

    private val solicitud = SolicitudItem(
        correo = "luis@correo.com",
        usuario = "luis_g",
        idTag = "#55",
    )

    private fun mostrarPantalla(
        amigos: List<AmigoItem> = listOf(amigo),
        solicitudes: List<SolicitudItem> = listOf(solicitud),
        onBuscarClick: (String, FiltroBusqueda) -> Unit = { _, _ -> },
        onAceptarSolicitud: (SolicitudItem) -> Unit = {},
        onRechazarSolicitud: (SolicitudItem) -> Unit = {},
        onEliminarClick: (AmigoItem) -> Unit = {},
        onBloquearClick: (AmigoItem) -> Unit = {},
    ) {
        regla.setContent {
            GestorDeEventosTheme {
                AmigosScreen(
                    miId = "7",
                    amigos = amigos,
                    solicitudes = solicitudes,
                    onBuscarClick = onBuscarClick,
                    onAceptarSolicitud = onAceptarSolicitud,
                    onRechazarSolicitud = onRechazarSolicitud,
                    onEliminarClick = onEliminarClick,
                    onBloquearClick = onBloquearClick,
                )
            }
        }
    }

    // ---------- Mis Amigos ----------

    @Test
    fun muestraLaSeccionDeAmigosYLosDatosPrincipales() {
        mostrarPantalla()

        regla.onNodeWithText("Comunidad & Amigos").assertIsDisplayed()
        regla.onNodeWithText("Mis Amigos").assertIsDisplayed()
        regla.onNodeWithText("Buscar / ID").assertIsDisplayed()
        regla.onNodeWithText("Solicitudes").assertIsDisplayed()
        regla.onNodeWithText("Amigos agregados").assertIsDisplayed()
        regla.onNodeWithText("ana_p").assertIsDisplayed()
        regla.onNodeWithText("#1234", substring = true).assertIsDisplayed()
    }

    @Test
    fun sinAmigosNiSolicitudesNoSalenDatosDeEjemplo() {
        regla.setContent { GestorDeEventosTheme { AmigosScreen() } }

        regla.onNodeWithText("Todavía no tienes amigos agregados.").assertIsDisplayed()
        regla.onAllNodesWithText("Pádel dobles semifinal").assertCountEquals(0)
        regla.onAllNodesWithText("carlos_m").assertCountEquals(0)
        regla.onAllNodesWithText("Invitaciones recibidas").assertCountEquals(0)
    }

    @Test
    fun noHayBotonDePerfilNiCodigoQr() {
        mostrarPantalla()
        regla.onAllNodesWithContentDescription("Perfil").assertCountEquals(0)

        regla.onNodeWithText("Buscar / ID").performClick()

        regla.onAllNodesWithText("Escanear código QR en pista").assertCountEquals(0)
        regla.onAllNodesWithText("Ver mi código").assertCountEquals(0)
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

    // ---------- Buscar / ID ----------

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

        regla.onNodeWithText("Buscar / ID").performClick()
        regla.onNodeWithText("#ID").performClick()

        val campo = regla.onAllNodes(hasSetTextAction())[0]
        campo.performTextInput("  12  ")

        regla.onNodeWithText("Añadir").performClick()

        assertEquals("12", consultaRecibida)
        assertEquals(FiltroBusqueda.ID_TAG, filtroRecibido)
    }

    @Test
    fun noEjecutaLaBusquedaConElCampoVacio() {
        var seHaBuscado = false

        mostrarPantalla(onBuscarClick = { _, _ -> seHaBuscado = true })

        regla.onNodeWithText("Buscar / ID").performClick()
        regla.onNodeWithText("Añadir").performClick()

        assertTrue(!seHaBuscado)
    }

    @Test
    fun laTarjetaDeBusquedaMuestraMiIdReal() {
        mostrarPantalla()

        regla.onNodeWithText("Buscar / ID").performClick()

        regla.onNodeWithText("Tu ID: 7").assertIsDisplayed()
    }

    @Test
    fun elBotonDeAnadirAmigoAbreLaPestanaDeBusqueda() {
        mostrarPantalla()

        regla.onNodeWithText("Mis Amigos").performClick()
        regla.onNodeWithContentDescription("Añadir amigo").performClick()

        regla.onNodeWithText("Añadir por ID o Usuario").assertIsDisplayed()
    }

    // ---------- Solicitudes ----------

    @Test
    fun laPestanaSolicitudesMuestraLasSolicitudesRecibidas() {
        mostrarPantalla()

        regla.onNodeWithText("Solicitudes", substring = false).performClick()

        regla.onNodeWithText("Solicitudes recibidas").assertIsDisplayed()
        regla.onNodeWithText("@luis_g").assertIsDisplayed()
        regla.onNodeWithText("Quiere ser tu amigo • #55").assertIsDisplayed()
    }

    @Test
    fun sinSolicitudesLaPestanaLoDice() {
        mostrarPantalla(solicitudes = emptyList())

        regla.onNodeWithText("Solicitudes").performClick()

        regla.onNodeWithText("No tienes solicitudes de amistad pendientes.").assertIsDisplayed()
    }

    @Test
    fun alAceptarUnaSolicitudEntregaLaSolicitudCorrecta() {
        var recibida: SolicitudItem? = null

        mostrarPantalla(onAceptarSolicitud = { recibida = it })

        regla.onNodeWithText("Solicitudes").performClick()
        regla.onNodeWithText("Aceptar").performClick()

        assertEquals(solicitud, recibida)
    }

    @Test
    fun alRechazarUnaSolicitudEntregaLaSolicitudCorrecta() {
        var recibida: SolicitudItem? = null

        mostrarPantalla(onRechazarSolicitud = { recibida = it })

        regla.onNodeWithText("Solicitudes").performClick()
        regla.onNodeWithText("Rechazar").performClick()

        assertEquals(solicitud, recibida)
    }
}

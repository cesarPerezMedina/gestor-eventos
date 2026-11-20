package com.example.gestordeeventos

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gestordeeventos.ui.buscar.BuscarEventoScreen
import com.example.gestordeeventos.ui.inicio.InicioScreen
import com.example.gestordeeventos.ui.theme.GestorDeEventosTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de interfaz de las correcciones de Inicio y Buscar: una sola barra inferior, sin botón de perfil,
 * sin precios, y filtros de Buscar que de verdad filtran. Se usan los datos de ejemplo de cada pantalla.
 */
@RunWith(AndroidJUnit4::class)
class InicioYBuscarPantallaTest {

    @get:Rule
    val regla = createComposeRule()

    // ---------- Inicio ----------

    @Test
    fun inicioNoDibujaMenuInferiorPropio() {
        regla.setContent { GestorDeEventosTheme(darkTheme = false) { InicioScreen(nombreUsuario = "ana") } }

        // El menú lo pone AppNavHost; la pantalla sola no debe tener ni etiquetas de menú ni el botón "+"
        regla.onAllNodesWithText("Buscar").assertCountEquals(0)
        regla.onAllNodesWithText("Amigos").assertCountEquals(0)
        regla.onAllNodesWithContentDescription("Crear evento").assertCountEquals(0)
    }

    @Test
    fun inicioNoTieneBotonDePerfil() {
        regla.setContent { GestorDeEventosTheme(darkTheme = false) { InicioScreen(nombreUsuario = "ana") } }

        regla.onAllNodesWithContentDescription("Perfil").assertCountEquals(0)
        regla.onNodeWithText("Hola, ana", substring = true).assertExists()
    }

    @Test
    fun inicioSinEventosMuestraUnMensaje() {
        regla.setContent {
            GestorDeEventosTheme(darkTheme = false) { InicioScreen(nombreUsuario = "ana", eventos = emptyList(), proximo = null) }
        }

        regla.onNodeWithText("no hay eventos públicos", substring = true).assertExists()
    }

    @Test
    fun unirmeAvisaDelEventoPulsado() {
        var pulsado: String? = null
        regla.setContent {
            GestorDeEventosTheme(darkTheme = false) { InicioScreen(nombreUsuario = "ana", onUnirseClick = { pulsado = it.id }) }
        }

        regla.onAllNodesWithText("Unirme")[0].performClick()

        assertEquals("1", pulsado)
    }

    @Test
    fun elChipDeDeporteFiltraLasSugerencias() {
        regla.setContent { GestorDeEventosTheme(darkTheme = false) { InicioScreen(nombreUsuario = "ana") } }

        regla.onNodeWithText("Pádel").performClick()

        regla.onNodeWithText("Pádel dobles").assertExists()
        regla.onAllNodesWithText("Fútbol 7 en el parque").assertCountEquals(0)
    }

    // ---------- Buscar ----------

    @Test
    fun buscarNoMuestraPrecios() {
        regla.setContent { GestorDeEventosTheme(darkTheme = false) { BuscarEventoScreen() } }

        regla.onAllNodesWithText("€", substring = true).assertCountEquals(0)
        regla.onAllNodesWithText("Gratis").assertCountEquals(0)
        regla.onAllNodesWithText("COSTE").assertCountEquals(0)
    }

    @Test
    fun escribirEnElBuscadorFiltraLaLista() {
        regla.setContent { GestorDeEventosTheme(darkTheme = false) { BuscarEventoScreen() } }

        regla.onNodeWithText("Nombre de evento, pista o club...").performTextInput("basket")

        regla.onNodeWithText("Basket 3x3 Callejero Nocturno").assertExists()
        regla.onAllNodesWithText("Fútbol 7 en el Parque").assertCountEquals(0)
    }

    @Test
    fun unaBusquedaSinResultadosMuestraElMensaje() {
        regla.setContent { GestorDeEventosTheme(darkTheme = false) { BuscarEventoScreen() } }

        regla.onNodeWithText("Nombre de evento, pista o club...").performTextInput("rugby")

        regla.onNodeWithText("No hay eventos con estos filtros").assertExists()
    }

    @Test
    fun elChipDeDeporteFiltraLaLista() {
        regla.setContent { GestorDeEventosTheme(darkTheme = false) { BuscarEventoScreen() } }

        regla.onNodeWithText("Pádel (4)").performClick()

        regla.onNodeWithText("Pádel Oro Dobles Mixto").assertExists()
        regla.onAllNodesWithText("Fútbol 7 en el Parque").assertCountEquals(0)
    }

    @Test
    fun elContadorSigueALaListaFiltrada() {
        regla.setContent { GestorDeEventosTheme(darkTheme = false) { BuscarEventoScreen() } }

        regla.onNodeWithText("4 disponibles").assertExists()
        regla.onNodeWithText("Nombre de evento, pista o club...").performTextInput("basket")

        regla.onNodeWithText("1 disponible").assertExists()
    }
}

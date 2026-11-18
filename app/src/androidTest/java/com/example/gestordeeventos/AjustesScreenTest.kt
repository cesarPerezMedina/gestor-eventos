package com.example.gestordeeventos

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import com.example.gestordeeventos.data.model.Deporte
import com.example.gestordeeventos.data.model.EscalaTexto
import com.example.gestordeeventos.data.model.ModoTema
import com.example.gestordeeventos.ui.ajustes.AjustesAcciones
import com.example.gestordeeventos.ui.ajustes.AjustesContenido
import com.example.gestordeeventos.ui.ajustes.AjustesEstado
import com.example.gestordeeventos.ui.theme.GestorDeEventosTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Comprueba la pantalla de Ajustes: interruptores, chips y textos (necesita emulador o móvil). */
class AjustesScreenTest {

    @get:Rule
    val reglaCompose = createComposeRule()

    private fun texto(id: Int, vararg args: Any) =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id, *args)

    /** Dibuja la pantalla con un estado que cambia de verdad al tocar, y avisa de cada cambio con [alCambiar]. */
    private fun mostrar(
        inicial: AjustesEstado = AjustesEstado(),
        alCambiar: (AjustesEstado) -> Unit = {},
    ) {
        reglaCompose.setContent {
            var estado by remember { mutableStateOf(inicial) }

            fun actualizar(nuevo: AjustesEstado) {
                estado = nuevo
                alCambiar(nuevo)
            }

            GestorDeEventosTheme {
                AjustesContenido(
                    estado = estado,
                    acciones = AjustesAcciones(
                        onModoTemaChange = { actualizar(estado.copy(modoTema = it)) },
                        onEscalaTextoChange = { actualizar(estado.copy(escalaTexto = it)) },
                        onRadioChange = { actualizar(estado.copy(radioBusquedaKm = it)) },
                        onDeporteAlternar = {
                            val favoritos = estado.deportesFavoritos
                            actualizar(
                                estado.copy(
                                    deportesFavoritos = if (it in favoritos) favoritos - it else favoritos + it,
                                ),
                            )
                        },
                        onRecordatorioChange = { actualizar(estado.copy(recordatorioPartidos = it)) },
                        onAvisosPlazasChange = { actualizar(estado.copy(avisosPlazasVacantes = it)) },
                        onInvitacionesChange = { actualizar(estado.copy(invitacionesDirectas = it)) },
                    ),
                    onBackClick = {},
                    onCerrarSesion = {},
                )
            }
        }
    }

    @Test
    fun muestraLasTresSecciones() {
        mostrar()

        reglaCompose.onNodeWithText(texto(R.string.ajustes_seccion_apariencia_accesibilidad)).assertExists()
        reglaCompose.onNodeWithText(texto(R.string.ajustes_seccion_juego)).assertExists()
        reglaCompose.onNodeWithText(texto(R.string.ajustes_seccion_notificaciones)).assertExists()
    }

    // ---------- Tema oscuro ----------

    @Test
    fun conTemaOscuroElInterruptorApareceActivado() {
        mostrar(AjustesEstado(modoTema = ModoTema.OSCURO))

        reglaCompose.onNodeWithText(texto(R.string.ajustes_tema_oscuro_titulo)).assertIsOn()
    }

    @Test
    fun conTemaClaroElInterruptorApareceDesactivado() {
        mostrar(AjustesEstado(modoTema = ModoTema.CLARO))

        reglaCompose.onNodeWithText(texto(R.string.ajustes_tema_oscuro_titulo)).assertIsOff()
    }

    @Test
    fun pulsarElInterruptorDeTemaAvisaConElModoElegido() {
        var ultimo: ModoTema? = null
        mostrar(AjustesEstado(modoTema = ModoTema.CLARO), alCambiar = { ultimo = it.modoTema })

        reglaCompose.onNodeWithText(texto(R.string.ajustes_tema_oscuro_titulo)).performClick()

        assertEquals(ModoTema.OSCURO, ultimo)
    }

    // ---------- Tamaño del texto ----------

    @Test
    fun muestraElTamanoDeTextoActualConSuPorcentaje() {
        mostrar(AjustesEstado(escalaTexto = EscalaTexto.GRANDE))

        val esperado = texto(R.string.ajustes_texto_valor, texto(R.string.ajustes_texto_grande).uppercase(), 115)
        reglaCompose.onNodeWithText(esperado).assertExists()
    }

    @Test
    fun muestraLaFraseDeEjemplo() {
        mostrar()

        reglaCompose.onNodeWithText(texto(R.string.ajustes_texto_ejemplo)).assertExists()
    }

    // ---------- Radio de búsqueda ----------

    @Test
    fun elRadioActualApareceSeleccionado() {
        mostrar(AjustesEstado(radioBusquedaKm = 25))

        reglaCompose.onNodeWithText(texto(R.string.ajustes_radio_km, 25)).performScrollTo().assertIsSelected()
    }

    @Test
    fun pulsarUnRadioAvisaConElValorElegido() {
        var elegido: Int? = null
        mostrar(alCambiar = { elegido = it.radioBusquedaKm })

        reglaCompose.onNodeWithText(texto(R.string.ajustes_radio_km, 50)).performScrollTo().performClick()

        assertEquals(50, elegido)
    }

    // ---------- Deportes favoritos ----------

    @Test
    fun losDeportesFavoritosAparecenSeleccionados() {
        mostrar(AjustesEstado(deportesFavoritos = setOf(Deporte.PADEL)))

        reglaCompose.onNodeWithText(texto(R.string.deporte_padel)).performScrollTo().assertIsSelected()
    }

    @Test
    fun pulsarUnDeporteLoMarcaComoFavorito() {
        var favoritos: Set<Deporte> = emptySet()
        mostrar(alCambiar = { favoritos = it.deportesFavoritos })

        reglaCompose.onNodeWithText(texto(R.string.deporte_baloncesto)).performScrollTo().performClick()

        assertEquals(setOf(Deporte.BALONCESTO), favoritos)
    }

    // ---------- Notificaciones y avisos ----------

    @Test
    fun losAvisosAparecenConSusValoresIniciales() {
        mostrar(AjustesEstado(recordatorioPartidos = true, invitacionesDirectas = false))

        reglaCompose.onNodeWithText(texto(R.string.ajustes_notif_recordatorio_titulo)).performScrollTo().assertIsOn()
        reglaCompose.onNodeWithText(texto(R.string.ajustes_notif_invitaciones_titulo)).performScrollTo().assertIsOff()
    }

    @Test
    fun pulsarUnAvisoLoActivaYAvisa() {
        var activo: Boolean? = null
        mostrar(alCambiar = { activo = it.invitacionesDirectas })

        reglaCompose.onNodeWithText(texto(R.string.ajustes_notif_invitaciones_titulo)).performScrollTo().performClick()

        assertEquals(true, activo)
    }

    // ---------- Pie ----------

    @Test
    fun muestraLaVersionDeLaApp() {
        mostrar(AjustesEstado(versionApp = "1.0"))

        reglaCompose.onNodeWithText(texto(R.string.ajustes_pie_version, "1.0")).performScrollTo().assertExists()
    }
}
package com.example.gestordeeventos

import com.example.gestordeeventos.data.model.Deporte
import com.example.gestordeeventos.data.model.EstadoEvento
import com.example.gestordeeventos.data.model.Evento
import com.example.gestordeeventos.data.model.EventoConCupo
import com.example.gestordeeventos.ui.evento.EtiquetaEvento
import com.example.gestordeeventos.ui.evento.FiltroMisEventos
import com.example.gestordeeventos.ui.evento.aMiEventoAbandonado
import com.example.gestordeeventos.ui.evento.aMiEventoItem
import com.example.gestordeeventos.ui.evento.categoria
import com.example.gestordeeventos.ui.evento.contar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Cómo se clasifica en "Mis eventos" un evento del que el usuario ha salido. */
class MisEventosAbandonadosTest {

    private val ahora = 1_000_000_000L

    private fun conCupo(fecha: Long, estado: EstadoEvento = EstadoEvento.PUBLICADO) = EventoConCupo(
        evento = Evento(id = 5, nombre = "Fútbol abierto", deporte = Deporte.FUTBOL, fechaHora = fecha, creadorId = 2L, estado = estado),
        apuntados = 3,
        yoApuntado = false,
    )

    @Test
    fun unEventoAbandonadoFuturoCaeEnCancelados() {
        val item = conCupo(ahora + 3_600_000L).aMiEventoAbandonado()

        assertTrue(item.abandonado)
        assertFalse(item.esCreador)
        assertEquals(FiltroMisEventos.CANCELADOS, item.categoria(ahora))
    }

    @Test
    fun siguePasandoEnCanceladosAunqueYaSePasaraLaFecha() {
        val item = conCupo(ahora - 100 * 3_600_000L).aMiEventoAbandonado()

        assertEquals(FiltroMisEventos.CANCELADOS, item.categoria(ahora))
    }

    @Test
    fun suEtiquetaEsAbandonado() {
        assertEquals(EtiquetaEvento.ABANDONADO, conCupo(ahora + 1).aMiEventoAbandonado().etiqueta)
    }

    @Test
    fun unEventoNormalNoEstaAbandonado() {
        val item = conCupo(ahora + 3_600_000L).aMiEventoItem(esCreador = false)

        assertFalse(item.abandonado)
        assertEquals(FiltroMisEventos.PROXIMOS, item.categoria(ahora))
    }

    @Test
    fun elContadorDeCanceladosIncluyeLosAbandonados() {
        val lista = listOf(
            conCupo(ahora + 1).aMiEventoItem(esCreador = false),
            conCupo(ahora + 2).aMiEventoAbandonado(),
        )

        assertEquals(1, lista.contar(FiltroMisEventos.CANCELADOS, ahora))
        assertEquals(1, lista.contar(FiltroMisEventos.PROXIMOS, ahora))
    }
}

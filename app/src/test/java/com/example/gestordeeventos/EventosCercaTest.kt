package com.example.gestordeeventos

import com.example.gestordeeventos.data.localizacion.Ubicacion
import com.example.gestordeeventos.data.localizacion.cajaDelimitadora
import com.example.gestordeeventos.data.localizacion.distanciaKm
import com.example.gestordeeventos.data.model.Evento
import com.example.gestordeeventos.data.repositorio.EventoRepositorio
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Búsqueda por cercanía del repositorio (Issues 57 y 60), sin base de datos ni emulador. */
class EventosCercaTest {

    private val centro = Ubicacion(latitud = 40.4168, longitud = -3.7038)

    /** Grados de latitud que equivalen a [km] kilómetros (hacia el norte). */
    private fun latPorKm(km: Double) = km / 111.195

    private fun evento(id: Long, km: Double, publico: Boolean = true) = Evento(
        id = id,
        nombre = "Evento $id",
        publico = publico,
        latitud = centro.latitud + latPorKm(km),
        longitud = centro.longitud,
        fechaHora = 1_000L * id,
        creadorId = 1L,
    )

    private fun repositorio(vararg eventos: Evento) = EventoRepositorio(
        eventoDao = FakeEventoDao(eventos.toList()),
        invitacionDao = FakeInvitacionDao(emptyList(), emptyList()),
    )

    private fun cerca(repo: EventoRepositorio, radioKm: Double) = runBlocking {
        repo.eventosPublicosCerca(centro, radioKm, usuarioId = 1L).first()
    }

    @Test
    fun devuelveSoloLosEventosDentroDelRadio() {
        val repo = repositorio(evento(1, km = 1.0), evento(2, km = 3.0), evento(3, km = 10.0))

        assertEquals(listOf(1L), cerca(repo, 2.0).map { it.evento.evento.id })
        assertEquals(listOf(1L, 2L), cerca(repo, 5.0).map { it.evento.evento.id })
        assertEquals(listOf(1L, 2L, 3L), cerca(repo, 25.0).map { it.evento.evento.id })
    }

    @Test
    fun ordenaDelMasCercanoAlMasLejano() {
        val repo = repositorio(evento(1, km = 8.0), evento(2, km = 1.0), evento(3, km = 4.0))

        assertEquals(listOf(2L, 3L, 1L), cerca(repo, 10.0).map { it.evento.evento.id })
    }

    @Test
    fun calculaLaDistanciaExactaALaQueEstaCadaEvento() {
        val repo = repositorio(evento(1, km = 3.0))

        val resultado = cerca(repo, 5.0).single()

        assertEquals(3.0, resultado.distanciaKm, 0.01)
        assertEquals(
            distanciaKm(centro.latitud, centro.longitud, resultado.evento.evento.latitud, resultado.evento.evento.longitud),
            resultado.distanciaKm,
            1e-9,
        )
    }

    @Test
    fun unEventoEnLaEsquinaDeLaCajaPeroFueraDelRadioNoSale() {
        // La esquina de la caja está a casi 1,41 veces el radio: dentro del rectángulo, pero fuera del círculo
        val caja = cajaDelimitadora(centro, 10.0)
        val esquina = Evento(
            id = 1,
            nombre = "En la esquina",
            latitud = caja.latMax * 0.9999 + centro.latitud * 0.0001,
            longitud = caja.lonMax * 0.9999 + centro.longitud * 0.0001,
            fechaHora = 1_000L,
            creadorId = 1L,
        )
        val repo = repositorio(esquina)

        assertTrue(distanciaKm(centro.latitud, centro.longitud, esquina.latitud, esquina.longitud) > 10.0)
        assertEquals(emptyList<Long>(), cerca(repo, 10.0).map { it.evento.evento.id })
    }

    @Test
    fun unEventoEnElMismoPuntoEstaACeroKm() {
        val repo = repositorio(evento(1, km = 0.0))

        val resultado = cerca(repo, 2.0).single()

        assertEquals(0.0, resultado.distanciaKm, 1e-9)
    }

    @Test
    fun conRadioCeroSoloSaleUnEventoJustoEnElMismoPunto() {
        val repo = repositorio(evento(1, km = 0.0), evento(2, km = 0.5))

        assertEquals(listOf(1L), cerca(repo, 0.0).map { it.evento.evento.id })
    }

    @Test
    fun sinEventosDevuelveUnaListaVacia() {
        assertEquals(emptyList<Long>(), cerca(repositorio(), 25.0).map { it.evento.evento.id })
    }
}

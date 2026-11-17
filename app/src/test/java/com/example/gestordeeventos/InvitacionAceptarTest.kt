package com.example.gestordeeventos

import com.example.gestordeeventos.data.model.Deporte
import com.example.gestordeeventos.data.model.EstadoInvitacion
import com.example.gestordeeventos.data.model.Evento
import com.example.gestordeeventos.data.model.Invitacion
import com.example.gestordeeventos.data.model.Participacion
import com.example.gestordeeventos.data.model.Usuario
import com.example.gestordeeventos.data.repositorio.InvitacionRepositorio
import com.example.gestordeeventos.data.repositorio.ResultadoRespuestaInvitacion
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Comprueba aceptar una invitación y apuntarse al evento (Issue 73), sin base de datos ni emulador. */
class InvitacionAceptarTest {

    private val ana = Usuario(id = 1, nombreUsuario = "ana_23", correo = "ana@correo.com", claveHash = "h")
    private val luis = Usuario(id = 2, nombreUsuario = "luis_9", correo = "luis@correo.com", claveHash = "h")
    private val marta = Usuario(id = 3, nombreUsuario = "marta", correo = "marta@correo.com", claveHash = "h")

    /** Privado, con 2 plazas. */
    private val privado = Evento(
        id = 10, nombre = "Fútbol 7", deporte = Deporte.FUTBOL, publico = false,
        maxParticipantes = 2, fechaHora = 2_000L, creadorId = luis.id
    )

    /** Sin límite de plazas (maxParticipantes = 0). */
    private val sinLimite = Evento(
        id = 11, nombre = "Tenis libre", deporte = Deporte.TENIS, publico = true,
        maxParticipantes = 0, fechaHora = 1_000L, creadorId = luis.id
    )

    private val dao = FakeInvitacionDao(listOf(privado, sinLimite), listOf(ana, luis, marta))
    private val repositorio = InvitacionRepositorio(dao)

    private suspend fun invitar(evento: Evento, para: Usuario, estado: EstadoInvitacion = EstadoInvitacion.PENDIENTE) =
        dao.insertar(
            Invitacion(eventoId = evento.id, deUsuarioId = luis.id, paraUsuarioId = para.id, estado = estado)
        )

    @Test
    fun aceptarApuntaAlUsuarioYMarcaLaInvitacionComoAceptada() = runTest {
        val id = invitar(privado, ana)

        val resultado = repositorio.aceptar(id, ana.id)

        assertEquals(ResultadoRespuestaInvitacion.OK, resultado)
        assertEquals(listOf(ana.id), dao.participantesDe(privado.id))
        assertEquals(EstadoInvitacion.ACEPTADA, dao.estadoDe(id))
    }

    @Test
    fun laInvitacionAceptadaDejaDeSalirEnLasPendientes() = runTest {
        val id = invitar(privado, ana)
        invitar(sinLimite, ana)

        repositorio.aceptar(id, ana.id)

        val pendientes = repositorio.observarPendientes(ana.id).first()
        assertEquals(listOf("Tenis libre"), pendientes.map { it.eventoNombre })
    }

    @Test
    fun siElEventoEstaCompletoNoSeAceptaYSigueSiendoPendiente() = runTest {
        dao.insertarParticipacion(Participacion(privado.id, luis.id))
        dao.insertarParticipacion(Participacion(privado.id, marta.id))
        val id = invitar(privado, ana)

        val resultado = repositorio.aceptar(id, ana.id)

        assertEquals(ResultadoRespuestaInvitacion.EVENTO_COMPLETO, resultado)
        assertEquals(EstadoInvitacion.PENDIENTE, dao.estadoDe(id))
        assertTrue(ana.id !in dao.participantesDe(privado.id))
        assertEquals(1, repositorio.observarPendientes(ana.id).first().size)
    }

    @Test
    fun siQuedaUnaPlazaLibreSePuedeAceptar() = runTest {
        dao.insertarParticipacion(Participacion(privado.id, luis.id))
        val id = invitar(privado, ana)

        assertEquals(ResultadoRespuestaInvitacion.OK, repositorio.aceptar(id, ana.id))
        assertEquals(2, dao.contarParticipantes(privado.id))
    }

    @Test
    fun conCeroPlazasMaximasNoHayLimite() = runTest {
        listOf(1L, 2L, 3L, 4L, 5L).forEach { dao.insertarParticipacion(Participacion(sinLimite.id, it + 100)) }
        val id = invitar(sinLimite, ana)

        assertEquals(ResultadoRespuestaInvitacion.OK, repositorio.aceptar(id, ana.id))
    }

    @Test
    fun nadiePuedeAceptarLaInvitacionDeOtro() = runTest {
        val id = invitar(privado, marta)

        val resultado = repositorio.aceptar(id, ana.id)

        assertEquals(ResultadoRespuestaInvitacion.NO_ES_PARA_TI, resultado)
        assertEquals(EstadoInvitacion.PENDIENTE, dao.estadoDe(id))
        assertTrue(dao.participantesDe(privado.id).isEmpty())
    }

    @Test
    fun unaInvitacionQueNoExisteDevuelveNoExiste() = runTest {
        assertEquals(ResultadoRespuestaInvitacion.NO_EXISTE, repositorio.aceptar(99, ana.id))
    }

    @Test
    fun siElEventoYaNoExisteDevuelveNoExiste() = runTest {
        val sinEvento = Evento(id = 77, nombre = "Borrado", fechaHora = 1L, creadorId = luis.id)
        val id = dao.insertar(
            Invitacion(eventoId = sinEvento.id, deUsuarioId = luis.id, paraUsuarioId = ana.id)
        )

        assertEquals(ResultadoRespuestaInvitacion.NO_EXISTE, repositorio.aceptar(id, ana.id))
    }

    @Test
    fun noSePuedeAceptarDosVeces() = runTest {
        val id = invitar(privado, ana)

        assertEquals(ResultadoRespuestaInvitacion.OK, repositorio.aceptar(id, ana.id))
        assertEquals(ResultadoRespuestaInvitacion.YA_RESPONDIDA, repositorio.aceptar(id, ana.id))
        assertEquals(listOf(ana.id), dao.participantesDe(privado.id))
    }

    @Test
    fun unaInvitacionRechazadaNoSePuedeAceptar() = runTest {
        val id = invitar(privado, ana)
        repositorio.rechazar(id, ana.id)

        assertEquals(ResultadoRespuestaInvitacion.YA_RESPONDIDA, repositorio.aceptar(id, ana.id))
        assertTrue(dao.participantesDe(privado.id).isEmpty())
        assertEquals(EstadoInvitacion.RECHAZADA, dao.estadoDe(id))
    }

    @Test
    fun siYaEstabaApuntadoSeAceptaSinDuplicarLaParticipacion() = runTest {
        dao.insertarParticipacion(Participacion(privado.id, ana.id))
        dao.insertarParticipacion(Participacion(privado.id, luis.id)) // el evento ya está lleno
        val id = invitar(privado, ana)

        assertEquals(ResultadoRespuestaInvitacion.OK, repositorio.aceptar(id, ana.id))
        assertEquals(listOf(ana.id, luis.id), dao.participantesDe(privado.id))
    }

    @Test
    fun cadaUsuarioAceptaLaSuya() = runTest {
        val deAna = invitar(privado, ana)
        val deMarta = invitar(privado, marta)

        repositorio.aceptar(deAna, ana.id)

        assertEquals(EstadoInvitacion.ACEPTADA, dao.estadoDe(deAna))
        assertEquals(EstadoInvitacion.PENDIENTE, dao.estadoDe(deMarta))
        assertEquals(listOf(ana.id), dao.participantesDe(privado.id))
    }
}

package com.example.gestordeeventos

import com.example.gestordeeventos.data.model.Deporte
import com.example.gestordeeventos.data.model.EstadoInvitacion
import com.example.gestordeeventos.data.model.Evento
import com.example.gestordeeventos.data.model.Invitacion
import com.example.gestordeeventos.data.model.Usuario
import com.example.gestordeeventos.data.repositorio.InvitacionRepositorio
import com.example.gestordeeventos.data.repositorio.ResultadoRespuestaInvitacion
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Recorre el flujo completo de invitaciones con varios usuarios (Issue 76), sin base de datos ni emulador:
 * se invita, se acepta, se rechaza y se llena el evento. La versión con base de datos real es
 * `FlujoInvitacionesTest`.
 */
class FlujoInvitacionesRepositorioTest {

    private val luis = Usuario(id = 1, nombreUsuario = "luis_9", correo = "luis@correo.com", claveHash = "h")
    private val ana = Usuario(id = 2, nombreUsuario = "ana_23", correo = "ana@correo.com", claveHash = "h")
    private val marta = Usuario(id = 3, nombreUsuario = "marta", correo = "marta@correo.com", claveHash = "h")
    private val pepe = Usuario(id = 4, nombreUsuario = "pepe", correo = "pepe@correo.com", claveHash = "h")

    /** Privado, con 2 plazas, creado por Luis. */
    private val evento = Evento(
        id = 10, nombre = "Fútbol 7", deporte = Deporte.FUTBOL, publico = false,
        maxParticipantes = 2, fechaHora = 2_000L, creadorId = luis.id
    )

    private val dao = FakeInvitacionDao(listOf(evento), listOf(luis, ana, marta, pepe))
    private val repositorio = InvitacionRepositorio(dao)

    @Test
    fun flujoCompletoConTresInvitadosYDosPlazas() = runTest {
        val deAna = dao.insertar(Invitacion(eventoId = evento.id, deUsuarioId = luis.id, paraUsuarioId = ana.id))
        val deMarta = dao.insertar(Invitacion(eventoId = evento.id, deUsuarioId = luis.id, paraUsuarioId = marta.id))
        val dePepe = dao.insertar(Invitacion(eventoId = evento.id, deUsuarioId = luis.id, paraUsuarioId = pepe.id))
        assertEquals(1, repositorio.observarNumeroPendientes(ana.id).first())

        // Ana y Pepe aceptan: el evento se llena
        assertEquals(ResultadoRespuestaInvitacion.OK, repositorio.aceptar(deAna, ana.id))
        assertEquals(ResultadoRespuestaInvitacion.OK, repositorio.aceptar(dePepe, pepe.id))
        assertEquals(listOf(ana.id, pepe.id), dao.participantesDe(evento.id))

        // Marta llega tarde: no hay plaza, su invitación sigue pendiente
        assertEquals(ResultadoRespuestaInvitacion.EVENTO_COMPLETO, repositorio.aceptar(deMarta, marta.id))
        assertEquals(1, repositorio.observarNumeroPendientes(marta.id).first())

        // Marta la rechaza y ya no queda nada pendiente para nadie
        assertEquals(ResultadoRespuestaInvitacion.OK, repositorio.rechazar(deMarta, marta.id))
        listOf(luis, ana, marta, pepe).forEach {
            assertTrue(repositorio.observarPendientes(it.id).first().isEmpty())
        }
        assertEquals(EstadoInvitacion.ACEPTADA, dao.estadoDe(deAna))
        assertEquals(EstadoInvitacion.RECHAZADA, dao.estadoDe(deMarta))
        assertEquals(EstadoInvitacion.ACEPTADA, dao.estadoDe(dePepe))
        assertEquals(listOf(ana.id, pepe.id), dao.participantesDe(evento.id))
    }

    @Test
    fun siAlguienSeDaDeBajaLaPlazaSeLiberaParaLosDemas() = runTest {
        val deAna = dao.insertar(Invitacion(eventoId = evento.id, deUsuarioId = luis.id, paraUsuarioId = ana.id))
        val deMarta = dao.insertar(Invitacion(eventoId = evento.id, deUsuarioId = luis.id, paraUsuarioId = marta.id))
        val dePepe = dao.insertar(Invitacion(eventoId = evento.id, deUsuarioId = luis.id, paraUsuarioId = pepe.id))
        repositorio.aceptar(deAna, ana.id)
        repositorio.aceptar(deMarta, marta.id)
        assertEquals(ResultadoRespuestaInvitacion.EVENTO_COMPLETO, repositorio.aceptar(dePepe, pepe.id))

        dao.quitarParticipacion(evento.id, ana.id) // Ana sale del evento (Issue 67, aún sin hacer)

        assertEquals(ResultadoRespuestaInvitacion.OK, repositorio.aceptar(dePepe, pepe.id))
        assertEquals(listOf(marta.id, pepe.id), dao.participantesDe(evento.id))
    }

    @Test
    fun unaInvitacionRechazadaNoVuelveAAparecer() = runTest {
        val id = dao.insertar(Invitacion(eventoId = evento.id, deUsuarioId = luis.id, paraUsuarioId = ana.id))

        repositorio.rechazar(id, ana.id)

        assertTrue(repositorio.observarPendientes(ana.id).first().isEmpty())
        assertEquals(ResultadoRespuestaInvitacion.YA_RESPONDIDA, repositorio.aceptar(id, ana.id))
        assertTrue(dao.participantesDe(evento.id).isEmpty())
    }
}

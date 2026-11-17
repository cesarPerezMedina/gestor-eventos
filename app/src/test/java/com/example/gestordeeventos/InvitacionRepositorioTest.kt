package com.example.gestordeeventos

import com.example.gestordeeventos.data.model.Deporte
import com.example.gestordeeventos.data.model.Evento
import com.example.gestordeeventos.data.model.EstadoInvitacion
import com.example.gestordeeventos.data.model.Invitacion
import com.example.gestordeeventos.data.model.Usuario
import com.example.gestordeeventos.data.repositorio.InvitacionRepositorio
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Comprueba las invitaciones pendientes sin necesidad de emulador. */
class InvitacionRepositorioTest {

    private val ana = Usuario(id = 1, nombreUsuario = "ana_23", correo = "ana@correo.com", claveHash = "h")
    private val luis = Usuario(id = 2, nombreUsuario = "luis_9", correo = "luis@correo.com", claveHash = "h")
    private val marta = Usuario(id = 3, nombreUsuario = "marta", correo = "marta@correo.com", claveHash = "h")

    private val futbol = Evento(
        id = 10, nombre = "Fútbol 7", deporte = Deporte.FUTBOL, fechaHora = 2_000L, creadorId = luis.id
    )
    private val tenis = Evento(
        id = 11, nombre = "Tenis dobles", deporte = Deporte.TENIS, fechaHora = 1_000L, creadorId = marta.id
    )

    private val dao = FakeInvitacionDao(listOf(futbol, tenis), listOf(ana, luis, marta))
    private val repositorio = InvitacionRepositorio(dao)

    private suspend fun invitar(
        evento: Evento,
        de: Usuario,
        para: Usuario,
        estado: EstadoInvitacion = EstadoInvitacion.PENDIENTE
    ) =
        dao.insertar(
            Invitacion(eventoId = evento.id, deUsuarioId = de.id, paraUsuarioId = para.id, estado = estado)
        )

    @Test
    fun sinInvitacionesLaListaEstaVacia() = runTest {
        assertTrue(repositorio.observarPendientes(ana.id).first().isEmpty())
    }

    @Test
    fun devuelveElEventoYQuienInvita() = runTest {
        val id = invitar(futbol, de = luis, para = ana)

        val pendiente = repositorio.observarPendientes(ana.id).first().single()

        assertEquals(id, pendiente.invitacionId)
        assertEquals("Fútbol 7", pendiente.eventoNombre)
        assertEquals(Deporte.FUTBOL, pendiente.deporte)
        assertEquals("luis_9", pendiente.deNombreUsuario)
    }

    @Test
    fun noMuestraLasInvitacionesDeOtroUsuario() = runTest {
        invitar(futbol, de = luis, para = marta)

        assertTrue(repositorio.observarPendientes(ana.id).first().isEmpty())
    }

    @Test
    fun noMuestraLasQueYaSeAceptaronORechazaron() = runTest {
        invitar(futbol, de = luis, para = ana, estado = EstadoInvitacion.ACEPTADA)
        invitar(tenis, de = marta, para = ana, estado = EstadoInvitacion.RECHAZADA)

        assertTrue(repositorio.observarPendientes(ana.id).first().isEmpty())
    }

    @Test
    fun ordenaPorFechaDelEventoLasMasProximasPrimero() = runTest {
        invitar(futbol, de = luis, para = ana) // fecha 2000
        invitar(tenis, de = marta, para = ana) // fecha 1000

        val nombres = repositorio.observarPendientes(ana.id).first().map { it.eventoNombre }

        assertEquals(listOf("Tenis dobles", "Fútbol 7"), nombres)
    }

    @Test
    fun laListaSeActualizaCuandoLlegaUnaInvitacionNueva() = runTest {
        assertEquals(0, repositorio.observarNumeroPendientes(ana.id).first())

        invitar(futbol, de = luis, para = ana)

        assertEquals(1, repositorio.observarNumeroPendientes(ana.id).first())
    }
}

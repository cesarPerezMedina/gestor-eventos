package com.example.gestordeeventos

import com.example.gestordeeventos.data.model.Deporte
import com.example.gestordeeventos.data.model.EstadoInvitacion
import com.example.gestordeeventos.data.model.Evento
import com.example.gestordeeventos.data.model.Invitacion
import com.example.gestordeeventos.data.model.Participacion
import com.example.gestordeeventos.data.model.Usuario
import com.example.gestordeeventos.data.repositorio.InvitacionRepositorio
import com.example.gestordeeventos.ui.invitaciones.InvitacionesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Comprueba que la lista de invitaciones se actualiza sola al aceptar o rechazar (Issue 75),
 * sin base de datos ni emulador.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class InvitacionesViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    private val ana = Usuario(id = 1, nombreUsuario = "ana_23", correo = "ana@correo.com", claveHash = "h")
    private val luis = Usuario(id = 2, nombreUsuario = "luis_9", correo = "luis@correo.com", claveHash = "h")

    private val futbol = Evento(
        id = 10, nombre = "Fútbol 7", deporte = Deporte.FUTBOL, publico = false,
        maxParticipantes = 1, fechaHora = 2_000L, creadorId = luis.id
    )
    private val tenis = Evento(
        id = 11, nombre = "Tenis dobles", deporte = Deporte.TENIS, fechaHora = 3_000L, creadorId = luis.id
    )

    private val dao = FakeInvitacionDao(listOf(futbol, tenis), listOf(ana, luis))
    private lateinit var viewModel: InvitacionesViewModel
    private var idFutbol = 0L
    private var idTenis = 0L

    /** Texto falso para los recursos: así los tests no dependen del idioma. */
    private fun texto(id: Int, nombre: String) = "texto:$id:$nombre"

    @Before
    fun preparar() {
        Dispatchers.setMain(dispatcher)
        runBlocking {
            idFutbol = dao.insertar(Invitacion(eventoId = futbol.id, deUsuarioId = luis.id, paraUsuarioId = ana.id))
            idTenis = dao.insertar(Invitacion(eventoId = tenis.id, deUsuarioId = luis.id, paraUsuarioId = ana.id))
        }
        viewModel = InvitacionesViewModel(InvitacionRepositorio(dao), ana.id, this::texto)
    }

    @After
    fun limpiar() {
        Dispatchers.resetMain()
    }

    private fun nombresPendientes() = viewModel.estado.value.invitaciones.map { it.eventoNombre }

    @Test
    fun alEmpezarCargaLasInvitacionesPendientes() {
        val estado = viewModel.estado.value

        assertFalse(estado.cargando)
        assertEquals(listOf("Fútbol 7", "Tenis dobles"), nombresPendientes())
        assertNull(estado.mensaje)
    }

    @Test
    fun alAceptarLaInvitacionDesapareceDeLaListaYElUsuarioQuedaApuntado() {
        val futbolPendiente = viewModel.estado.value.invitaciones.first { it.eventoId == futbol.id }

        viewModel.aceptar(futbolPendiente)

        assertEquals(listOf("Tenis dobles"), nombresPendientes())
        assertEquals(listOf(ana.id), dao.participantesDe(futbol.id))
        assertEquals(EstadoInvitacion.ACEPTADA, dao.estadoDe(idFutbol))
        assertEquals("texto:${R.string.invitaciones_msg_aceptada}:Fútbol 7", viewModel.estado.value.mensaje)
    }

    @Test
    fun alRechazarLaInvitacionDesapareceDeLaListaYNoSeApunta() {
        val tenisPendiente = viewModel.estado.value.invitaciones.first { it.eventoId == tenis.id }

        viewModel.rechazar(tenisPendiente)

        assertEquals(listOf("Fútbol 7"), nombresPendientes())
        assertTrue(dao.participantesDe(tenis.id).isEmpty())
        assertEquals(EstadoInvitacion.RECHAZADA, dao.estadoDe(idTenis))
        assertEquals("texto:${R.string.invitaciones_msg_rechazada}:Tenis dobles", viewModel.estado.value.mensaje)
    }

    @Test
    fun siElEventoEstaCompletoLaInvitacionSigueEnLaListaYAvisa() {
        runBlocking { dao.insertarParticipacion(Participacion(futbol.id, luis.id)) } // plaza única ocupada
        val futbolPendiente = viewModel.estado.value.invitaciones.first { it.eventoId == futbol.id }

        viewModel.aceptar(futbolPendiente)

        assertEquals(listOf("Fútbol 7", "Tenis dobles"), nombresPendientes())
        assertEquals("texto:${R.string.invitaciones_msg_completo}:Fútbol 7", viewModel.estado.value.mensaje)
    }

    @Test
    fun siLaInvitacionYaSeRespondioAvisaSinCambiarNada() {
        val futbolPendiente = viewModel.estado.value.invitaciones.first { it.eventoId == futbol.id }
        viewModel.aceptar(futbolPendiente)

        viewModel.aceptar(futbolPendiente) // segundo toque con la tarjeta antigua

        assertEquals(listOf(ana.id), dao.participantesDe(futbol.id))
        assertEquals("texto:${R.string.invitaciones_msg_ya_respondida}:Fútbol 7", viewModel.estado.value.mensaje)
    }

    @Test
    fun mensajeMostradoBorraElAviso() {
        viewModel.rechazar(viewModel.estado.value.invitaciones.first())
        assertTrue(viewModel.estado.value.mensaje != null)

        viewModel.mensajeMostrado()

        assertNull(viewModel.estado.value.mensaje)
    }

    @Test
    fun cuandoNoQuedanInvitacionesLaListaEstaVacia() {
        viewModel.estado.value.invitaciones.forEach { viewModel.rechazar(it) }

        assertTrue(viewModel.estado.value.invitaciones.isEmpty())
        assertFalse(viewModel.estado.value.cargando)
    }
}

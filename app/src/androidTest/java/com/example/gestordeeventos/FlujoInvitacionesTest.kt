package com.example.gestordeeventos

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gestordeeventos.data.local.AppDatabase
import com.example.gestordeeventos.data.model.EstadoInvitacion
import com.example.gestordeeventos.data.model.Evento
import com.example.gestordeeventos.data.model.Usuario
import com.example.gestordeeventos.data.repositorio.EventoRepositorio
import com.example.gestordeeventos.data.repositorio.InvitacionRepositorio
import com.example.gestordeeventos.data.repositorio.ResultadoRespuestaInvitacion
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Prueba de integración del flujo completo de invitaciones (Issue 76), con una base de datos Room real
 * en memoria: se crea un evento con invitados, se recibe, se acepta o rechaza, y se comprueba qué ve
 * cada usuario después. Une las Issues 43 (eventos privados), 72, 73, 74 y 75.
 */
@RunWith(AndroidJUnit4::class)
class FlujoInvitacionesTest {

    private lateinit var db: AppDatabase
    private lateinit var eventos: EventoRepositorio
    private lateinit var invitaciones: InvitacionRepositorio
    private var luisId = 0L
    private var anaId = 0L
    private var martaId = 0L

    @Before
    fun crearBaseDeDatos() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java).build()
        eventos = EventoRepositorio(db.eventoDao(), db.invitacionDao())
        // La misma transacción real que usa la app en InvitacionRepositorio.crear(context)
        invitaciones = InvitacionRepositorio(db.invitacionDao()) { bloque -> db.withTransaction { bloque() } }
        runBlocking {
            val usuarios = db.usuarioDao()
            luisId = usuarios.insertar(Usuario(nombreUsuario = "luis_9", correo = "luis@correo.com", claveHash = "h"))
            anaId = usuarios.insertar(Usuario(nombreUsuario = "ana_23", correo = "ana@correo.com", claveHash = "h"))
            martaId = usuarios.insertar(Usuario(nombreUsuario = "marta", correo = "marta@correo.com", claveHash = "h"))
        }
    }

    @After
    fun cerrarBaseDeDatos() {
        db.close()
    }

    /** Luis crea el evento [nombre] e invita a [invitados]. Devuelve el id del evento. */
    private suspend fun crearEvento(
        nombre: String,
        publico: Boolean = false,
        maximo: Int = 0,
        invitados: List<Long> = listOf(anaId, martaId)
    ): Long {
        val resultado = eventos.crearEvento(
            Evento(nombre = nombre, publico = publico, maxParticipantes = maximo, fechaHora = 2_000L, creadorId = luisId),
            invitados
        )
        assertTrue(resultado.mensaje, resultado.ok)
        return db.eventoDao().listarCreadosPorUsuario(luisId).first { it.nombre == nombre }.id
    }

    private suspend fun invitacionDe(usuarioId: Long, eventoId: Long): Long =
        invitaciones.observarPendientes(usuarioId).first().single { it.eventoId == eventoId }.invitacionId

    private suspend fun participantes(eventoId: Long) = db.eventoDao().contarParticipantes(eventoId)

    @Test
    fun crearUnEventoConInvitadosDejaUnaPendienteParaCadaUno() = runBlocking {
        val eventoId = crearEvento("Fútbol 7")

        assertEquals(listOf(eventoId), invitaciones.observarPendientes(anaId).first().map { it.eventoId })
        assertEquals(listOf(eventoId), invitaciones.observarPendientes(martaId).first().map { it.eventoId })
        assertTrue(invitaciones.observarPendientes(luisId).first().isEmpty())
    }

    @Test
    fun elCreadorNoSeInvitaASiMismo() = runBlocking {
        crearEvento("Fútbol 7", invitados = listOf(luisId, anaId))

        assertTrue(invitaciones.observarPendientes(luisId).first().isEmpty())
        assertEquals(1, invitaciones.observarPendientes(anaId).first().size)
    }

    @Test
    fun aceptarApuntaAlUsuarioYElEventoPasaASusListas() = runBlocking {
        val eventoId = crearEvento("Fútbol 7")

        val resultado = invitaciones.aceptar(invitacionDe(anaId, eventoId), anaId)

        assertEquals(ResultadoRespuestaInvitacion.OK, resultado)
        assertTrue(db.eventoDao().estaApuntado(eventoId, anaId))
        assertTrue(invitaciones.observarPendientes(anaId).first().isEmpty())
        assertEquals(listOf(eventoId), eventos.observarEventosUnidos(anaId).first().map { it.id })
        assertEquals(listOf(eventoId), eventos.misEventos(anaId).first().map { it.id })
        // Lo de Marta no cambia: su invitación sigue pendiente
        assertEquals(1, invitaciones.observarPendientes(martaId).first().size)
    }

    @Test
    fun rechazarNoApuntaYElEventoPrivadoDejaDeVerse() = runBlocking {
        val eventoId = crearEvento("Fútbol 7")
        assertTrue(eventos.puedeVer(eventoId, martaId))

        val resultado = invitaciones.rechazar(invitacionDe(martaId, eventoId), martaId)

        assertEquals(ResultadoRespuestaInvitacion.OK, resultado)
        assertFalse(db.eventoDao().estaApuntado(eventoId, martaId))
        assertTrue(invitaciones.observarPendientes(martaId).first().isEmpty())
        assertTrue(eventos.observarEventosUnidos(martaId).first().isEmpty())
        assertTrue(eventos.misEventos(martaId).first().isEmpty())
        assertFalse(eventos.puedeVer(eventoId, martaId))
        // Con la invitación rechazada ya no puede apuntarse a un evento privado
        assertFalse(eventos.apuntarse(eventoId, martaId).ok)
    }

    @Test
    fun siElEventoEstaCompletoAceptarNoCambiaNada() = runBlocking {
        val eventoId = crearEvento("Fútbol 7", maximo = 1)
        invitaciones.aceptar(invitacionDe(anaId, eventoId), anaId)

        val idDeMarta = invitacionDe(martaId, eventoId)
        val resultado = invitaciones.aceptar(idDeMarta, martaId)

        assertEquals(ResultadoRespuestaInvitacion.EVENTO_COMPLETO, resultado)
        assertEquals(1, participantes(eventoId))
        assertFalse(db.eventoDao().estaApuntado(eventoId, martaId))
        assertEquals(EstadoInvitacion.PENDIENTE, db.invitacionDao().buscarPorId(idDeMarta)?.estado)
        // Sigue pudiendo rechazarla
        assertEquals(ResultadoRespuestaInvitacion.OK, invitaciones.rechazar(idDeMarta, martaId))
    }

    @Test
    fun noSePuedeResponderDosVeces() = runBlocking {
        val eventoId = crearEvento("Fútbol 7")
        val id = invitacionDe(anaId, eventoId)

        assertEquals(ResultadoRespuestaInvitacion.OK, invitaciones.aceptar(id, anaId))
        assertEquals(ResultadoRespuestaInvitacion.YA_RESPONDIDA, invitaciones.aceptar(id, anaId))
        assertEquals(ResultadoRespuestaInvitacion.YA_RESPONDIDA, invitaciones.rechazar(id, anaId))
        assertEquals(1, participantes(eventoId))
        assertEquals(EstadoInvitacion.ACEPTADA, db.invitacionDao().buscarPorId(id)?.estado)
    }

    @Test
    fun nadiePuedeResponderPorOtroUsuario() = runBlocking {
        val eventoId = crearEvento("Fútbol 7")
        val idDeMarta = invitacionDe(martaId, eventoId)

        assertEquals(ResultadoRespuestaInvitacion.NO_ES_PARA_TI, invitaciones.aceptar(idDeMarta, anaId))
        assertEquals(ResultadoRespuestaInvitacion.NO_ES_PARA_TI, invitaciones.rechazar(idDeMarta, anaId))
        assertEquals(0, participantes(eventoId))
        assertEquals(EstadoInvitacion.PENDIENTE, db.invitacionDao().buscarPorId(idDeMarta)?.estado)
    }

    @Test
    fun apuntarseAUnEventoPublicoMarcaLaInvitacionComoAceptada() = runBlocking {
        val eventoId = crearEvento("Pádel abierto", publico = true, invitados = listOf(anaId))
        assertEquals(1, invitaciones.observarPendientes(anaId).first().size)

        assertTrue(eventos.apuntarse(eventoId, anaId).ok)

        assertTrue(invitaciones.observarPendientes(anaId).first().isEmpty())
        assertEquals(1, participantes(eventoId))
    }

    @Test
    fun elNumeroDePendientesBajaAlResponder() = runBlocking {
        val primero = crearEvento("Fútbol 7", invitados = listOf(anaId))
        val segundo = crearEvento("Tenis dobles", invitados = listOf(anaId))
        assertEquals(2, invitaciones.observarNumeroPendientes(anaId).first())

        invitaciones.aceptar(invitacionDe(anaId, primero), anaId)
        assertEquals(1, invitaciones.observarNumeroPendientes(anaId).first())

        invitaciones.rechazar(invitacionDe(anaId, segundo), anaId)
        assertEquals(0, invitaciones.observarNumeroPendientes(anaId).first())
    }
}

package com.example.gestordeeventos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gestordeeventos.data.local.AppDatabase
import com.example.gestordeeventos.data.model.EstadoInvitacion
import com.example.gestordeeventos.data.model.Evento
import com.example.gestordeeventos.data.model.Usuario
import com.example.gestordeeventos.data.repositorio.EventoRepositorio
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Pruebas de salirse de un evento (Issue 67), con una base de datos Room real en memoria. */
@RunWith(AndroidJUnit4::class)
class SalirDeEventoTest {

    private lateinit var db: AppDatabase
    private lateinit var eventos: EventoRepositorio
    private var luisId = 0L
    private var anaId = 0L
    private var martaId = 0L

    @Before
    fun crearBaseDeDatos() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java).build()
        eventos = EventoRepositorio(db.eventoDao(), db.invitacionDao())
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

    /** Luis crea el evento [nombre]. Devuelve su id. */
    private suspend fun crearEvento(
        nombre: String,
        publico: Boolean = true,
        maximo: Int = 0,
        invitados: List<Long> = emptyList()
    ): Long {
        val resultado = eventos.crearEvento(
            Evento(nombre = nombre, publico = publico, maxParticipantes = maximo, fechaHora = 2_000L, creadorId = luisId),
            invitados
        )
        assertTrue(resultado.mensaje, resultado.ok)
        return db.eventoDao().listarCreadosPorUsuario(luisId).first { it.nombre == nombre }.id
    }

    @Test
    fun salirQuitaLaParticipacionYElEventoDejaDeSerSuyo() = runBlocking {
        val eventoId = crearEvento("Fútbol 7")
        assertTrue(eventos.apuntarse(eventoId, anaId).ok)
        assertEquals(listOf(eventoId), eventos.observarEventosUnidos(anaId).first().map { it.id })

        val resultado = eventos.salirDeEvento(eventoId, anaId)

        assertTrue(resultado.mensaje, resultado.ok)
        assertFalse(db.eventoDao().estaApuntado(eventoId, anaId))
        assertTrue(eventos.observarEventosUnidos(anaId).first().isEmpty())
        assertTrue(eventos.misEventos(anaId).first().isEmpty())
        assertEquals(0, db.eventoDao().contarParticipantes(eventoId))
    }

    @Test
    fun salirNoAfectaALosDemasParticipantes() = runBlocking {
        val eventoId = crearEvento("Fútbol 7")
        eventos.apuntarse(eventoId, anaId)
        eventos.apuntarse(eventoId, martaId)

        eventos.salirDeEvento(eventoId, anaId)

        assertFalse(db.eventoDao().estaApuntado(eventoId, anaId))
        assertTrue(db.eventoDao().estaApuntado(eventoId, martaId))
        assertEquals(1, db.eventoDao().contarParticipantes(eventoId))
    }

    @Test
    fun salirLiberaLaPlazaParaOtroUsuario() = runBlocking {
        val eventoId = crearEvento("Tenis", maximo = 1)
        assertTrue(eventos.apuntarse(eventoId, anaId).ok)
        assertFalse(eventos.apuntarse(eventoId, martaId).ok) // completo

        eventos.salirDeEvento(eventoId, anaId)

        assertTrue(eventos.apuntarse(eventoId, martaId).ok)
    }

    @Test
    fun enUnEventoPublicoSePuedeVolverAApuntarse() = runBlocking {
        val eventoId = crearEvento("Pádel abierto")
        eventos.apuntarse(eventoId, anaId)
        eventos.salirDeEvento(eventoId, anaId)

        assertTrue(eventos.apuntarse(eventoId, anaId).ok)
        assertTrue(db.eventoDao().estaApuntado(eventoId, anaId))
    }

    @Test
    fun enUnEventoPrivadoSinInvitacionNuevaNoSePuedeVolver() = runBlocking {
        val eventoId = crearEvento("Fútbol 7", publico = false, invitados = listOf(anaId))
        assertTrue(eventos.apuntarse(eventoId, anaId).ok) // la invitación pasa a aceptada
        assertTrue(eventos.misEventos(anaId).first().isNotEmpty())

        val resultado = eventos.salirDeEvento(eventoId, anaId)

        assertTrue(resultado.mensaje, resultado.ok)
        assertTrue(eventos.misEventos(anaId).first().isEmpty())
        assertFalse(eventos.puedeVer(eventoId, anaId))
        assertFalse(eventos.apuntarse(eventoId, anaId).ok)
    }

    @Test
    fun alSalirLaInvitacionAceptadaPasaARechazada() = runBlocking {
        val eventoId = crearEvento("Fútbol 7", publico = false, invitados = listOf(anaId))
        eventos.apuntarse(eventoId, anaId)

        eventos.salirDeEvento(eventoId, anaId)

        val invitacion = db.invitacionDao().observarPendientes(anaId).first()
        assertTrue(invitacion.isEmpty())
        assertFalse(db.eventoDao().tieneInvitacion(eventoId, anaId))
    }

    @Test
    fun elCreadorNoPuedeSalirDeSuPropioEvento() = runBlocking {
        val eventoId = crearEvento("Fútbol 7")

        val resultado = eventos.salirDeEvento(eventoId, luisId)

        assertFalse(resultado.ok)
        assertEquals(eventoId, db.eventoDao().buscarPorId(eventoId)?.id)
        assertEquals(listOf(eventoId), eventos.observarEventosCreados(luisId).first().map { it.id })
    }

    @Test
    fun siNoEstabaApuntadoNoHayNadaQueHacer() = runBlocking {
        val eventoId = crearEvento("Fútbol 7")

        val resultado = eventos.salirDeEvento(eventoId, anaId)

        assertFalse(resultado.ok)
        assertEquals("No estás apuntado a este evento", resultado.mensaje)
    }

    @Test
    fun salirDeUnEventoQueNoExisteAvisa() = runBlocking {
        val resultado = eventos.salirDeEvento(999, anaId)

        assertFalse(resultado.ok)
        assertEquals("El evento ya no existe", resultado.mensaje)
    }

    @Test
    fun salirDeUnEventoNoCambiaLosOtrosEventosDelUsuario() = runBlocking {
        val primero = crearEvento("Fútbol 7")
        val segundo = crearEvento("Tenis dobles")
        eventos.apuntarse(primero, anaId)
        eventos.apuntarse(segundo, anaId)

        eventos.salirDeEvento(primero, anaId)

        assertEquals(listOf(segundo), eventos.observarEventosUnidos(anaId).first().map { it.id })
    }
}

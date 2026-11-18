package com.example.gestordeeventos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gestordeeventos.data.local.AppDatabase
import com.example.gestordeeventos.data.model.Evento
import com.example.gestordeeventos.data.model.Usuario
import com.example.gestordeeventos.data.repositorio.EventoRepositorio
import com.example.gestordeeventos.data.repositorio.InvitacionRepositorio
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Pruebas de eliminar un evento propio (Issue 68), con una base de datos Room real en memoria. */
@RunWith(AndroidJUnit4::class)
class EliminarEventoTest {

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
        invitaciones = InvitacionRepositorio(db.invitacionDao())
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

    private suspend fun crearEvento(
        nombre: String,
        publico: Boolean = true,
        invitados: List<Long> = emptyList()
    ): Long {
        val resultado = eventos.crearEvento(
            Evento(nombre = nombre, publico = publico, fechaHora = 2_000L, creadorId = luisId),
            invitados
        )
        assertTrue(resultado.mensaje, resultado.ok)
        return db.eventoDao().listarCreadosPorUsuario(luisId).first { it.nombre == nombre }.id
    }

    @Test
    fun elCreadorPuedeEliminarSuEvento() = runBlocking {
        val eventoId = crearEvento("Fútbol 7")

        val resultado = eventos.eliminarEvento(eventoId, luisId)

        assertTrue(resultado.mensaje, resultado.ok)
        assertNull(db.eventoDao().buscarPorId(eventoId))
        assertTrue(eventos.observarEventosCreados(luisId).first().isEmpty())
        assertTrue(eventos.eventosPublicos().first().isEmpty())
    }

    @Test
    fun alEliminarSeBorranParticipacionesEInvitaciones() = runBlocking {
        val eventoId = crearEvento("Fútbol 7", publico = false, invitados = listOf(anaId, martaId))
        val idDeAna = invitaciones.observarPendientes(anaId).first().single().invitacionId
        invitaciones.aceptar(idDeAna, anaId) // Ana se apunta; Marta sigue con la invitación pendiente

        eventos.eliminarEvento(eventoId, luisId)

        assertEquals(0, db.eventoDao().contarParticipantes(eventoId))
        assertNull(db.invitacionDao().buscarPorId(idDeAna))
        assertTrue(invitaciones.observarPendientes(martaId).first().isEmpty())
        assertTrue(eventos.observarEventosUnidos(anaId).first().isEmpty())
        assertTrue(eventos.misEventos(anaId).first().isEmpty())
        assertTrue(eventos.misEventos(martaId).first().isEmpty())
    }

    @Test
    fun soloElCreadorPuedeEliminarElEvento() = runBlocking {
        val eventoId = crearEvento("Fútbol 7")
        eventos.apuntarse(eventoId, anaId)

        val resultado = eventos.eliminarEvento(eventoId, anaId)

        assertFalse(resultado.ok)
        assertEquals("Solo el creador puede eliminar el evento", resultado.mensaje)
        assertNotNull(db.eventoDao().buscarPorId(eventoId))
        assertTrue(db.eventoDao().estaApuntado(eventoId, anaId))
    }

    @Test
    fun eliminarUnEventoQueNoExisteAvisa() = runBlocking {
        val resultado = eventos.eliminarEvento(999, luisId)

        assertFalse(resultado.ok)
        assertEquals("El evento ya no existe", resultado.mensaje)
    }

    @Test
    fun noSePuedeEliminarDosVeces() = runBlocking {
        val eventoId = crearEvento("Fútbol 7")

        assertTrue(eventos.eliminarEvento(eventoId, luisId).ok)
        assertFalse(eventos.eliminarEvento(eventoId, luisId).ok)
    }

    @Test
    fun eliminarUnEventoNoTocaLosDemas() = runBlocking {
        val primero = crearEvento("Fútbol 7")
        val segundo = crearEvento("Tenis dobles")
        eventos.apuntarse(segundo, anaId)

        eventos.eliminarEvento(primero, luisId)

        assertEquals(listOf(segundo), eventos.observarEventosCreados(luisId).first().map { it.id })
        assertEquals(listOf(segundo), eventos.observarEventosUnidos(anaId).first().map { it.id })
        assertEquals(1, db.eventoDao().contarParticipantes(segundo))
    }

    @Test
    fun lasListasDelUsuarioSeActualizanSolasAlEliminar() = runBlocking {
        val eventoId = crearEvento("Fútbol 7")
        assertEquals(1, eventos.observarEventosCreados(luisId).first().size)
        assertEquals(1, eventos.misEventos(luisId).first().size)

        eventos.eliminarEvento(eventoId, luisId)

        assertTrue(eventos.observarEventosCreados(luisId).first().isEmpty())
        assertTrue(eventos.misEventos(luisId).first().isEmpty())
    }
}

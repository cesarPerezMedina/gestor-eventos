package com.example.gestordeeventos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gestordeeventos.data.local.AppDatabase
import com.example.gestordeeventos.data.model.Deporte
import com.example.gestordeeventos.data.model.Evento
import com.example.gestordeeventos.data.model.Participacion
import com.example.gestordeeventos.data.model.Usuario
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Pruebas de las consultas de la Issue 38. */
@RunWith(AndroidJUnit4::class)
class EventoDaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun crearBaseDeDatos() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java).build()
    }

    @After
    fun cerrarBaseDeDatos() {
        db.close()
    }

    private suspend fun crearUsuario(nombre: String): Long =
        db.usuarioDao().insertar(
            Usuario(
                nombreUsuario = nombre,
                correo = "$nombre@correo.com",
                claveHash = "hash"
            )
        )

    private suspend fun crearEvento(nombre: String, creadorId: Long, fecha: Long): Long =
        db.eventoDao().insertar(
            Evento(
                nombre = nombre,
                deporte = Deporte.FUTBOL,
                fechaHora = fecha,
                creadorId = creadorId
            )
        )

    @Test
    fun recuperaLosEventosCreadosPorElUsuario() = runBlocking {
        val usuario = crearUsuario("carlos")
        val otroUsuario = crearUsuario("ana")

        val eventoPropio = crearEvento("Fútbol propio", usuario, 200L)
        crearEvento("Fútbol de Ana", otroUsuario, 100L)
        crearEvento("Pádel propio", usuario, 300L)

        val eventos = db.eventoDao().listarCreadosPorUsuario(usuario)

        val eventoPadel = eventos.last().id
        assertEquals(listOf(eventoPropio, eventoPadel), eventos.map { it.id })
        assertEquals(listOf("Fútbol propio", "Pádel propio"), eventos.map { it.nombre })
    }

    @Test
    fun recuperaLosEventosEnLosQueElUsuarioParticipa() = runBlocking {
        val usuario = crearUsuario("carlos")
        val otroUsuario = crearUsuario("ana")

        val eventoAjeno = crearEvento("Evento de Ana", otroUsuario, 200L)
        val eventoPropio = crearEvento("Evento propio", usuario, 100L)
        crearEvento("Evento sin participar", otroUsuario, 300L)

        db.participacionDao().insertar(Participacion(eventoAjeno, usuario))
        db.participacionDao().insertar(Participacion(eventoPropio, usuario))

        val eventos = db.eventoDao().listarEventosParticipadosPorUsuario(usuario)

        assertEquals(listOf(eventoPropio, eventoAjeno), eventos.map { it.id })
        assertEquals(listOf("Evento propio", "Evento de Ana"), eventos.map { it.nombre })
    }

    @Test
    fun noIncluyeEventosDeOtrosUsuariosNiEventosSinParticipacion() = runBlocking {
        val usuario = crearUsuario("carlos")
        val otroUsuario = crearUsuario("ana")

        crearEvento("Solo de Ana", otroUsuario, 100L)
        val eventoPropio = crearEvento("Propio", usuario, 200L)

        val creados = db.eventoDao().listarCreadosPorUsuario(usuario)
        val participados = db.eventoDao().listarEventosParticipadosPorUsuario(usuario)

        assertEquals(listOf(eventoPropio), creados.map { it.id })
        assertEquals(emptyList<Long>(), participados.map { it.id })
    }
}

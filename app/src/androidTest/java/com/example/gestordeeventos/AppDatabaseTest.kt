package com.example.gestordeeventos

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gestordeeventos.data.local.AppDatabase
import com.example.gestordeeventos.data.model.Amigo
import com.example.gestordeeventos.data.model.Amistad
import com.example.gestordeeventos.data.model.Deporte
import com.example.gestordeeventos.data.model.Evento
import com.example.gestordeeventos.data.model.Participacion
import com.example.gestordeeventos.data.model.Usuario
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

/** Comprueba que la base de datos se crea y que guardar y leer datos funciona. Usa una base en memoria. */
@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

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

    private fun usuarioDePrueba(nombre: String = "ana_23") =
        Usuario(nombreUsuario = nombre, correo = "$nombre@correo.com", claveHash = "hash")

    @Test
    fun guardaYLeeUnUsuario() = runBlocking {
        val id = db.usuarioDao().insertar(usuarioDePrueba())
        val leido = db.usuarioDao().buscarPorId(id)
        assertNotNull(leido)
        assertEquals("ana_23", leido!!.nombreUsuario)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun noPermiteDosUsuariosConElMismoCorreo(): Unit = runBlocking {
        db.usuarioDao().insertar(usuarioDePrueba())
        db.usuarioDao().insertar(usuarioDePrueba().copy(nombreUsuario = "otra_persona"))
    }

    @Test
    fun guardaUnEventoConSuCreadorYParticipantes() = runBlocking {
        val creador = db.usuarioDao().insertar(usuarioDePrueba())
        val eventoId = db.eventoDao().insertar(
            Evento(nombre = "Fútbol 7", deporte = Deporte.FUTBOL, fechaHora = 1_800_000_000_000, creadorId = creador)
        )
        db.participacionDao().insertar(Participacion(eventoId = eventoId, usuarioId = creador))

        val evento = db.eventoDao().buscarPorId(eventoId)
        assertEquals(Deporte.FUTBOL, evento!!.deporte)
        assertEquals(1, db.participacionDao().contarDeEvento(eventoId))
    }

    @Test
    fun alBorrarElEventoSeBorranSusParticipaciones() = runBlocking {
        val creador = db.usuarioDao().insertar(usuarioDePrueba())
        val eventoId = db.eventoDao().insertar(Evento(nombre = "Pádel", fechaHora = 1L, creadorId = creador))
        db.participacionDao().insertar(Participacion(eventoId, creador))

        db.openHelper.writableDatabase.execSQL("DELETE FROM eventos WHERE id = $eventoId")

        assertNull(db.eventoDao().buscarPorId(eventoId))
        assertEquals(0, db.participacionDao().contarDeEvento(eventoId))
    }

    @Test
    fun guardaUnaAmistadYAmbosUsuariosSonAmigos() = runBlocking {
        val ana = db.usuarioDao().insertar(usuarioDePrueba("ana_23"))
        val luis = db.usuarioDao().insertar(usuarioDePrueba("luis_7"))

        db.amistadDao().insertar(Amistad.entre(luis, ana))

        assertTrue(db.amistadDao().sonAmigos(ana, luis))
        assertTrue(db.amistadDao().sonAmigos(luis, ana))
        assertEquals(listOf(Amigo(luis, "luis_7")), db.amistadDao().listarAmigos(ana))
        assertEquals(listOf(Amigo(ana, "ana_23")), db.amistadDao().listarAmigos(luis))
    }

    @Test
    fun noDuplicaUnaAmistadAunqueSeGuardeEnOrdenInverso() = runBlocking {
        val ana = db.usuarioDao().insertar(usuarioDePrueba("ana_23"))
        val luis = db.usuarioDao().insertar(usuarioDePrueba("luis_7"))

        db.amistadDao().insertar(Amistad.entre(ana, luis))
        val segunda = db.amistadDao().insertar(Amistad.entre(luis, ana))

        assertEquals(-1L, segunda)
        assertEquals(1, db.amistadDao().contarAmigos(ana))
    }

    @Test
    fun listaLosAmigosOrdenadosPorNombreDeUsuario() = runBlocking {
        val ana = db.usuarioDao().insertar(usuarioDePrueba("ana_23"))
        val zoe = db.usuarioDao().insertar(usuarioDePrueba("zoe"))
        val bea = db.usuarioDao().insertar(usuarioDePrueba("bea"))
        db.amistadDao().insertar(Amistad.entre(ana, zoe))
        db.amistadDao().insertar(Amistad.entre(bea, ana))

        val nombres = db.amistadDao().listarAmigos(ana).map { it.nombreUsuario }

        assertEquals(listOf("bea", "zoe"), nombres)
    }

    @Test
    fun eliminaUnaAmistadEnCualquierOrden() = runBlocking {
        val ana = db.usuarioDao().insertar(usuarioDePrueba("ana_23"))
        val luis = db.usuarioDao().insertar(usuarioDePrueba("luis_7"))
        db.amistadDao().insertar(Amistad.entre(ana, luis))

        val borradas = db.amistadDao().eliminar(luis, ana)

        assertEquals(1, borradas)
        assertFalse(db.amistadDao().sonAmigos(ana, luis))
    }

    @Test
    fun alBorrarUnUsuarioSeBorranSusAmistades() = runBlocking {
        val ana = db.usuarioDao().insertar(usuarioDePrueba("ana_23"))
        val luis = db.usuarioDao().insertar(usuarioDePrueba("luis_7"))
        db.amistadDao().insertar(Amistad.entre(ana, luis))

        db.openHelper.writableDatabase.execSQL("DELETE FROM usuarios WHERE id = $ana")

        assertEquals(0, db.amistadDao().contarAmigos(luis))
    }

    @Test(expected = SQLiteConstraintException::class)
    fun noPermiteUnaAmistadConUnUsuarioQueNoExiste(): Unit = runBlocking {
        val ana = db.usuarioDao().insertar(usuarioDePrueba("ana_23"))
        db.amistadDao().insertar(Amistad.entre(ana, 9_999L))
    }
}

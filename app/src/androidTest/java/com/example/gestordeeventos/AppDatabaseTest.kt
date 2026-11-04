package com.example.gestordeeventos

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
}

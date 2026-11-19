package com.example.gestordeeventos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gestordeeventos.data.local.AppDatabase
import com.example.gestordeeventos.data.model.Evento
import com.example.gestordeeventos.data.model.Usuario
import com.example.gestordeeventos.data.repositorio.EventoRepositorio
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Un evento del que sales no desaparece sin más: pasa a "Mis eventos > Unidos > Cancelados" como abandonado.
 * Base de datos Room real en memoria.
 */
@RunWith(AndroidJUnit4::class)
class EventosAbandonadosTest {

    private lateinit var db: AppDatabase
    private lateinit var eventos: EventoRepositorio
    private var luisId = 0L
    private var anaId = 0L

    @Before
    fun crearBaseDeDatos() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java).build()
        eventos = EventoRepositorio(db.eventoDao(), db.invitacionDao())
        runBlocking {
            luisId = db.usuarioDao().insertar(Usuario(nombreUsuario = "luis_9", correo = "luis@correo.com", claveHash = "h"))
            anaId = db.usuarioDao().insertar(Usuario(nombreUsuario = "ana_23", correo = "ana@correo.com", claveHash = "h"))
        }
    }

    @After
    fun cerrarBaseDeDatos() {
        db.close()
    }

    private suspend fun crearEventoDeLuis(nombre: String = "Fútbol 7"): Long {
        assertTrue(eventos.crearEvento(Evento(nombre = nombre, fechaHora = 2_000L, creadorId = luisId), emptyList()).ok)
        return db.eventoDao().listarCreadosPorUsuario(luisId).first { it.nombre == nombre }.id
    }

    @Test
    fun alSalirElEventoPasaAAbandonados() = runBlocking {
        val id = crearEventoDeLuis()
        eventos.apuntarse(id, anaId)

        assertTrue(eventos.salirDeEvento(id, anaId).ok)

        assertTrue(eventos.observarMisUnidosConCupo(anaId).first().isEmpty())
        val abandonados = eventos.observarMisSalidasConCupo(anaId).first()
        assertEquals(listOf(id), abandonados.map { it.evento.id })
        assertEquals(0, abandonados[0].apuntados) // su plaza queda libre
    }

    @Test
    fun siNoHaSalidoNoEstaEnAbandonados() = runBlocking {
        val id = crearEventoDeLuis()
        eventos.apuntarse(id, anaId)

        assertTrue(eventos.observarMisSalidasConCupo(anaId).first().isEmpty())
    }

    @Test
    fun alVolverAApuntarseDejaDeSerAbandonado() = runBlocking {
        val id = crearEventoDeLuis()
        eventos.apuntarse(id, anaId)
        eventos.salirDeEvento(id, anaId)

        assertTrue(eventos.apuntarse(id, anaId).ok)

        assertTrue(eventos.observarMisSalidasConCupo(anaId).first().isEmpty())
        assertEquals(listOf(id), eventos.observarMisUnidosConCupo(anaId).first().map { it.evento.id })
    }

    @Test
    fun salirDosVecesNoDuplicaElAbandonado() = runBlocking {
        val id = crearEventoDeLuis()
        eventos.apuntarse(id, anaId)
        eventos.salirDeEvento(id, anaId)
        eventos.apuntarse(id, anaId)
        eventos.salirDeEvento(id, anaId)

        assertEquals(1, eventos.observarMisSalidasConCupo(anaId).first().size)
    }

    @Test
    fun losAbandonadosSonSoloDeQuienSalio() = runBlocking {
        val id = crearEventoDeLuis()
        eventos.apuntarse(id, anaId)
        eventos.salirDeEvento(id, anaId)

        assertTrue(eventos.observarMisSalidasConCupo(luisId).first().isEmpty())
    }

    @Test
    fun siElCreadorEliminaElEventoDesapareceTambienDeAbandonados() = runBlocking {
        val id = crearEventoDeLuis()
        eventos.apuntarse(id, anaId)
        eventos.salirDeEvento(id, anaId)

        assertTrue(eventos.eliminarEvento(id, luisId).ok)

        assertTrue(eventos.observarMisSalidasConCupo(anaId).first().isEmpty())
    }
}

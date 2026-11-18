package com.example.gestordeeventos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gestordeeventos.data.local.AppDatabase
import com.example.gestordeeventos.data.model.Evento
import com.example.gestordeeventos.data.model.Participacion
import com.example.gestordeeventos.data.model.Usuario
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
 * Comprueba la SQL de la búsqueda por cercanía (`observarPublicosEnCaja`) con una base de datos real en memoria
 * (Issue 60; necesita emulador o móvil). La lógica del radio exacto se prueba sin emulador en `EventosCercaTest`.
 */
@RunWith(AndroidJUnit4::class)
class BusquedaEventosDaoTest {

    private lateinit var db: AppDatabase
    private var creadorId = 0L

    // Rectángulo de prueba: entre 40 y 41 de latitud y entre -4 y -3 de longitud
    private val latMin = 40.0
    private val latMax = 41.0
    private val lonMin = -4.0
    private val lonMax = -3.0

    @Before
    fun crearBaseDeDatos() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java).build()
        creadorId = runBlocking { crearUsuario("creador") }
    }

    @After
    fun cerrarBaseDeDatos() {
        db.close()
    }

    private suspend fun crearUsuario(nombre: String): Long =
        db.usuarioDao().insertar(Usuario(nombreUsuario = nombre, correo = "$nombre@correo.com", claveHash = "hash"))

    private suspend fun crearEvento(nombre: String, lat: Double, lon: Double, publico: Boolean = true): Long =
        db.eventoDao().insertar(
            Evento(nombre = nombre, publico = publico, latitud = lat, longitud = lon, fechaHora = 1_000L, creadorId = creadorId)
        )

    private fun enCaja(usuarioId: Long = creadorId) = runBlocking {
        db.eventoDao().observarPublicosEnCaja(latMin, latMax, lonMin, lonMax, usuarioId).first()
    }

    @Test
    fun devuelveSoloLosPublicosDentroDeLaCaja() = runBlocking {
        val dentro = crearEvento("Dentro", 40.5, -3.5)
        crearEvento("Privado dentro", 40.5, -3.5, publico = false)
        crearEvento("Fuera al norte", 42.0, -3.5)
        crearEvento("Fuera al oeste", 40.5, -5.0)

        assertEquals(listOf(dentro), enCaja().map { it.evento.id })
    }

    @Test
    fun losPrivadosNuncaSalen() = runBlocking {
        crearEvento("Privado", 40.5, -3.5, publico = false)

        assertTrue(enCaja().isEmpty())
    }

    @Test
    fun losBordesDeLaCajaCuentanComoDentro() = runBlocking {
        val enLaEsquina = crearEvento("Esquina", latMin, lonMax)
        val enElBorde = crearEvento("Borde", latMax, -3.5)

        assertEquals(setOf(enLaEsquina, enElBorde), enCaja().map { it.evento.id }.toSet())
    }

    @Test
    fun unEventoSinPuntoEnElMapaQuedaFueraAunqueLaCajaCubraElCero() = runBlocking {
        crearEvento("Sin punto", 0.0, 0.0)

        val sinPunto = db.eventoDao().observarPublicosEnCaja(-1.0, 1.0, -1.0, 1.0, creadorId).first()

        assertTrue(sinPunto.isEmpty())
    }

    @Test
    fun cuentaLosApuntadosYSabeSiYoEstoyApuntado() = runBlocking {
        val ana = crearUsuario("ana")
        val luis = crearUsuario("luis")
        val otro = crearUsuario("otro")
        val evento = crearEvento("Fútbol", 40.5, -3.5)
        db.eventoDao().insertarParticipacion(Participacion(evento, ana))
        db.eventoDao().insertarParticipacion(Participacion(evento, luis))

        val paraAna = enCaja(ana).single()
        val paraOtro = enCaja(otro).single()

        assertEquals(2, paraAna.apuntados)
        assertTrue(paraAna.yoApuntado)
        assertEquals(2, paraOtro.apuntados)
        assertFalse(paraOtro.yoApuntado)
    }

    @Test
    fun sinEventosEnLaCajaDevuelveUnaListaVacia() = runBlocking {
        crearEvento("Lejos", 10.0, 10.0)

        assertTrue(enCaja().isEmpty())
    }
}

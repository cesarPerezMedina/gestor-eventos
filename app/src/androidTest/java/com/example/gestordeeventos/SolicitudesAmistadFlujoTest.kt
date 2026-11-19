package com.example.gestordeeventos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gestordeeventos.data.local.AppDatabase
import com.example.gestordeeventos.data.model.Usuario
import com.example.gestordeeventos.data.repositorio.AmistadRepositorio
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
 * Flujo completo de una solicitud de amistad entre dos cuentas, con una base de datos Room real en memoria:
 * una cuenta la envía, a la otra le aparece en "Solicitudes", y al aceptarla o rechazarla se actualiza todo.
 */
@RunWith(AndroidJUnit4::class)
class SolicitudesAmistadFlujoTest {

    private lateinit var db: AppDatabase
    private lateinit var amigos: AmistadRepositorio
    private var anaId = 0L
    private var luisId = 0L

    private val ana = "ana@correo.com"
    private val luis = "luis@correo.com"

    @Before
    fun crearBaseDeDatos() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java).build()
        amigos = AmistadRepositorio(db.usuarioDao(), db.amistadDao())
        runBlocking {
            anaId = db.usuarioDao().insertar(Usuario(nombreUsuario = "ana_23", correo = ana, claveHash = "h"))
            luisId = db.usuarioDao().insertar(Usuario(nombreUsuario = "luis_9", correo = luis, claveHash = "h"))
        }
    }

    @After
    fun cerrarBaseDeDatos() {
        db.close()
    }

    @Test
    fun laSolicitudEnviadaLeApareceAlDestinatario() = runBlocking {
        assertTrue(amigos.enviarSolicitud(ana, "luis_9").ok)

        val recibidas = amigos.observarSolicitudesRecibidas(luis).first()

        assertEquals(1, recibidas.size)
        assertEquals("ana_23", recibidas[0].nombreUsuario)
        assertEquals(ana, recibidas[0].correo)
        assertEquals(anaId, recibidas[0].id)
    }

    @Test
    fun unaSolicitudPendienteNoCuentaComoAmigoHastaAceptarla() = runBlocking {
        amigos.enviarSolicitud(ana, "luis_9")

        assertTrue(amigos.observarAmigos(ana).first().isEmpty())
        assertTrue(amigos.observarAmigos(luis).first().isEmpty())
    }

    @Test
    fun quienLaEnviaNoLaTieneEnSusRecibidas() = runBlocking {
        amigos.enviarSolicitud(ana, "luis_9")

        assertTrue(amigos.observarSolicitudesRecibidas(ana).first().isEmpty())
    }

    @Test
    fun sePuedeEnviarPorIdNumericoConOSinAlmohadilla() = runBlocking {
        assertTrue(amigos.enviarSolicitud(ana, "#$luisId").ok)

        assertEquals(1, amigos.observarSolicitudesRecibidas(luis).first().size)
    }

    @Test
    fun aceptarlaLaConvierteEnAmistadParaLosDos() = runBlocking {
        amigos.enviarSolicitud(ana, "luis_9")

        assertTrue(amigos.aceptarSolicitud(luis, ana).ok)

        assertTrue(amigos.observarSolicitudesRecibidas(luis).first().isEmpty())
        assertEquals(listOf("luis_9"), amigos.observarAmigos(ana).first().map { it.nombreUsuario })
        assertEquals(listOf("ana_23"), amigos.observarAmigos(luis).first().map { it.nombreUsuario })
    }

    @Test
    fun rechazarlaLaBorraYNoHaceAmigos() = runBlocking {
        amigos.enviarSolicitud(ana, "luis_9")

        assertTrue(amigos.rechazarSolicitud(luis, ana).ok)

        assertTrue(amigos.observarSolicitudesRecibidas(luis).first().isEmpty())
        assertTrue(amigos.observarAmigos(ana).first().isEmpty())
        assertTrue(amigos.observarAmigos(luis).first().isEmpty())
    }

    @Test
    fun unaSolicitudPendienteNoCuentaComoAmigo() = runBlocking {
        amigos.enviarSolicitud(ana, "luis_9")

        assertTrue(amigos.observarAmigos(ana).first().isEmpty())
        assertTrue(amigos.observarAmigos(luis).first().isEmpty())
    }

    @Test
    fun noSePuedeEnviarDosVecesLaMismaSolicitud() = runBlocking {
        assertTrue(amigos.enviarSolicitud(ana, "luis_9").ok)

        val segunda = amigos.enviarSolicitud(ana, "luis_9")

        assertFalse(segunda.ok)
        assertEquals(1, amigos.observarSolicitudesRecibidas(luis).first().size)
    }
}

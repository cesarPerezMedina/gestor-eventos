package com.example.gestordeeventos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gestordeeventos.data.local.AppDatabase
import com.example.gestordeeventos.data.model.Deporte
import com.example.gestordeeventos.data.model.EstadoInvitacion
import com.example.gestordeeventos.data.model.Evento
import com.example.gestordeeventos.data.model.Invitacion
import com.example.gestordeeventos.data.model.Usuario
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Comprueba la consulta SQL de invitaciones pendientes contra una base de datos real en memoria. */
@RunWith(AndroidJUnit4::class)
class InvitacionDaoTest {

    private lateinit var db: AppDatabase
    private var anaId = 0L
    private var luisId = 0L
    private var futbolId = 0L
    private var tenisId = 0L

    @Before
    fun crearBaseDeDatos() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java).build()
        runBlocking {
            val usuarios = db.usuarioDao()
            anaId = usuarios.insertar(Usuario(nombreUsuario = "ana_23", correo = "ana@correo.com", claveHash = "h"))
            luisId = usuarios.insertar(Usuario(nombreUsuario = "luis_9", correo = "luis@correo.com", claveHash = "h"))
            futbolId = db.eventoDao().insertar(
                Evento(nombre = "Fútbol 7", deporte = Deporte.FUTBOL, fechaHora = 2_000L, creadorId = luisId)
            )
            tenisId = db.eventoDao().insertar(
                Evento(nombre = "Tenis dobles", deporte = Deporte.TENIS, fechaHora = 1_000L, creadorId = luisId)
            )
        }
    }

    @After
    fun cerrarBaseDeDatos() {
        db.close()
    }

    private suspend fun invitar(eventoId: Long, estado: EstadoInvitacion = EstadoInvitacion.PENDIENTE) =
        db.invitacionDao().insertar(
            Invitacion(eventoId = eventoId, deUsuarioId = luisId, paraUsuarioId = anaId, estado = estado)
        )

    @Test
    fun devuelveSoloLasPendientesConNombreDeEventoYDeQuienInvita() = runBlocking {
        invitar(futbolId)
        invitar(tenisId, EstadoInvitacion.RECHAZADA)

        val pendientes = db.invitacionDao().observarPendientes(anaId).first()

        assertEquals(1, pendientes.size)
        assertEquals("Fútbol 7", pendientes[0].eventoNombre)
        assertEquals(Deporte.FUTBOL, pendientes[0].deporte)
        assertEquals("luis_9", pendientes[0].deNombreUsuario)
    }

    @Test
    fun ordenaPorFechaDelEvento() = runBlocking {
        invitar(futbolId) // 2000
        invitar(tenisId) // 1000

        val nombres = db.invitacionDao().observarPendientes(anaId).first().map { it.eventoNombre }

        assertEquals(listOf("Tenis dobles", "Fútbol 7"), nombres)
    }

    @Test
    fun otroUsuarioNoVeLasInvitacionesDeAna() = runBlocking {
        invitar(futbolId)

        assertTrue(db.invitacionDao().observarPendientes(luisId).first().isEmpty())
    }

    @Test
    fun responderSiPendienteCambiaElEstadoSoloUnaVez() = runBlocking {
        val id = invitar(futbolId)
        val dao = db.invitacionDao()

        assertEquals(1, dao.responderSiPendiente(id, anaId, EstadoInvitacion.RECHAZADA))
        assertEquals(EstadoInvitacion.RECHAZADA, dao.buscarPorId(id)?.estado)
        assertEquals(0, dao.responderSiPendiente(id, anaId, EstadoInvitacion.ACEPTADA))
        assertEquals(EstadoInvitacion.RECHAZADA, dao.buscarPorId(id)?.estado)
    }

    @Test
    fun responderSiPendienteNoCambiaLaInvitacionDeOtroUsuario() = runBlocking {
        val id = invitar(futbolId)

        assertEquals(0, db.invitacionDao().responderSiPendiente(id, luisId, EstadoInvitacion.RECHAZADA))
        assertEquals(EstadoInvitacion.PENDIENTE, db.invitacionDao().buscarPorId(id)?.estado)
    }

    @Test
    fun laRechazadaDejaDeSalirEnLasPendientes() = runBlocking {
        val id = invitar(futbolId)
        db.invitacionDao().responderSiPendiente(id, anaId, EstadoInvitacion.RECHAZADA)

        assertTrue(db.invitacionDao().observarPendientes(anaId).first().isEmpty())
    }
}

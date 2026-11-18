package com.example.gestordeeventos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gestordeeventos.data.local.AppDatabase
import com.example.gestordeeventos.data.model.Deporte
import com.example.gestordeeventos.data.model.Evento
import com.example.gestordeeventos.data.model.Usuario
import com.example.gestordeeventos.data.repositorio.EventoRepositorio
import com.example.gestordeeventos.data.repositorio.InvitacionRepositorio
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de integración del almacenamiento de los eventos creados (Issue 51): se crea un evento con
 * `EventoRepositorio` (las reglas) y se comprueba que queda bien guardado en Room (la base de datos real,
 * en memoria) y que se recupera igual, ordenado y solo para quien corresponde.
 */
@RunWith(AndroidJUnit4::class)
class AlmacenamientoEventosTest {

    private lateinit var contexto: Context
    private lateinit var db: AppDatabase
    private lateinit var eventos: EventoRepositorio
    private lateinit var invitaciones: InvitacionRepositorio
    private var luisId = 0L
    private var anaId = 0L

    @Before
    fun crearBaseDeDatos() {
        contexto = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java).build()
        eventos = EventoRepositorio(db.eventoDao(), db.invitacionDao())
        invitaciones = InvitacionRepositorio(db.invitacionDao())
        runBlocking {
            luisId = db.usuarioDao().insertar(Usuario(nombreUsuario = "luis_9", correo = "luis@correo.com", claveHash = "h"))
            anaId = db.usuarioDao().insertar(Usuario(nombreUsuario = "ana_23", correo = "ana@correo.com", claveHash = "h"))
        }
    }

    @After
    fun cerrarBaseDeDatos() {
        db.close()
    }

    private fun evento(
        nombre: String,
        creadorId: Long = luisId,
        fecha: Long = 2_000L,
        publico: Boolean = true,
    ) = Evento(nombre = nombre, fechaHora = fecha, creadorId = creadorId, publico = publico)

    @Test
    fun guardaTodosLosCamposDelEvento() = runBlocking {
        val original = Evento(
            nombre = "Fútbol 7 en el parque",
            descripcion = "Trae botas y agua",
            deporte = Deporte.FUTBOL,
            publico = false,
            minParticipantes = 6,
            maxParticipantes = 14,
            direccion = "Parque del Retiro, Madrid",
            latitud = 40.4153,
            longitud = -3.6844,
            fechaHora = 1_795_300_000_000L,
            creadorId = luisId,
        )

        assertTrue(eventos.crearEvento(original, emptyList()).ok)

        val guardado = eventos.eventosCreadosPorUsuario(luisId).single()
        assertEquals(original.copy(id = guardado.id), guardado)
        assertEquals(guardado, db.eventoDao().buscarPorId(guardado.id))
    }

    @Test
    fun elNombreYLaDescripcionSeGuardanSinEspaciosSobrantes() = runBlocking {
        eventos.crearEvento(evento("  Tenis dobles  ").copy(descripcion = "  Pista 2  "), emptyList())

        val guardado = eventos.eventosCreadosPorUsuario(luisId).single()
        assertEquals("Tenis dobles", guardado.nombre)
        assertEquals("Pista 2", guardado.descripcion)
    }

    @Test
    fun cadaEventoRecibeUnIdDistinto() = runBlocking {
        eventos.crearEvento(evento("Fútbol 7"), emptyList())
        eventos.crearEvento(evento("Fútbol 7"), emptyList()) // mismo nombre: son dos eventos

        val ids = eventos.eventosCreadosPorUsuario(luisId).map { it.id }
        assertEquals(2, ids.size)
        assertNotEquals(ids[0], ids[1])
    }

    @Test
    fun losEventosCreadosSalenOrdenadosPorFecha() = runBlocking {
        eventos.crearEvento(evento("Tercero", fecha = 3_000L), emptyList())
        eventos.crearEvento(evento("Primero", fecha = 1_000L), emptyList())
        eventos.crearEvento(evento("Segundo", fecha = 2_000L), emptyList())

        assertEquals(
            listOf("Primero", "Segundo", "Tercero"),
            eventos.eventosCreadosPorUsuario(luisId).map { it.nombre }
        )
        assertEquals(
            listOf("Primero", "Segundo", "Tercero"),
            eventos.observarEventosCreados(luisId).first().map { it.nombre }
        )
    }

    @Test
    fun cadaUsuarioSoloVeSusEventosCreados() = runBlocking {
        eventos.crearEvento(evento("De Luis"), emptyList())
        eventos.crearEvento(evento("De Ana", creadorId = anaId), emptyList())

        assertEquals(listOf("De Luis"), eventos.eventosCreadosPorUsuario(luisId).map { it.nombre })
        assertEquals(listOf("De Ana"), eventos.eventosCreadosPorUsuario(anaId).map { it.nombre })
    }

    @Test
    fun unNombreDemasiadoCortoNoSeGuarda() = runBlocking {
        val resultado = eventos.crearEvento(evento("ab"), emptyList())

        assertFalse(resultado.ok)
        assertTrue(eventos.eventosCreadosPorUsuario(luisId).isEmpty())
    }

    @Test
    fun unNombreSoloConEspaciosNoSeGuarda() = runBlocking {
        assertFalse(eventos.crearEvento(evento("     "), emptyList()).ok)
        assertTrue(eventos.eventosCreadosPorUsuario(luisId).isEmpty())
    }

    @Test
    fun unMaximoMenorQueElMinimoNoSeGuarda() = runBlocking {
        val resultado = eventos.crearEvento(evento("Fútbol 7").copy(minParticipantes = 10, maxParticipantes = 4), emptyList())

        assertFalse(resultado.ok)
        assertTrue(eventos.eventosCreadosPorUsuario(luisId).isEmpty())
    }

    @Test
    fun sinLimiteDeParticipantesSeGuardaConCero() = runBlocking {
        assertTrue(eventos.crearEvento(evento("Fútbol 7").copy(minParticipantes = 4, maxParticipantes = 0), emptyList()).ok)

        assertEquals(0, eventos.eventosCreadosPorUsuario(luisId).single().maxParticipantes)
    }

    @Test
    fun elEventoPublicoSaleEnBuscarYElPrivadoNo() = runBlocking {
        eventos.crearEvento(evento("Abierto", publico = true), emptyList())
        eventos.crearEvento(evento("Cerrado", publico = false), emptyList())

        assertEquals(listOf("Abierto"), eventos.eventosPublicos().first().map { it.nombre })
        // El creador ve los dos en sus listas
        assertEquals(2, eventos.misEventos(luisId).first().size)
    }

    @Test
    fun unEventoPrivadoGuardaUnaInvitacionPorPersonaSinRepetirNiInvitarAlCreador() = runBlocking {
        eventos.crearEvento(evento("Cerrado", publico = false), listOf(anaId, anaId, luisId))

        assertEquals(1, invitaciones.observarPendientes(anaId).first().size)
        assertTrue(invitaciones.observarPendientes(luisId).first().isEmpty())
    }

    @Test
    fun siElEventoNoSeGuardaNoSeCreanInvitaciones() = runBlocking {
        eventos.crearEvento(evento("ab", publico = false), listOf(anaId))

        assertTrue(invitaciones.observarPendientes(anaId).first().isEmpty())
    }

    @Test
    fun misEventosSeActualizaSoloAlCrearOtroEvento() = runBlocking {
        assertTrue(eventos.misEventos(luisId).first().isEmpty())

        eventos.crearEvento(evento("Fútbol 7"), emptyList())
        assertEquals(1, eventos.misEventos(luisId).first().size)

        eventos.crearEvento(evento("Tenis dobles"), emptyList())
        assertEquals(2, eventos.misEventos(luisId).first().size)
    }

    @Test
    fun losEventosSeConservanAlCerrarYVolverAAbrirLaBaseDeDatos() = runBlocking {
        val nombreBd = "prueba_persistencia_eventos.db"
        contexto.deleteDatabase(nombreBd)
        var base = Room.databaseBuilder(contexto, AppDatabase::class.java, nombreBd).build()
        try {
            val usuarioId = base.usuarioDao().insertar(
                Usuario(nombreUsuario = "pepe", correo = "pepe@correo.com", claveHash = "h")
            )
            EventoRepositorio(base.eventoDao(), base.invitacionDao())
                .crearEvento(evento("Fútbol 7", creadorId = usuarioId), emptyList())
            base.close()

            base = Room.databaseBuilder(contexto, AppDatabase::class.java, nombreBd).build()
            val guardados = base.eventoDao().listarCreadosPorUsuario(usuarioId)

            assertEquals(listOf("Fútbol 7"), guardados.map { it.nombre })
            assertNotNull(base.eventoDao().buscarPorId(guardados.single().id))
        } finally {
            base.close()
            contexto.deleteDatabase(nombreBd)
        }
    }
}

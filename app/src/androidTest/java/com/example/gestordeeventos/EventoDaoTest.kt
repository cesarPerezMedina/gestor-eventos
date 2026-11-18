package com.example.gestordeeventos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gestordeeventos.data.local.AppDatabase
import com.example.gestordeeventos.data.model.EstadoEvento
import com.example.gestordeeventos.data.model.Evento
import com.example.gestordeeventos.data.model.Participacion
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

/** Pruebas de "Mis eventos": consultas con plazas (Issue 64) y limpieza automática de eventos pasados (Issue 66). */
@RunWith(AndroidJUnit4::class)
class MisEventosDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var repositorio: EventoRepositorio

    private val hora = 3_600_000L
    private val ahora = 1_000 * hora

    @Before
    fun crearBaseDeDatos() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java).build()
        repositorio = EventoRepositorio(db.eventoDao(), db.invitacionDao())
    }

    @After
    fun cerrarBaseDeDatos() {
        db.close()
    }

    private suspend fun usuario(nombre: String): Long =
        db.usuarioDao().insertar(Usuario(nombreUsuario = nombre, correo = "$nombre@correo.com", claveHash = "hash"))

    private suspend fun evento(
        nombre: String,
        creadorId: Long,
        fecha: Long = ahora + 24 * hora,
        estado: EstadoEvento = EstadoEvento.PUBLICADO,
    ): Long = db.eventoDao().insertar(
        Evento(nombre = nombre, fechaHora = fecha, creadorId = creadorId, maxParticipantes = 14, estado = estado)
    )

    // ---------- Issue 64 ----------

    @Test
    fun creados_incluye_borradores_y_cancelados_con_sus_apuntados() = runBlocking {
        val carlos = usuario("carlos")
        val ana = usuario("ana")
        val publicado = evento("Fútbol", carlos)
        evento("Borrador", carlos, fecha = ahora + 48 * hora, estado = EstadoEvento.BORRADOR)
        evento("De Ana", ana)
        db.participacionDao().insertar(Participacion(publicado, ana))

        val creados = repositorio.observarMisCreadosConCupo(carlos).first()

        assertEquals(listOf("Fútbol", "Borrador"), creados.map { it.evento.nombre })
        assertEquals(1, creados.first { it.evento.nombre == "Fútbol" }.apuntados)
    }

    @Test
    fun unidos_solo_trae_eventos_de_otros_en_los_que_participo() = runBlocking {
        val carlos = usuario("carlos")
        val ana = usuario("ana")
        val deAna = evento("De Ana", ana)
        val propio = evento("Propio", carlos)
        evento("Sin apuntarme", ana)
        db.participacionDao().insertar(Participacion(deAna, carlos))
        db.participacionDao().insertar(Participacion(propio, carlos)) // mi propio evento no cuenta como "unido"

        val unidos = repositorio.observarMisUnidosConCupo(carlos).first()

        assertEquals(listOf("De Ana"), unidos.map { it.evento.nombre })
        assertTrue(unidos.single().yoApuntado)
    }

    // ---------- Issue 66 ----------

    @Test
    fun archiva_solo_los_publicados_pasados_hace_mas_de_48_horas() = runBlocking {
        val carlos = usuario("carlos")
        val viejo = evento("Viejo", carlos, fecha = ahora - 49 * hora)
        val reciente = evento("Reciente", carlos, fecha = ahora - 47 * hora)
        val futuro = evento("Futuro", carlos)
        val borradorViejo = evento("Borrador viejo", carlos, fecha = ahora - 100 * hora, estado = EstadoEvento.BORRADOR)

        val archivados = repositorio.archivarEventosPasados(ahora)

        assertEquals(1, archivados)
        val dao = db.eventoDao()
        assertEquals(EstadoEvento.ARCHIVADO, dao.buscarPorId(viejo)!!.estado)
        assertEquals(EstadoEvento.PUBLICADO, dao.buscarPorId(reciente)!!.estado)
        assertEquals(EstadoEvento.PUBLICADO, dao.buscarPorId(futuro)!!.estado)
        assertEquals(EstadoEvento.BORRADOR, dao.buscarPorId(borradorViejo)!!.estado)
    }

    @Test
    fun un_evento_archivado_deja_de_salir_en_buscar_pero_sigue_en_mis_eventos() = runBlocking {
        val carlos = usuario("carlos")
        evento("Viejo", carlos, fecha = ahora - 49 * hora)

        repositorio.archivarEventosPasados(ahora)

        assertTrue(repositorio.eventosPublicos().first().isEmpty())
        assertEquals(listOf("Viejo"), repositorio.observarMisCreadosConCupo(carlos).first().map { it.evento.nombre })
    }

    @Test
    fun no_se_puede_apuntar_a_un_evento_archivado() = runBlocking {
        val carlos = usuario("carlos")
        val ana = usuario("ana")
        val viejo = evento("Viejo", carlos, fecha = ahora - 49 * hora)
        repositorio.archivarEventosPasados(ahora)

        val resultado = repositorio.apuntarse(viejo, ana)

        assertEquals(false, resultado.ok)
    }

    @Test
    fun archivar_dos_veces_no_hace_nada_la_segunda() = runBlocking {
        val carlos = usuario("carlos")
        evento("Viejo", carlos, fecha = ahora - 49 * hora)

        assertEquals(1, repositorio.archivarEventosPasados(ahora))
        assertEquals(0, repositorio.archivarEventosPasados(ahora))
    }
}
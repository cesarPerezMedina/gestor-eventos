package com.example.gestordeeventos.data.repositorio

import android.content.Context
import com.example.gestordeeventos.data.local.AppDatabase
import com.example.gestordeeventos.data.local.dao.EventoDao
import com.example.gestordeeventos.data.local.dao.InvitacionDao
import com.example.gestordeeventos.data.model.Evento
import com.example.gestordeeventos.data.model.EventoConCupo
import com.example.gestordeeventos.data.model.Invitacion
import com.example.gestordeeventos.data.model.Participacion
import kotlinx.coroutines.flow.Flow

data class ResultadoEvento(val ok: Boolean, val mensaje: String)

/**
 * Reglas de los eventos, incluida la diferencia entre públicos y privados (Issue 43).
 * Los usuarios se identifican aquí por su id.
 */
class EventoRepositorio(
    private val eventoDao: EventoDao,
    private val invitacionDao: InvitacionDao,
) {

    /** Eventos que salen en Buscar: solo los públicos, siempre actualizados. */
    fun eventosPublicos(): Flow<List<Evento>> = eventoDao.observarPublicos()

    /** Eventos de Buscar con su número de apuntados: solo los públicos, siempre actualizados. */
    fun eventosPublicosConCupo(): Flow<List<EventoConCupo>> = eventoDao.observarPublicosConCupo()

    /** Eventos de "Mis eventos": los que creo, en los que participo o a los que me han invitado. */
    fun misEventos(usuarioId: Long): Flow<List<Evento>> = eventoDao.observarMisEventos(usuarioId)

    /** Eventos creados por el usuario, de más próximos a más lejanos. */
    suspend fun eventosCreadosPorUsuario(usuarioId: Long): List<Evento> =
        eventoDao.listarCreadosPorUsuario(usuarioId)

    /** Eventos en los que el usuario tiene una participación. */
    suspend fun eventosEnLosQueParticipa(usuarioId: Long): List<Evento> =
        eventoDao.listarEventosParticipadosPorUsuario(usuarioId)

    /** Público: cualquiera. Privado: solo el creador, los invitados y los participantes. */
    suspend fun puedeVer(eventoId: Long, usuarioId: Long): Boolean =
        eventoDao.puedeVer(eventoId, usuarioId)

    /**
     * Guarda el evento y, si hay invitados, crea una invitación para cada uno.
     * La visibilidad sale de [Evento.publico]; el creador siempre es quien tiene la sesión.
     */
    suspend fun crearEvento(evento: Evento, invitadosIds: List<Long>): ResultadoEvento {
        // ... (igual que lo tenías)
    }

    /** Apuntarse a un evento. En uno privado solo se puede con invitación. */
    suspend fun apuntarse(eventoId: Long, usuarioId: Long): ResultadoEvento {
        // ... (igual que lo tenías)
    }

    companion object {
        /** Crea el repositorio usando la base de datos local de la aplicación. */
        fun crear(context: Context): EventoRepositorio {
            val db = AppDatabase.obtener(context)
            return EventoRepositorio(db.eventoDao(), db.invitacionDao())
        }
    }
}

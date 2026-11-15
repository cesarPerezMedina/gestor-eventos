package com.example.gestordeeventos.data.repositorio

import com.example.gestordeeventos.data.local.dao.EventoDao
import com.example.gestordeeventos.data.local.dao.InvitacionDao
import com.example.gestordeeventos.data.localizacion.EventoConDistancia
import com.example.gestordeeventos.data.localizacion.Ubicacion
import com.example.gestordeeventos.data.localizacion.cajaDelimitadora
import com.example.gestordeeventos.data.localizacion.distanciaKm
import com.example.gestordeeventos.data.model.Evento
import com.example.gestordeeventos.data.model.EventoConCupo
import com.example.gestordeeventos.data.model.Invitacion
import com.example.gestordeeventos.data.model.Participacion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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

    /** Eventos de Buscar con su número de apuntados y si [usuarioId] ya está apuntado: solo los públicos, siempre actualizados. */
    fun eventosPublicosConCupo(usuarioId: Long): Flow<List<EventoConCupo>> = eventoDao.observarPublicosConCupo(usuarioId)

    /**
     * Búsqueda por cercanía (Issue 57): eventos públicos a menos de [radioKm] km de [centro],
     * del más cercano al más lejano. Primero la base de datos descarta lo que queda fuera de un
     * rectángulo alrededor del punto; después se calcula la distancia exacta y se filtra por el radio.
     */
    fun eventosPublicosCerca(centro: Ubicacion, radioKm: Double, usuarioId: Long): Flow<List<EventoConDistancia>> {
        val caja = cajaDelimitadora(centro, radioKm)
        return eventoDao
            .observarPublicosEnCaja(caja.latMin, caja.latMax, caja.lonMin, caja.lonMax, usuarioId)
            .map { lista ->
                lista
                    .map { EventoConDistancia(it, distanciaKm(centro.latitud, centro.longitud, it.evento.latitud, it.evento.longitud)) }
                    .filter { it.distanciaKm <= radioKm }
                    .sortedBy { it.distanciaKm }
            }
    }

    /** Eventos de "Mis eventos": los que creo, en los que participo o a los que me han invitado. */
    fun misEventos(usuarioId: Long): Flow<List<Evento>> = eventoDao.observarMisEventos(usuarioId)

    /** Público: cualquiera. Privado: solo el creador, los invitados y los participantes. */
    suspend fun puedeVer(eventoId: Long, usuarioId: Long): Boolean =
        eventoDao.puedeVer(eventoId, usuarioId)

    /**
     * Guarda el evento y, si hay invitados, crea una invitación para cada uno.
     * La visibilidad sale de [Evento.publico]; el creador siempre es quien tiene la sesión.
     */
    suspend fun crearEvento(evento: Evento, invitadosIds: List<Long>): ResultadoEvento {
        if (evento.nombre.trim().length < 3) {
            return ResultadoEvento(false, "El nombre debe tener al menos 3 caracteres")
        }
        if (evento.maxParticipantes in 1 until evento.minParticipantes) {
            return ResultadoEvento(false, "El máximo de jugadores no puede ser menor que el mínimo")
        }

        val eventoId = eventoDao.insertar(evento.copy(nombre = evento.nombre.trim(), descripcion = evento.descripcion.trim()))

        invitadosIds
            .distinct()
            .filter { it != evento.creadorId }
            .forEach { invitado ->
                invitacionDao.insertar(
                    Invitacion(eventoId = eventoId, deUsuarioId = evento.creadorId, paraUsuarioId = invitado)
                )
            }

        return ResultadoEvento(
            ok = true,
            mensaje = if (evento.publico) "Evento público creado" else "Evento privado creado",
        )
    }

    /** Apuntarse a un evento. En uno privado solo se puede con invitación. */
    suspend fun apuntarse(eventoId: Long, usuarioId: Long): ResultadoEvento {
        val evento = eventoDao.buscarPorId(eventoId)
            ?: return ResultadoEvento(false, "El evento ya no existe")

        if (eventoDao.estaApuntado(eventoId, usuarioId)) {
            return ResultadoEvento(false, "Ya estás apuntado a este evento")
        }
        if (!evento.publico && evento.creadorId != usuarioId && !eventoDao.tieneInvitacion(eventoId, usuarioId)) {
            return ResultadoEvento(false, "Este evento es privado: necesitas una invitación")
        }
        if (evento.maxParticipantes > 0 && eventoDao.contarParticipantes(eventoId) >= evento.maxParticipantes) {
            return ResultadoEvento(false, "El evento está completo")
        }

        eventoDao.insertarParticipacion(Participacion(eventoId = eventoId, usuarioId = usuarioId))
        eventoDao.aceptarInvitacion(eventoId, usuarioId)
        return ResultadoEvento(true, "Te has apuntado al evento")
    }
}
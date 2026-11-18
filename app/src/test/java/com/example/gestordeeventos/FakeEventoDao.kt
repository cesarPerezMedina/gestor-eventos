package com.example.gestordeeventos

import com.example.gestordeeventos.data.local.dao.EventoDao
import com.example.gestordeeventos.data.model.Evento
import com.example.gestordeeventos.data.model.EventoConCupo
import com.example.gestordeeventos.data.model.Participacion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * DAO falso en memoria para probar la búsqueda de eventos sin base de datos ni emulador.
 * Imita las consultas de Buscar: [observarPublicosConCupo] y [observarPublicosEnCaja].
 * (La SQL de verdad se comprueba en el test con emulador `BusquedaEventosDaoTest`.)
 *
 * @param filtrarPublicos si es false, las consultas devuelven también los privados; sirve para comprobar que
 * el ViewModel no los enseña aunque la consulta fallara.
 */
class FakeEventoDao(
    eventos: List<Evento> = emptyList(),
    private val participaciones: List<Participacion> = emptyList(),
    private val filtrarPublicos: Boolean = true,
) : EventoDao {

    private val eventos = MutableStateFlow(eventos)

    /** Simula que alguien crea un evento mientras la pantalla está abierta. */
    fun agregar(evento: Evento) {
        eventos.value = eventos.value + evento
    }

    private fun Evento.aCupo(usuarioId: Long) = EventoConCupo(
        evento = this,
        apuntados = participaciones.count { it.eventoId == id },
        yoApuntado = participaciones.any { it.eventoId == id && it.usuarioId == usuarioId },
    )

    private fun visibles(lista: List<Evento>) = if (filtrarPublicos) lista.filter { it.publico } else lista

    override fun observarPublicos(): Flow<List<Evento>> =
        eventos.map { visibles(it).sortedBy { e -> e.fechaHora } }

    override fun observarPublicosConCupo(usuarioId: Long): Flow<List<EventoConCupo>> =
        eventos.map { lista -> visibles(lista).sortedBy { it.fechaHora }.map { it.aCupo(usuarioId) } }

    override fun observarPublicosEnCaja(
        latMin: Double,
        latMax: Double,
        lonMin: Double,
        lonMax: Double,
        usuarioId: Long,
    ): Flow<List<EventoConCupo>> = eventos.map { lista ->
        visibles(lista)
            .filter { !(it.latitud == 0.0 && it.longitud == 0.0) }
            .filter { it.latitud in latMin..latMax && it.longitud in lonMin..lonMax }
            .map { it.aCupo(usuarioId) }
    }

    // Lo demás no se usa en la búsqueda

    override suspend fun insertar(evento: Evento): Long = throw UnsupportedOperationException()
    override suspend fun buscarPorId(id: Long): Evento? = eventos.value.firstOrNull { it.id == id }
    override fun observarMisEventos(usuarioId: Long): Flow<List<Evento>> = throw UnsupportedOperationException()
    override suspend fun puedeVer(eventoId: Long, usuarioId: Long): Boolean = throw UnsupportedOperationException()
    override suspend fun estaApuntado(eventoId: Long, usuarioId: Long): Boolean = throw UnsupportedOperationException()
    override suspend fun contarParticipantes(eventoId: Long): Int = throw UnsupportedOperationException()
    override suspend fun tieneInvitacion(eventoId: Long, usuarioId: Long): Boolean = throw UnsupportedOperationException()
    override suspend fun insertarParticipacion(participacion: Participacion): Long = throw UnsupportedOperationException()
    override suspend fun aceptarInvitacion(eventoId: Long, usuarioId: Long): Int = throw UnsupportedOperationException()
    override suspend fun listarCreadosPorUsuario(usuarioId: Long): List<Evento> = throw UnsupportedOperationException()
    override suspend fun listarEventosParticipadosPorUsuario(usuarioId: Long): List<Evento> = throw UnsupportedOperationException()
    override fun observarCreados(usuarioId: Long): Flow<List<Evento>> = throw UnsupportedOperationException()
    override fun observarUnidos(usuarioId: Long): Flow<List<Evento>> = throw UnsupportedOperationException()
    override suspend fun eliminarParticipacion(eventoId: Long, usuarioId: Long): Int = throw UnsupportedOperationException()
    override suspend fun cerrarInvitaciones(eventoId: Long, usuarioId: Long): Int = throw UnsupportedOperationException()
    override fun observarCreadosConCupo(usuarioId: Long): Flow<List<EventoConCupo>> = throw UnsupportedOperationException()
    override fun observarUnidosConCupo(usuarioId: Long): Flow<List<EventoConCupo>> = throw UnsupportedOperationException()
    override suspend fun eliminar(eventoId: Long): Int = throw UnsupportedOperationException()
    override suspend fun archivarPasados(limite: Long): Int = throw UnsupportedOperationException()
    override suspend fun borrarParticipacion(eventoId: Long, usuarioId: Long): Int = throw UnsupportedOperationException()
    override suspend fun eliminarSiEsCreador(eventoId: Long, creadorId: Long): Int = throw UnsupportedOperationException()
}

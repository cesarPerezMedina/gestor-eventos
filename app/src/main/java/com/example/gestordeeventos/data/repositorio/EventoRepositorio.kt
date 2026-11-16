package com.example.gestordeeventos.data.repositorio

import android.content.Context
import com.example.gestordeeventos.data.local.AppDatabase
import com.example.gestordeeventos.data.local.dao.EventoDao
import com.example.gestordeeventos.data.model.Evento

/**
 * Repositorio de acceso a los eventos.
 * Encapsula las consultas que necesita la capa de presentación para recuperar
 * los eventos creados por el usuario y aquellos en los que participa.
 */
class EventoRepositorio(private val eventoDao: EventoDao) {

    suspend fun eventosCreadosPorUsuario(usuarioId: Long): List<Evento> =
        eventoDao.listarCreadosPorUsuario(usuarioId)

    suspend fun eventosEnLosQueParticipa(usuarioId: Long): List<Evento> =
        eventoDao.listarEventosParticipadosPorUsuario(usuarioId)

    companion object {
        /** Crea el repositorio usando la base de datos local de la aplicación. */
        fun crear(context: Context): EventoRepositorio =
            EventoRepositorio(AppDatabase.obtener(context).eventoDao())
    }
}

package com.example.gestordeeventos.data.repositorio

import com.example.gestordeeventos.data.local.dao.UsuarioDao
import com.example.gestordeeventos.data.model.Usuario

/** Un usuario encontrado al buscar: solo lo que puede verse de otra persona (no se comparte su correo). */
data class UsuarioEncontrado(
    val id: Long,
    val nombreUsuario: String
)

/**
 * Lógica de búsqueda de usuarios por ID o nombre de usuario (Issue 31), sobre la base de datos local.
 *
 * - El texto puede llevar una "@" delante y no distingue mayúsculas.
 * - Si es un número, se busca el usuario con ese ID y también los nombres que lo contienen (p. ej. "ana_23").
 * - Si no, se buscan los nombres que contienen el texto.
 * - Nunca devuelve al propio usuario que busca.
 * - Orden: primero el ID exacto, luego el nombre exacto y después el resto por orden alfabético.
 *
 * Solo busca: enviar la solicitud de amistad es cosa de `AmistadRepositorio`.
 */
class BusquedaUsuariosRepositorio(private val usuarioDao: UsuarioDao) {

    /** Devuelve como máximo [LIMITE_RESULTADOS] usuarios; la lista está vacía si el texto está vacío. */
    suspend fun buscar(consulta: String, miCorreo: String): List<UsuarioEncontrado> {
        val texto = consulta.trim().removePrefix("@").trim()
        if (texto.isEmpty()) return emptyList()

        val porId = texto.toLongOrNull()
            ?.let { usuarioDao.buscarPorId(it) }
            ?.takeUnless { it.correo.equals(miCorreo, ignoreCase = true) }
        val porNombre = usuarioDao.buscarPorNombreUsuarioParcial(texto, miCorreo, LIMITE_RESULTADOS)

        return (listOfNotNull(porId) + porNombre)
            .distinctBy { it.id }
            .sortedBy { prioridad(it, texto, porId) }
            .take(LIMITE_RESULTADOS)
            .map { UsuarioEncontrado(id = it.id, nombreUsuario = it.nombreUsuario) }
    }

    private fun prioridad(usuario: Usuario, texto: String, porId: Usuario?): Int = when {
        usuario.id == porId?.id -> 0
        usuario.nombreUsuario.equals(texto, ignoreCase = true) -> 1
        else -> 2
    }

    companion object {
        /** Máximo de usuarios que devuelve una búsqueda. */
        const val LIMITE_RESULTADOS = 20
    }
}

package com.example.gestordeeventos.data.repositorio

import com.example.gestordeeventos.data.local.dao.UsuarioDao
import com.example.gestordeeventos.data.model.UsuarioEncontrado

/**
 * Operaciones del módulo de amigos sobre la base de datos local.
 * Hoy solo la búsqueda de usuarios (Issue 31); aquí irán también añadir y listar amigos.
 */
class AmigosRepositorio(private val usuarioDao: UsuarioDao) {

    /**
     * Busca usuarios por su ID o por parte de su nombre de usuario, sin distinguir mayúsculas.
     *
     * - [texto] se limpia antes de buscar: sin espacios sobrantes y sin la `@` inicial (la app muestra
     *   los nombres como `@nombre`). Si queda vacío, no se consulta y se devuelve una lista vacía.
     * - Si el texto es solo un número, ese ID sale el primero (y también se buscan nombres que lo contengan).
     * - Nunca devuelve al propio [usuarioActualId]. Cada resultado indica si ya es amigo.
     * - Devuelve como mucho [limite] usuarios.
     */
    suspend fun buscarUsuarios(
        texto: String,
        usuarioActualId: Long,
        limite: Int = LIMITE_RESULTADOS
    ): List<UsuarioEncontrado> {
        require(limite > 0) { "limite debe ser mayor que 0" }
        val consulta = texto.trim().removePrefix("@").trim()
        if (consulta.isEmpty()) return emptyList()

        // toLongOrNull da null si el número es demasiado grande: entonces solo se busca por nombre.
        val id = if (consulta.all { it in '0'..'9' }) consulta.toLongOrNull() else null
        return usuarioDao.buscarUsuarios(usuarioActualId, id, escaparParaLike(consulta), limite)
    }

    companion object {
        /** Cuántos usuarios se devuelven como máximo en una búsqueda. */
        const val LIMITE_RESULTADOS = 20
    }
}

/**
 * Prepara un texto para usarlo dentro de un LIKE de SQL: `%` y `_` son comodines allí, y el guion bajo es
 * un carácter válido en los nombres de usuario ("marta_padel"). Se escapan con `\` para buscarlos tal cual.
 */
internal fun escaparParaLike(texto: String): String =
    texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")

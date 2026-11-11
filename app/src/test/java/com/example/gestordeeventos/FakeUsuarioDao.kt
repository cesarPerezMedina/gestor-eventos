package com.example.gestordeeventos

import com.example.gestordeeventos.data.local.dao.UsuarioDao
import com.example.gestordeeventos.data.model.Usuario
import com.example.gestordeeventos.data.model.UsuarioEncontrado

/** DAO falso en memoria para probar repositorios y ViewModels sin base de datos ni emulador. */
class FakeUsuarioDao : UsuarioDao {
    private val usuarios = mutableListOf<Usuario>()

    /** Si no es null, las búsquedas lanzan este error (para probar fallos inesperados). */
    var fallo: Throwable? = null

    /** Usuarios guardados, para comprobar qué se ha insertado. */
    val guardados: List<Usuario> get() = usuarios

    override suspend fun insertar(usuario: Usuario): Long {
        val id = usuarios.size + 1L
        usuarios += usuario.copy(id = id)
        return id
    }

    override suspend fun buscarPorId(id: Long): Usuario? = usuarios.firstOrNull { it.id == id }

    override suspend fun buscarPorCorreoONombreUsuario(identificador: String): Usuario? {
        fallo?.let { throw it }
        return usuarios.firstOrNull {
            it.correo.equals(identificador, ignoreCase = true) ||
                it.nombreUsuario.equals(identificador, ignoreCase = true)
        }
    }

    override suspend fun existeCorreo(correo: String): Boolean {
        fallo?.let { throw it }
        return usuarios.any { it.correo.equals(correo, ignoreCase = true) }
    }

    override suspend fun existeNombreUsuario(nombreUsuario: String): Boolean {
        fallo?.let { throw it }
        return usuarios.any { it.nombreUsuario.equals(nombreUsuario, ignoreCase = true) }
    }

    /** Lo que devolverá [buscarUsuarios]. La consulta SQL real se prueba en `BusquedaUsuariosDaoTest`. */
    var resultadosBusqueda: List<UsuarioEncontrado> = emptyList()

    /** Parámetros de la última llamada a [buscarUsuarios], o null si no se ha llamado. */
    var ultimaBusqueda: BusquedaRealizada? = null

    data class BusquedaRealizada(val usuarioActualId: Long, val id: Long?, val patron: String, val limite: Int)

    override suspend fun buscarUsuarios(
        usuarioActualId: Long,
        id: Long?,
        patron: String,
        limite: Int
    ): List<UsuarioEncontrado> {
        fallo?.let { throw it }
        ultimaBusqueda = BusquedaRealizada(usuarioActualId, id, patron, limite)
        return resultadosBusqueda
    }
}

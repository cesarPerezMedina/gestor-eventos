package com.example.gestordeeventos

import com.example.gestordeeventos.data.local.dao.UsuarioDao
import com.example.gestordeeventos.data.model.Usuario

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

    override suspend fun buscarPorCorreo(correo: String): Usuario? =
        usuarios.firstOrNull { it.correo.equals(correo, ignoreCase = true) }

    override suspend fun cambiarContrasena(correo: String, nueva: String) {
        val index = usuarios.indexOfFirst { it.correo.equals(correo, ignoreCase = true) }
        if (index != -1) {
            usuarios[index] = usuarios[index].copy(claveHash = nueva)
        }
    }

    override suspend fun buscarPorCorreoONombreUsuario(identificador: String): Usuario? {
        fallo?.let { throw it }
        return usuarios.firstOrNull {
            it.correo.equals(identificador, ignoreCase = true) ||
                it.nombreUsuario.equals(identificador, ignoreCase = true)
        }
    }

    override suspend fun buscarPorUsuarioOCorreo(identificador: String): Usuario? {
        fallo?.let { throw it }
        return usuarios.firstOrNull {
            it.correo.equals(identificador, ignoreCase = true) ||
                it.nombreUsuario.equals(identificador, ignoreCase = true) ||
                it.id.toString() == identificador
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
}

package com.example.gestordeeventos.data.repositorio

import com.example.gestordeeventos.data.local.dao.UsuarioDao
import java.security.MessageDigest

/**
 * Acceso a datos de la recuperación de contraseña (Issue 14).
 *
 * Como la app guarda todo en local (Room) y no hay servidor de correo, "recuperar" consiste en
 * comprobar que existe una cuenta con ese correo y permitir fijar una contraseña nueva.
 *
 * TODO: cuando el registro (Issue 13) defina cómo se calcula claveHash, pasar aquí esa misma
 *  función en `calcularHash`, para que el login acepte las contraseñas restablecidas.
 */
class RecuperarRepositorio(
    private val usuarioDao: UsuarioDao,
    private val calcularHash: (String) -> String = ::sha256Provisional
) {
    /** Devuelve el id del usuario con ese correo, o null si no existe ninguna cuenta. */
    suspend fun buscarIdPorCorreo(correo: String): Long? =
        usuarioDao.buscarPorCorreo(correo.trim())?.id

    suspend fun cambiarClave(usuarioId: Long, claveNueva: String) {
        usuarioDao.actualizarClave(usuarioId, calcularHash(claveNueva))
    }
}

/** Hash provisional (SHA-256 en hexadecimal). Sustituir por el de la Issue 13. */
internal fun sha256Provisional(clave: String): String =
    MessageDigest.getInstance("SHA-256")
        .digest(clave.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

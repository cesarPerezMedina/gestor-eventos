package com.example.gestordeeventos.data.repositorio

import android.content.Context
import androidx.core.content.edit
import com.example.gestordeeventos.data.local.AppDatabase
import com.example.gestordeeventos.data.local.dao.UsuarioDao
import com.example.gestordeeventos.data.model.Usuario

/**
 * Dónde se guarda el ID del usuario con la sesión iniciada.
 * Es una interfaz para poder sustituirla por una versión en memoria en los tests.
 */
interface AlmacenSesion {
    fun leerUsuarioId(): Long?
    fun guardarUsuarioId(id: Long)
    fun borrar()
}

/** Guarda el ID en un archivo de preferencias del propio móvil ("sesion"). No necesita librerías nuevas. */
class AlmacenSesionPreferencias(context: Context) : AlmacenSesion {
    private val preferencias = context.applicationContext.getSharedPreferences(NOMBRE_ARCHIVO, Context.MODE_PRIVATE)

    override fun leerUsuarioId(): Long? =
        if (preferencias.contains(CLAVE_USUARIO_ID)) preferencias.getLong(CLAVE_USUARIO_ID, SIN_USUARIO) else null

    override fun guardarUsuarioId(id: Long) {
        preferencias.edit { putLong(CLAVE_USUARIO_ID, id) }
    }

    override fun borrar() {
        preferencias.edit { remove(CLAVE_USUARIO_ID) }
    }

    private companion object {
        const val NOMBRE_ARCHIVO = "sesion"
        const val CLAVE_USUARIO_ID = "usuario_id"
        const val SIN_USUARIO = -1L
    }
}

/**
 * Recuerda quién tiene la sesión iniciada para que siga abierta al cerrar y volver a abrir la app (Issue 16).
 * Solo se guarda el ID del usuario, nunca la contraseña.
 */
class SesionRepositorio(
    private val almacen: AlmacenSesion,
    private val usuarioDao: UsuarioDao
) {

    /** Recuerda a este usuario como el de la sesión actual. Llamar justo después de un login o registro correcto. */
    fun iniciar(usuarioId: Long) {
        almacen.guardarUsuarioId(usuarioId)
    }

    /** true si hay un ID guardado. No comprueba que el usuario siga existiendo: para eso, [usuarioActual]. */
    fun haySesionGuardada(): Boolean = almacen.leerUsuarioId() != null

    /**
     * Devuelve el usuario de la sesión guardada, o null si no hay sesión.
     * Si el ID guardado ya no corresponde a ningún usuario (por ejemplo, se recreó la base de datos),
     * la sesión se borra y también devuelve null.
     */
    suspend fun usuarioActual(): Usuario? {
        val id = almacen.leerUsuarioId() ?: return null
        val usuario = usuarioDao.buscarPorId(id)
        if (usuario == null) almacen.borrar()
        return usuario
    }

    /** Olvida la sesión (cerrar sesión, Issue 17). */
    fun cerrar() {
        almacen.borrar()
    }

    companion object {
        /** Crea el repositorio con la base de datos y las preferencias reales de la app. */
        fun crear(context: Context): SesionRepositorio = SesionRepositorio(
            almacen = AlmacenSesionPreferencias(context),
            usuarioDao = AppDatabase.obtener(context).usuarioDao()
        )
    }
}

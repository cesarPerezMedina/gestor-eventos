package com.example.gestordeeventos.util


import android.content.Context

/**
 * Guarda en el móvil quién ha iniciado sesión (solo su correo).
 * - El login llamará a guardarSesion(correo) cuando el usuario entre.
 * - Cerrar sesión llama a cerrarSesion() y olvida al usuario.
 */
class SesionManager(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("sesion", Context.MODE_PRIVATE)

    fun guardarSesion(correo: String) {
        prefs.edit().putString(CLAVE_CORREO, correo).apply()
    }

    fun correoActual(): String? = prefs.getString(CLAVE_CORREO, null)

    fun haySesion(): Boolean = correoActual() != null

    fun cerrarSesion() {
        prefs.edit().remove(CLAVE_CORREO).apply()
    }

    private companion object {
        const val CLAVE_CORREO = "correo"
    }
}

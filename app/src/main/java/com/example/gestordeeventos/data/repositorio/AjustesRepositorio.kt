package com.example.gestordeeventos.data.repositorio

import android.content.Context
import androidx.core.content.edit
import com.example.gestordeeventos.data.model.EscalaTexto
import com.example.gestordeeventos.data.model.ModoTema
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Dónde se guardan los ajustes del usuario (pares clave → texto).
 * Es una interfaz para poder sustituirla por una versión en memoria en los tests.
 */
interface AlmacenAjustes {
    fun leer(clave: String): String?
    fun guardar(clave: String, valor: String)
}

/** Guarda los ajustes en un archivo de preferencias del propio móvil ("ajustes"). No necesita librerías nuevas. */
class AlmacenAjustesPreferencias(context: Context) : AlmacenAjustes {
    private val preferencias = context.applicationContext.getSharedPreferences(NOMBRE_ARCHIVO, Context.MODE_PRIVATE)

    override fun leer(clave: String): String? = preferencias.getString(clave, null)

    override fun guardar(clave: String, valor: String) {
        preferencias.edit { putString(clave, valor) }
    }

    private companion object {
        const val NOMBRE_ARCHIVO = "ajustes"
    }
}

/**
 * Ajustes de la app que se recuerdan entre sesiones: tema claro/oscuro y tamaño del texto.
 * Los valores son un StateFlow: la pantalla de Ajustes y la actividad principal los observan y la app se
 * repinta al instante cuando cambian.
 */
class AjustesRepositorio(private val almacen: AlmacenAjustes) {

    private val _modoTema = MutableStateFlow(ModoTema.desdeNombre(almacen.leer(CLAVE_MODO_TEMA)))

    /** Aspecto elegido. Por defecto, el del sistema. */
    val modoTema: StateFlow<ModoTema> = _modoTema.asStateFlow()

    /** Guarda el aspecto elegido y avisa a quien lo esté observando. */
    fun cambiarModoTema(modo: ModoTema) {
        almacen.guardar(CLAVE_MODO_TEMA, modo.name)
        _modoTema.value = modo
    }

    private val _escalaTexto = MutableStateFlow(EscalaTexto.desdeNombre(almacen.leer(CLAVE_ESCALA_TEXTO)))

    /** Tamaño de texto elegido. Por defecto, normal. */
    val escalaTexto: StateFlow<EscalaTexto> = _escalaTexto.asStateFlow()

    /** Guarda el tamaño de texto elegido y avisa a quien lo esté observando. */
    fun cambiarEscalaTexto(escala: EscalaTexto) {
        almacen.guardar(CLAVE_ESCALA_TEXTO, escala.name)
        _escalaTexto.value = escala
    }

    companion object {
        private const val CLAVE_MODO_TEMA = "modo_tema"
        private const val CLAVE_ESCALA_TEXTO = "escala_texto"

        @Volatile
        private var instancia: AjustesRepositorio? = null

        /**
         * Única instancia de la app. Es compartida a propósito: MainActivity (que pinta el tema) y la
         * pantalla de Ajustes (que lo cambia) deben ver el mismo valor.
         */
        fun obtener(context: Context): AjustesRepositorio =
            instancia ?: synchronized(this) {
                instancia ?: AjustesRepositorio(AlmacenAjustesPreferencias(context)).also { instancia = it }
            }
    }
}

package com.example.gestordeeventos.data.repositorio

import android.content.Context
import androidx.core.content.edit
import com.example.gestordeeventos.data.model.Deporte
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
 * Ajustes de la app que se recuerdan entre sesiones: tema claro/oscuro, tamaño del texto, radio de búsqueda,
 * deportes favoritos y avisos.
 * Los valores son un StateFlow: la pantalla de Ajustes y la actividad principal los observan y la app se
 * repinta al instante cuando cambian.
 *
 * Todo se guarda como texto en [AlmacenAjustes] (los booleanos como "true"/"false" y los deportes por su
 * nombre separados por comas), así que el almacén no necesita cambiar.
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

    // ---------- Preferencias de juego ----------

    private val _radioBusquedaKm = MutableStateFlow(leerRadioBusquedaKm())

    /** Radio de búsqueda de partidos, en km. Por defecto, [RADIO_BUSQUEDA_POR_DEFECTO_KM]. */
    val radioBusquedaKm: StateFlow<Int> = _radioBusquedaKm.asStateFlow()

    /** Guarda el radio elegido. Si no es uno de [RADIOS_BUSQUEDA_KM], se ignora. */
    fun cambiarRadioBusquedaKm(km: Int) {
        if (km !in RADIOS_BUSQUEDA_KM) return
        almacen.guardar(CLAVE_RADIO_BUSQUEDA, km.toString())
        _radioBusquedaKm.value = km
    }

    private val _deportesFavoritos = MutableStateFlow(leerDeportesFavoritos())

    /** Deportes marcados como favoritos. Por defecto, ninguno. */
    val deportesFavoritos: StateFlow<Set<Deporte>> = _deportesFavoritos.asStateFlow()

    /** Marca el deporte como favorito si no lo era, o lo quita si ya lo era. */
    fun alternarDeporteFavorito(deporte: Deporte) {
        val actuales = _deportesFavoritos.value
        val nuevos = if (deporte in actuales) actuales - deporte else actuales + deporte
        almacen.guardar(CLAVE_DEPORTES_FAVORITOS, nuevos.joinToString(SEPARADOR) { it.name })
        _deportesFavoritos.value = nuevos
    }

    // ---------- Notificaciones y avisos (solo se guarda la preferencia) ----------

    private val _recordatorioPartidos = MutableStateFlow(leerBooleano(CLAVE_RECORDATORIO_PARTIDOS, true))

    /** Avisar poco antes de que empiece un partido. Por defecto, sí. */
    val recordatorioPartidos: StateFlow<Boolean> = _recordatorioPartidos.asStateFlow()

    fun cambiarRecordatorioPartidos(activo: Boolean) {
        almacen.guardar(CLAVE_RECORDATORIO_PARTIDOS, activo.toString())
        _recordatorioPartidos.value = activo
    }

    private val _avisosPlazasVacantes = MutableStateFlow(leerBooleano(CLAVE_AVISOS_PLAZAS, true))

    /** Avisar cuando a un partido cercano le falte un jugador. Por defecto, sí. */
    val avisosPlazasVacantes: StateFlow<Boolean> = _avisosPlazasVacantes.asStateFlow()

    fun cambiarAvisosPlazasVacantes(activo: Boolean) {
        almacen.guardar(CLAVE_AVISOS_PLAZAS, activo.toString())
        _avisosPlazasVacantes.value = activo
    }

    private val _invitacionesDirectas = MutableStateFlow(leerBooleano(CLAVE_INVITACIONES_DIRECTAS, false))

    /** Avisar al instante de las invitaciones de tus amigos. Por defecto, no. */
    val invitacionesDirectas: StateFlow<Boolean> = _invitacionesDirectas.asStateFlow()

    fun cambiarInvitacionesDirectas(activo: Boolean) {
        almacen.guardar(CLAVE_INVITACIONES_DIRECTAS, activo.toString())
        _invitacionesDirectas.value = activo
    }

    // ---------- Lectura del almacén ----------

    private fun leerBooleano(clave: String, porDefecto: Boolean): Boolean =
        almacen.leer(clave)?.toBooleanStrictOrNull() ?: porDefecto

    private fun leerRadioBusquedaKm(): Int =
        almacen.leer(CLAVE_RADIO_BUSQUEDA)?.toIntOrNull()?.takeIf { it in RADIOS_BUSQUEDA_KM }
            ?: RADIO_BUSQUEDA_POR_DEFECTO_KM

    private fun leerDeportesFavoritos(): Set<Deporte> {
        val nombres = almacen.leer(CLAVE_DEPORTES_FAVORITOS).orEmpty().split(SEPARADOR)
        return Deporte.entries.filter { it.name in nombres && it in DEPORTES_FAVORITOS_DISPONIBLES }.toSet()
    }

    companion object {
        private const val CLAVE_MODO_TEMA = "modo_tema"
        private const val CLAVE_ESCALA_TEXTO = "escala_texto"
        private const val CLAVE_RADIO_BUSQUEDA = "radio_busqueda_km"
        private const val CLAVE_DEPORTES_FAVORITOS = "deportes_favoritos"
        private const val CLAVE_RECORDATORIO_PARTIDOS = "aviso_recordatorio_partidos"
        private const val CLAVE_AVISOS_PLAZAS = "aviso_plazas_vacantes"
        private const val CLAVE_INVITACIONES_DIRECTAS = "aviso_invitaciones_directas"
        private const val SEPARADOR = ","

        /** Radios entre los que el usuario puede elegir en Ajustes. */
        val RADIOS_BUSQUEDA_KM = listOf(5, 10, 25, 50)

        const val RADIO_BUSQUEDA_POR_DEFECTO_KM = 10

        /** Deportes que se pueden marcar como favoritos ("Otros" no es un deporte concreto). */
        val DEPORTES_FAVORITOS_DISPONIBLES: List<Deporte> = Deporte.entries.filter { it != Deporte.OTROS }

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
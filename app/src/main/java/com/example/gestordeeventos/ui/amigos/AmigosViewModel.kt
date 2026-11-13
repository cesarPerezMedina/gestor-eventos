package com.example.gestordeeventos.ui.amigos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gestordeeventos.data.model.Amigo
import com.example.gestordeeventos.data.repositorio.AmistadRepositorio
import com.example.gestordeeventos.data.repositorio.ResultadoAmistad
import kotlinx.coroutines.launch

/**
 * @param miCorreo devuelve el correo del usuario que ha iniciado sesión (o null si no hay sesión).
 */
class AmigosViewModel(
    private val repositorio: AmistadRepositorio,
    private val miCorreo: suspend () -> String?,
) : ViewModel() {

    /** Botón "Añadir" y "+ Conectar": envía una solicitud pendiente. */
    fun enviarSolicitud(consulta: String, onResultado: (ok: Boolean, mensaje: String) -> Unit) {
        ejecutar(onResultado) { yo -> repositorio.enviarSolicitud(yo, consulta) }
    }

    /** Botón "Aceptar" de una solicitud recibida. */
    fun aceptarSolicitud(correoSolicitante: String, onResultado: (ok: Boolean, mensaje: String) -> Unit) {
        ejecutar(onResultado) { yo -> repositorio.aceptarSolicitud(yo, correoSolicitante) }
    }

    /** Botón "Rechazar" de una solicitud recibida. */
    fun rechazarSolicitud(correoSolicitante: String, onResultado: (ok: Boolean, mensaje: String) -> Unit) {
        ejecutar(onResultado) { yo -> repositorio.rechazarSolicitud(yo, correoSolicitante) }
    }

    // Comprueba que hay sesión y ejecuta la acción en segundo plano
    private fun ejecutar(
        onResultado: (ok: Boolean, mensaje: String) -> Unit,
        accion: suspend (miCorreo: String) -> ResultadoAmistad,
    ) {
        viewModelScope.launch {
            val yo = miCorreo()
            if (yo == null) {
                onResultado(false, "Inicia sesión para gestionar tus amigos")
                return@launch
            }
            val resultado = accion(yo)
            onResultado(resultado.ok, resultado.mensaje)
        }
    }
}
/**
 * Convierte un Amigo de Room (id + nombreUsuario) en lo que pinta la pantalla.
 * La base de datos todavía no guarda deportes ni estado de conexión de los usuarios,
 * así que esos dos campos llevan un valor fijo hasta que existan.
 */
private fun Amigo.aAmigoItem(): AmigoItem = AmigoItem(
    id = id.toString(),
    usuario = nombreUsuario,
    deportes = "Amigo",
    idTag = "#$id",
    conectado = false,
)

// Hace falta para poder darle el repositorio al ViewModel
class AmigosViewModelFactory(
    private val repositorio: AmistadRepositorio,
    private val miCorreo: suspend () -> String?,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AmigosViewModel(repositorio, miCorreo) as T
}

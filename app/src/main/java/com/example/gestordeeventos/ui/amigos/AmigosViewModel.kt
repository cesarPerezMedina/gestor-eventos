package com.example.gestordeeventos.ui.amigos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gestordeeventos.data.model.Amigo
import com.example.gestordeeventos.data.repositorio.AmistadRepositorio
import com.example.gestordeeventos.data.repositorio.ResultadoAmistad
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * @param miCorreo (suspend) devuelve el correo del usuario con sesión iniciada, o null si no hay sesión.
 */
class AmigosViewModel(
    private val repositorio: AmistadRepositorio,
    private val miCorreo: suspend () -> String?,
) : ViewModel() {

    /**
     * Amigos agregados del usuario con sesión iniciada.
     * Viene de un Flow de Room, así que se actualiza solo cuando se acepta una
     * solicitud, se elimina un amigo, etc. La pantalla solo tiene que recogerlo
     * con collectAsState().
     */
    val amigos: StateFlow<List<AmigoItem>> = flow {
        val yo = miCorreo()
        if (yo == null) {
            emit(emptyList())
        } else {
            emitAll(
                repositorio.observarAmigos(yo)
                    .map { lista -> lista.map { it.aAmigoItem() } }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

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

    /** "Eliminar" del diálogo de un amigo: lo quita de la lista. [idAmigo] es el id de AmigoItem. */
    fun eliminarAmigo(idAmigo: String, onResultado: (ok: Boolean, mensaje: String) -> Unit) {
        val id = idAmigo.toLongOrNull()
        if (id == null) {
            onResultado(false, "Amigo no válido")
            return
        }
        ejecutar(onResultado) { yo -> repositorio.eliminarAmigo(yo, id) }
    }

    /** "Bloquear" del diálogo de un amigo: lo quita de la lista y le impide enviar solicitudes. */
    fun bloquearAmigo(idAmigo: String, onResultado: (ok: Boolean, mensaje: String) -> Unit) {
        val id = idAmigo.toLongOrNull()
        if (id == null) {
            onResultado(false, "Amigo no válido")
            return
        }
        ejecutar(onResultado) { yo -> repositorio.bloquearAmigo(yo, id) }
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
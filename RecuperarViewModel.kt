package com.example.gestordeeventos.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestordeeventos.data.local.AppDatabase
import com.example.gestordeeventos.data.repositorio.RecuperarRepositorio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Pasos del flujo: 1) pedir el correo, 2) elegir la nueva contraseña, 3) confirmación. */
enum class PasoRecuperar { CORREO, NUEVA_CLAVE, HECHO }

data class RecuperarUiState(
    val paso: PasoRecuperar = PasoRecuperar.CORREO,
    val correo: String = "",
    val nuevaClave: String = "",
    val confirmarClave: String = "",
    val errorCorreo: String? = null,
    val errorClave: String? = null,
    val errorConfirmar: String? = null,
    val cargando: Boolean = false
)

class RecuperarViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = RecuperarRepositorio(
        AppDatabase.obtener(application).usuarioDao()
    )

    private val _estado = MutableStateFlow(RecuperarUiState())
    val estado: StateFlow<RecuperarUiState> = _estado.asStateFlow()

    private var usuarioId: Long? = null

    fun onCorreoChange(valor: String) =
        _estado.update { it.copy(correo = valor, errorCorreo = null) }

    fun onNuevaClaveChange(valor: String) =
        _estado.update { it.copy(nuevaClave = valor, errorClave = null) }

    fun onConfirmarClaveChange(valor: String) =
        _estado.update { it.copy(confirmarClave = valor, errorConfirmar = null) }

    /** Paso 1: valida el correo y comprueba que existe una cuenta con él. */
    fun continuar() {
        val correo = _estado.value.correo
        val error = ValidacionRecuperacion.errorCorreo(correo)
        if (error != null) {
            _estado.update { it.copy(errorCorreo = error) }
            return
        }
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true) }
            val id = repositorio.buscarIdPorCorreo(correo)
            if (id == null) {
                _estado.update {
                    it.copy(cargando = false, errorCorreo = "No hay ninguna cuenta con ese correo")
                }
            } else {
                usuarioId = id
                _estado.update { it.copy(cargando = false, paso = PasoRecuperar.NUEVA_CLAVE) }
            }
        }
    }

    /** Paso 2: valida la contraseña nueva y la guarda. */
    fun cambiarClave() {
        val estado = _estado.value
        val errorClave = ValidacionRecuperacion.errorClave(estado.nuevaClave)
        val errorConfirmar = ValidacionRecuperacion.errorConfirmacion(
            estado.nuevaClave, estado.confirmarClave
        )
        if (errorClave != null || errorConfirmar != null) {
            _estado.update { it.copy(errorClave = errorClave, errorConfirmar = errorConfirmar) }
            return
        }
        val id = usuarioId ?: return volverACorreo()
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true) }
            repositorio.cambiarClave(id, estado.nuevaClave)
            _estado.update {
                it.copy(
                    cargando = false, paso = PasoRecuperar.HECHO,
                    nuevaClave = "", confirmarClave = ""
                )
            }
        }
    }

    fun volverACorreo() = _estado.update {
        it.copy(
            paso = PasoRecuperar.CORREO,
            nuevaClave = "", confirmarClave = "",
            errorClave = null, errorConfirmar = null
        )
    }
}

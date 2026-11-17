package com.example.gestordeeventos.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gestordeeventos.data.local.dao.UsuarioDao
import com.example.gestordeeventos.util.ClaveHash
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel(
    private val dao: UsuarioDao,
) : ViewModel() {

    private var correoActual: String? = null
    private var codigoActual: String? = null
    private var caducaEn: Long = 0L

    fun solicitarCodigo(correo: String, onResultado: (ok: Boolean, mensaje: String) -> Unit) {
        viewModelScope.launch {
            if (dao.buscarPorCorreo(correo) == null) {
                onResultado(false, "No existe ninguna cuenta con ese correo")
                return@launch
            }
            correoActual = correo
            codigoActual = (100000..999999).random().toString()
            caducaEn = System.currentTimeMillis() + 15 * 60 * 1000   // 15 minutos
            Log.d("Recuperar", "Código para $correo: $codigoActual")  // simula el envío del email
            onResultado(true, "Código enviado")
        }
    }

    fun restablecer(
        codigo: String,
        nueva: String,
        repetir: String,
        onResultado: (ok: Boolean, mensaje: String) -> Unit,
    ) {
        viewModelScope.launch {
            val correo = correoActual
            when {
                correo == null -> onResultado(false, "Primero pide un código")
                codigo != codigoActual -> onResultado(false, "Código incorrecto")
                System.currentTimeMillis() > caducaEn -> onResultado(false, "El código ha caducado")
                nueva != repetir -> onResultado(false, "Las contraseñas no coinciden")
                nueva.length < 8 || nueva.none { it.isDigit() || !it.isLetterOrDigit() } ->
                    onResultado(false, "Mínimo 8 caracteres y un número o símbolo")
                else -> {
                    // Mismo formato que el registro y el login (PBKDF2); se calcula fuera del hilo principal
                    val claveHash = withContext(Dispatchers.Default) { ClaveHash.generar(nueva) }
                    dao.cambiarContrasena(correo, claveHash)
                    codigoActual = null
                    correoActual = null
                    onResultado(true, "Contraseña cambiada")
                }
            }
        }
    }
}

class AuthViewModelFactory(private val dao: UsuarioDao) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AuthViewModel(dao) as T
}

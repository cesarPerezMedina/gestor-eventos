package com.example.gestordeeventos.navegacion

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gestordeeventos.data.local.AppDatabase
import com.example.gestordeeventos.ui.ajustes.AjustesScreen
import com.example.gestordeeventos.ui.auth.AuthViewModel
import com.example.gestordeeventos.ui.auth.AuthViewModelFactory
import com.example.gestordeeventos.ui.auth.LoginScreen
import com.example.gestordeeventos.ui.auth.RecuperarScreen
import com.example.gestordeeventos.ui.auth.RegistroScreen
import com.example.gestordeeventos.ui.auth.RestablecerScreen
import com.example.gestordeeventos.util.SesionManager
import com.example.gestordeeventos.ui.inicio.InicioScreen


@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Crea el ViewModel (ajusta estas dos líneas si tu AppDatabase se llama distinto)
    val dao = remember { AppDatabase.obtener(context).usuarioDao() }
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(dao))

    // Recuerda el correo escrito en "Recuperar" para poder reenviar el código
    var correoRecuperar by remember { mutableStateOf("") }

    val sesion = remember { SesionManager(context) }

    fun aviso(mensaje: String) {
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
    }

    NavHost(
        navController = navController,
        startDestination = Ruta.Login.ruta,
    ) {
        composable(Ruta.Login.ruta) {
            LoginScreen(
                onLoginClick = { email, password -> /* pendiente: lógica de login */ },
                onForgotPasswordClick = { navController.navigate(Ruta.Recuperar.ruta) },
                onRegisterClick = { navController.navigate(Ruta.Registro.ruta) },
            )
        }

        composable(Ruta.Registro.ruta) {
            RegistroScreen(
                onRegisterClick = { usuario, correo, contrasena, repetir -> /* pendiente: lógica de registro */ },
                onLoginClick = { navController.popBackStack() },
            )
        }

        composable(Ruta.Recuperar.ruta) {
            RecuperarScreen(
                onSendClick = { correo ->
                    correoRecuperar = correo
                    authViewModel.solicitarCodigo(correo) { ok, mensaje ->
                        aviso(mensaje)
                        if (ok) navController.navigate(Ruta.Restablecer.ruta)
                    }
                },
                onBackClick = { navController.popBackStack() },
            )
        }

        composable(Ruta.Restablecer.ruta) {
            RestablecerScreen(
                onSaveClick = { codigo, nueva, repetir ->
                    authViewModel.restablecer(codigo, nueva, repetir) { ok, mensaje ->
                        aviso(mensaje)
                        if (ok) {
                            navController.navigate(Ruta.Login.ruta) {
                                popUpTo(Ruta.Login.ruta) { inclusive = true }
                            }
                        }
                    }
                },
                onBackClick = { navController.popBackStack() },
                onResendClick = {
                    authViewModel.solicitarCodigo(correoRecuperar) { _, mensaje -> aviso(mensaje) }
                },
            )
        }
        composable(Ruta.Ajustes.ruta) {
            AjustesScreen(
                onBackClick = { navController.popBackStack() },
                onCerrarSesion = {
                    sesion.cerrarSesion() // olvida al usuario guardado
                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(0) { inclusive = true } // borra el historial: "atrás" ya no vuelve a la app
                    }
                },
            )
        }
        composable(Ruta.Inicio.ruta) {
            InicioScreen(
                nombreUsuario = "ana_23", // provisional, luego saldrá de la sesión
                onAjustesClick = { navController.navigate(Ruta.Ajustes.ruta) },
            )
        }


    }
}
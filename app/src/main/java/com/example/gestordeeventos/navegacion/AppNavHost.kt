package com.example.gestordeeventos.navegacion

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gestordeeventos.ui.auth.LoginScreen
import com.example.gestordeeventos.ui.auth.RegistroScreen
import com.example.gestordeeventos.ui.auth.RecuperarScreen


@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Ruta.Login.ruta,
    ) {
        composable(Ruta.Login.ruta) {
            LoginScreen(
                onLoginClick = { email, password -> /* aquí irá el AuthViewModel */ },
                onForgotPasswordClick = { navController.navigate(Ruta.Recuperar.ruta) },
                onRegisterClick = { navController.navigate(Ruta.Registro.ruta) },
            )
        }

        composable(Ruta.Registro.ruta) {
            RegistroScreen(
                onRegisterClick = { usuario, correo, contrasena, repetir -> /* AuthViewModel */ },
                onLoginClick = { navController.popBackStack() },
            )
        }
        composable(Ruta.Recuperar.ruta) {
            RecuperarScreen(
                onSendClick = { correo -> /* AuthViewModel */ },
                onBackClick = { navController.popBackStack() },
            )
        }
    }
}

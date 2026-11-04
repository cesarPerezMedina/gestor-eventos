package com.example.gestordeeventos.navegacion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gestordeeventos.ui.auth.RecuperarScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Ruta.LOGIN, modifier = modifier) {
        composable(Ruta.LOGIN) {
            // TODO: sustituir por LoginScreen cuando exista; recibirá onOlvidoClave como parámetro.
            LoginProvisional(onOlvidoClave = { navController.navigate(Ruta.RECUPERAR) })
        }
        composable(Ruta.RECUPERAR) {
            RecuperarScreen(
                onVolver = { navController.popBackStack() },
                onClaveCambiada = { navController.popBackStack() }
            )
        }
    }
}

/** Pantalla temporal para poder llegar a la recuperación hasta que exista el login real. */
@Composable
private fun LoginProvisional(onOlvidoClave: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Inicio de sesión (provisional)")
        TextButton(onClick = onOlvidoClave) { Text("He olvidado mi contraseña") }
    }
}

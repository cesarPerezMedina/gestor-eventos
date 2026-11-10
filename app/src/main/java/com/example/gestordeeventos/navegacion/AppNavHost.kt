package com.example.gestordeeventos.navegacion

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gestordeeventos.ui.ajustes.AjustesScreen
import com.example.gestordeeventos.ui.amigos.AmigosScreen
import com.example.gestordeeventos.ui.auth.LoginScreen
import com.example.gestordeeventos.ui.auth.RecuperarScreen
import com.example.gestordeeventos.ui.auth.RegistroScreen
import com.example.gestordeeventos.ui.buscar.BuscarEventoScreen
import com.example.gestordeeventos.ui.componentes.BarraInferior
import com.example.gestordeeventos.ui.componentes.pestanasInferiores
import com.example.gestordeeventos.ui.crear.CrearEventoScreen
import com.example.gestordeeventos.ui.evento.DetalleEventoScreen
import com.example.gestordeeventos.ui.evento.MisEventosScreen

/** Pantallas de autenticación: ya gestionan por sí mismas los márgenes de las barras del sistema. */
private val rutasAuth = listOf(Ruta.Login.ruta, Ruta.Registro.ruta, Ruta.Recuperar.ruta)

/**
 * Estructura de navegación principal de la app.
 *
 * La app empieza en el Login. Tras iniciar sesión se entra en las cinco pestañas, con la barra inferior.
 * La barra solo se ve en las pantallas que son pestañas; en cualquier otra (Login, Registro, Recuperar,
 * detalle de un evento) se oculta sola.
 *
 * Para añadir una pantalla: 1) crea su [Ruta], 2) regístrala con `composable(...)` aquí abajo,
 * 3) si debe ser pestaña, añádela a `pestanasInferiores` (ver BarraInferior.kt).
 */
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route
    val mostrarBarra = pestanasInferiores.any { it.ruta.ruta == rutaActual }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (mostrarBarra) {
                BarraInferior(
                    rutaActual = rutaActual,
                    onNavegar = { ruta -> navController.navegarAPestana(ruta) }
                )
            }
        }
    ) { relleno ->
        NavHost(
            navController = navController,
            startDestination = Ruta.Login.ruta,
            modifier = if (rutaActual in rutasAuth) Modifier else Modifier.padding(relleno)
        ) {
            composable(Ruta.Login.ruta) {
                LoginScreen(
                    // Provisional: aquí irá la comprobación con el AuthViewModel antes de entrar.
                    onLoginClick = { _, _ -> navController.entrarEnLaApp() },
                    onForgotPasswordClick = { navController.navigate(Ruta.Recuperar.ruta) },
                    onRegisterClick = { navController.navigate(Ruta.Registro.ruta) }
                )
            }
            composable(Ruta.Registro.ruta) {
                RegistroScreen(
                    onRegisterClick = { _, _, _, _ -> /* AuthViewModel */ },
                    onLoginClick = { navController.popBackStack() }
                )
            }
            composable(Ruta.Recuperar.ruta) {
                RecuperarScreen(
                    onSendClick = { /* AuthViewModel */ },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Ruta.MisEventos.ruta) { MisEventosScreen() }
            composable(Ruta.Buscar.ruta) { BuscarEventoScreen() }
            composable(Ruta.Crear.ruta) { CrearEventoScreen() }
            composable(Ruta.Amigos.ruta) { AmigosScreen() }
            composable(Ruta.Ajustes.ruta) { AjustesScreen() }
            composable(
                route = Ruta.DetalleEvento.ruta,
                arguments = listOf(navArgument(ARG_EVENTO_ID) { type = NavType.LongType })
            ) { entrada ->
                DetalleEventoScreen(eventoId = entrada.arguments?.getLong(ARG_EVENTO_ID) ?: 0L)
            }
        }
    }
}

/** Entra en la app (Mis eventos) y quita de la pila el Login, para que "atrás" no vuelva a él. */
private fun NavHostController.entrarEnLaApp() {
    navigate(Ruta.MisEventos.ruta) {
        popUpTo(Ruta.Login.ruta) { inclusive = true }
        launchSingleTop = true
    }
}

/**
 * Cambia de pestaña sin acumular pantallas: vuelve siempre a la pantalla inicial, no abre la misma
 * pestaña dos veces y recupera el estado que tenía la pestaña a la que se vuelve.
 */
private fun NavHostController.navegarAPestana(ruta: Ruta) {
    navigate(ruta.ruta) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

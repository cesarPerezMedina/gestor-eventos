package com.example.gestordeeventos.navegacion

import android.widget.Toast
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.gestordeeventos.data.local.AppDatabase
import com.example.gestordeeventos.ui.ajustes.AjustesScreen
import com.example.gestordeeventos.ui.auth.AuthViewModel
import com.example.gestordeeventos.ui.auth.AuthViewModelFactory
import com.example.gestordeeventos.ui.auth.LoginScreen
import com.example.gestordeeventos.ui.auth.RecuperarScreen
import com.example.gestordeeventos.ui.auth.RegistroScreen
import com.example.gestordeeventos.ui.auth.RestablecerScreen
import com.example.gestordeeventos.ui.amigos.AmigosScreen
import com.example.gestordeeventos.ui.componentes.BarraInferior
import com.example.gestordeeventos.ui.buscar.BuscarEventoScreen
import com.example.gestordeeventos.ui.crear.CrearEventoScreen
import com.example.gestordeeventos.ui.evento.MisEventosScreen
import com.example.gestordeeventos.ui.inicio.InicioScreen
import com.example.gestordeeventos.util.SesionManager
import com.example.gestordeeventos.ui.amigos.AmigosViewModel
import com.example.gestordeeventos.ui.amigos.AmigosViewModelFactory
import com.example.gestordeeventos.data.repositorio.AmistadRepositorio;
import com.example.gestordeeventos.data.repositorio.SesionRepositorio



@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current


    val dao = remember { AppDatabase.obtener(context).usuarioDao() }
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(dao))
    val sesion = remember { SesionManager(context) }
    val sesionRepositorio = remember { SesionRepositorio.crear(context) }
    val amistadRepositorio = remember {
        val db = AppDatabase.obtener(context)
        AmistadRepositorio(db.usuarioDao(), db.amistadDao())
    }
    val amigosViewModel: AmigosViewModel = viewModel(
        factory = AmigosViewModelFactory(
            repositorio = amistadRepositorio,
            miCorreo = { sesionRepositorio.usuarioActual()?.correo },   // AJUSTAR: ver abajo
        )
    )

    // Recuerda el correo escrito en "Recuperar" para poder reenviar el código
    var correoRecuperar by remember { mutableStateOf("") }

    fun aviso(mensaje: String) {
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
    }

    // --- Menú principal de abajo ---
    // Solo se muestra en las cuatro secciones principales
    val rutaActual = navController.currentBackStackEntryAsState().value?.destination?.route
    val rutasConBarra = listOf(
        Ruta.Inicio.ruta,
        Ruta.Buscar.ruta,
        Ruta.Eventos.ruta,
        Ruta.Amigos.ruta,
    )


    fun irASeccion(ruta: String) {
        navController.navigate(ruta) {
            popUpTo(Ruta.Inicio.ruta) { saveState = true } // no apila secciones repetidas
            launchSingleTop = true                          // no abre dos veces la misma
            restoreState = true                             // recupera el estado de la sección
        }
    }

    fun irACrear() {
        navController.navigate(Ruta.Crear.ruta) { launchSingleTop = true }
    }

    Scaffold(
        // Cada pantalla ya gestiona la barra de estado; aquí solo reservamos hueco para el menú
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (rutaActual in rutasConBarra) {
                BarraInferior(
                    rutaActual = rutaActual,
                    onNavegar = { ruta -> irASeccion(ruta) },
                    onCrearClick = { irACrear() },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Ruta.Login.ruta,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
        ) {
            // ---------- Acceso ----------
            composable(Ruta.Login.ruta) {
                LoginScreen(
                    // Cuando hagas el login: sesion.guardarSesion(email) y navegar a Ruta.Inicio
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

            // ---------- Secciones principales (menú de abajo) ----------
            composable(Ruta.Inicio.ruta) {
                InicioScreen(
                    nombreUsuario = "ana_23", // provisional, luego saldrá de la sesión
                    onAjustesClick = { navController.navigate(Ruta.Ajustes.ruta) },
                    onCrearClick = { irACrear() },
                )
            }

            // Cuando cada sección tenga su diseño, se rellena su propio archivo
            composable(Ruta.Buscar.ruta) { BuscarEventoScreen() }
            composable(Ruta.Eventos.ruta) { MisEventosScreen() }
            composable(Ruta.Amigos.ruta) { AmigosScreen() }

            // ---------- Otras pantallas (sin menú de abajo) ----------
            composable(Ruta.Crear.ruta) {
                CrearEventoScreen(onBackClick = { navController.popBackStack() })
            }
            composable(Ruta.Amigos.ruta) {
                val amigos by amigosViewModel.amigos.collectAsState()
                AmigosScreen(
                    amigos = amigos,
                    onEliminarClick = { amigosViewModel.eliminarAmigo(it.id) { _, mensaje -> aviso(mensaje) } },
                    onBloquearClick = { amigosViewModel.bloquearAmigo(it.id) { _, mensaje -> aviso(mensaje) } },
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
        }
    }
}
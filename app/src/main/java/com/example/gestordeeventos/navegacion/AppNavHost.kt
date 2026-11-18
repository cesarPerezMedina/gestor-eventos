package com.example.gestordeeventos.navegacion

import android.widget.Toast
import com.example.gestordeeventos.ui.evento.EventosViewModel
import com.example.gestordeeventos.ui.evento.EventosViewModelFactory
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.gestordeeventos.data.local.AppDatabase
import com.example.gestordeeventos.data.repositorio.AmistadRepositorio
import com.example.gestordeeventos.data.repositorio.EventoRepositorio
import com.example.gestordeeventos.data.repositorio.InvitacionRepositorio
import com.example.gestordeeventos.data.repositorio.SesionRepositorio
import com.example.gestordeeventos.data.model.Usuario
import com.example.gestordeeventos.ui.ajustes.AjustesScreen
import com.example.gestordeeventos.ui.amigos.AmigosScreen
import com.example.gestordeeventos.ui.amigos.AmigosViewModel
import com.example.gestordeeventos.ui.amigos.AmigosViewModelFactory
import com.example.gestordeeventos.ui.auth.AuthViewModel
import com.example.gestordeeventos.ui.auth.AuthViewModelFactory
import com.example.gestordeeventos.ui.auth.LoginScreen
import com.example.gestordeeventos.ui.auth.LoginViewModel
import com.example.gestordeeventos.ui.auth.RecuperarScreen
import com.example.gestordeeventos.ui.auth.RegistroScreen
import com.example.gestordeeventos.ui.auth.RegistroViewModel
import com.example.gestordeeventos.ui.auth.RestablecerScreen
import com.example.gestordeeventos.ui.buscar.BuscarEventoScreen
import com.example.gestordeeventos.ui.componentes.BarraInferior
import com.example.gestordeeventos.ui.crear.CrearEventoScreen
import com.example.gestordeeventos.ui.crear.CrearEventoViewModel
import com.example.gestordeeventos.ui.crear.CrearEventoViewModelFactory
import com.example.gestordeeventos.ui.evento.MisEventosScreen
import com.example.gestordeeventos.ui.inicio.InicioScreen
import com.example.gestordeeventos.ui.invitaciones.InvitacionesRoute
import com.example.gestordeeventos.util.SesionManager


@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val db = remember { AppDatabase.obtener(context) }
    val dao = remember { db.usuarioDao() }
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(dao))
    val sesion = remember { SesionManager(context) }
    val sesionRepositorio = remember { SesionRepositorio.crear(context) }
    val amistadRepositorio = remember { AmistadRepositorio(db.usuarioDao(), db.amistadDao()) }
    val eventoRepositorio = remember { EventoRepositorio(db.eventoDao(), db.invitacionDao()) }
    val invitacionRepositorio = remember { InvitacionRepositorio.crear(context) }

    val amigosViewModel: AmigosViewModel = viewModel(
        factory = AmigosViewModelFactory(
            repositorio = amistadRepositorio,
            miCorreo = { sesionRepositorio.usuarioActual()?.correo },
        )
    )
    val crearEventoViewModel: CrearEventoViewModel = viewModel(
        factory = CrearEventoViewModelFactory(
            repositorio = eventoRepositorio,
            miId = { sesionRepositorio.usuarioActual()?.id },
        )
    )

    // Usuario con la sesión iniciada (Issues 16 y 20) y pantalla inicial: se deciden al arrancar
    // con la sesión guardada. Hasta entonces no se dibujan las pantallas (es una consulta rápida).
    var usuarioActual by remember { mutableStateOf<Usuario?>(null) }
    var inicio by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        usuarioActual = sesionRepositorio.usuarioActual()
        if (inicio == null) inicio = if (usuarioActual != null) Ruta.Inicio.ruta else Ruta.Login.ruta
    }

    // Recuerda el correo escrito en "Recuperar" para poder reenviar el código
    var correoRecuperar by remember { mutableStateOf("") }

    fun aviso(mensaje: String) {
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
    }

    // --- Menú principal de abajo ---
    val rutaActual = navController.currentBackStackEntryAsState().value?.destination?.route
    val rutasConBarra = listOf(
        Ruta.Inicio.ruta,
        Ruta.Buscar.ruta,
        Ruta.Eventos.ruta,
        Ruta.Amigos.ruta,
    )

    fun irASeccion(ruta: String) {
        navController.navigate(ruta) {
            popUpTo(Ruta.Inicio.ruta) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun irACrear() {
        navController.navigate(Ruta.Crear.ruta) { launchSingleTop = true }
    }

    Scaffold(
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
        val destinoInicial = inicio ?: return@Scaffold
        NavHost(
            navController = navController,
            startDestination = destinoInicial,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
        ) {
            // ---------- Acceso ----------
            composable(Ruta.Login.ruta) {
                val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
                val estadoLogin by loginViewModel.estado.collectAsStateWithLifecycle()

                // Login correcto: se recuerda la sesión y se entra a Inicio sin poder volver al Login con "atrás"
                LaunchedEffect(estadoLogin.usuarioId) {
                    estadoLogin.usuarioId?.let { id ->
                        sesionRepositorio.iniciar(id)
                        usuarioActual = sesionRepositorio.usuarioActual()
                        usuarioActual?.let { sesion.guardarSesion(it.correo) }
                        navController.navigate(Ruta.Inicio.ruta) { popUpTo(Ruta.Login.ruta) { inclusive = true } }
                    }
                }

                LoginScreen(
                    onLoginClick = { identificador, clave -> loginViewModel.iniciarSesion(identificador, clave) },
                    onForgotPasswordClick = { navController.navigate(Ruta.Recuperar.ruta) },
                    onRegisterClick = { navController.navigate(Ruta.Registro.ruta) },
                    estado = estadoLogin,
                )
            }

            composable(Ruta.Registro.ruta) {
                val registroViewModel: RegistroViewModel = viewModel(factory = RegistroViewModel.Factory)
                val estadoRegistro by registroViewModel.estado.collectAsStateWithLifecycle()

                // Registro correcto: se vuelve al Login para iniciar sesión con la cuenta nueva
                LaunchedEffect(estadoRegistro.registroCompletado) {
                    if (estadoRegistro.registroCompletado) navController.popBackStack()
                }

                RegistroScreen(
                    onRegisterClick = { usuario, correo, clave, repetir ->
                        registroViewModel.registrar(usuario, correo, clave, repetir)
                    },
                    onLoginClick = { navController.popBackStack() },
                    estado = estadoRegistro,
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
                    nombreUsuario = usuarioActual?.nombreUsuario ?: "",
                    onAjustesClick = { navController.navigate(Ruta.Ajustes.ruta) },
                    onNotificacionesClick = { navController.navigate(Ruta.Invitaciones.ruta) },
                    onCrearClick = { irACrear() },
                )
            }

            composable(Ruta.Buscar.ruta) { BuscarEventoScreen() }
            composable(Ruta.Eventos.ruta) {
                val eventosViewModel: EventosViewModel = viewModel(
                    factory = EventosViewModelFactory(
                        repositorio = eventoRepositorio,
                        miId = { sesionRepositorio.usuarioActual()?.id },
                    )
                )
                val estadoEventos by eventosViewModel.estado.collectAsStateWithLifecycle()
                MisEventosScreen(
                    estado = estadoEventos,
                    nombreUsuario = usuarioActual?.nombreUsuario ?: "",
                    onNotificacionesClick = { navController.navigate(Ruta.Invitaciones.ruta) },
                    onPerfilClick = { navController.navigate(Ruta.Ajustes.ruta) },
                    onVerClick = { aviso("Detalle de «${it.nombre}»: próximamente") },
                    onEliminarConfirmado = { evento ->
                        eventosViewModel.eliminar(evento) { _, mensaje -> aviso(mensaje) }
                    },
                )
            }

            // ---------- Otras pantallas (sin menú de abajo) ----------
            composable(Ruta.Crear.ruta) {
                CrearEventoScreen(
                    amigos = emptyList(),
                    onBackClick = { navController.popBackStack() },
                    onCrearEventoClick = { nuevo ->
                        crearEventoViewModel.crearEvento(nuevo) { ok, mensaje ->
                            aviso(mensaje)
                            if (ok) {
                                navController.popBackStack()
                            }
                        }
                    },
                )
            }
            composable(Ruta.Amigos.ruta) {
                val amigos by amigosViewModel.amigos.collectAsStateWithLifecycle()
                AmigosScreen(
                    amigos = amigos,
                    onEliminarClick = { amigosViewModel.eliminarAmigo(it.id) { _, mensaje -> aviso(mensaje) } },
                    onBloquearClick = { amigosViewModel.bloquearAmigo(it.id) { _, mensaje -> aviso(mensaje) } },
                )
            }

            composable(Ruta.Invitaciones.ruta) {
                // Hace falta el usuario con sesión: sus invitaciones son las que se enseñan
                usuarioActual?.id?.let { usuarioId ->
                    InvitacionesRoute(
                        usuarioId = usuarioId,
                        repositorio = invitacionRepositorio,
                        onBackClick = { navController.popBackStack() },
                    )
                }
            }

            composable(Ruta.Ajustes.ruta) {
                AjustesScreen(
                    onBackClick = { navController.popBackStack() },
                    onCerrarSesion = {
                        sesion.cerrarSesion() // olvida el correo guardado
                        // Olvida también el ID de la sesión (Issue 16); si no, al reabrir la app entraría sola
                        sesionRepositorio.cerrar()
                        usuarioActual = null
                        navController.navigate(Ruta.Login.ruta) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}

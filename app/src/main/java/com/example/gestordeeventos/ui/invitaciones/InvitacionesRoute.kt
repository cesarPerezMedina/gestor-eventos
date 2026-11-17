package com.example.gestordeeventos.ui.invitaciones

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestordeeventos.data.repositorio.InvitacionRepositorio

/**
 * Conecta [InvitacionesScreen] con [InvitacionesViewModel] (Issue 75): enseña las invitaciones reales
 * del usuario, responde al pulsar Aceptar o Rechazar y muestra el aviso del resultado.
 * La pantalla en sí sigue siendo solo interfaz.
 */
@Composable
fun InvitacionesRoute(
    usuarioId: Long,
    repositorio: InvitacionRepositorio,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
) {
    val contexto = LocalContext.current
    val viewModel: InvitacionesViewModel = viewModel(
        key = "invitaciones-$usuarioId",
        factory = InvitacionesViewModelFactory(repositorio, usuarioId) { id, nombre ->
            contexto.getString(id, nombre)
        }
    )
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Cada aviso se enseña una vez y después se borra del estado
    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
            viewModel.mensajeMostrado()
        }
    }

    // Hasta que llega la primera lista no se dibuja nada, para no enseñar "no tienes invitaciones" un instante
    if (!estado.cargando) {
        InvitacionesScreen(
            modifier = modifier,
            invitaciones = estado.invitaciones,
            snackbarHostState = snackbarHostState,
            onAceptarClick = viewModel::aceptar,
            onRechazarClick = viewModel::rechazar,
            onBackClick = onBackClick,
        )
    }
}

package com.example.gestordeeventos.ui.buscar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.gestordeeventos.data.localizacion.LocalizacionProvider
import com.example.gestordeeventos.data.localizacion.ResultadoUbicacion
import com.example.gestordeeventos.ui.componentes.rememberSolicitudPermiso
import com.example.gestordeeventos.util.EstadoPermiso
import com.example.gestordeeventos.util.PermisoApp
import kotlinx.coroutines.launch

/**
 * Conecta la pantalla Buscar con el ViewModel y con la ubicación del móvil (Issue 57).
 * Pide el permiso al entrar, obtiene la posición y se la pasa al ViewModel para buscar por cercanía.
 * La pantalla en sí ([BuscarEventoScreen]) sigue siendo solo interfaz.
 */
@Composable
fun BuscarEventoRoute(
    viewModel: BuscarViewModel,
    modifier: Modifier = Modifier,
    onCrearEventoClick: () -> Unit = {},
    onDetalleClick: (String) -> Unit = {},
    onMapaClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val localizacion = remember { LocalizacionProvider(context) }
    val estado by viewModel.estado.collectAsState()
    var aviso by remember { mutableStateOf<String?>(null) }

    fun cargarUbicacion() {
        scope.launch {
            when (val resultado = localizacion.obtenerUbicacionActual()) {
                is ResultadoUbicacion.Obtenida -> {
                    viewModel.actualizarUbicacion(resultado.ubicacion)
                    aviso = null
                }
                ResultadoUbicacion.SinPermiso -> aviso = "Activa el permiso de ubicación para ver los eventos más cercanos"
                ResultadoUbicacion.UbicacionDesactivada -> aviso = "Activa la ubicación del móvil para ver los eventos más cercanos"
                ResultadoUbicacion.NoDisponible -> aviso = "No se pudo obtener tu ubicación. Toca para reintentar"
                is ResultadoUbicacion.Fallo -> aviso = "Error al obtener tu ubicación. Toca para reintentar"
            }
        }
    }

    val permiso = rememberSolicitudPermiso(PermisoApp.UBICACION) { concedido ->
        if (concedido) cargarUbicacion() else aviso = "Activa el permiso de ubicación para ver los eventos más cercanos"
    }

    // Al entrar: si ya hay permiso se busca la posición; si no, se pide
    LaunchedEffect(Unit) {
        if (permiso.concedido) cargarUbicacion() else permiso.solicitar()
    }

    BuscarEventoScreen(
        modifier = modifier,
        eventos = estado.eventos,
        filtrosDeporte = estado.filtrosDeporte,
        onModoChange = viewModel::cambiarModo,
        onRadioChange = viewModel::cambiarRadio,
        avisoUbicacion = aviso,
        onAvisoUbicacionClick = {
            when {
                permiso.concedido -> cargarUbicacion()
                permiso.estado == EstadoPermiso.BLOQUEADO -> permiso.abrirAjustes()
                else -> permiso.solicitar()
            }
        },
        onMapaClick = onMapaClick,
        onDetalleClick = onDetalleClick,
        onCrearEventoClick = onCrearEventoClick,
    )
}

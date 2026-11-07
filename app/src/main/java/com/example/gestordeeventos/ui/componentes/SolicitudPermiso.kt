package com.example.gestordeeventos.ui.componentes

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.gestordeeventos.util.EstadoPermiso
import com.example.gestordeeventos.util.PermisoApp

/**
 * Estado de un permiso y las acciones para pedirlo, listo para usar desde cualquier pantalla.
 *
 * @property estado en qué punto está el permiso (ver [EstadoPermiso]).
 * @property solicitar muestra el diálogo del sistema para pedir el permiso.
 * @property abrirAjustes abre los ajustes de la app, para cuando el permiso está [EstadoPermiso.BLOQUEADO].
 */
class SolicitudPermiso(
    val estado: EstadoPermiso,
    val solicitar: () -> Unit,
    val abrirAjustes: () -> Unit
) {
    val concedido: Boolean get() = estado == EstadoPermiso.CONCEDIDO
}

/**
 * Recuerda el estado de [permiso] y lo mantiene al día: se recalcula tras la respuesta del usuario y al volver
 * a la app (por ejemplo, desde Ajustes).
 *
 * @param onResultado se llama cuando el usuario responde al diálogo, con `true` si el permiso quedó concedido.
 */
@Composable
fun rememberSolicitudPermiso(permiso: PermisoApp, onResultado: (Boolean) -> Unit = {}): SolicitudPermiso {
    val context = LocalContext.current
    var yaSolicitado by rememberSaveable(permiso) { mutableStateOf(false) }
    var estado by remember(permiso) { mutableStateOf(estadoActual(context, permiso, yaSolicitado)) }
    val resultado by rememberUpdatedState(onResultado)

    val lanzador = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        yaSolicitado = true
        estado = estadoActual(context, permiso, yaSolicitado = true)
        resultado(estado == EstadoPermiso.CONCEDIDO)
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        estado = estadoActual(context, permiso, yaSolicitado)
    }

    return remember(estado, permiso, lanzador) {
        SolicitudPermiso(
            estado = estado,
            solicitar = { lanzador.launch(permiso.permisos.toTypedArray()) },
            abrirAjustes = { context.abrirAjustesDeLaApp() }
        )
    }
}

private fun estadoActual(context: Context, permiso: PermisoApp, yaSolicitado: Boolean): EstadoPermiso {
    if (!permiso.requiereSolicitud(Build.VERSION.SDK_INT)) return EstadoPermiso.CONCEDIDO

    val concedidos = permiso.permisos
        .filter { context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
        .toSet()
    val actividad = context.buscarActividad()
    val debeExplicar = actividad != null && permiso.permisos.any { actividad.shouldShowRequestPermissionRationale(it) }

    return EstadoPermiso.calcular(
        concedido = permiso.estaConcedido(concedidos),
        debeExplicar = debeExplicar,
        yaSolicitado = yaSolicitado
    )
}

private fun Context.buscarActividad(): Activity? {
    var actual = this
    while (actual is ContextWrapper) {
        if (actual is Activity) return actual
        actual = actual.baseContext
    }
    return null
}

private fun Context.abrirAjustesDeLaApp() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

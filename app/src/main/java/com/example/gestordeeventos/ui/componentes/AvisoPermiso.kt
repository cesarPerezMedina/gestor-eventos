package com.example.gestordeeventos.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.gestordeeventos.R
import com.example.gestordeeventos.ui.theme.Espacio
import com.example.gestordeeventos.ui.theme.GestorDeEventosTheme
import com.example.gestordeeventos.ui.theme.Tamano
import com.example.gestordeeventos.util.EstadoPermiso
import com.example.gestordeeventos.util.PermisoApp

/**
 * Muestra [contenido] solo si [permiso] está concedido; si no, enseña un aviso que explica para qué se usa
 * y deja pedirlo (o abrir los Ajustes si el usuario lo bloqueó).
 *
 * Ejemplo: `ConPermiso(PermisoApp.UBICACION) { ...contenido que necesita la ubicación... }`
 */
@Composable
fun ConPermiso(
    permiso: PermisoApp,
    modifier: Modifier = Modifier,
    contenido: @Composable () -> Unit
) {
    val solicitud = rememberSolicitudPermiso(permiso)
    if (solicitud.concedido) {
        contenido()
    } else {
        AvisoPermiso(
            permiso = permiso,
            estado = solicitud.estado,
            onConceder = solicitud.solicitar,
            onAbrirAjustes = solicitud.abrirAjustes,
            modifier = modifier
        )
    }
}

/** Tarjeta que explica para qué se necesita un permiso y ofrece pedirlo. No guarda estado propio. */
@Composable
fun AvisoPermiso(
    permiso: PermisoApp,
    estado: EstadoPermiso,
    onConceder: () -> Unit,
    onAbrirAjustes: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bloqueado = estado == EstadoPermiso.BLOQUEADO
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = Tamano.elevacionTarjeta)
    ) {
        Column(
            modifier = Modifier.padding(Espacio.m),
            verticalArrangement = Arrangement.spacedBy(Espacio.s)
        ) {
            Text(text = stringResource(permiso.titulo()), style = MaterialTheme.typography.titleMedium)
            Text(text = stringResource(permiso.motivo()), style = MaterialTheme.typography.bodyMedium)
            if (bloqueado) {
                Text(
                    text = stringResource(R.string.permiso_bloqueado),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Button(
                onClick = if (bloqueado) onAbrirAjustes else onConceder,
                modifier = Modifier.heightIn(min = Tamano.alturaBoton)
            ) {
                Text(
                    text = stringResource(
                        if (bloqueado) R.string.permiso_boton_ajustes else R.string.permiso_boton_conceder
                    )
                )
            }
        }
    }
}

private fun PermisoApp.titulo(): Int = when (this) {
    PermisoApp.UBICACION -> R.string.permiso_ubicacion_titulo
    PermisoApp.NOTIFICACIONES -> R.string.permiso_notificaciones_titulo
}

private fun PermisoApp.motivo(): Int = when (this) {
    PermisoApp.UBICACION -> R.string.permiso_ubicacion_motivo
    PermisoApp.NOTIFICACIONES -> R.string.permiso_notificaciones_motivo
}

@Preview(showBackground = true)
@Composable
private fun AvisoPermisoPendientePreview() {
    GestorDeEventosTheme {
        AvisoPermiso(
            permiso = PermisoApp.UBICACION,
            estado = EstadoPermiso.PENDIENTE,
            onConceder = {},
            onAbrirAjustes = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AvisoPermisoBloqueadoPreview() {
    GestorDeEventosTheme {
        AvisoPermiso(
            permiso = PermisoApp.NOTIFICACIONES,
            estado = EstadoPermiso.BLOQUEADO,
            onConceder = {},
            onAbrirAjustes = {}
        )
    }
}

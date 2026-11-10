package com.example.gestordeeventos.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gestordeeventos.R
import com.example.gestordeeventos.ui.theme.Espacio
import com.example.gestordeeventos.ui.theme.GestorDeEventosTheme

private val TamanoAvatar = 44.dp

/**
 * Fila de un usuario: avatar con su inicial, nombre de usuario e ID.
 * Se usa en la lista de amigos y servirá también para los resultados de buscar usuarios.
 *
 * @param accion contenido opcional al final de la fila (por ejemplo, un botón de añadir o de opciones).
 */
@Composable
fun UsuarioItem(
    id: Long,
    nombreUsuario: String,
    modifier: Modifier = Modifier,
    accion: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = TamanoAvatar + Espacio.m)
            .padding(vertical = Espacio.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Espacio.m)
    ) {
        Box(
            modifier = Modifier
                .size(TamanoAvatar)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = nombreUsuario.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.usuario_nombre, nombreUsuario),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.usuario_id, id),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        accion?.invoke()
    }
}

@Preview(showBackground = true)
@Composable
private fun UsuarioItemPreview() {
    GestorDeEventosTheme {
        UsuarioItem(id = 7, nombreUsuario = "marta_padel", modifier = Modifier.padding(Espacio.m))
    }
}

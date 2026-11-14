package com.example.gestordeeventos.ui.componentes

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.gestordeeventos.R
import com.example.gestordeeventos.ui.theme.Espacio
import com.example.gestordeeventos.ui.theme.GestorDeEventosTheme

/**
 * Selector de la visibilidad de un evento: Público o Privado.
 *
 * No guarda estado: recibe si el evento es público (`Evento.publico`) y avisa con [onCambio] cuando el usuario
 * elige la otra opción. Debajo de las opciones explica qué significa la elegida.
 */
@Composable
fun SelectorVisibilidad(
    publico: Boolean,
    onCambio: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Espacio.s)) {
        Text(text = stringResource(R.string.selector_visibilidad_titulo), style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(Espacio.s)) {
            OpcionVisibilidad(
                seleccionada = publico,
                onClick = { onCambio(true) },
                etiqueta = R.string.visibilidad_publico,
                icono = Icons.Filled.Public,
                enabled = enabled
            )
            OpcionVisibilidad(
                seleccionada = !publico,
                onClick = { onCambio(false) },
                etiqueta = R.string.visibilidad_privado,
                icono = Icons.Filled.Lock,
                enabled = enabled
            )
        }
        Text(
            text = stringResource(
                if (publico) R.string.visibilidad_publico_descripcion else R.string.visibilidad_privado_descripcion
            ),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun OpcionVisibilidad(
    seleccionada: Boolean,
    onClick: () -> Unit,
    @StringRes etiqueta: Int,
    icono: ImageVector,
    enabled: Boolean
) {
    FilterChip(
        selected = seleccionada,
        onClick = onClick,
        enabled = enabled,
        label = { Text(text = stringResource(etiqueta)) },
        leadingIcon = {
            Icon(imageVector = icono, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize))
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun SelectorVisibilidadPreview() {
    GestorDeEventosTheme {
        var publico by remember { mutableStateOf(true) }
        SelectorVisibilidad(publico = publico, onCambio = { publico = it })
    }
}

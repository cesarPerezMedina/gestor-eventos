package com.example.gestordeeventos.ui.componentes

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gestordeeventos.R
import com.example.gestordeeventos.ui.theme.Espacio
import com.example.gestordeeventos.ui.theme.GestorDeEventosTheme
import com.example.gestordeeventos.ui.theme.Tamano

private val AlturaOpcion = 72.dp

/**
 * Selector de la visibilidad de un evento (Issue 42): **Público** (cualquiera puede encontrarlo y unirse) o
 * **Privado** (solo pueden unirse las personas invitadas). Cada opción explica en una línea qué significa.
 *
 * Es un componente sin estado: quien lo usa guarda el valor y lo cambia en [onCambiar].
 * Trabaja con el mismo booleano que `Evento.publico`, que vale `true` por defecto.
 *
 * @param publico `true` si el evento es público, `false` si es privado.
 * @param onCambiar se llama con `true` al elegir Público y con `false` al elegir Privado.
 */
@Composable
fun SelectorVisibilidad(
    publico: Boolean,
    onCambiar: (publico: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // selectableGroup: los lectores de pantalla anuncian las opciones como un grupo en el que se elige una.
    Column(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(Espacio.s)
    ) {
        OpcionVisibilidad(
            icono = Icons.Default.Public,
            titulo = R.string.visibilidad_publico,
            descripcion = R.string.visibilidad_publico_descripcion,
            seleccionada = publico,
            onClick = { onCambiar(true) }
        )
        OpcionVisibilidad(
            icono = Icons.Default.Lock,
            titulo = R.string.visibilidad_privado,
            descripcion = R.string.visibilidad_privado_descripcion,
            seleccionada = !publico,
            onClick = { onCambiar(false) }
        )
    }
}

@Composable
private fun OpcionVisibilidad(
    icono: ImageVector,
    @StringRes titulo: Int,
    @StringRes descripcion: Int,
    seleccionada: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colores = MaterialTheme.colorScheme
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .selectable(selected = seleccionada, onClick = onClick, role = Role.RadioButton),
        shape = MaterialTheme.shapes.medium,
        color = if (seleccionada) colores.primaryContainer else colores.surface,
        contentColor = if (seleccionada) colores.onPrimaryContainer else colores.onSurfaceVariant,
        border = BorderStroke(
            width = if (seleccionada) 2.dp else 1.dp,
            color = if (seleccionada) colores.primary else colores.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = AlturaOpcion)
                .padding(horizontal = Espacio.m, vertical = Espacio.s),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Espacio.m)
        ) {
            Icon(imageVector = icono, contentDescription = null, modifier = Modifier.size(Tamano.iconoL))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = stringResource(titulo), style = MaterialTheme.typography.titleMedium)
                Text(text = stringResource(descripcion), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// --- Vistas previas (solo Android Studio, no se muestran en la app) ---

@Preview(showBackground = true, name = "Público")
@Composable
private fun SelectorVisibilidadPublicoPreview() {
    GestorDeEventosTheme {
        SelectorVisibilidad(publico = true, onCambiar = {}, modifier = Modifier.padding(Espacio.m))
    }
}

@Preview(showBackground = true, name = "Privado")
@Composable
private fun SelectorVisibilidadPrivadoPreview() {
    GestorDeEventosTheme {
        SelectorVisibilidad(publico = false, onCambiar = {}, modifier = Modifier.padding(Espacio.m))
    }
}

@Preview(showBackground = true, name = "Interactivo (modo interactivo de la vista previa)")
@Composable
private fun SelectorVisibilidadInteractivoPreview() {
    GestorDeEventosTheme {
        var publico by remember { mutableStateOf(true) }
        SelectorVisibilidad(publico = publico, onCambiar = { publico = it }, modifier = Modifier.padding(Espacio.m))
    }
}

@Preview(showBackground = true, name = "Oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SelectorVisibilidadOscuroPreview() {
    GestorDeEventosTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SelectorVisibilidad(publico = false, onCambiar = {}, modifier = Modifier.padding(Espacio.m))
        }
    }
}

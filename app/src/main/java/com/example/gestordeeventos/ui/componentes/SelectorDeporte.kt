package com.example.gestordeeventos.ui.componentes

import android.content.res.Configuration
import androidx.annotation.DrawableRes
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gestordeeventos.R
import com.example.gestordeeventos.data.model.Deporte
import com.example.gestordeeventos.ui.theme.Espacio
import com.example.gestordeeventos.ui.theme.GestorDeEventosTheme
import com.example.gestordeeventos.ui.theme.Tamano

/** Cuántas opciones caben en cada fila: con los 6 deportes salen dos filas de tres. */
private const val COLUMNAS = 3

private val AlturaOpcion = 88.dp

/**
 * Selector del tipo de deporte de un evento (Issue 40): una opción por cada [Deporte]
 * (Fútbol, Baloncesto, Tenis, Pádel, Atletismo y Otros), con su icono y su nombre. Solo se puede elegir una.
 *
 * Es un componente sin estado: quien lo usa guarda el deporte elegido y lo cambia en [onSeleccionar].
 *
 * @param seleccionado deporte elegido, o null si todavía no se ha elegido ninguno.
 * @param onSeleccionar se llama con el deporte que el usuario pulsa.
 */
@Composable
fun SelectorDeporte(
    seleccionado: Deporte?,
    onSeleccionar: (Deporte) -> Unit,
    modifier: Modifier = Modifier
) {
    // selectableGroup: los lectores de pantalla anuncian las opciones como un grupo en el que se elige una.
    Column(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(Espacio.s)
    ) {
        Deporte.entries.chunked(COLUMNAS).forEach { fila ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Espacio.s)) {
                fila.forEach { deporte ->
                    OpcionDeporte(
                        deporte = deporte,
                        seleccionado = deporte == seleccionado,
                        onClick = { onSeleccionar(deporte) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/** Icono de cada deporte. Está aquí para que otras pantallas (como la tarjeta de evento) puedan reutilizarlo. */
@DrawableRes
internal fun iconoDeDeporte(deporte: Deporte): Int = when (deporte) {
    Deporte.FUTBOL -> R.drawable.ic_futbol
    Deporte.BALONCESTO -> R.drawable.ic_baloncesto
    Deporte.TENIS -> R.drawable.ic_tenis
    Deporte.PADEL -> R.drawable.ic_padel
    Deporte.ATLETISMO -> R.drawable.ic_atletismo
    Deporte.OTROS -> R.drawable.ic_otros
}

@Composable
private fun OpcionDeporte(
    deporte: Deporte,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colores = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.selectable(selected = seleccionado, onClick = onClick, role = Role.RadioButton),
        shape = MaterialTheme.shapes.medium,
        color = if (seleccionado) colores.primaryContainer else colores.surface,
        contentColor = if (seleccionado) colores.onPrimaryContainer else colores.onSurfaceVariant,
        border = BorderStroke(
            width = if (seleccionado) 2.dp else 1.dp,
            color = if (seleccionado) colores.primary else colores.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = AlturaOpcion)
                .padding(Espacio.s),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Espacio.xs, Alignment.CenterVertically)
        ) {
            Icon(
                painter = painterResource(iconoDeDeporte(deporte)),
                contentDescription = null,
                modifier = Modifier.size(Tamano.iconoL)
            )
            Text(
                text = stringResource(deporte.etiqueta),
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

// --- Vistas previas (solo Android Studio, no se muestran en la app) ---

@Preview(showBackground = true, name = "Sin elegir")
@Composable
private fun SelectorDeporteSinElegirPreview() {
    GestorDeEventosTheme {
        SelectorDeporte(seleccionado = null, onSeleccionar = {}, modifier = Modifier.padding(Espacio.m))
    }
}

@Preview(showBackground = true, name = "Pádel elegido")
@Composable
private fun SelectorDeporteElegidoPreview() {
    GestorDeEventosTheme {
        SelectorDeporte(seleccionado = Deporte.PADEL, onSeleccionar = {}, modifier = Modifier.padding(Espacio.m))
    }
}

@Preview(showBackground = true, name = "Interactivo (modo interactivo de la vista previa)")
@Composable
private fun SelectorDeporteInteractivoPreview() {
    GestorDeEventosTheme {
        var elegido by remember { mutableStateOf<Deporte?>(Deporte.FUTBOL) }
        SelectorDeporte(
            seleccionado = elegido,
            onSeleccionar = { elegido = it },
            modifier = Modifier.padding(Espacio.m)
        )
    }
}

@Preview(showBackground = true, name = "Oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SelectorDeporteOscuroPreview() {
    GestorDeEventosTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SelectorDeporte(seleccionado = Deporte.TENIS, onSeleccionar = {}, modifier = Modifier.padding(Espacio.m))
        }
    }
}

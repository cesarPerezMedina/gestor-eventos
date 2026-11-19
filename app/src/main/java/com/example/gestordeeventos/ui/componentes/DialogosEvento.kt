package com.example.gestordeeventos.ui.componentes

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.gestordeeventos.R
import com.example.gestordeeventos.ui.theme.PaletaApp
import com.example.gestordeeventos.ui.theme.SuperficieApp

// Mismos colores que el resto de la app: violeta para el texto principal, rojo para lo que no se puede deshacer
private val DialogoTinta get() = PaletaApp.elegir(Color(0xFF1B1B3A), Color(0xFFECEAFB))
private val DialogoSubtitulo get() = PaletaApp.elegir(Color(0xFF4A5068), Color(0xFFB9BDD3))
private val DialogoAccion get() = PaletaApp.elegir(Color(0xFF5B0FBF), Color(0xFF8A5CEB))
private val DialogoPeligro get() = PaletaApp.elegir(Color(0xFFBA1A1A), Color(0xFFF2B8B5))

/**
 * Cuadro de confirmación genérico: título, texto y dos botones. No guarda estado; quien lo usa decide cuándo
 * enseñarlo y qué hacer al confirmar. Con [peligroso] el botón de confirmar sale en rojo.
 */
@Composable
fun DialogoConfirmacion(
    titulo: String,
    texto: String,
    textoConfirmar: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier,
    peligroso: Boolean = false,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onCancelar,
        containerColor = SuperficieApp,
        title = { Text(titulo, color = DialogoTinta, fontWeight = FontWeight.Bold) },
        text = { Text(texto, color = DialogoSubtitulo) },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text(
                    text = textoConfirmar,
                    color = if (peligroso) DialogoPeligro else DialogoAccion,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text(stringResource(R.string.evento_cancelar), color = DialogoAccion)
            }
        },
    )
}

/** Confirmación antes de salirse de un evento (Issue 67). */
@Composable
fun ConfirmarSalirEventoDialog(
    nombreEvento: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DialogoConfirmacion(
        titulo = stringResource(R.string.evento_salir_titulo),
        texto = stringResource(R.string.evento_salir_texto, nombreEvento),
        textoConfirmar = stringResource(R.string.evento_salir_confirmar),
        onConfirmar = onConfirmar,
        onCancelar = onCancelar,
        modifier = modifier,
    )
}

/** Confirmación antes de eliminar un evento propio (Issue 68). Se borra para todos y no se puede deshacer. */
@Composable
fun ConfirmarEliminarEventoDialog(
    nombreEvento: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DialogoConfirmacion(
        titulo = stringResource(R.string.evento_eliminar_titulo),
        texto = stringResource(R.string.evento_eliminar_texto, nombreEvento),
        textoConfirmar = stringResource(R.string.evento_eliminar_confirmar),
        onConfirmar = onConfirmar,
        onCancelar = onCancelar,
        modifier = modifier,
        peligroso = true,
    )
}

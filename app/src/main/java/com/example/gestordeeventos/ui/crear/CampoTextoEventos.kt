package com.example.gestordeeventos.ui.crear

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestordeeventos.ui.theme.GestorDeEventosTheme

// --- Reglas de los campos de texto del evento (Issue 41) ---
const val NOMBRE_EVENTO_MIN = 3
const val NOMBRE_EVENTO_MAX = 60
const val DESCRIPCION_EVENTO_MAX = 300

internal val CreError = Color(0xFFD32F2F)

/**
 * Comprueba el nombre del evento. Devuelve el mensaje de error, o null si es válido.
 * Es una función normal (sin pantalla) para poder usarla también desde el ViewModel antes de guardar.
 */
fun errorNombreEvento(nombre: String): String? {
    val texto = nombre.trim()
    return when {
        texto.isEmpty() -> "Escribe un nombre para el evento"
        texto.length < NOMBRE_EVENTO_MIN -> "Mínimo $NOMBRE_EVENTO_MIN caracteres"
        else -> null
    }
}

/**
 * Campo "Nombre del evento": obligatorio, de $NOMBRE_EVENTO_MIN a $NOMBRE_EVENTO_MAX caracteres.
 * Con contador. El error aparece cuando el usuario sale del campo, o al pulsar "Crear" con [mostrarError].
 *
 * Es un componente sin estado: quien lo usa guarda el texto y lo cambia en [onCambio].
 */
@Composable
fun CampoNombreEvento(
    valor: String,
    onCambio: (String) -> Unit,
    modifier: Modifier = Modifier,
    mostrarError: Boolean = false,
) {
    var tocado by rememberSaveable { mutableStateOf(false) }
    var teniaFoco by remember { mutableStateOf(false) }
    val error = if (mostrarError || tocado) errorNombreEvento(valor) else null

    Column(modifier = modifier.fillMaxWidth()) {
        Etiqueta("Nombre del evento")
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = valor,
            onValueChange = { onCambio(it.take(NOMBRE_EVENTO_MAX)) },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { estado ->
                    // Se marca como "tocado" al salir del campo, no mientras se escribe
                    if (teniaFoco && !estado.isFocused) tocado = true
                    teniaFoco = estado.isFocused
                },
            singleLine = true,
            isError = error != null,
            shape = RoundedCornerShape(14.dp),
            placeholder = {
                Text(
                    "Ej: Fútbol 7 entre semana - Los Pinos",
                    color = CreGris,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            leadingIcon = { Icon(Icons.Outlined.EditNote, contentDescription = null, tint = CreSubtitulo) },
            supportingText = { PieCampo(error = error, longitud = valor.length, maximo = NOMBRE_EVENTO_MAX) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next,
            ),
            colors = coloresCampoEvento(),
        )
    }
}

/**
 * Campo "Descripción": opcional, hasta $DESCRIPCION_EVENTO_MAX caracteres, de 3 a 6 líneas, con contador.
 */
@Composable
fun CampoDescripcionEvento(
    valor: String,
    onCambio: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Etiqueta("Descripción", derecha = "Opcional", derechaGris = true)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = valor,
            onValueChange = { onCambio(it.take(DESCRIPCION_EVENTO_MAX)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 6,
            shape = RoundedCornerShape(14.dp),
            placeholder = { Text("Nivel, material, normas...", color = CreGris, fontSize = 13.sp) },
            supportingText = { PieCampo(error = null, longitud = valor.length, maximo = DESCRIPCION_EVENTO_MAX) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Default,
            ),
            colors = coloresCampoEvento(),
        )
    }
}

/** Línea de debajo de cada campo: el error a la izquierda y el contador "12/60" a la derecha. */
@Composable
private fun PieCampo(error: String?, longitud: Int, maximo: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = error.orEmpty(),
            color = CreError,
            fontSize = 11.sp,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = "$longitud/$maximo",
            color = if (longitud >= maximo) CreError else CreGris,
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun coloresCampoEvento() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    errorContainerColor = Color.White,
    focusedBorderColor = CreVioletaClaro,
    unfocusedBorderColor = CreLinea,
    errorBorderColor = CreError,
    cursorColor = CreVioleta,
)

// --- Vistas previas (solo Android Studio) ---

@Preview(showBackground = true, widthDp = 390, name = "Vacíos con error")
@Composable
private fun CamposTextoEventoVaciosPreview() {
    GestorDeEventosTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            CampoNombreEvento(valor = "", onCambio = {}, mostrarError = true)
            CampoDescripcionEvento(valor = "", onCambio = {})
        }
    }
}

@Preview(showBackground = true, widthDp = 390, name = "Interactivo")
@Composable
private fun CamposTextoEventoInteractivoPreview() {
    GestorDeEventosTheme {
        var nombre by remember { mutableStateOf("Fútbol 7 entre semana") }
        var descripcion by remember { mutableStateOf("Partido amistoso, nivel intermedio.") }
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            CampoNombreEvento(valor = nombre, onCambio = { nombre = it })
            CampoDescripcionEvento(valor = descripcion, onCambio = { descripcion = it })
        }
    }
}
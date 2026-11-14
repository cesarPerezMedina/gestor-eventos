package com.example.gestordeeventos.ui.componentes

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.example.gestordeeventos.R
import com.example.gestordeeventos.ui.theme.Espacio
import com.example.gestordeeventos.ui.theme.GestorDeEventosTheme
import com.example.gestordeeventos.util.ErrorParticipantes
import com.example.gestordeeventos.util.ValidadorParticipantes

/**
 * Los dos campos para definir el número de participantes de un evento: mínimo y máximo.
 *
 * No guarda estado: recibe los textos y avisa con [onMinimoChange] y [onMaximoChange]. Solo deja escribir cifras.
 * Con [mostrarErrores] a `true` marca los campos no válidos según [ValidadorParticipantes]; conviene activarlo
 * cuando el usuario ya ha tocado los campos o pulsa "Crear". Para obtener los valores ya validados:
 * `ValidadorParticipantes.rango(minimo, maximo)`.
 */
@Suppress("LongParameterList") // Es un componente sin estado: sus parámetros son valores y avisos de cambio.
@Composable
fun CamposParticipantes(
    minimo: String,
    maximo: String,
    onMinimoChange: (String) -> Unit,
    onMaximoChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    mostrarErrores: Boolean = false,
    enabled: Boolean = true
) {
    val errorMinimo = if (mostrarErrores) ValidadorParticipantes.errorMinimo(minimo) else null
    val errorMaximo = if (mostrarErrores) ValidadorParticipantes.errorMaximo(minimo, maximo) else null

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Espacio.s)) {
        Text(text = stringResource(R.string.participantes_titulo), style = MaterialTheme.typography.titleSmall)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Espacio.m)) {
            CampoNumero(
                valor = minimo,
                onCambio = onMinimoChange,
                etiqueta = R.string.participantes_minimo,
                error = errorMinimo,
                enabled = enabled,
                modifier = Modifier.weight(1f)
            )
            CampoNumero(
                valor = maximo,
                onCambio = onMaximoChange,
                etiqueta = R.string.participantes_maximo,
                error = errorMaximo,
                enabled = enabled,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun CampoNumero(
    valor: String,
    onCambio: (String) -> Unit,
    @StringRes etiqueta: Int,
    error: ErrorParticipantes?,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = valor,
        onValueChange = { onCambio(it.filter(Char::isDigit).take(ValidadorParticipantes.CIFRAS_MAXIMAS)) },
        modifier = modifier,
        enabled = enabled,
        label = { Text(text = stringResource(etiqueta)) },
        isError = error != null,
        supportingText = { if (error != null) Text(text = error.texto()) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true
    )
}

@Composable
private fun ErrorParticipantes.texto(): String = when (this) {
    ErrorParticipantes.MINIMO_INVALIDO, ErrorParticipantes.MAXIMO_INVALIDO -> stringResource(
        R.string.participantes_error_numero,
        ValidadorParticipantes.VALOR_MINIMO,
        ValidadorParticipantes.VALOR_MAXIMO
    )
    ErrorParticipantes.MAXIMO_MENOR_QUE_MINIMO -> stringResource(R.string.participantes_error_rango)
}

@Preview(showBackground = true)
@Composable
private fun CamposParticipantesPreview() {
    GestorDeEventosTheme {
        var minimo by remember { mutableStateOf("4") }
        var maximo by remember { mutableStateOf("10") }
        CamposParticipantes(minimo, maximo, onMinimoChange = { minimo = it }, onMaximoChange = { maximo = it })
    }
}

@Preview(showBackground = true)
@Composable
private fun CamposParticipantesConErrorPreview() {
    GestorDeEventosTheme {
        CamposParticipantes(
            minimo = "10",
            maximo = "4",
            onMinimoChange = {},
            onMaximoChange = {},
            mostrarErrores = true
        )
    }
}

package com.example.gestordeeventos.ui.componentes

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.example.gestordeeventos.R
import com.example.gestordeeventos.ui.theme.Espacio
import com.example.gestordeeventos.ui.theme.GestorDeEventosTheme
import com.example.gestordeeventos.util.ErrorParticipantes
import com.example.gestordeeventos.util.ValidadorParticipantes

/** Cuántos dígitos caben: los del número más grande permitido. */
private val DIGITOS_MAXIMOS = ValidadorParticipantes.MAXIMO_PERMITIDO.toString().length

/**
 * Lo que hay escrito en los campos de participantes.
 *
 * @property minimo texto del campo mínimo (vacío si no se ha escrito nada).
 * @property maximo texto del campo máximo.
 * @property mostrarErrores `true` para enseñar también los errores de "obligatorio" en campos vacíos
 *   (por ejemplo, tras pulsar "Crear" con el formulario a medias). Un número mal puesto se avisa siempre.
 */
data class ParticipantesUiState(
    val minimo: String = "",
    val maximo: String = "",
    val mostrarErrores: Boolean = false
)

/**
 * Campos para definir cuántas personas pueden apuntarse a un evento (Issue 44): **mínimo** y **máximo**,
 * uno al lado del otro. Solo admiten números (hasta 3 cifras) y avisan de lo que está mal: campo obligatorio,
 * número fuera de rango o máximo menor que el mínimo (reglas en [ValidadorParticipantes]).
 *
 * Es un componente sin estado: quien lo usa guarda el texto y lo cambia en [onMinimoChange] y [onMaximoChange].
 * Cuando `ValidadorParticipantes.sonValidos` da `true`, los textos se pueden pasar a `Int` sin riesgo.
 */
@Composable
fun CamposParticipantes(
    estado: ParticipantesUiState,
    onMinimoChange: (String) -> Unit,
    onMaximoChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val errorMinimo = ValidadorParticipantes.validarMinimo(estado.minimo)
        .takeIf { estado.mostrarErrores || estado.minimo.isNotEmpty() }
    val errorMaximo = ValidadorParticipantes.validarMaximo(estado.minimo, estado.maximo)
        .takeIf { estado.mostrarErrores || estado.maximo.isNotEmpty() }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Espacio.m),
        verticalAlignment = Alignment.Top
    ) {
        CampoNumero(
            valor = estado.minimo,
            onCambio = { onMinimoChange(it.soloDigitos()) },
            etiqueta = R.string.participantes_minimo,
            error = errorMinimo,
            imeAction = ImeAction.Next
        )
        CampoNumero(
            valor = estado.maximo,
            onCambio = { onMaximoChange(it.soloDigitos()) },
            etiqueta = R.string.participantes_maximo,
            error = errorMaximo,
            imeAction = ImeAction.Done
        )
    }
}

@Composable
private fun RowScope.CampoNumero(
    valor: String,
    onCambio: (String) -> Unit,
    @StringRes etiqueta: Int,
    error: ErrorParticipantes?,
    imeAction: ImeAction
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        modifier = Modifier.weight(1f),
        singleLine = true,
        label = { Text(text = stringResource(etiqueta)) },
        isError = error != null,
        supportingText = { if (error != null) Text(text = error.texto()) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeAction)
    )
}

@Composable
private fun ErrorParticipantes.texto(): String = when (this) {
    ErrorParticipantes.MINIMO_VACIO -> stringResource(R.string.participantes_error_minimo_vacio)
    ErrorParticipantes.MAXIMO_VACIO -> stringResource(R.string.participantes_error_maximo_vacio)
    ErrorParticipantes.FUERA_DE_RANGO -> stringResource(
        R.string.participantes_error_rango,
        ValidadorParticipantes.MINIMO_PERMITIDO,
        ValidadorParticipantes.MAXIMO_PERMITIDO
    )
    ErrorParticipantes.MAXIMO_MENOR_QUE_MINIMO -> stringResource(R.string.participantes_error_maximo_menor)
}

/** Deja solo las cifras (también al pegar texto) y recorta a las que caben. */
private fun String.soloDigitos(): String = filter { it in '0'..'9' }.take(DIGITOS_MAXIMOS)

// --- Vistas previas (solo Android Studio, no se muestran en la app) ---

@Preview(showBackground = true, name = "Vacíos")
@Composable
private fun CamposParticipantesVaciosPreview() {
    GestorDeEventosTheme {
        CamposParticipantes(ParticipantesUiState(), {}, {}, Modifier.padding(Espacio.m))
    }
}

@Preview(showBackground = true, name = "Válidos")
@Composable
private fun CamposParticipantesValidosPreview() {
    GestorDeEventosTheme {
        CamposParticipantes(ParticipantesUiState(minimo = "4", maximo = "10"), {}, {}, Modifier.padding(Espacio.m))
    }
}

@Preview(showBackground = true, name = "Máximo menor que el mínimo")
@Composable
private fun CamposParticipantesMaximoMenorPreview() {
    GestorDeEventosTheme {
        CamposParticipantes(ParticipantesUiState(minimo = "10", maximo = "4"), {}, {}, Modifier.padding(Espacio.m))
    }
}

@Preview(showBackground = true, name = "Vacíos con errores")
@Composable
private fun CamposParticipantesVaciosConErroresPreview() {
    GestorDeEventosTheme {
        CamposParticipantes(ParticipantesUiState(mostrarErrores = true), {}, {}, Modifier.padding(Espacio.m))
    }
}

@Preview(showBackground = true, name = "Interactivo (modo interactivo de la vista previa)")
@Composable
private fun CamposParticipantesInteractivoPreview() {
    GestorDeEventosTheme {
        var estado by remember { mutableStateOf(ParticipantesUiState()) }
        CamposParticipantes(
            estado = estado,
            onMinimoChange = { estado = estado.copy(minimo = it) },
            onMaximoChange = { estado = estado.copy(maximo = it) },
            modifier = Modifier.padding(Espacio.m)
        )
    }
}

@Preview(showBackground = true, name = "Oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CamposParticipantesOscuroPreview() {
    GestorDeEventosTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            CamposParticipantes(ParticipantesUiState(minimo = "10", maximo = "4"), {}, {}, Modifier.padding(Espacio.m))
        }
    }
}

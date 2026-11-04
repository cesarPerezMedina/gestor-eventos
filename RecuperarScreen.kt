package com.example.gestordeeventos.ui.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestordeeventos.ui.theme.GestorDeEventosTheme

/**
 * Pantalla "He olvidado mi contraseña" (Issue 14).
 *
 * @param onVolver pulsar la flecha atrás o "Volver" estando en el primer paso (ir al login).
 * @param onClaveCambiada pulsar "Volver a iniciar sesión" tras cambiar la contraseña.
 */
@Composable
fun RecuperarScreen(
    onVolver: () -> Unit,
    onClaveCambiada: () -> Unit,
    viewModel: RecuperarViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    // Atrás en el paso 2 vuelve al paso 1 en vez de salir de la pantalla
    BackHandler(enabled = estado.paso == PasoRecuperar.NUEVA_CLAVE) { viewModel.volverACorreo() }

    RecuperarContenido(
        estado = estado,
        onCorreoChange = viewModel::onCorreoChange,
        onNuevaClaveChange = viewModel::onNuevaClaveChange,
        onConfirmarClaveChange = viewModel::onConfirmarClaveChange,
        onContinuar = viewModel::continuar,
        onCambiarClave = viewModel::cambiarClave,
        onAtras = {
            if (estado.paso == PasoRecuperar.NUEVA_CLAVE) viewModel.volverACorreo() else onVolver()
        },
        onTerminar = onClaveCambiada
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecuperarContenido(
    estado: RecuperarUiState,
    onCorreoChange: (String) -> Unit,
    onNuevaClaveChange: (String) -> Unit,
    onConfirmarClaveChange: (String) -> Unit,
    onContinuar: () -> Unit,
    onCambiarClave: () -> Unit,
    onAtras: () -> Unit,
    onTerminar: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Recuperar contraseña") },
                navigationIcon = {
                    if (estado.paso != PasoRecuperar.HECHO) {
                        IconButton(onClick = onAtras) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (estado.paso) {
                PasoRecuperar.CORREO -> PasoCorreo(estado, onCorreoChange, onContinuar, onAtras)
                PasoRecuperar.NUEVA_CLAVE -> PasoNuevaClave(
                    estado, onNuevaClaveChange, onConfirmarClaveChange, onCambiarClave
                )
                PasoRecuperar.HECHO -> PasoHecho(onTerminar)
            }
        }
    }
}

@Composable
private fun PasoCorreo(
    estado: RecuperarUiState,
    onCorreoChange: (String) -> Unit,
    onContinuar: () -> Unit,
    onVolver: () -> Unit
) {
    Text("¿Has olvidado tu contraseña?", style = MaterialTheme.typography.headlineSmall)
    Text(
        "Escribe el correo con el que te registraste y podrás elegir una contraseña nueva.",
        style = MaterialTheme.typography.bodyMedium
    )
    OutlinedTextField(
        value = estado.correo,
        onValueChange = onCorreoChange,
        label = { Text("Correo electrónico") },
        singleLine = true,
        isError = estado.errorCorreo != null,
        supportingText = { estado.errorCorreo?.let { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onContinuar() }),
        modifier = Modifier.fillMaxWidth()
    )
    BotonPrincipal("Continuar", estado.cargando, onContinuar)
    TextButton(onClick = onVolver, modifier = Modifier.align(Alignment.CenterHorizontally)) {
        Text("Volver a iniciar sesión")
    }
}

@Composable
private fun PasoNuevaClave(
    estado: RecuperarUiState,
    onNuevaClaveChange: (String) -> Unit,
    onConfirmarClaveChange: (String) -> Unit,
    onCambiarClave: () -> Unit
) {
    Text("Elige una contraseña nueva", style = MaterialTheme.typography.headlineSmall)
    Text(
        "Mínimo ${ValidacionRecuperacion.LONGITUD_MINIMA_CLAVE} caracteres, con mayúscula, " +
            "minúscula, número y símbolo.",
        style = MaterialTheme.typography.bodyMedium
    )
    CampoClave(
        valor = estado.nuevaClave,
        etiqueta = "Nueva contraseña",
        error = estado.errorClave,
        imeAction = ImeAction.Next,
        onCambio = onNuevaClaveChange
    )
    CampoClave(
        valor = estado.confirmarClave,
        etiqueta = "Confirmar contraseña",
        error = estado.errorConfirmar,
        imeAction = ImeAction.Done,
        onCambio = onConfirmarClaveChange,
        onDone = onCambiarClave
    )
    BotonPrincipal("Cambiar contraseña", estado.cargando, onCambiarClave)
}

@Composable
private fun PasoHecho(onTerminar: () -> Unit) {
    Spacer(Modifier.height(24.dp))
    Text("¡Contraseña actualizada!", style = MaterialTheme.typography.headlineSmall)
    Text(
        "Ya puedes iniciar sesión con tu contraseña nueva.",
        style = MaterialTheme.typography.bodyMedium
    )
    BotonPrincipal("Volver a iniciar sesión", false, onTerminar)
}

@Composable
private fun CampoClave(
    valor: String,
    etiqueta: String,
    error: String?,
    imeAction: ImeAction,
    onCambio: (String) -> Unit,
    onDone: () -> Unit = {}
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        singleLine = true,
        isError = error != null,
        supportingText = { error?.let { Text(it) } },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            TextButton(onClick = { visible = !visible }) { Text(if (visible) "Ocultar" else "Mostrar") }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun BotonPrincipal(texto: String, cargando: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = !cargando, modifier = Modifier.fillMaxWidth().height(52.dp)) {
        if (cargando) CircularProgressIndicator(modifier = Modifier.height(24.dp)) else Text(texto)
    }
}

// ---------- Previews (se ven en Android Studio sin ejecutar la app) ----------

@Preview(showBackground = true)
@Composable
private fun PreviewPasoCorreo() = GestorDeEventosTheme {
    RecuperarContenido(RecuperarUiState(), {}, {}, {}, {}, {}, {}, {})
}

@Preview(showBackground = true)
@Composable
private fun PreviewPasoNuevaClave() = GestorDeEventosTheme {
    RecuperarContenido(
        RecuperarUiState(paso = PasoRecuperar.NUEVA_CLAVE, errorConfirmar = "Las contraseñas no coinciden"),
        {}, {}, {}, {}, {}, {}, {}
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewPasoHecho() = GestorDeEventosTheme {
    RecuperarContenido(RecuperarUiState(paso = PasoRecuperar.HECHO), {}, {}, {}, {}, {}, {}, {})
}

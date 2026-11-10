package com.example.gestordeeventos.ui.amigos

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.gestordeeventos.R
import com.example.gestordeeventos.data.model.Amigo
import com.example.gestordeeventos.ui.componentes.UsuarioItem
import com.example.gestordeeventos.ui.theme.Espacio
import com.example.gestordeeventos.ui.theme.GestorDeEventosTheme
import com.example.gestordeeventos.ui.theme.Tamano

/**
 * Pestaña "Amigos": título, acceso a la búsqueda de usuarios y lista de amigos agregados.
 * Si todavía no hay ninguno, muestra un estado vacío que invita a buscar usuarios.
 *
 * Es una pantalla sin lógica: recibe los datos ya preparados.
 *
 * @param amigos amigos del usuario (ver `AmistadDao.listarAmigos`).
 * @param onBuscarClick se llama al pulsar el buscador o el botón del estado vacío (Issue 30).
 */
@Composable
fun AmigosScreen(
    modifier: Modifier = Modifier,
    amigos: List<Amigo> = emptyList(),
    onBuscarClick: () -> Unit = {}
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(Espacio.m),
        verticalArrangement = Arrangement.spacedBy(Espacio.s)
    ) {
        item {
            Text(
                text = stringResource(R.string.amigos_titulo),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        item {
            BuscadorUsuarios(onClick = onBuscarClick, modifier = Modifier.padding(vertical = Espacio.s))
        }

        if (amigos.isEmpty()) {
            item { EstadoVacioAmigos(onBuscarClick = onBuscarClick) }
        } else {
            item {
                Text(
                    text = stringResource(R.string.amigos_seccion, amigos.size),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Espacio.xs)
                )
            }
            items(items = amigos, key = { it.id }) { amigo ->
                UsuarioItem(id = amigo.id, nombreUsuario = amigo.nombreUsuario)
            }
        }
    }
}

/** Campo de búsqueda de solo lectura: al pulsarlo se abrirá la búsqueda de usuarios (Issue 30). */
@Composable
private fun BuscadorUsuarios(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Tamano.alturaCampo),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Espacio.m),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Espacio.s)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.amigos_buscar_hint),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Lo que se ve cuando el usuario aún no tiene amigos. */
@Composable
private fun EstadoVacioAmigos(onBuscarClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Espacio.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Espacio.s)
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(Tamano.iconoL * 2),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(R.string.amigos_vacio_titulo),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.amigos_vacio_texto),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onBuscarClick,
            modifier = Modifier
                .padding(top = Espacio.s)
                .heightIn(min = Tamano.alturaBoton)
        ) {
            Text(text = stringResource(R.string.amigos_vacio_boton))
        }
    }
}

// --- Vistas previas (solo Android Studio, no se muestran en la app) ---

private val amigosDeEjemplo = listOf(
    Amigo(id = 3, nombreUsuario = "ana_runner"),
    Amigo(id = 7, nombreUsuario = "marta_padel"),
    Amigo(id = 12, nombreUsuario = "carlos99"),
    Amigo(id = 21, nombreUsuario = "nombre_de_usuario_muy_largo_20")
)

@Preview(showBackground = true, name = "Con amigos")
@Composable
private fun AmigosConAmigosPreview() {
    GestorDeEventosTheme { AmigosScreen(amigos = amigosDeEjemplo) }
}

@Preview(showBackground = true, name = "Sin amigos")
@Composable
private fun AmigosVacioPreview() {
    GestorDeEventosTheme { AmigosScreen() }
}

@Preview(showBackground = true, name = "Con amigos (oscuro)", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AmigosOscuroPreview() {
    GestorDeEventosTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) { AmigosScreen(amigos = amigosDeEjemplo) }
    }
}

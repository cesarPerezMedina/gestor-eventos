package com.example.gestordeeventos.ui.theme

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview


/**
 * Catálogo visual del sistema de diseño: sirve para comprobar de un vistazo
 * colores, tipografías, formas y componentes en modo claro y oscuro.
 * (Solo es una vista previa de Android Studio, no se muestra en la app.)
 */
@Composable
private fun CatalogoDeDiseno() {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(Espacio.m),
            verticalArrangement = Arrangement.spacedBy(Espacio.m)
        ) {
            Text("Encuentra. Organiza. Juega.", style = MaterialTheme.typography.headlineMedium)
            Text("Título de sección", style = MaterialTheme.typography.titleLarge)
            Text(
                "Texto de cuerpo: partido de pádel mañana a las 18:00 en el polideportivo.",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                "Texto secundario",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(Espacio.s)) {
                Button(onClick = {}) { Text("Crear evento") }
                OutlinedButton(onClick = {}) { Text("Buscar") }
                TextButton(onClick = {}) { Text("Cancelar") }
            }

            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Nombre del evento") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(Espacio.s)) {
                AssistChip(onClick = {}, label = { Text("Fútbol") })
                AssistChip(onClick = {}, label = { Text("Pádel") })
                AssistChip(onClick = {}, label = { Text("Tenis") })
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(Espacio.m), verticalArrangement = Arrangement.spacedBy(Espacio.xs)) {
                    Text("Partido de baloncesto", style = MaterialTheme.typography.titleMedium)
                    Text("Sábado · 17:30 · 6/10 participantes", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(Espacio.s)) {
                Swatch("primary", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
                Swatch("secondary", MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.onSecondary)
                Swatch("tertiary", MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.onTertiary)
                Swatch("error", MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.onError)
            }
        }
    }
}

@Composable
private fun Swatch(
    nombre: String,
    fondo: androidx.compose.ui.graphics.Color,
    texto: androidx.compose.ui.graphics.Color
) {
    Surface(color = fondo, shape = MaterialTheme.shapes.small) {
        Text(
            nombre,
            color = texto,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = Espacio.s, vertical = Espacio.m)
        )
    }
}

@Preview(name = "Claro", showBackground = true, heightDp = 640)
@Composable
private fun CatalogoClaroPreview() {
    GestorDeEventosTheme(darkTheme = false) { CatalogoDeDiseno() }
}

@Preview(
    name = "Oscuro", showBackground = true, heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun CatalogoOscuroPreview() {
    GestorDeEventosTheme(darkTheme = true) { CatalogoDeDiseno() }
}

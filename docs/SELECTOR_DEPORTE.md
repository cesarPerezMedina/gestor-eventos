# Selector de deporte (Issue 40)

`SelectorDeporte` (en `ui/componentes/`) deja elegir el deporte de un evento: Fútbol, Baloncesto, Tenis,
Pádel, Atletismo u Otros. Es una de las piezas del formulario de crear evento.

- **Una opción por cada `Deporte`:** icono y nombre, en una cuadrícula de tres columnas. Si se añade un deporte
  al enum, aparece solo. El nombre sale de `Deporte.etiqueta`, así que ya está en español e inglés.
- **Solo se elige uno.** La opción elegida se marca con el color primario y un borde más grueso.
- **Accesible:** cada opción se anuncia como botón de opción y el conjunto como un grupo.
- **Sin estado:** quien lo usa guarda el deporte elegido.

```kotlin
var deporte by remember { mutableStateOf<Deporte?>(null) }   // null = aún sin elegir
SelectorDeporte(seleccionado = deporte, onSeleccionar = { deporte = it })
```

`iconoDeDeporte(deporte)` devuelve el icono de cada deporte para reutilizarlo (por ejemplo, en la tarjeta de evento).

Pruebas: `SelectorDeporteTest` (necesita emulador o móvil). Las vistas previas están en `SelectorDeporte.kt`.

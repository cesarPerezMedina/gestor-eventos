# Selector de visibilidad (Issue 42)

`SelectorVisibilidad` (en `ui/componentes/`) deja elegir si un evento es **público** o **privado**. Es una de
las piezas del formulario de crear evento.

- **Público:** cualquiera puede encontrar el evento y unirse.
- **Privado:** solo pueden unirse las personas que se inviten.
- Cada opción lleva un icono, su nombre y una línea que explica qué significa. Se elige una sola, y la elegida se
  marca con el color primario y un borde más grueso.
- **Accesible:** cada opción se anuncia como botón de opción y el conjunto como un grupo.
- **Sin estado y con el mismo booleano que `Evento.publico`** (`true` = público, valor por defecto del evento).
  Quien lo usa guarda el valor:

```kotlin
var publico by remember { mutableStateOf(true) }
SelectorVisibilidad(publico = publico, onCambiar = { publico = it })
```

Este selector solo elige el valor. Lo que cambia entre un evento público y uno privado (quién lo ve, cómo se une
la gente) es la Issue 43.

Textos en español e inglés en `strings_visibilidad.xml`. Pruebas: `SelectorVisibilidadTest` (necesita emulador o
móvil). Las vistas previas están en `SelectorVisibilidad.kt`.

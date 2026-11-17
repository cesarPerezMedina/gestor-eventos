# Ajustes de la app

## Tema claro / oscuro (Issue 78)

En **Ajustes → Apariencia** el usuario elige entre tres opciones:

| Opción | Efecto |
|---|---|
| Seguir al sistema (por defecto) | La app copia el modo claro/oscuro del móvil. |
| Claro | Siempre claro, aunque el móvil esté en oscuro. |
| Oscuro | Siempre oscuro, aunque el móvil esté en claro. |

La elección se guarda en el móvil (preferencias `ajustes`, clave `modo_tema`) y se recuerda al cerrar y abrir la app.

### Cómo funciona

- `ModoTema` (`data/model`): las tres opciones y `esOscuro(sistemaOscuro)`.
- `AjustesRepositorio` (`data/repositorio`): guarda el modo y lo expone como `StateFlow`. Hay una única instancia
  (`AjustesRepositorio.obtener(context)`), compartida por `MainActivity` y por la pantalla de Ajustes.
- `AjustesViewModel` + `AjustesScreen` (`ui/ajustes`): el selector. La pantalla obtiene su ViewModel sola, así que
  la navegación no necesita cambios.
- `MainActivity`: observa `modoTema` y se lo pasa a `GestorDeEventosTheme(darkTheme = ...)`. También actualiza el
  color de los iconos de las barras del sistema para que se vean con el tema elegido.

### Cómo probarlo

1. Abrir la app, iniciar sesión y entrar en Ajustes (icono de engranaje en Inicio).
2. Elegir **Oscuro**: la pantalla de Ajustes cambia al instante.
3. Cerrar la app del todo y volver a abrirla: sigue en oscuro.
4. Elegir **Seguir al sistema** y cambiar el modo oscuro del móvil: la app lo sigue.

### Importante para el equipo

Solo las pantallas que usan `MaterialTheme.colorScheme` cambian de color. Varias pantallas antiguas (Login, Inicio,
Buscar...) tienen colores fijos escritos a mano (`Color(0xFF...)`) y se verán igual en claro y oscuro hasta que se
cambien a `MaterialTheme.colorScheme.*`. Hazlo cuando toques cada pantalla; el selector ya funciona para todas.

## Tamaño del texto (Issue 79)

En **Ajustes → Tamaño del texto** el usuario elige entre cuatro tamaños:

| Opción | Factor |
|---|---|
| Pequeño | 0,85 |
| Normal (por defecto) | 1,0 |
| Grande | 1,15 |
| Muy grande | 1,3 |

El factor multiplica el tamaño de **todos** los textos escritos en `sp` (que es casi toda la app). Se aplica en
`GestorDeEventosTheme(escalaTexto = ...)`, que `MainActivity` alimenta con `AjustesRepositorio.escalaTexto`, y se
guarda en el móvil junto al tema (clave `escala_texto`). Se combina con el tamaño de letra que ya tenga el móvil.

- `EscalaTexto` (`data/model`): las cuatro opciones y su factor.
- `AjustesRepositorio.escalaTexto` / `cambiarEscalaTexto`: el valor, igual que el tema.
- La lista de opciones reutiliza `OpcionesSeleccion`.

### Cómo probarlo

1. Entrar en Ajustes y elegir **Muy grande**: los textos crecen al instante, incluida la propia pantalla de Ajustes.
2. Navegar por las demás pantallas y comprobar que **nada se corta ni se solapa** (sobre todo botones, la barra
   inferior y los formularios).
3. Cerrar la app del todo y abrirla: el tamaño se mantiene.

### Importante para el equipo

Los textos con tamaño en `dp` en vez de `sp`, o los contenedores con altura fija, no se adaptan bien. Si una pantalla se
rompe con **Muy grande**, cambia esa altura fija por `heightIn(min = ...)` o deja que el contenido decida el alto.

## Pruebas

- `AjustesRepositorioTest` (sin emulador): valores por defecto, guardado, recuerdo entre arranques, valores
  desconocidos, la regla `esOscuro` y los factores de tamaño de texto.
- `AjustesScreenTest` (con emulador): los selectores de tema y de tamaño muestran sus opciones, marcan la actual y avisan al pulsar.

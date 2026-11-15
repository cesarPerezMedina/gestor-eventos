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

## Pruebas

- `AjustesRepositorioTest` (sin emulador): valor por defecto, guardado, recuerdo entre arranques, valores
  desconocidos y la regla `esOscuro`.
- `AjustesScreenTest` (con emulador): el selector muestra las tres opciones, marca la actual y avisa al pulsar.

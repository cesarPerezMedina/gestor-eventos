# Permisos de Android (Issue 24)

## Qué permisos usa la app

| Permiso | Tipo | Cómo se concede |
|---|---|---|
| `INTERNET`, `ACCESS_NETWORK_STATE` | Normal | Al instalar. Solo se declaran en el `AndroidManifest.xml`; Android no muestra ningún diálogo. |
| `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` | Peligroso | En tiempo de ejecución, con un solo diálogo. Cuenta como concedido si el usuario da al menos la ubicación aproximada. |
| `POST_NOTIFICATIONS` | Peligroso (desde Android 13) | En tiempo de ejecución. En versiones anteriores no existe y se considera concedido. |

Todos están declarados en `app/src/main/AndroidManifest.xml`. Se añade también `uses-feature` de GPS con
`required="false"` para que la app pueda instalarse en dispositivos sin GPS.

## Piezas

```
util/
  PermisoApp.kt       Lista de permisos de la app que se piden al usuario (UBICACION, NOTIFICACIONES)
  EstadoPermiso.kt    CONCEDIDO, PENDIENTE, EXPLICAR o BLOQUEADO
ui/componentes/
  SolicitudPermiso.kt rememberSolicitudPermiso(): estado del permiso + acciones para pedirlo y abrir Ajustes
  AvisoPermiso.kt     ConPermiso() y AvisoPermiso(): tarjeta que explica para qué se pide y deja concederlo
res/values*/strings_permisos.xml   Textos en español e inglés
```

Los textos están en un archivo aparte (`strings_permisos.xml`) y no en `strings.xml`, para no mezclarlos con los de
otras Issues. Ninguna pantalla existente se ha modificado.

## Cómo usarlo desde una pantalla

Mostrar contenido solo si hay permiso (si no, se enseña el aviso con el botón para concederlo):

```kotlin
ConPermiso(PermisoApp.UBICACION) {
    // contenido que necesita la ubicación
}
```

Pedirlo desde un botón o al entrar en una pantalla:

```kotlin
val ubicacion = rememberSolicitudPermiso(PermisoApp.UBICACION) { concedido -> /* reaccionar */ }

when (ubicacion.estado) {
    EstadoPermiso.CONCEDIDO -> { /* usar la ubicación */ }
    EstadoPermiso.BLOQUEADO -> Button(onClick = ubicacion.abrirAjustes) { Text("Abrir ajustes") }
    else -> Button(onClick = ubicacion.solicitar) { Text("Conceder permiso") }
}
```

## Qué significa cada estado

- `PENDIENTE`: todavía no se ha pedido. Se puede pedir con normalidad.
- `EXPLICAR`: el usuario lo denegó una vez. Conviene explicarle para qué sirve antes de volver a pedirlo.
- `BLOQUEADO`: lo denegó con "no volver a preguntar". Android ya no muestra el diálogo; hay que llevarlo a Ajustes
  (`abrirAjustes`). Al volver a la app, el estado se recalcula solo.

## Qué NO incluye

Este trabajo solo gestiona los permisos. **Leer la posición del dispositivo** (obtener latitud y longitud) necesita
una librería como `play-services-location`, que no está en el proyecto, y corresponde a otra Issue.

## Cómo probarlo

- Tests sin emulador: `app/src/test/.../util/PermisosTest.kt` (`./gradlew test`).
- A mano, en emulador o móvil (los diálogos de permisos no se pueden comprobar con tests locales):
  1. Poner `ConPermiso(PermisoApp.UBICACION) { ... }` en una pantalla de prueba.
  2. Abrir la app: debe verse el aviso. Pulsar "Conceder permiso" y **Permitir**: debe aparecer el contenido.
  3. Quitar el permiso en Ajustes y repetir, pero pulsando **No permitir** dos veces: el aviso debe cambiar a
     "Abrir ajustes".
  4. Volver de Ajustes con el permiso activado: el contenido debe aparecer sin reiniciar la app.

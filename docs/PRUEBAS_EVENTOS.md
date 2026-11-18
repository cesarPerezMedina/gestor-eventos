# Pruebas de integración del almacenamiento de eventos (Issue 51)

`AlmacenamientoEventosTest` (con emulador) crea eventos con `EventoRepositorio.crearEvento` y comprueba que quedan
bien guardados en Room y que se recuperan igual. Usa una base de datos real en memoria, así que valida a la vez las
reglas, las consultas SQL y las claves foráneas.

| Qué se comprueba | Casos |
|---|---|
| **Se guarda todo** | Todos los campos (nombre, descripción, deporte, visibilidad, mínimo y máximo, dirección, coordenadas, fecha, creador). |
| **Se limpia** | El nombre y la descripción se guardan sin espacios sobrantes. |
| **Identificadores** | Cada evento recibe un id distinto, aunque el nombre se repita. |
| **Recuperar** | Los eventos creados salen ordenados por fecha y cada usuario solo ve los suyos. |
| **No se guarda lo inválido** | Nombre corto o solo con espacios, y máximo menor que el mínimo. Sin evento no se crean invitaciones. |
| **Sin límite** | Máximo 0 se guarda tal cual (sin límite de participantes). |
| **Visibilidad** | El público sale en Buscar y el privado no; el creador ve los dos en "Mis eventos". |
| **Invitaciones** | Un privado guarda una invitación por persona, sin repetir ni invitar al creador. |
| **Listas vivas** | "Mis eventos" se actualiza al crear otro evento. |
| **Persistencia** | Los eventos siguen ahí al cerrar y volver a abrir la base de datos (en un archivo real). |

Los casos de eliminar y salir de un evento están en `EliminarEventoTest` y `SalirDeEventoTest`
(ver [`GESTION_EVENTOS.md`](GESTION_EVENTOS.md)).

## Cómo ejecutarla

Con emulador o móvil: clic derecho sobre `app/src/androidTest/.../AlmacenamientoEventosTest` > *Run*, o
`./gradlew connectedDebugAndroidTest`.

## Resultados

| Fecha | Resultado | Notas |
|---|---|---|
| _pendiente_ | _pendiente_ | |

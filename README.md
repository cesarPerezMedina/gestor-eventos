# Gestor de Eventos Deportivos

**Encuentra. Organiza. Juega.**

Aplicación Android diseñada para crear, buscar y gestionar eventos deportivos, conectando a personas de una misma zona para practicar deporte juntas.

## Funcionalidades principales

- **Registro e inicio de sesión:** creación de cuentas y recuperación de contraseñas.
- **Crear eventos:** organizar actividades deportivas indicando nombre, descripción, fecha, hora, ubicación y número de participantes.
- **Buscar eventos:** encontrar eventos públicos por nombre o proximidad.
- **Mis eventos:** consultar, abandonar o eliminar eventos.
- **Invitaciones:** aceptar o rechazar invitaciones a eventos.
- **Amigos:** añadir usuarios mediante su ID o nombre de usuario.
- **Ajustes:** personalizar el tema claro u oscuro y el tamaño del texto.
- **Cerrar sesión:** salir de la cuenta.

## Deportes disponibles

Fútbol, baloncesto, tenis, pádel, atletismo y otras disciplinas deportivas.

## Tipos de eventos

- **Públicos:** visibles en las búsquedas y recomendaciones de eventos cercanos.
- **Privados:** accesibles mediante invitación o autorización.

## Ubicación y gestión

- La aplicación utilizará la ubicación del usuario, con su permiso, para recomendar eventos públicos cercanos. Cada evento podrá incluir una dirección o un punto seleccionado en un mapa.
- Los eventos cuya fecha haya pasado dejarán de aparecer en la lista de eventos activos.

## Novedades: base de datos local (Issue 4)

La aplicación guarda sus datos en el propio dispositivo con **Room**, una capa sobre SQLite. No necesita servidor ni cuentas externas, y es suficiente para probar la app en clase.

- **Almacenamiento persistente:** los datos siguen ahí al cerrar y volver a abrir la aplicación.
- **Cuatro tablas:**

  | Tabla | Contenido |
  |---|---|
  | `usuarios` | Cuentas de usuario, con correo y nombre de usuario únicos. |
  | `eventos` | Eventos con deporte, visibilidad, aforo, ubicación, fecha y hora, y su creador. |
  | `participaciones` | Qué usuario participa en qué evento. |
  | `invitaciones` | Invitaciones a eventos, con estado pendiente, aceptada o rechazada. |

- **Integridad de los datos:** no se pueden repetir correos ni nombres de usuario, y al borrar un evento se borran también sus participaciones e invitaciones.
- **Acceso mediante DAOs:** cada tabla tiene su interfaz de consultas en `data/local/dao`, y `AppDatabase.obtener(context)` devuelve siempre la misma base de datos.
- **Prueba automática:** `AppDatabaseTest` comprueba que se guardan y leen datos y que las restricciones funcionan.
- **Contraseñas:** nunca se guardan en texto plano; la tabla de usuarios almacena un hash.

La guía completa está en [`docs/BASE_DE_DATOS.md`](docs/BASE_DE_DATOS.md).

> Los datos son locales: dos dispositivos distintos no comparten usuarios ni eventos.

## Tecnologías

| Área | Tecnología |
|---|---|
| Lenguaje | Kotlin |
| Interfaz | Jetpack Compose con Material 3 |
| Arquitectura | MVVM por capas (`ui`, `data`, `navegacion`, `util`) |
| Navegación | Navigation Compose |
| Base de datos local | Room (con KSP) |
| Versiones | Centralizadas en `gradle/libs.versions.toml` |
| Compatibilidad | Android 10 (API 29) o superior |

## Estructura del proyecto

```
app/src/main/java/com/example/gestordeeventos/
  data/
    local/          AppDatabase y DAOs de Room
    model/          Usuario, Evento, Participacion, Invitacion, Deporte
    repositorio/    Repositorios (acceso a los datos desde los ViewModel)
    localizacion/   Ubicación del dispositivo
  navegacion/       Rutas y navegación entre pantallas
  ui/               Pantallas, ViewModels, componentes y tema
  util/             Utilidades (fechas, distancias...)
docs/               Documentación técnica
```

## Cómo ejecutar el proyecto

1. Clonar el repositorio y abrirlo con **Android Studio**.
2. Esperar a que termine la sincronización de Gradle (*File → Sync Project with Gradle Files*).
3. Ejecutar la app en un emulador o en un móvil con Android 10 o superior.
4. Para pasar las pruebas de la base de datos, abrir `AppDatabaseTest` y pulsar el botón de ejecutar (necesita emulador o móvil conectado).

## Forma de trabajo

El proyecto se organiza en un GitHub Project con unas 80 Issues repartidas entre los tres miembros del equipo. Cada Issue se desarrolla en una rama propia y se incorpora a `main` mediante Pull Request, para ver el progreso paso a paso.

## Posibles mejoras futuras

- Notificaciones y recordatorios.
- Chat entre participantes.
- Valoraciones de eventos.
- Calendario deportivo personal.

## Autores

- Iñaki Marín
- Carlos Martín
- César Pérez

## Estado del proyecto

En fase de diseño y desarrollo. Ya está configurada la base de datos local (Issue 4); el resto de funcionalidades se irán incorporando Issue a Issue.

*El deporte se disfruta más cuando se comparte.*

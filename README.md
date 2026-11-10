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

## Novedades: inicio de sesión (Issue 10)

La lógica del inicio de sesión ya está implementada sobre la base de datos local. La pantalla de Login (Issue 9) solo tiene que conectarse a ella.

- **Correo o nombre de usuario:** se puede entrar con cualquiera de los dos, sin distinguir mayúsculas.
- **Contraseñas con hash:** la contraseña nunca se guarda en texto plano. Se guarda un hash PBKDF2 con una sal aleatoria (`util/ClaveHash.kt`), y el login comprueba la contraseña contra ese hash.
- **Mensaje único de error:** si el usuario no existe o la contraseña es incorrecta se muestra el mismo mensaje, para no revelar qué usuarios están registrados.
- **`LoginViewModel`:** guarda lo que escribe el usuario, valida que no haya campos vacíos, muestra el estado de carga y los errores, y avisa cuando la sesión se ha iniciado.
- **Mensajes en español e inglés** (`strings_auth.xml`).
- **Pruebas automáticas:** `ClaveHashTest`, `AuthRepositorioTest` y `LoginViewModelTest` se ejecutan sin emulador. `UsuarioDaoTest` comprueba la consulta SQL y necesita emulador o móvil.

La guía con un ejemplo para conectar la pantalla está en [`docs/AUTENTICACION.md`](docs/AUTENTICACION.md).

## Novedades: registro de usuarios (Issue 13)

La lógica del registro ya guarda usuarios en la base de datos local. La pantalla de Registro (Issue 11) solo tiene que conectarse a ella.

- **Nombre de usuario:** de 3 a 20 caracteres, solo letras, números y guion bajo (`ValidadorRegistro.validarNombreUsuario`).
- **Sin duplicados:** no se permite un correo ni un nombre de usuario ya registrado (sin distinguir mayúsculas).
- **Correo normalizado:** se guarda en minúsculas y sin espacios.
- **Contraseña segura:** solo se guarda su hash PBKDF2, nunca en texto plano.
- **`RegistroViewModel`:** valida todos los campos, muestra el error en el campo correcto, controla la carga y avisa cuando el registro termina.
- **Pruebas automáticas:** `ValidadorNombreUsuarioTest`, `AuthRepositorioRegistroTest` y `RegistroViewModelTest` (sin emulador); `UsuarioDaoTest` ampliado (con emulador).

Tras registrarse, ya se puede iniciar sesión con ese usuario. Ejemplo de conexión en [`docs/AUTENTICACION.md`](docs/AUTENTICACION.md).

## Tecnologías

| Área | Tecnología |
|---|---|
| Lenguaje | Kotlin |
| Interfaz | Jetpack Compose con Material 3 |
| Arquitectura | MVVM por capas (`ui`, `data`, `navegacion`, `util`) |
| Navegación | Navigation Compose |
| Base de datos local | Room (con KSP) |
| Seguridad de contraseñas | Hash PBKDF2 con sal (`ClaveHash`) |
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
  util/             Utilidades (ClaveHash, validaciones, permisos, fechas, distancias...)
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

El deporte se disfruta más cuando se comparte.


1. Estructura del sistema de diseño
   El sistema se organiza en los siguientes archivos, cada uno con una responsabilidad específica:

Archivo

Funcionalidad

Color.kt

Define la paleta de colores corporativa para los modos claro y oscuro.

Type.kt

Configura la tipografía Poppins y los diferentes estilos tipográficos.

Shape.kt

Establece las formas y los radios de las esquinas de los componentes.

Espaciado.kt

Centraliza los valores de espaciado (Espacio) y dimensiones (Tamano).

Theme.kt

Integra los elementos del sistema y gestiona los modos claro y oscuro, así como la escala del texto.

VistaPreviaTema.kt

Proporciona un catálogo de componentes mediante funciones @Preview para facilitar la revisión visual del diseño.

2. Paleta de colores corporativa
   Se establece una paleta cromática basada en los colores verde, ámbar y azul, que constituyen los principales elementos de identidad visual de la aplicación.

La paleta contempla dos configuraciones, correspondientes al modo claro y al modo oscuro, con el propósito de garantizar una presentación adecuada en ambos entornos.

Rol

Modo claro

Modo oscuro

Aplicación

Primario

#0A7B43

#6FDC98

Identidad corporativa, botones principales y barra de navegación inferior.

Secundario

#F59E0B

#FFB84D

Elementos destacados, avisos y estados de los eventos.

Terciario

#1D5FBF

#A9C7FF

Información, enlaces, mapas y eventos privados.

Fondo

#F6F8F5

#0F1511

Fondo general de las pantallas.

Superficie

#FFFFFF

#151B17

Tarjetas, ventanas y cuadros de diálogo.

Texto

#17211B

#E1E9E2

Contenido textual principal.

Error

#BA1A1A

#FFB4AB

Mensajes de error y acciones destructivas.

Asimismo, se tiene en consideración la accesibilidad visual, estableciendo como criterio que las combinaciones de texto y fondo alcancen un contraste mínimo de 4,5:1, correspondiente al nivel AA de las pautas WCAG.

3. Sistema tipográfico
   La tipografía principal seleccionada para la aplicación es Poppins, incorporada al proyecto mediante los recursos disponibles en res/font.

Se utilizan tres variantes tipográficas:

Regular.

Medium.

Bold.

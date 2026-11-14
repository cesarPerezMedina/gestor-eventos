# Autenticación local (Issues 10 y 13)

El inicio de sesión funciona con la base de datos local (Room), sin servidor. Esta guía cubre el
**inicio de sesión (Issue 10)**, el **registro de usuarios (Issue 13)** la **sesión recordada (Issue 16)** y el **flujo completo conectado (Issue 20)**.

## Piezas

```
util/ClaveHash.kt                    Convierte contraseñas en hash y las comprueba
data/repositorio/SesionRepositorio.kt  iniciar(id), usuarioActual(), cerrar() (sesión que sobrevive al cierre de la app)
util/ValidadorRegistro.kt            Validación de nombre de usuario, correo y contraseña
data/local/dao/UsuarioDao.kt         buscarPorCorreoONombreUsuario(...), existeCorreo(...), existeNombreUsuario(...)
data/repositorio/AuthRepositorio.kt  iniciarSesion(...) -> ResultadoLogin
                                     registrar(...) -> ResultadoRegistro
ui/auth/LoginViewModel.kt            Estado y lógica de la pantalla de login
ui/auth/RegistroViewModel.kt         Estado y lógica de la pantalla de registro
res/values*/strings_auth.xml         Mensajes de error (español e inglés)
```

## Cómo funciona

1. La pantalla llama a `viewModel.iniciarSesion()`.
2. El ViewModel comprueba que los campos no estén vacíos. Si lo están, pone un error en cada campo.
3. Si no, llama a `AuthRepositorio.iniciarSesion(identificador, clave)`.
4. El repositorio busca al usuario por **correo o nombre de usuario** (sin distinguir mayúsculas) y comprueba la
   contraseña con `ClaveHash.verificar`.
5. El resultado es `Exito(usuario)` o `CredencialesIncorrectas`. **No se distingue** entre "el usuario no existe" y
   "contraseña incorrecta", para no revelar qué usuarios están registrados. La pantalla muestra el mismo mensaje.
6. Si todo va bien, `estado.usuarioId` deja de ser `null` y la pantalla puede navegar.

## Contraseñas: cómo se guardan

La contraseña **nunca** se guarda. En `Usuario.claveHash` se guarda el resultado de PBKDF2 con una sal aleatoria:

```
pbkdf2:100000:<sal en Base64>:<hash en Base64>
```

Por eso dos usuarios con la misma contraseña tienen valores distintos, y por eso no se puede "ver" una contraseña
mirando la base de datos. El cálculo es lento a propósito (unas décimas de segundo), así que el repositorio lo hace
fuera del hilo principal.

## Cómo se conecta el login

La pantalla `LoginScreen` ya está conectada a `LoginViewModel` en `AppNavHost` (ver «Flujo completo conectado»).
`LoginViewModel` recibe un `texto: (Int) -> String` para convertir ids de `strings.xml` en texto sin necesitar un
`Context`; su `Factory` ya lo prepara con la base de datos y el contexto de la aplicación.

## Registro de usuarios (Issue 13)

`AuthRepositorio.registrar(nombreUsuario, correo, clave)` crea la cuenta en la base de datos local:

1. Normaliza los datos: quita espacios al nombre y guarda el correo en minúsculas.
2. Comprueba que el correo y el nombre de usuario no existan ya (sin distinguir mayúsculas).
3. Calcula el hash PBKDF2 de la contraseña (fuera del hilo principal) y guarda el usuario.

Devuelve `ResultadoRegistro.Exito(usuario)`, `CorreoEnUso` o `NombreUsuarioEnUso`. Además, la tabla tiene índices
únicos en `correo` y `nombreUsuario` como segunda barrera.

Reglas del **nombre de usuario** (`ValidadorRegistro.validarNombreUsuario`): de 3 a 20 caracteres, solo letras sin
tilde, números y guion bajo. El correo y la contraseña usan las reglas de la Issue 12.

`RegistroViewModel` valida los cuatro campos a la vez (cada uno muestra su propio error), evita envíos dobles,
traduce `CorreoEnUso` / `NombreUsuarioEnUso` a un error en el campo correspondiente, vacía las contraseñas al
terminar y pone `registroCompletado = true` para que la pantalla navegue.

`RegistroScreen` está conectada a `RegistroViewModel` en `AppNavHost`, igual que el login.

## Sesión recordada (Issue 16)

`SesionRepositorio` guarda **solo el ID** del usuario con la sesión iniciada, para que al cerrar y volver a abrir la
app no haya que escribir otra vez el correo y la contraseña. El ID se guarda en un archivo de preferencias del móvil
(`sesion`, a través de `AlmacenSesionPreferencias`); no hace falta ninguna librería nueva. La contraseña nunca se guarda.

| Función | Qué hace |
|---|---|
| `iniciar(usuarioId)` | Recuerda a ese usuario. Se llama tras un login o registro correcto. |
| `usuarioActual()` | Devuelve el usuario de la sesión guardada, o `null` si no hay. Si el ID ya no existe en la base de datos (por ejemplo, se recreó), borra la sesión y devuelve `null`. |
| `haySesionGuardada()` | `true` si hay un ID guardado (no comprueba que el usuario exista). |
| `cerrar()` | Olvida la sesión. Es lo que usará el botón de cerrar sesión (Issue 17). |

`AlmacenSesion` es una interfaz: los tests usan una versión en memoria y no necesitan emulador.

Ya está conectada en `AppNavHost` (ver la sección siguiente).

## Flujo completo conectado (Issue 20)

Las pantallas de Login y Registro ya usan sus ViewModels, y `AppNavHost` une todo:

```
Arranque ──► ¿hay sesión guardada y el usuario existe? ── sí ──► Inicio (con el menú de abajo)
                         │ no
                         ▼
                       Login ──► credenciales correctas ──► guarda la sesión ──► Inicio
                         │  └─► "Regístrate" ──► Registro ──► cuenta creada ──► vuelve a Login
                         └────► "¿Has olvidado tu contraseña?" ──► Recuperar ──► Restablecer ──► Login (Issue 15)
```

| Pieza | Qué hace |
|---|---|
| `AppNavHost` | Decide la pantalla inicial con `SesionRepositorio.usuarioActual()`, crea los ViewModels de Login y Registro y pasa a Inicio el nombre real del usuario. |
| `LoginScreen` / `RegistroScreen` | Mantienen sus campos, pero aceptan un `estado` opcional (`LoginUiState` / `RegistroUiState`) para mostrar errores bajo cada campo, el error general y el estado de carga (botón desactivado). |
| `LoginViewModel.iniciarSesion(identificador, clave)` | Atajo para pantallas que guardan sus propios campos: copia los valores, borra errores anteriores y lanza el login. Igual `RegistroViewModel.registrar(...)`. |
| Cerrar sesión (Issue 17) | El botón de Ajustes llama a `SesionManager.cerrarSesion()` **y** a `SesionRepositorio.cerrar()`. Si solo se llamara a la primera, el ID de la sesión seguiría guardado y la app entraría sola al reabrir. |

Correcciones hechas al integrar:

- **Errores obsoletos:** al reenviar un formulario ya corregido, los errores del intento anterior seguían en el estado.
  Ahora se borran antes de cada envío.
- **Restablecer contraseña:** `AuthViewModel` guardaba la contraseña nueva con SHA-256, un formato que el login no
  reconoce, así que tras restablecerla no se podía iniciar sesión. Ahora usa `ClaveHash` (PBKDF2), igual que el registro.
- **Pista de contraseña:** la pantalla de registro decía «Mínimo 8 caracteres, con letras y números», pero la regla real
  exige además mayúscula, minúscula y símbolo. El texto ya coincide con `ValidadorRegistro`.

Cómo probarlo a mano en el emulador:

1. Abrir la app: aparece el Login. Pulsar «Iniciar sesión» con los campos vacíos: aparece un error bajo cada campo.
2. «Regístrate» → crear una cuenta (por ejemplo `ana_23`, `ana@correo.com`, `Clave#2026`). Debe volver al Login.
3. Repetir el registro con el mismo correo: error «Ya existe una cuenta con ese correo».
4. Iniciar sesión con la cuenta nueva (con el correo o con el nombre de usuario): llega a Inicio con su nombre.
5. Cerrar la app del todo y volver a abrirla: entra directo a Inicio.
6. En Ajustes, «Cerrar sesión» y reabrir la app: aparece el Login.
7. «¿Has olvidado tu contraseña?» → el código aparece en Logcat (filtro `Recuperar`) → restablecer → iniciar sesión con la nueva.

## Qué NO hace todavía

- **Verificación real del correo:** el código de recuperación no se envía por email, se escribe en Logcat (simulado).
- **Una sola sesión:** conviven `SesionRepositorio` (ID del usuario) y `SesionManager` (correo). Conviene unificarlas.

## Pruebas

| Test | Qué comprueba | Cómo se ejecuta |
|---|---|---|
| `ClaveHashTest` | El hash no contiene la clave, es distinto cada vez y se verifica bien | Local (sin emulador) |
| `AuthRepositorioTest` | Login por correo o nombre, mayúsculas, espacios, clave incorrecta, usuario inexistente | Local |
| `LoginViewModelTest` | Campos vacíos, errores, éxito, clave incorrecta, fallo inesperado, atajo y errores obsoletos | Local |
| `ValidadorNombreUsuarioTest` | Longitud, caracteres permitidos y espacios del nombre de usuario | Local |
| `AuthRepositorioRegistroTest` | Registro, hash, correo normalizado, duplicados, login tras registrarse | Local |
| `RegistroViewModelTest` | Errores por campo, éxito, duplicados, fallo inesperado, atajo y errores obsoletos | Local |
| `SesionRepositorioTest` | Guardar, recuperar, sustituir y cerrar la sesión; sesión de un usuario que ya no existe | Local |
| `UsuarioDaoTest` | Las consultas SQL reales de login y de existencia (sin distinguir mayúsculas) | Emulador o móvil |

Los tests locales se ejecutan con el botón verde junto a cada clase, o con `./gradlew testDebugUnitTest`.

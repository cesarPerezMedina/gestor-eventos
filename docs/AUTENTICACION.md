# Autenticación local (Issues 10 y 13)

El inicio de sesión funciona con la base de datos local (Room), sin servidor. Esta guía cubre el
**inicio de sesión (Issue 10)** y el **registro de usuarios (Issue 13)**.

## Piezas

```
util/ClaveHash.kt                    Convierte contraseñas en hash y las comprueba
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

## Cómo conectarlo desde `LoginScreen` (Issue 9)

```kotlin
@Composable
fun LoginScreen(onSesionIniciada: (Long) -> Unit, viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    LaunchedEffect(estado.usuarioId) {
        estado.usuarioId?.let(onSesionIniciada)
    }

    OutlinedTextField(
        value = estado.identificador,
        onValueChange = viewModel::onIdentificadorChange,
        isError = estado.errorIdentificador != null,
        supportingText = { estado.errorIdentificador?.let { Text(it) } },
        // ...
    )
    // Lo mismo con estado.clave / viewModel::onClaveChange / estado.errorClave
    // estado.errorGeneral: mensaje general ("Correo, usuario o contraseña incorrectos")
    // estado.cargando: desactivar el botón y mostrar un indicador

    Button(onClick = viewModel::iniciarSesion, enabled = !estado.cargando) { /* ... */ }
}
```

`LoginViewModel` recibe un `texto: (Int) -> String` para convertir ids de `strings.xml` en texto sin necesitar un
`Context`. La `Factory` ya lo prepara con la base de datos y el contexto de la aplicación.

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

### Cómo conectarlo desde `RegistroScreen` (Issue 11)

```kotlin
@Composable
fun RegistroScreen(
    onRegistrado: () -> Unit,
    viewModel: RegistroViewModel = viewModel(factory = RegistroViewModel.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    LaunchedEffect(estado.registroCompletado) {
        if (estado.registroCompletado) onRegistrado()
    }

    // Un campo por cada valor: nombreUsuario, correo, clave, confirmacion
    OutlinedTextField(
        value = estado.nombreUsuario,
        onValueChange = viewModel::onNombreUsuarioChange,
        isError = estado.errorNombreUsuario != null,
        supportingText = { estado.errorNombreUsuario?.let { Text(it) } },
    )
    // Igual con onCorreoChange / onClaveChange / onConfirmacionChange y sus errorXxx
    // estado.errorGeneral: mensaje general; estado.cargando: desactivar el botón

    Button(onClick = viewModel::registrar, enabled = !estado.cargando) { /* ... */ }
}
```

## Qué NO hace todavía

- **Recordar la sesión** al cerrar la app: Issue 16.
- **Las pantallas** de login y registro (Issues 9 y 11): esta parte solo incluye la lógica que usarán.
- **Cerrar sesión:** Issue 17.

## Pruebas

| Test | Qué comprueba | Cómo se ejecuta |
|---|---|---|
| `ClaveHashTest` | El hash no contiene la clave, es distinto cada vez y se verifica bien | Local (sin emulador) |
| `AuthRepositorioTest` | Login por correo o nombre, mayúsculas, espacios, clave incorrecta, usuario inexistente | Local |
| `LoginViewModelTest` | Campos vacíos, errores, éxito, clave incorrecta, fallo inesperado | Local |
| `ValidadorNombreUsuarioTest` | Longitud, caracteres permitidos y espacios del nombre de usuario | Local |
| `AuthRepositorioRegistroTest` | Registro, hash, correo normalizado, duplicados, login tras registrarse | Local |
| `RegistroViewModelTest` | Errores por campo, éxito, duplicados, fallo inesperado | Local |
| `UsuarioDaoTest` | Las consultas SQL reales de login y de existencia (sin distinguir mayúsculas) | Emulador o móvil |

Los tests locales se ejecutan con el botón verde junto a cada clase, o con `./gradlew testDebugUnitTest`.

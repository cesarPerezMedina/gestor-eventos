# Autenticación local (Issues 10 y 13)

El inicio de sesión funciona con la base de datos local (Room), sin servidor. Esta guía describe la parte del
**inicio de sesión (Issue 10)**; la de registro (Issue 13) se añade en su propia Issue.

## Piezas

```
util/ClaveHash.kt                    Convierte contraseñas en hash y las comprueba
data/local/dao/UsuarioDao.kt         buscarPorCorreoONombreUsuario(...)
data/repositorio/AuthRepositorio.kt  iniciarSesion(...) -> ResultadoLogin
ui/auth/LoginViewModel.kt            Estado y lógica de la pantalla de login
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

## Qué NO hace todavía

- **Recordar la sesión** al cerrar la app: Issue 16.
- **Registrar usuarios:** Issue 13. Hasta entonces la tabla de usuarios está vacía y ningún login puede tener éxito
  en la app real (los tests sí crean usuarios de prueba).
- **Cerrar sesión:** Issue 17.

## Pruebas

| Test | Qué comprueba | Cómo se ejecuta |
|---|---|---|
| `ClaveHashTest` | El hash no contiene la clave, es distinto cada vez y se verifica bien | Local (sin emulador) |
| `AuthRepositorioTest` | Login por correo o nombre, mayúsculas, espacios, clave incorrecta, usuario inexistente | Local |
| `LoginViewModelTest` | Campos vacíos, errores, éxito, clave incorrecta, fallo inesperado | Local |
| `UsuarioDaoTest` | La consulta SQL real (sin distinguir mayúsculas) | Emulador o móvil |

Los tests locales se ejecutan con el botón verde junto a cada clase, o con `./gradlew testDebugUnitTest`.

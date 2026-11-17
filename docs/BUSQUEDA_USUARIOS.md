# Búsqueda de usuarios (Issue 31)

Lógica para buscar a otros usuarios por **ID** o **nombre de usuario**, sobre la base de datos local (Room).
Solo busca: la pantalla (Issue 30) y el envío de la solicitud de amistad (`AmistadRepositorio`) se enganchan a esto.

```
data/repositorio/BusquedaUsuariosRepositorio.kt   buscar(consulta, miCorreo) y UsuarioEncontrado(id, nombreUsuario)
data/local/dao/UsuarioDao.kt                      buscarPorNombreUsuarioParcial(texto, miCorreo, limite)
```

## Reglas

- El texto puede llevar una `@` delante y no distingue mayúsculas.
- Si es un número, se devuelve el usuario con ese ID y también los nombres que lo contienen (p. ej. `ana_23`).
- Si no, se devuelven los nombres que contienen el texto.
- Nunca aparece el propio usuario que busca.
- Orden: ID exacto, nombre exacto y el resto por orden alfabético. Máximo 20 resultados.
- El resultado (`UsuarioEncontrado`) solo lleva ID y nombre de usuario: no se muestra el correo de otras personas.
- El guion bajo de los nombres (`ana_23`) se busca tal cual: la consulta usa `instr` y no `LIKE`, donde `_` sería un comodín.

## Cómo usarlo

```kotlin
val busqueda = BusquedaUsuariosRepositorio(db.usuarioDao())
val resultados: List<UsuarioEncontrado> = busqueda.buscar(consulta = "@ana", miCorreo = sesion.correo)
```

## Pruebas

- `BusquedaUsuariosRepositorioTest` (sin emulador, `./gradlew test`): las reglas anteriores, con `FakeUsuarioDao`.
- `BusquedaUsuariosDaoTest` (con emulador): la consulta SQL real sobre una base en memoria.

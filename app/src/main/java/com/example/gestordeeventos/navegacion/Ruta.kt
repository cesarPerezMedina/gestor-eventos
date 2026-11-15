package com.example.gestordeeventos.navegacion

sealed class Ruta(val ruta: String) {
    data object Login : Ruta("login")
    data object Registro : Ruta("registro")
    data object Recuperar : Ruta("recuperar")
    data object Restablecer : Ruta("restablecer")
    data object Ajustes : Ruta("ajustes")
    data object Inicio : Ruta("inicio")
    data object Buscar : Ruta("buscar")
    data object Eventos : Ruta("eventos")
    data object Amigos : Ruta("amigos")

    // Otras pantallas
    data object Crear : Ruta("crear")


}
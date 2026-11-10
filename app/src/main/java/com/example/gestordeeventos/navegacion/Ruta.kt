package com.example.gestordeeventos.navegacion

sealed class Ruta(val ruta: String) {
    data object Login : Ruta("login")
    data object Registro : Ruta("registro")
}
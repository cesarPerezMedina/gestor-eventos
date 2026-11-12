package com.example.gestordeeventos.util

/** En qué punto está un permiso respecto al usuario. */
enum class EstadoPermiso {
    /** El usuario ya lo ha concedido (o la versión de Android no lo necesita). */
    CONCEDIDO,

    /** Todavía no se le ha preguntado: se puede pedir con normalidad. */
    PENDIENTE,

    /** Lo denegó antes pero se le puede volver a preguntar: conviene explicarle para qué sirve. */
    EXPLICAR,

    /** Lo denegó con "no volver a preguntar": Android ya no muestra el diálogo, hay que llevarlo a Ajustes. */
    BLOQUEADO;

    companion object {
        /**
         * Calcula el estado a partir de lo que informa Android.
         *
         * @param concedido el permiso está concedido.
         * @param debeExplicar Android indica que conviene explicar el motivo (`shouldShowRequestPermissionRationale`).
         * @param yaSolicitado la app ya ha pedido el permiso al menos una vez.
         */
        fun calcular(concedido: Boolean, debeExplicar: Boolean, yaSolicitado: Boolean): EstadoPermiso = when {
            concedido -> CONCEDIDO
            debeExplicar -> EXPLICAR
            yaSolicitado -> BLOQUEADO
            else -> PENDIENTE
        }
    }
}

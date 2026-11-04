package com.example.gestordeeventos.ui.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidacionRecuperacionTest {

    @Test fun correoVacioDaError() = assertNotNull(ValidacionRecuperacion.errorCorreo("  "))
    @Test fun correoSinArrobaDaError() = assertNotNull(ValidacionRecuperacion.errorCorreo("juan.correo.com"))
    @Test fun correoCorrectoNoDaError() = assertNull(ValidacionRecuperacion.errorCorreo("juan@correo.com"))

    @Test fun claveCortaDaError() = assertNotNull(ValidacionRecuperacion.errorClave("Ab1!"))
    @Test fun claveSinMayusculaDaError() = assertNotNull(ValidacionRecuperacion.errorClave("abcdef1!"))
    @Test fun claveSinNumeroDaError() = assertNotNull(ValidacionRecuperacion.errorClave("Abcdefg!"))
    @Test fun claveSinSimboloDaError() = assertNotNull(ValidacionRecuperacion.errorClave("Abcdefg1"))
    @Test fun claveSeguraNoDaError() = assertNull(ValidacionRecuperacion.errorClave("Abcdef1!"))

    @Test fun confirmacionDistintaDaError() =
        assertEquals("Las contraseñas no coinciden", ValidacionRecuperacion.errorConfirmacion("a", "b"))
    @Test fun confirmacionIgualNoDaError() = assertNull(ValidacionRecuperacion.errorConfirmacion("a", "a"))
}

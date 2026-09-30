package com.jluna.revisarjugadas.dominio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class QuinielaTest {

    private val sorteo = SorteoQuiniela(
        jurisdiccion = Jurisdiccion.NACIONAL,
        fecha = LocalDate.of(2026, 9, 30),
        turno = Turno.NOCTURNA,
        numeros = listOf(
            "4523", "0817", "9910", "1234", "5678", "0023", "7745", "3001", "6698", "2210",
            "8123", "4467", "0999", "3356", "7712", "1188", "5540", "9023", "6601", "2377",
        ),
    )

    @Test
    fun `multiplicadores de la tabla de pagos`() {
        assertEquals(7.0, Quiniela.multiplicador(1, 1)!!, 0.0)
        assertEquals(70.0, Quiniela.multiplicador(2, 1)!!, 0.0)
        assertEquals(14.0, Quiniela.multiplicador(2, 5)!!, 0.0)
        assertEquals(3.5, Quiniela.multiplicador(2, 20)!!, 0.0)
        assertEquals(120.0, Quiniela.multiplicador(3, 5)!!, 0.0)
        assertEquals(175.0, Quiniela.multiplicador(4, 20)!!, 0.0)
        assertNull("1 cifra solo a la cabeza", Quiniela.multiplicador(1, 5))
    }

    @Test
    fun `a la cabeza solo mira el primer numero`() {
        assertTrue(controlarQuiniela("23", 1, sorteo)!!.gano)
        assertFalse(controlarQuiniela("17", 1, sorteo)!!.gano)
    }

    @Test
    fun `a los 20 encuentra todas las posiciones`() {
        val r = controlarQuiniela("23", 20, sorteo)!!
        assertEquals(listOf(1, 6, 11, 18), r.posiciones)
        assertEquals(350.0, r.premio(100.0), 0.0)
    }

    @Test
    fun `a los 5 no cuenta la posicion 6`() {
        assertEquals(listOf(1), controlarQuiniela("23", 5, sorteo)!!.posiciones)
        assertFalse(controlarQuiniela("45", 5, sorteo)!!.gano) // el 7745 está en la posición 7
    }

    @Test
    fun `cuatro cifras exactas`() {
        assertTrue(controlarQuiniela("1234", 5, sorteo)!!.gano)
        assertFalse(controlarQuiniela("1234", 1, sorteo)!!.gano)
    }

    @Test
    fun `apuestas invalidas`() {
        assertNull(controlarQuiniela("12345", 1, sorteo))
        assertNull(controlarQuiniela("", 1, sorteo))
        assertNull(controlarQuiniela("7", 5, sorteo))
        assertNull(controlarQuiniela("12", 3, sorteo))
    }

    @Test
    fun `valida el extracto`() {
        assertEquals(emptyList<String>(), sorteo.validar())
        val mal = sorteo.copy(fecha = LocalDate.of(2026, 9, 27), numeros = sorteo.numeros.take(19) + "12a4")
        val errores = mal.validar()
        assertTrue(errores.any { "domingos" in it })
        assertTrue(errores.any { "Posición 20" in it })
    }

    @Test
    fun `proximo turno segun la hora`() {
        val miercoles = LocalDate.of(2026, 9, 30)
        assertEquals(miercoles to Turno.MATUTINA, Quiniela.proximoSorteo(miercoles.atTime(13, 0)))
        // Después de la Nocturna del sábado, el próximo es la Previa del lunes
        val sabado = LocalDate.of(2026, 10, 3)
        assertEquals(LocalDate.of(2026, 10, 5) to Turno.PREVIA, Quiniela.proximoSorteo(sabado.atTime(22, 0)))
    }

    @Test
    fun `ida y vuelta por el mapa de Firestore`() {
        assertEquals(sorteo, Mapeo.sorteoQuiniela(Mapeo.aMapa(sorteo)))
    }
}

package com.jluna.revisarjugadas.dominio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class Quini6Test {

    /** Sorteo real 3412 del domingo 27/09/2026. */
    private val sorteo3412 = SorteoQuini6(
        fecha = LocalDate.of(2026, 9, 27),
        numeroSorteo = 3412,
        tradicional = listOf(3, 4, 19, 21, 31, 36),
        segunda = listOf(3, 8, 11, 12, 14, 21),
        revancha = listOf(8, 25, 29, 34, 42, 43),
        siempreSale = listOf(1, 7, 11, 31, 32, 45),
        siempreSaleAciertos = 5,
    )

    private fun List<ResultadoModalidad>.de(m: ModalidadQuini6) = first { it.modalidad == m }

    @Test
    fun `el sorteo real es valido`() {
        assertEquals(emptyList<String>(), sorteo3412.validar())
    }

    @Test
    fun `el pozo extra coincide con el publicado`() {
        // Publicado por El Litoral: 03 04 08 11 12 14 19 21 25 29 31 34 36 42 43
        assertEquals(listOf(3, 4, 8, 11, 12, 14, 19, 21, 25, 29, 31, 34, 36, 42, 43), sorteo3412.pozoExtra)
    }

    @Test
    fun `seis aciertos en el tradicional gana y queda fuera del pozo extra`() {
        val r = controlarQuini6(listOf(3, 4, 19, 21, 31, 36), true, true, sorteo3412)
        assertTrue(r.de(ModalidadQuini6.TRADICIONAL).premiada)
        assertEquals(6, r.de(ModalidadQuini6.POZO_EXTRA).aciertos.size)
        assertFalse(r.de(ModalidadQuini6.POZO_EXTRA).premiada)
    }

    @Test
    fun `pozo extra con numeros de la revancha aunque no la haya jugado`() {
        val r = controlarQuini6(listOf(3, 25, 29, 34, 42, 43), juegaRevancha = false, juegaSiempreSale = false, sorteo3412)
        assertTrue(r.de(ModalidadQuini6.POZO_EXTRA).premiada)
        assertFalse(r.de(ModalidadQuini6.REVANCHA).participa)
        assertFalse(r.de(ModalidadQuini6.REVANCHA).premiada)
    }

    @Test
    fun `cuatro aciertos en la segunda cobra, tres no`() {
        val cuatro = controlarQuini6(listOf(3, 8, 11, 12, 40, 41), false, false, sorteo3412)
        assertTrue(cuatro.de(ModalidadQuini6.SEGUNDA).premiada)
        val tres = controlarQuini6(listOf(3, 8, 11, 39, 40, 41), false, false, sorteo3412)
        assertFalse(tres.de(ModalidadQuini6.SEGUNDA).premiada)
    }

    @Test
    fun `siempre sale paga con los aciertos del sorteo solo si se jugo`() {
        val numeros = listOf(1, 7, 11, 31, 32, 44) // 5 aciertos; ese domingo pagó a 5
        assertTrue(controlarQuini6(numeros, false, true, sorteo3412).de(ModalidadQuini6.SIEMPRE_SALE).premiada)
        assertFalse(controlarQuini6(numeros, false, false, sorteo3412).de(ModalidadQuini6.SIEMPRE_SALE).premiada)
    }

    @Test
    fun `detecta sorteos mal cargados`() {
        val mal = sorteo3412.copy(fecha = LocalDate.of(2026, 9, 28), tradicional = listOf(3, 3, 50, 1, 2))
        val errores = mal.validar()
        assertTrue(errores.any { "miércoles" in it })
        assertTrue(errores.any { "6 números" in it })
        assertTrue(errores.any { "repetidos" in it })
        assertTrue(errores.any { "50" in it })
    }

    @Test
    fun `ida y vuelta por el mapa de Firestore`() {
        // Firestore devuelve Long: simulamos eso
        val mapa = Mapeo.aMapa(sorteo3412).mapValues { (_, v) ->
            when (v) {
                is List<*> -> v.map { (it as Int).toLong() }
                is Int -> v.toLong()
                else -> v
            }
        }
        assertEquals(sorteo3412, Mapeo.sorteoQuini6(mapa))
    }
}

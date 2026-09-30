package com.jluna.revisarjugadas.data.quini6

import java.time.LocalDate

/**
 * Una boleta de Quini 6. Los mismos 6 números juegan en todas las modalidades:
 * Tradicional (Primer Sorteo y La Segunda) siempre, y Revancha / Siempre Sale si se pagaron.
 */
data class JugadaQuini6(
    val id: String? = null,
    val fechaSorteo: LocalDate,
    val numeros: List<Int>,
    val revancha: Boolean,
    val siempreSale: Boolean,
) {
    companion object {
        const val CANTIDAD_NUMEROS = 6
        val RANGO_NUMEROS = 0..45
    }
}

/** Formato de dos cifras con que se muestran los números del Quini 6 (00 a 45). */
fun Int.comoBolilla(): String = toString().padStart(2, '0')

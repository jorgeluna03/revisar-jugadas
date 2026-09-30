package com.jluna.revisarjugadas.data.jugadas

import com.jluna.revisarjugadas.dominio.Jurisdiccion
import com.jluna.revisarjugadas.dominio.Turno
import com.jluna.revisarjugadas.dominio.idSorteoQuini6
import com.jluna.revisarjugadas.dominio.idSorteoQuiniela
import java.time.LocalDate

/** Una jugada guardada por el usuario. */
sealed interface Jugada {
    val id: String?
    val fechaSorteo: LocalDate

    /** Ids de los sorteos (en `sorteos/`) contra los que se controla esta jugada. */
    val idsSorteos: List<String>
}

/**
 * Una boleta de Quini 6. Los mismos 6 números juegan en todas las modalidades:
 * Tradicional (Primer Sorteo y La Segunda) y Pozo Extra siempre; Revancha y Siempre Sale si se pagaron.
 */
data class JugadaQuini6(
    override val id: String? = null,
    override val fechaSorteo: LocalDate,
    val numeros: List<Int>,
    val revancha: Boolean,
    val siempreSale: Boolean,
) : Jugada {
    override val idsSorteos get() = listOf(idSorteoQuini6(fechaSorteo))
}

/**
 * Una apuesta de Quiniela: un número de 1 a 4 cifras, a la cabeza o a los 5 / 10 / 20,
 * en un turno y en una o varias jurisdicciones (se controla en cada una).
 */
data class JugadaQuiniela(
    override val id: String? = null,
    override val fechaSorteo: LocalDate,
    val turno: Turno,
    val jurisdicciones: List<Jurisdiccion>,
    val numero: String,
    val ubicacion: Int,
    /** Lo apostado en cada jurisdicción, si el usuario lo cargó (sirve para estimar el premio). */
    val importe: Double?,
) : Jugada {
    override val idsSorteos get() = jurisdicciones.map { idSorteoQuiniela(it, fechaSorteo, turno) }
}

/** Formato de dos cifras con que se muestran los números del Quini 6 (00 a 45). */
fun Int.comoBolilla(): String = toString().padStart(2, '0')

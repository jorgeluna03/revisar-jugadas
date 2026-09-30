package com.jluna.revisarjugadas.dominio

import java.time.LocalDate

/**
 * Resultado de un sorteo de Quini 6.
 * Reglas: "Características del juego Quini 6" (Lotería de Santa Fe). Ver PLAN.md.
 */
data class SorteoQuini6(
    val fecha: LocalDate,
    val numeroSorteo: Int?,
    val tradicional: List<Int>,
    val segunda: List<Int>,
    val revancha: List<Int>,
    val siempreSale: List<Int>,
    /** Con cuántos aciertos se pagó el Siempre Sale (6, si no hubo ganadores 5, y así). */
    val siempreSaleAciertos: Int,
) {
    val id: String get() = idSorteoQuini6(fecha)

    /** Los números distintos de Tradicional, La Segunda y Revancha (hasta 18). */
    val pozoExtra: List<Int> get() = (tradicional + segunda + revancha).distinct().sorted()

    /** Devuelve la lista de problemas encontrados (vacía si el sorteo es válido). */
    fun validar(): List<String> = buildList {
        if (!CalendarioQuini6.esDiaDeSorteo(fecha)) add("El $fecha no es miércoles ni domingo")
        validarExtraccion("Tradicional", tradicional)
        validarExtraccion("La Segunda", segunda)
        validarExtraccion("Revancha", revancha)
        validarExtraccion("Siempre Sale", siempreSale)
        if (siempreSaleAciertos !in 1..6) add("Los aciertos del Siempre Sale tienen que estar entre 1 y 6")
    }

    private fun MutableList<String>.validarExtraccion(nombre: String, numeros: List<Int>) {
        if (numeros.size != Quini6.CANTIDAD_NUMEROS) add("$nombre: tiene que tener 6 números (tiene ${numeros.size})")
        if (numeros.toSet().size != numeros.size) add("$nombre: hay números repetidos")
        numeros.filter { it !in Quini6.RANGO_NUMEROS }.forEach { add("$nombre: el $it no está entre 00 y 45") }
    }
}

object Quini6 {
    const val CANTIDAD_NUMEROS = 6
    val RANGO_NUMEROS = 0..45
}

fun idSorteoQuini6(fecha: LocalDate) = "QUINI6-$fecha"

enum class ModalidadQuini6(val nombre: String) {
    TRADICIONAL("Tradicional"),
    SEGUNDA("La Segunda"),
    REVANCHA("Revancha"),
    SIEMPRE_SALE("Siempre Sale"),
    POZO_EXTRA("Pozo Extra"),
}

data class ResultadoModalidad(
    val modalidad: ModalidadQuini6,
    /** Los números de la jugada que salieron en esta modalidad. */
    val aciertos: List<Int>,
    /** false si la boleta no incluía esta modalidad (Revancha o Siempre Sale sin pagar). */
    val participa: Boolean,
    val premiada: Boolean,
)

/** Controla una jugada de Quini 6 contra un sorteo, modalidad por modalidad. */
fun controlarQuini6(
    numeros: List<Int>,
    juegaRevancha: Boolean,
    juegaSiempreSale: Boolean,
    sorteo: SorteoQuini6,
): List<ResultadoModalidad> {
    fun aciertos(extraccion: List<Int>) = numeros.filter { it in extraccion }.sorted()

    val tradicional = aciertos(sorteo.tradicional)
    val segunda = aciertos(sorteo.segunda)
    val revancha = aciertos(sorteo.revancha)
    val siempreSale = aciertos(sorteo.siempreSale)
    val pozoExtra = aciertos(sorteo.pozoExtra)

    // El Pozo Extra excluye a quien ya acertó 6 en alguna modalidad que jugó
    val yaGanoConSeis = tradicional.size == 6 || segunda.size == 6 || (juegaRevancha && revancha.size == 6)

    return listOf(
        ResultadoModalidad(ModalidadQuini6.TRADICIONAL, tradicional, participa = true, premiada = tradicional.size >= 4),
        ResultadoModalidad(ModalidadQuini6.SEGUNDA, segunda, participa = true, premiada = segunda.size >= 4),
        ResultadoModalidad(
            ModalidadQuini6.REVANCHA, revancha,
            participa = juegaRevancha,
            premiada = juegaRevancha && revancha.size == 6,
        ),
        ResultadoModalidad(
            ModalidadQuini6.SIEMPRE_SALE, siempreSale,
            participa = juegaSiempreSale,
            premiada = juegaSiempreSale && siempreSale.size >= sorteo.siempreSaleAciertos,
        ),
        // Participan todas las boletas, sin apuesta adicional
        ResultadoModalidad(
            ModalidadQuini6.POZO_EXTRA, pozoExtra,
            participa = true,
            premiada = pozoExtra.size == 6 && !yaGanoConSeis,
        ),
    )
}

package com.jluna.revisarjugadas.data.quini6

import java.time.DayOfWeek
import java.time.LocalDate

/** El Quini 6 se sortea los miércoles y los domingos. */
object CalendarioQuini6 {

    private val diasDeSorteo = setOf(DayOfWeek.WEDNESDAY, DayOfWeek.SUNDAY)

    fun esDiaDeSorteo(fecha: LocalDate) = fecha.dayOfWeek in diasDeSorteo

    /** El próximo sorteo, contando hoy si hoy hay sorteo. */
    fun proximoSorteo(hoy: LocalDate = LocalDate.now()): LocalDate =
        generateSequence(hoy) { it.plusDays(1) }.first(::esDiaDeSorteo)

    /** Fechas para elegir al cargar una jugada: algunos sorteos pasados y los próximos, en orden. */
    fun fechasParaElegir(
        hoy: LocalDate = LocalDate.now(),
        anteriores: Int = 4,
        siguientes: Int = 3,
    ): List<LocalDate> {
        val proximo = proximoSorteo(hoy)
        val pasados = generateSequence(proximo.minusDays(1)) { it.minusDays(1) }
            .filter(::esDiaDeSorteo)
            .take(anteriores)
            .toList()
            .reversed()
        val futuros = generateSequence(proximo) { it.plusDays(1) }
            .filter(::esDiaDeSorteo)
            .take(siguientes)
            .toList()
        return pasados + futuros
    }
}

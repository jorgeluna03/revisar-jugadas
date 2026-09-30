package com.jluna.revisarjugadas.dominio

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Temas y textos de las notificaciones de resultados. La app se suscribe a los temas
 * y la herramienta admin envía los avisos: los dos usan esta definición.
 */
object Avisos {
    const val TEMA_QUINI6 = "quini6"

    fun temaQuiniela(jurisdiccion: Jurisdiccion) = "quiniela-${jurisdiccion.name}"

    data class Aviso(val tema: String, val titulo: String, val texto: String)

    fun quini6(sorteo: SorteoQuini6, corregido: Boolean = false) = Aviso(
        tema = TEMA_QUINI6,
        titulo = if (corregido) "Quini 6: resultado corregido" else "¡Sorteo finalizado!",
        texto = "Ya están los números del Quini 6 del ${dia(sorteo.fecha)}. Revisá tus jugadas.",
    )

    fun quiniela(sorteo: SorteoQuiniela, corregido: Boolean = false) = Aviso(
        tema = temaQuiniela(sorteo.jurisdiccion),
        titulo = "Quiniela ${sorteo.jurisdiccion.nombre} · ${sorteo.turno.nombre}" +
            if (corregido) " (corregida)" else "",
        texto = "Salió el ${sorteo.numeros.first()} a la cabeza. Revisá tus jugadas.",
    )

    private val formatoDia = DateTimeFormatter.ofPattern("EEEE d/M", Locale.forLanguageTag("es-AR"))

    /** "domingo 27/9" */
    private fun dia(fecha: LocalDate) = fecha.format(formatoDia)
}

package com.jluna.revisarjugadas.dominio

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class Jurisdiccion(val nombre: String) {
    NACIONAL("Nacional"),
    BUENOS_AIRES("Buenos Aires"),
    CORDOBA("Córdoba"),
    SANTA_FE("Santa Fe"),
}

/** Turnos de lunes a sábado. Los horarios son los mismos en las cuatro jurisdicciones. */
enum class Turno(val nombre: String, val hora: String) {
    PREVIA("La Previa", "10:15"),
    PRIMERA("La Primera", "12:00"),
    MATUTINA("Matutina", "15:00"),
    VESPERTINA("Vespertina", "18:00"),
    NOCTURNA("Nocturna", "21:00"),
}

object Quiniela {
    const val CANTIDAD_NUMEROS = 20

    /** A cuántas posiciones se puede apostar: a la cabeza, a los 5, a los 10 o a los 20. */
    val UBICACIONES = listOf(1, 5, 10, 20)

    fun esDiaDeSorteo(fecha: LocalDate) = fecha.dayOfWeek != DayOfWeek.SUNDAY

    /** El próximo turno que todavía no se sorteó, a partir de [ahora]. */
    fun proximoSorteo(ahora: LocalDateTime = LocalDateTime.now()): Pair<LocalDate, Turno> {
        val hoy = ahora.toLocalDate()
        if (esDiaDeSorteo(hoy)) {
            Turno.entries.firstOrNull { ahora.toLocalTime() < LocalTime.parse(it.hora) }?.let { return hoy to it }
        }
        val siguiente = generateSequence(hoy.plusDays(1)) { it.plusDays(1) }.first(::esDiaDeSorteo)
        return siguiente to Turno.PREVIA
    }

    /** Días con sorteo para elegir al cargar una apuesta: algunos anteriores y los próximos. */
    fun fechasParaElegir(hoy: LocalDate = LocalDate.now(), anteriores: Int = 3, siguientes: Int = 3): List<LocalDate> {
        val pasados = generateSequence(hoy.minusDays(1)) { it.minusDays(1) }
            .filter(::esDiaDeSorteo).take(anteriores).toList().reversed()
        val futuros = generateSequence(hoy) { it.plusDays(1) }
            .filter(::esDiaDeSorteo).take(siguientes).toList()
        return pasados + futuros
    }

    /**
     * Cuántas veces lo apostado paga un acierto. A la cabeza: 1 cifra ×7, 2 ×70, 3 ×600, 4 ×3500;
     * a los 5, 10 o 20 se divide por la ubicación (ej. 2 cifras a los 5 = ×14).
     * La apuesta a 1 cifra solo se juega a la cabeza.
     */
    fun multiplicador(cifras: Int, ubicacion: Int): Double? {
        val cabeza = when (cifras) {
            1 -> 7.0
            2 -> 70.0
            3 -> 600.0
            4 -> 3500.0
            else -> return null
        }
        if (ubicacion !in UBICACIONES || (cifras == 1 && ubicacion != 1)) return null
        return cabeza / ubicacion
    }
}

fun idSorteoQuiniela(jurisdiccion: Jurisdiccion, fecha: LocalDate, turno: Turno) =
    "QUINIELA-${jurisdiccion.name}-$fecha-${turno.name}"

/** Los 20 números de un sorteo de Quiniela, en orden de extracción (posición 1 = la cabeza). */
data class SorteoQuiniela(
    val jurisdiccion: Jurisdiccion,
    val fecha: LocalDate,
    val turno: Turno,
    val numeros: List<String>,
) {
    val id: String get() = idSorteoQuiniela(jurisdiccion, fecha, turno)

    fun validar(): List<String> = buildList {
        if (!Quiniela.esDiaDeSorteo(fecha)) add("Los domingos no hay Quiniela")
        if (numeros.size != Quiniela.CANTIDAD_NUMEROS) add("Tiene que tener 20 números (tiene ${numeros.size})")
        numeros.withIndex()
            .filterNot { (_, n) -> n.length == 4 && n.all(Char::isDigit) }
            .forEach { (i, n) -> add("Posición ${i + 1}: \"$n\" no es un número de 4 cifras") }
    }
}

data class ResultadoQuiniela(
    /** Posiciones (1 a 20) dentro de la ubicación apostada donde salió el número. */
    val posiciones: List<Int>,
    val multiplicador: Double,
) {
    val gano: Boolean get() = posiciones.isNotEmpty()

    /** Premio estimado para un importe apostado (se cobra una vez aunque salga repetido). */
    fun premio(importe: Double): Double = if (gano) importe * multiplicador else 0.0
}

/**
 * Controla una apuesta de Quiniela: gana si alguno de los primeros [ubicacion] números
 * termina con [numero] (de 1 a 4 cifras). Devuelve null si la apuesta no es válida.
 */
fun controlarQuiniela(numero: String, ubicacion: Int, sorteo: SorteoQuiniela): ResultadoQuiniela? {
    if (numero.isEmpty() || numero.length > 4 || !numero.all(Char::isDigit)) return null
    val multiplicador = Quiniela.multiplicador(numero.length, ubicacion) ?: return null
    val posiciones = sorteo.numeros.take(ubicacion).withIndex()
        .filter { (_, salido) -> salido.endsWith(numero) }
        .map { (i, _) -> i + 1 }
    return ResultadoQuiniela(posiciones, multiplicador)
}

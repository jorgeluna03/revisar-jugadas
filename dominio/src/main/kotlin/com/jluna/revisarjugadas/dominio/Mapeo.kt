package com.jluna.revisarjugadas.dominio

import java.time.LocalDate

/**
 * Conversión de los sorteos a/desde mapas, el formato que usan tanto el SDK de Firebase
 * para Android como el Admin SDK. Así el formato en Firestore se define en un solo lugar.
 *
 * Colección: `sorteos/{id}`. Los ids se arman con [idSorteoQuini6] e [idSorteoQuiniela].
 */
object Mapeo {
    const val JUEGO_QUINI6 = "QUINI6"
    const val JUEGO_QUINIELA = "QUINIELA"

    fun aMapa(s: SorteoQuini6): Map<String, Any?> = mapOf(
        "juego" to JUEGO_QUINI6,
        "fecha" to s.fecha.toString(),
        "numeroSorteo" to s.numeroSorteo,
        "tradicional" to s.tradicional,
        "segunda" to s.segunda,
        "revancha" to s.revancha,
        "siempreSale" to s.siempreSale,
        "siempreSaleAciertos" to s.siempreSaleAciertos,
        "pozoExtra" to s.pozoExtra,
    )

    fun aMapa(s: SorteoQuiniela): Map<String, Any?> = mapOf(
        "juego" to JUEGO_QUINIELA,
        "fecha" to s.fecha.toString(),
        "jurisdiccion" to s.jurisdiccion.name,
        "turno" to s.turno.name,
        "numeros" to s.numeros,
    )

    fun sorteoQuini6(m: Map<String, Any?>): SorteoQuini6? = runCatching {
        if (m["juego"] != JUEGO_QUINI6) return null
        SorteoQuini6(
            fecha = LocalDate.parse(m["fecha"] as String),
            numeroSorteo = (m["numeroSorteo"] as? Number)?.toInt(),
            tradicional = enteros(m["tradicional"]),
            segunda = enteros(m["segunda"]),
            revancha = enteros(m["revancha"]),
            siempreSale = enteros(m["siempreSale"]),
            siempreSaleAciertos = (m["siempreSaleAciertos"] as Number).toInt(),
        )
    }.getOrNull()

    fun sorteoQuiniela(m: Map<String, Any?>): SorteoQuiniela? = runCatching {
        if (m["juego"] != JUEGO_QUINIELA) return null
        SorteoQuiniela(
            jurisdiccion = Jurisdiccion.valueOf(m["jurisdiccion"] as String),
            fecha = LocalDate.parse(m["fecha"] as String),
            turno = Turno.valueOf(m["turno"] as String),
            numeros = (m["numeros"] as List<*>).map { it as String },
        )
    }.getOrNull()

    // Firestore devuelve los enteros como Long
    private fun enteros(valor: Any?): List<Int> = (valor as List<*>).map { (it as Number).toInt() }
}

package com.jluna.revisarjugadas.admin

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.int
import com.jluna.revisarjugadas.dominio.Avisos
import com.jluna.revisarjugadas.dominio.Jurisdiccion
import com.jluna.revisarjugadas.dominio.Mapeo
import com.jluna.revisarjugadas.dominio.SorteoQuini6
import com.jluna.revisarjugadas.dominio.SorteoQuiniela
import com.jluna.revisarjugadas.dominio.Turno
import com.jluna.revisarjugadas.dominio.idSorteoQuini6
import com.jluna.revisarjugadas.dominio.idSorteoQuiniela
import java.time.LocalDate

/**
 * Carga resultados de sorteos en Firestore y avisa a los usuarios.
 *
 * Ejemplos (desde la raíz del proyecto):
 *   ./gradlew -q :admin:run --args="quini6 --fecha 2026-09-27 --sorteo 3412 --tradicional 3,4,19,21,31,36 ..."
 *   ./gradlew -q :admin:run --args="quiniela --fecha 2026-09-30 --jurisdiccion NACIONAL --turno NOCTURNA --numeros 4523,0817,..."
 *   ./gradlew -q :admin:run --args="ver --fecha 2026-09-30"
 *
 * Sin --confirmar solo muestra lo que cargaría (no escribe nada ni avisa).
 */
fun main(args: Array<String>) = Admin()
    .subcommands(CargarQuini6(), CargarQuiniela(), Ver(), Avisar())
    .main(args)

class Admin : CliktCommand(name = "admin") {
    private val credenciales by option(
        "--credenciales",
        help = "Archivo JSON de la cuenta de servicio de Firebase (por defecto: GOOGLE_APPLICATION_CREDENTIALS o admin/credenciales.json)",
    )

    override fun help(context: Context) = "Carga resultados de Quini 6 y Quiniela en Firestore."

    override fun run() {
        currentContext.findOrSetObject { Firestore(credenciales) }
    }
}

class CargarQuini6 : CliktCommand(name = "quini6") {
    private val db by requireObject<Firestore>()
    private val fecha by option("--fecha", help = "AAAA-MM-DD").convert { LocalDate.parse(it) }.required()
    private val numeroSorteo by option("--sorteo", help = "Número de sorteo, ej. 3412").int()
    private val tradicional by option("--tradicional").convert { numeros(it) }.required()
    private val segunda by option("--segunda").convert { numeros(it) }.required()
    private val revancha by option("--revancha").convert { numeros(it) }.required()
    private val siempreSale by option("--siempre-sale").convert { numeros(it) }.required()
    private val siempreSaleAciertos by option("--siempre-sale-aciertos", help = "Con cuántos aciertos pagó").int().required()
    private val confirmar by option("--confirmar", help = "Escribir en Firestore y avisar").flag()
    private val reemplazar by option("--reemplazar", help = "Permitir pisar un resultado ya cargado").flag()
    private val sinAviso by option("--sin-aviso", help = "Cargar sin enviar la notificación").flag()

    override fun help(context: Context) = "Carga el resultado de un sorteo de Quini 6."

    override fun run() {
        val sorteo = SorteoQuini6(fecha, numeroSorteo, tradicional, segunda, revancha, siempreSale, siempreSaleAciertos)
        fallarSiHayErrores(sorteo.validar())

        echo("Quini 6 del ${sorteo.fecha}" + (numeroSorteo?.let { " (sorteo $it)" } ?: ""))
        echo("  Tradicional:   ${bolillas(sorteo.tradicional)}")
        echo("  La Segunda:    ${bolillas(sorteo.segunda)}")
        echo("  Revancha:      ${bolillas(sorteo.revancha)}")
        echo("  Siempre Sale:  ${bolillas(sorteo.siempreSale)}  (pagó a $siempreSaleAciertos aciertos)")
        echo("  Pozo Extra:    ${bolillas(sorteo.pozoExtra)}")

        val resultado = guardar(db, sorteo.id, Mapeo.aMapa(sorteo), confirmar, reemplazar)
        if (resultado.escribio && !sinAviso) avisar(db, Avisos.quini6(sorteo, corregido = resultado == Guardado.REEMPLAZADO))
    }
}

class CargarQuiniela : CliktCommand(name = "quiniela") {
    private val db by requireObject<Firestore>()
    private val fecha by option("--fecha", help = "AAAA-MM-DD").convert { LocalDate.parse(it) }.required()
    private val jurisdiccion by option("--jurisdiccion").enum<Jurisdiccion>().required()
    private val turno by option("--turno").enum<Turno>().required()
    private val numeros by option("--numeros", help = "Los 20 números en orden, separados por comas o espacios")
        .convert { texto -> texto.split(Regex("[^0-9]+")).filter { it.isNotEmpty() } }
        .required()
    private val confirmar by option("--confirmar", help = "Escribir en Firestore y avisar").flag()
    private val reemplazar by option("--reemplazar", help = "Permitir pisar un resultado ya cargado").flag()
    private val sinAviso by option("--sin-aviso", help = "Cargar sin enviar la notificación").flag()

    override fun help(context: Context) = "Carga el extracto (20 números) de un sorteo de Quiniela."

    override fun run() {
        val sorteo = SorteoQuiniela(jurisdiccion, fecha, turno, numeros)
        fallarSiHayErrores(sorteo.validar())

        echo("Quiniela ${jurisdiccion.nombre} · ${turno.nombre} del $fecha")
        sorteo.numeros.chunked(5).forEachIndexed { fila, cinco ->
            echo("  " + cinco.withIndex().joinToString("   ") { (i, n) -> "${(fila * 5 + i + 1).toString().padStart(2)}. $n" })
        }

        val resultado = guardar(db, sorteo.id, Mapeo.aMapa(sorteo), confirmar, reemplazar)
        if (resultado.escribio && !sinAviso) avisar(db, Avisos.quiniela(sorteo, corregido = resultado == Guardado.REEMPLAZADO))
    }
}

class Ver : CliktCommand(name = "ver") {
    private val db by requireObject<Firestore>()
    private val fecha by option("--fecha", help = "AAAA-MM-DD (por defecto hoy)")
        .convert { LocalDate.parse(it) }
        .default(LocalDate.now())
    private val juego by option("--juego").choice("todos", "quini6", "quiniela").default("todos")

    override fun help(context: Context) = "Muestra los resultados cargados para una fecha."

    override fun run() {
        val sorteos = db.sorteosDelDia(fecha)
            .filter { juego == "todos" || it["juego"] == juego.uppercase() }
        if (sorteos.isEmpty()) {
            echo("No hay resultados cargados para el $fecha")
            return
        }
        sorteos.forEach { m ->
            Mapeo.sorteoQuini6(m)?.let { echo("Quini 6: Tradicional ${bolillas(it.tradicional)} · Segunda ${bolillas(it.segunda)} · Revancha ${bolillas(it.revancha)} · Siempre Sale ${bolillas(it.siempreSale)}") }
            Mapeo.sorteoQuiniela(m)?.let { echo("Quiniela ${it.jurisdiccion.nombre} ${it.turno.nombre}: cabeza ${it.numeros.first()} · ${it.numeros.joinToString(" ")}") }
        }
    }
}

/** Reenvía el aviso de un resultado ya cargado (por ejemplo, para probar las notificaciones). */
class Avisar : CliktCommand(name = "avisar") {
    private val db by requireObject<Firestore>()
    private val fecha by option("--fecha", help = "AAAA-MM-DD").convert { LocalDate.parse(it) }.required()
    private val juego by option("--juego").choice("quini6", "quiniela").required()
    private val jurisdiccion by option("--jurisdiccion", help = "Solo para quiniela").enum<Jurisdiccion>()
    private val turno by option("--turno", help = "Solo para quiniela").enum<Turno>()

    override fun help(context: Context) = "Reenvía la notificación de un resultado ya cargado."

    override fun run() {
        val aviso = if (juego == "quini6") {
            val datos = db.leer(idSorteoQuini6(fecha)) ?: throw CliktError("No hay un Quini 6 cargado para el $fecha")
            Avisos.quini6(Mapeo.sorteoQuini6(datos)!!)
        } else {
            val j = jurisdiccion ?: throw CliktError("Falta --jurisdiccion")
            val t = turno ?: throw CliktError("Falta --turno")
            val datos = db.leer(idSorteoQuiniela(j, fecha, t)) ?: throw CliktError("No está cargada esa quiniela")
            Avisos.quiniela(Mapeo.sorteoQuiniela(datos)!!)
        }
        avisar(db, aviso)
    }
}

private enum class Guardado(val escribio: Boolean) { SIN_CAMBIOS(false), VISTA_PREVIA(false), NUEVO(true), REEMPLAZADO(true) }

private fun guardar(db: Firestore, id: String, datos: Map<String, Any?>, confirmar: Boolean, reemplazar: Boolean): Guardado {
    val existente = db.leer(id)
    if (existente != null) {
        val igual = datos.all { (k, v) -> normalizar(existente[k]) == normalizar(v) }
        if (igual) {
            println("\nYa estaba cargado con estos mismos números ($id). No hay nada que hacer.")
            return Guardado.SIN_CAMBIOS
        }
        if (!reemplazar) throw CliktError("\nYa hay otro resultado cargado para $id. Revisalo con \"ver\" y usá --reemplazar si querés pisarlo.")
    }
    if (!confirmar) {
        println("\nVista previa: no se escribió nada. Agregá --confirmar para cargarlo.")
        return Guardado.VISTA_PREVIA
    }
    db.guardar(id, datos)
    println("\n✔ Cargado en Firestore: sorteos/$id" + if (existente != null) " (reemplazado)" else "")
    return if (existente != null) Guardado.REEMPLAZADO else Guardado.NUEVO
}

private fun avisar(db: Firestore, aviso: Avisos.Aviso) {
    db.enviar(aviso)
    println("✔ Aviso enviado al tema \"${aviso.tema}\": ${aviso.titulo} — ${aviso.texto}")
}

private fun fallarSiHayErrores(errores: List<String>) {
    if (errores.isNotEmpty()) throw CliktError("Datos inválidos:\n" + errores.joinToString("\n") { "  - $it" })
}

private fun numeros(texto: String): List<Int> = texto.split(Regex("[^0-9]+")).filter { it.isNotEmpty() }.map { it.toInt() }

private fun bolillas(numeros: List<Int>) = numeros.joinToString(" ") { it.toString().padStart(2, '0') }

// Firestore devuelve Long donde nosotros guardamos Int
private fun normalizar(v: Any?): Any? = when (v) {
    is Number -> v.toLong()
    is List<*> -> v.map(::normalizar)
    else -> v
}

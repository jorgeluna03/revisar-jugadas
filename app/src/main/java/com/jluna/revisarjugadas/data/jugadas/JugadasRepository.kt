package com.jluna.revisarjugadas.data.jugadas

import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.snapshots
import com.jluna.revisarjugadas.dominio.Jurisdiccion
import com.jluna.revisarjugadas.dominio.Mapeo
import com.jluna.revisarjugadas.dominio.Turno
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

/**
 * Jugadas del usuario, guardadas en `usuarios/{uid}/jugadas/{id}`.
 * Firestore guarda una copia local, así que funciona también sin conexión.
 */
class JugadasRepository(
    uid: String,
    db: FirebaseFirestore = Firebase.firestore,
) {
    private val coleccion = db.collection("usuarios").document(uid).collection("jugadas")

    /** Todas las jugadas, de la fecha más nueva a la más vieja. */
    val jugadas: Flow<List<Jugada>> = coleccion.snapshots().map { snapshot ->
        snapshot.documents
            .mapNotNull { it.aJugada() }
            .sortedWith(compareByDescending<Jugada> { it.fechaSorteo }.thenBy { it.id })
    }

    /** Crea la jugada si no tiene id, o la reemplaza si ya existía. */
    suspend fun guardar(jugada: Jugada) {
        val datos = when (jugada) {
            is JugadaQuini6 -> mapOf(
                "juego" to Mapeo.JUEGO_QUINI6,
                "numeros" to jugada.numeros.sorted(),
                "revancha" to jugada.revancha,
                "siempreSale" to jugada.siempreSale,
            )
            is JugadaQuiniela -> buildMap {
                put("juego", Mapeo.JUEGO_QUINIELA)
                put("turno", jugada.turno.name)
                put("jurisdicciones", jugada.jurisdicciones.map { it.name })
                put("numero", jugada.numero)
                put("ubicacion", jugada.ubicacion)
                jugada.importe?.let { put("importe", it) }
            }
        } + mapOf(
            "fechaSorteo" to jugada.fechaSorteo.toString(),
            "actualizada" to FieldValue.serverTimestamp(),
        )
        val documento = jugada.id?.let(coleccion::document) ?: coleccion.document()
        documento.set(datos).await()
    }

    suspend fun borrar(id: String) {
        coleccion.document(id).delete().await()
    }

    private fun DocumentSnapshot.aJugada(): Jugada? = runCatching {
        val fecha = LocalDate.parse(getString("fechaSorteo"))
        when (getString("juego")) {
            Mapeo.JUEGO_QUINI6 -> JugadaQuini6(
                id = id,
                fechaSorteo = fecha,
                numeros = (get("numeros") as List<*>).map { (it as Number).toInt() },
                revancha = getBoolean("revancha") ?: false,
                siempreSale = getBoolean("siempreSale") ?: false,
            )
            Mapeo.JUEGO_QUINIELA -> JugadaQuiniela(
                id = id,
                fechaSorteo = fecha,
                turno = Turno.valueOf(getString("turno")!!),
                jurisdicciones = (get("jurisdicciones") as List<*>).map { Jurisdiccion.valueOf(it as String) },
                numero = getString("numero")!!,
                ubicacion = getLong("ubicacion")!!.toInt(),
                importe = getDouble("importe"),
            )
            else -> null
        }
    }.getOrNull() // un documento con formato inesperado no rompe la lista
}

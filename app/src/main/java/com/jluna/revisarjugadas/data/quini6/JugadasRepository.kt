package com.jluna.revisarjugadas.data.quini6

import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.snapshots
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

    /** Las jugadas de Quini 6, de la fecha más nueva a la más vieja. */
    val jugadasQuini6: Flow<List<JugadaQuini6>> = coleccion
        .whereEqualTo(CAMPO_JUEGO, JUEGO_QUINI6)
        .snapshots()
        .map { snapshot ->
            snapshot.documents
                .mapNotNull { it.aJugadaQuini6() }
                .sortedWith(compareByDescending<JugadaQuini6> { it.fechaSorteo }.thenBy { it.id })
        }

    /** Crea la jugada si no tiene id, o la reemplaza si ya existía. */
    suspend fun guardar(jugada: JugadaQuini6) {
        val datos = mapOf(
            CAMPO_JUEGO to JUEGO_QUINI6,
            "fechaSorteo" to jugada.fechaSorteo.toString(),
            "numeros" to jugada.numeros.sorted(),
            "revancha" to jugada.revancha,
            "siempreSale" to jugada.siempreSale,
            "actualizada" to FieldValue.serverTimestamp(),
        )
        val documento = if (jugada.id == null) coleccion.document() else coleccion.document(jugada.id)
        documento.set(datos).await()
    }

    suspend fun borrar(id: String) {
        coleccion.document(id).delete().await()
    }

    private fun DocumentSnapshot.aJugadaQuini6(): JugadaQuini6? {
        val fecha = getString("fechaSorteo")?.let(LocalDate::parse) ?: return null
        val numeros = (get("numeros") as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: return null
        return JugadaQuini6(
            id = id,
            fechaSorteo = fecha,
            numeros = numeros,
            revancha = getBoolean("revancha") ?: false,
            siempreSale = getBoolean("siempreSale") ?: false,
        )
    }

    private companion object {
        const val CAMPO_JUEGO = "juego"
        const val JUEGO_QUINI6 = "QUINI6"
    }
}

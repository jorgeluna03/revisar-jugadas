package com.jluna.revisarjugadas.data.sorteos

import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.snapshots
import com.jluna.revisarjugadas.dominio.Mapeo
import com.jluna.revisarjugadas.dominio.SorteoQuini6
import com.jluna.revisarjugadas.dominio.SorteoQuiniela
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/** Resultados publicados, en `sorteos/{id}` (solo lectura para la app). */
class SorteosRepository(db: FirebaseFirestore = Firebase.firestore) {

    private val coleccion = db.collection("sorteos")

    /** Los últimos sorteos de Quini 6 cargados, del más nuevo al más viejo. */
    fun quini6Recientes(cantidad: Int = 10): Flow<List<SorteoQuini6>> =
        coleccion.whereEqualTo("juego", Mapeo.JUEGO_QUINI6).snapshots().map { snapshot ->
            snapshot.mapas().mapNotNull(Mapeo::sorteoQuini6).sortedByDescending { it.fecha }.take(cantidad)
        }

    /** Todos los sorteos de Quiniela cargados para una fecha. */
    fun quinielasDelDia(fecha: LocalDate): Flow<List<SorteoQuiniela>> =
        coleccion
            .whereEqualTo("juego", Mapeo.JUEGO_QUINIELA)
            .whereEqualTo("fecha", fecha.toString())
            .snapshots()
            .map { snapshot -> snapshot.mapas().mapNotNull(Mapeo::sorteoQuiniela) }

    /**
     * Los sorteos con esos ids que ya estén cargados, como mapa id → datos.
     * Firestore permite hasta 30 ids por consulta, así que se divide en grupos.
     */
    fun porIds(ids: Set<String>): Flow<Map<String, Map<String, Any?>>> {
        if (ids.isEmpty()) return flowOf(emptyMap())
        val consultas = ids.chunked(30).map { grupo ->
            coleccion.whereIn(FieldPath.documentId(), grupo).snapshots()
                .map { snapshot -> snapshot.documents.associate { it.id to it.data.orEmpty() } }
        }
        return combine(consultas) { partes -> partes.fold(emptyMap()) { todo, parte -> todo + parte } }
    }

    private fun QuerySnapshot.mapas() = documents.mapNotNull { it.data }
}

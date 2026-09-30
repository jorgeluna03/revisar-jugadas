package com.jluna.revisarjugadas.ui.jugadas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestoreException
import com.jluna.revisarjugadas.data.jugadas.Jugada
import com.jluna.revisarjugadas.data.jugadas.JugadasRepository
import com.jluna.revisarjugadas.data.sorteos.SorteosRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class JugadasViewModel(
    private val repo: JugadasRepository,
    private val sorteosRepo: SorteosRepository = SorteosRepository(),
) : ViewModel() {

    var error by mutableStateOf<String?>(null)
        private set

    /** null mientras se cargan por primera vez. */
    val jugadas: StateFlow<List<Jugada>?> = repo.jugadas
        .map<List<Jugada>, List<Jugada>?> { it }
        .catch {
            error = "No se pudieron leer tus jugadas: ${descripcion(it)}"
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Los sorteos ya cargados que corresponden a alguna jugada (id → datos). */
    val sorteos: StateFlow<Map<String, Map<String, Any?>>> = jugadas
        .filterNotNull()
        .map { lista -> lista.flatMap { it.idsSorteos }.toSet() }
        .distinctUntilChanged()
        .flatMapLatest { ids -> sorteosRepo.porIds(ids) }
        .catch {
            error = "No se pudieron leer los resultados: ${descripcion(it)}"
            emit(emptyMap())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun buscar(id: String): Jugada? = jugadas.value?.find { it.id == id }

    /**
     * No espera la confirmación del servidor: Firestore guarda primero en el teléfono
     * y sincroniza cuando hay conexión, así que la pantalla puede cerrarse enseguida.
     */
    fun guardar(jugada: Jugada) = ejecutar("guardar la jugada") { repo.guardar(jugada) }

    fun borrar(id: String) = ejecutar("borrar la jugada") { repo.borrar(id) }

    private fun ejecutar(descripcion: String, accion: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                accion()
            } catch (e: Exception) {
                error = "No se pudo $descripcion: ${descripcion(e)}"
            }
        }
    }
}

/** Texto entendible para los errores más comunes de Firestore. */
fun descripcion(e: Throwable): String {
    // Firestore a veces envuelve el error real en otra excepción
    val firestore = generateSequence(e) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
    return when (firestore?.code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED -> "sin permiso (revisá las reglas de Firestore)"
        FirebaseFirestoreException.Code.UNAVAILABLE -> "sin conexión"
        else -> e.message ?: "error inesperado"
    }
}

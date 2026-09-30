package com.jluna.revisarjugadas.ui.jugadas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestoreException
import com.jluna.revisarjugadas.data.quini6.JugadaQuini6
import com.jluna.revisarjugadas.data.quini6.JugadasRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JugadasViewModel(private val repo: JugadasRepository) : ViewModel() {

    var error by mutableStateOf<String?>(null)
        private set

    /** null mientras se cargan por primera vez. */
    val jugadas: StateFlow<List<JugadaQuini6>?> = repo.jugadasQuini6
        .map<List<JugadaQuini6>, List<JugadaQuini6>?> { it }
        .catch {
            error = "No se pudieron leer tus jugadas: ${descripcion(it)}"
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun buscar(id: String): JugadaQuini6? = jugadas.value?.find { it.id == id }

    /**
     * No espera la confirmación del servidor: Firestore guarda primero en el teléfono
     * y sincroniza cuando hay conexión, así que la pantalla puede cerrarse enseguida.
     */
    fun guardar(jugada: JugadaQuini6) = ejecutar("guardar la jugada") { repo.guardar(jugada) }

    fun borrar(id: String) = ejecutar("borrar la jugada") { repo.borrar(id) }

    fun descartarError() {
        error = null
    }

    private fun ejecutar(descripcion: String, accion: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                accion()
            } catch (e: Exception) {
                error = "No se pudo $descripcion: ${descripcion(e)}"
            }
        }
    }

    private fun descripcion(e: Throwable): String {
        // Firestore a veces envuelve el error real en otra excepción
        val firestore = generateSequence(e) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
        return when (firestore?.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> "sin permiso (revisá las reglas de Firestore)"
            FirebaseFirestoreException.Code.UNAVAILABLE -> "sin conexión"
            else -> e.message ?: "error inesperado"
        }
    }
}

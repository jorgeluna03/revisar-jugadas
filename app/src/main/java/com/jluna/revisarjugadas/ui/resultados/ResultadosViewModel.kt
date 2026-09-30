package com.jluna.revisarjugadas.ui.resultados

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jluna.revisarjugadas.data.sorteos.SorteosRepository
import com.jluna.revisarjugadas.dominio.Quiniela
import com.jluna.revisarjugadas.dominio.SorteoQuini6
import com.jluna.revisarjugadas.dominio.SorteoQuiniela
import com.jluna.revisarjugadas.ui.jugadas.descripcion
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class ResultadosViewModel(private val repo: SorteosRepository = SorteosRepository()) : ViewModel() {

    var error by mutableStateOf<String?>(null)
        private set

    /** null mientras carga. */
    val quini6: StateFlow<List<SorteoQuini6>?> = repo.quini6Recientes()
        .map<List<SorteoQuini6>, List<SorteoQuini6>?> { it }
        .catch { error = "No se pudieron leer los resultados: ${descripcion(it)}"; emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Por defecto, el último día con Quiniela (hoy, o el sábado si hoy es domingo)
    private val _fechaQuiniela = MutableStateFlow(
        generateSequence(LocalDate.now()) { it.minusDays(1) }.first(Quiniela::esDiaDeSorteo),
    )
    val fechaQuiniela: StateFlow<LocalDate> = _fechaQuiniela.asStateFlow()

    /** null mientras carga la fecha elegida. */
    val quinielas: StateFlow<List<SorteoQuiniela>?> = _fechaQuiniela
        .flatMapLatest { fecha ->
            repo.quinielasDelDia(fecha).map<List<SorteoQuiniela>, List<SorteoQuiniela>?> { it }
        }
        .catch { error = "No se pudieron leer los resultados: ${descripcion(it)}"; emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun elegirFechaQuiniela(fecha: LocalDate) {
        _fechaQuiniela.value = fecha
    }
}

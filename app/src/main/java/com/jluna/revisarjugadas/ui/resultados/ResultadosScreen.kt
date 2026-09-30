package com.jluna.revisarjugadas.ui.resultados

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jluna.revisarjugadas.dominio.Jurisdiccion
import com.jluna.revisarjugadas.dominio.Quiniela
import com.jluna.revisarjugadas.dominio.SorteoQuini6
import com.jluna.revisarjugadas.dominio.SorteoQuiniela
import com.jluna.revisarjugadas.dominio.Turno
import com.jluna.revisarjugadas.ui.jugadas.Bolilla
import com.jluna.revisarjugadas.ui.jugadas.formatoCorto
import com.jluna.revisarjugadas.ui.jugadas.formatoLargo
import com.jluna.revisarjugadas.ui.screens.PantallaVacia
import java.time.LocalDate

@Composable
fun ResultadosScreen(vm: ResultadosViewModel = viewModel()) {
    var pestania by rememberSaveable { mutableStateOf(0) }

    Column(Modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = pestania) {
            Tab(selected = pestania == 0, onClick = { pestania = 0 }, text = { Text("Quini 6") })
            Tab(selected = pestania == 1, onClick = { pestania = 1 }, text = { Text("Quinielas") })
        }
        vm.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
        }
        when (pestania) {
            0 -> ResultadosQuini6(vm)
            else -> ResultadosQuiniela(vm)
        }
    }
}

@Composable
private fun ResultadosQuini6(vm: ResultadosViewModel) {
    val sorteos by vm.quini6.collectAsStateWithLifecycle()
    val lista = sorteos
    when {
        lista == null -> Cargando()
        lista.isEmpty() -> PantallaVacia("Sin resultados", "Todavía no se cargó ningún sorteo de Quini 6.")
        else -> LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(lista, key = { it.id }) { TarjetaSorteoQuini6(it) }
        }
    }
}

@Composable
private fun TarjetaSorteoQuini6(sorteo: SorteoQuini6) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(
                sorteo.fecha.formatoLargo() + (sorteo.numeroSorteo?.let { " · Sorteo $it" } ?: ""),
                style = MaterialTheme.typography.titleSmall,
            )
            FilaModalidad("Tradicional", sorteo.tradicional)
            FilaModalidad("La Segunda", sorteo.segunda)
            FilaModalidad("Revancha", sorteo.revancha)
            FilaModalidad("Siempre Sale", sorteo.siempreSale, nota = "Pagó a ${sorteo.siempreSaleAciertos} aciertos")
            Text("Pozo Extra", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 12.dp))
            Text(
                sorteo.pozoExtra.joinToString("  ") { it.toString().padStart(2, '0') },
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun FilaModalidad(nombre: String, numeros: List<Int>, nota: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp)) {
        Text(nombre, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
        nota?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary) }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
        numeros.sorted().forEach { Bolilla(it) }
    }
}

@Composable
private fun ResultadosQuiniela(vm: ResultadosViewModel) {
    val fecha by vm.fechaQuiniela.collectAsStateWithLifecycle()
    val sorteos by vm.quinielas.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        // Los últimos días con sorteo, del más viejo al de hoy
        val fechas = remember {
            generateSequence(LocalDate.now()) { it.minusDays(1) }.filter(Quiniela::esDiaDeSorteo).take(7).toList().reversed()
        }
        LazyRow(
            state = rememberLazyListState(initialFirstVisibleItemIndex = fechas.size - 1),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            items(fechas) { f ->
                FilterChip(
                    selected = f == fecha,
                    onClick = { vm.elegirFechaQuiniela(f) },
                    label = { Text(if (f == LocalDate.now()) "Hoy" else f.formatoCorto()) },
                )
            }
        }

        val lista = sorteos
        if (lista == null) {
            Cargando()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(Jurisdiccion.entries) { jurisdiccion ->
                    TarjetaJurisdiccion(jurisdiccion, lista.filter { it.jurisdiccion == jurisdiccion })
                }
            }
        }
    }
}

@Composable
private fun TarjetaJurisdiccion(jurisdiccion: Jurisdiccion, sorteos: List<SorteoQuiniela>) {
    var abierto by rememberSaveable(jurisdiccion) { mutableStateOf<String?>(null) }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(vertical = 8.dp)) {
            Text(
                jurisdiccion.nombre,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            Turno.entries.forEach { turno ->
                val sorteo = sorteos.find { it.turno == turno }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = sorteo != null) { abierto = if (abierto == turno.name) null else turno.name }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text("${turno.nombre} (${turno.hora})", modifier = Modifier.weight(1f))
                    Text(
                        sorteo?.numeros?.first() ?: "—",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (sorteo != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    )
                }
                if (sorteo != null && abierto == turno.name) Extracto(sorteo)
            }
        }
    }
}

/** Los 20 números en dos columnas: del 1 al 10 y del 11 al 20. */
@Composable
private fun Extracto(sorteo: SorteoQuiniela) {
    Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp)) {
        HorizontalDivider()
        Row(Modifier.padding(top = 8.dp)) {
            listOf(0, 10).forEach { desde ->
                Column(Modifier.weight(1f)) {
                    (desde until desde + 10).forEach { i ->
                        Text(
                            "${(i + 1).toString().padStart(2)}.  ${sorteo.numeros[i]}",
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Cargando() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

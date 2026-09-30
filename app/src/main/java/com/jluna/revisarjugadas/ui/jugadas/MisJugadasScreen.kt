package com.jluna.revisarjugadas.ui.jugadas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jluna.revisarjugadas.data.quini6.JugadaQuini6
import com.jluna.revisarjugadas.ui.screens.PantallaVacia
import java.time.LocalDate

@Composable
fun MisJugadasScreen(
    vm: JugadasViewModel,
    onNueva: () -> Unit,
    onAbrir: (String) -> Unit,
) {
    val jugadas by vm.jugadas.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNueva,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nueva jugada") },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            val lista = jugadas
            when {
                lista == null -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                lista.isEmpty() -> PantallaVacia(
                    titulo = "Todavía no cargaste jugadas",
                    mensaje = "Tocá \"Nueva jugada\" y anotá los 6 números de tu boleta de Quini 6.",
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Text("Mis jugadas", style = MaterialTheme.typography.headlineSmall)
                    }
                    items(lista, key = { it.id.orEmpty() }) { jugada ->
                        TarjetaJugada(jugada, onClick = { jugada.id?.let(onAbrir) })
                    }
                }
            }

            vm.error?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.TopCenter).padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun TarjetaJugada(jugada: JugadaQuini6, onClick: () -> Unit) {
    val pendiente = !jugada.fechaSorteo.isBefore(LocalDate.now())

    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Quini 6 · ${jugada.fechaSorteo.formatoLargo()}",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    if (pendiente) "Por sortear" else "Sorteado",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 12.dp),
            ) {
                jugada.numeros.sorted().forEach { Bolilla(it) }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp),
            ) {
                AssistChip(onClick = onClick, label = { Text("Tradicional") })
                if (jugada.revancha) AssistChip(onClick = onClick, label = { Text("Revancha") })
                if (jugada.siempreSale) AssistChip(onClick = onClick, label = { Text("Siempre Sale") })
            }
        }
    }
}

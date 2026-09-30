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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jluna.revisarjugadas.data.jugadas.Jugada
import com.jluna.revisarjugadas.data.jugadas.JugadaQuini6
import com.jluna.revisarjugadas.data.jugadas.JugadaQuiniela
import com.jluna.revisarjugadas.dominio.Mapeo
import com.jluna.revisarjugadas.dominio.controlarQuini6
import com.jluna.revisarjugadas.dominio.controlarQuiniela
import com.jluna.revisarjugadas.ui.screens.PantallaVacia
import java.time.LocalDate

enum class TipoJuego { QUINI6, QUINIELA }

@Composable
fun MisJugadasScreen(
    vm: JugadasViewModel,
    onNueva: (TipoJuego) -> Unit,
    onAbrir: (String) -> Unit,
) {
    val jugadas by vm.jugadas.collectAsStateWithLifecycle()
    val sorteos by vm.sorteos.collectAsStateWithLifecycle()
    var elegirJuego by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { elegirJuego = true },
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
                    mensaje = "Tocá \"Nueva jugada\" y anotá tu boleta de Quini 6 o tu apuesta de Quiniela.",
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Text("Mis jugadas", style = MaterialTheme.typography.headlineSmall)
                    }
                    items(lista, key = { it.id.orEmpty() }) { jugada ->
                        val onClick = { jugada.id?.let(onAbrir); Unit }
                        when (jugada) {
                            is JugadaQuini6 -> TarjetaQuini6(jugada, sorteos, onClick)
                            is JugadaQuiniela -> TarjetaQuiniela(jugada, sorteos, onClick)
                        }
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

    if (elegirJuego) {
        AlertDialog(
            onDismissRequest = { elegirJuego = false },
            title = { Text("¿Qué jugaste?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { elegirJuego = false; onNueva(TipoJuego.QUINI6) },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Quini 6") }
                    OutlinedButton(
                        onClick = { elegirJuego = false; onNueva(TipoJuego.QUINIELA) },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Quiniela") }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { elegirJuego = false }) { Text("Cancelar") } },
        )
    }
}

private enum class Estado(val texto: String) {
    POR_SORTEAR("Por sortear"),
    ESPERANDO("Esperando resultado"),
    GANO("¡Ganaste!"),
    NO_GANO("Sin premio"),
}

@Composable
private fun TarjetaJugada(
    titulo: String,
    estado: Estado,
    onClick: () -> Unit,
    contenido: @Composable () -> Unit,
) {
    val colores = if (estado == Estado.GANO) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    } else {
        CardDefaults.cardColors()
    }
    Card(onClick = onClick, colors = colores, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(titulo, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Text(
                    estado.texto,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (estado == Estado.GANO) FontWeight.Bold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            contenido()
        }
    }
}

private fun estadoSinResultado(fecha: LocalDate) =
    if (fecha.isBefore(LocalDate.now())) Estado.ESPERANDO else Estado.POR_SORTEAR

@Composable
private fun TarjetaQuini6(jugada: JugadaQuini6, sorteos: Map<String, Map<String, Any?>>, onClick: () -> Unit) {
    val sorteo = sorteos[jugada.idsSorteos.first()]?.let(Mapeo::sorteoQuini6)
    val resultados = sorteo?.let { controlarQuini6(jugada.numeros, jugada.revancha, jugada.siempreSale, it) }
    val estado = when {
        resultados == null -> estadoSinResultado(jugada.fechaSorteo)
        resultados.any { it.premiada } -> Estado.GANO
        else -> Estado.NO_GANO
    }
    // Con resultado, se resaltan los números que salieron en alguna modalidad jugada
    val acertados = resultados?.filter { it.participa }?.flatMap { it.aciertos }?.toSet()

    TarjetaJugada("Quini 6 · ${jugada.fechaSorteo.formatoLargo()}", estado, onClick) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 12.dp)) {
            jugada.numeros.sorted().forEach { Bolilla(it, resaltada = acertados == null || it in acertados) }
        }
        if (resultados == null) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                AssistChip(onClick = onClick, label = { Text("Tradicional") })
                if (jugada.revancha) AssistChip(onClick = onClick, label = { Text("Revancha") })
                if (jugada.siempreSale) AssistChip(onClick = onClick, label = { Text("Siempre Sale") })
            }
        } else {
            Column(Modifier.padding(top = 12.dp)) {
                resultados.forEach { r ->
                    Row {
                        Text(r.modalidad.nombre, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Text(
                            when {
                                !r.participa -> "no jugada"
                                r.premiada -> "${r.aciertos.size} aciertos · ¡premio!"
                                else -> "${r.aciertos.size} aciertos"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (r.premiada) FontWeight.Bold else FontWeight.Normal,
                            color = if (r.participa) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaQuiniela(jugada: JugadaQuiniela, sorteos: Map<String, Map<String, Any?>>, onClick: () -> Unit) {
    val porJurisdiccion = jugada.jurisdicciones.zip(jugada.idsSorteos) { jurisdiccion, id ->
        val resultado = sorteos[id]?.let(Mapeo::sorteoQuiniela)?.let { controlarQuiniela(jugada.numero, jugada.ubicacion, it) }
        jurisdiccion to resultado
    }
    val estado = when {
        porJurisdiccion.any { it.second?.gano == true } -> Estado.GANO
        porJurisdiccion.all { it.second != null } -> Estado.NO_GANO
        else -> estadoSinResultado(jugada.fechaSorteo)
    }

    TarjetaJugada("Quiniela · ${jugada.turno.nombre} · ${jugada.fechaSorteo.formatoCorto()}", estado, onClick) {
        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 8.dp)) {
            Text(jugada.numero, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                " ${textoUbicacion(jugada.ubicacion)}" + (jugada.importe?.let { " · ${it.enPesos()}" } ?: ""),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
            )
        }
        Column(Modifier.padding(top = 4.dp)) {
            porJurisdiccion.forEach { (jurisdiccion, resultado) ->
                Row {
                    Text(jurisdiccion.nombre, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text(
                        when {
                            resultado == null -> "sin resultado"
                            !resultado.gano -> "no salió"
                            else -> "salió en ${resultado.posiciones.joinToString(", ") { "la $it°" }}" +
                                (jugada.importe?.let { " · ${resultado.premio(it).enPesos()}" } ?: " · ×${resultado.multiplicador.comoMultiplicador()}")
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (resultado?.gano == true) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

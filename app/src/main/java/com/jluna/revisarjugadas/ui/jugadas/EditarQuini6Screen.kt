package com.jluna.revisarjugadas.ui.jugadas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.jluna.revisarjugadas.dominio.CalendarioQuini6
import com.jluna.revisarjugadas.dominio.Quini6
import com.jluna.revisarjugadas.data.jugadas.JugadaQuini6
import java.time.LocalDate

/** Pantalla para cargar una jugada de Quini 6 nueva ([inicial] null) o editar una existente. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarQuini6Screen(
    inicial: JugadaQuini6?,
    onGuardar: (JugadaQuini6) -> Unit,
    onBorrar: (String) -> Unit,
    onVolver: () -> Unit,
) {
    // LocalDate no se puede guardar directo en rememberSaveable: se guarda como texto
    var fechaTexto by rememberSaveable {
        mutableStateOf((inicial?.fechaSorteo ?: CalendarioQuini6.proximoSorteo()).toString())
    }
    var seleccionados by rememberSaveable { mutableStateOf(inicial?.numeros.orEmpty().sorted()) }
    var revancha by rememberSaveable { mutableStateOf(inicial?.revancha ?: true) }
    var siempreSale by rememberSaveable { mutableStateOf(inicial?.siempreSale ?: true) }
    var confirmarBorrado by remember { mutableStateOf(false) }

    val fecha = LocalDate.parse(fechaTexto)
    val completa = seleccionados.size == Quini6.CANTIDAD_NUMEROS

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (inicial == null) "Nuevo Quini 6" else "Editar Quini 6") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (inicial?.id != null) {
                        IconButton(onClick = { confirmarBorrado = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Borrar jugada")
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Text("Sorteo", style = MaterialTheme.typography.titleMedium)
            SelectorDeFecha(fecha, onElegir = { fechaTexto = it.toString() })

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 24.dp),
            ) {
                Text("Tus números", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(
                    "${seleccionados.size} de ${Quini6.CANTIDAD_NUMEROS}",
                    color = if (completa) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            GrillaDeNumeros(
                seleccionados = seleccionados,
                onTocar = { numero ->
                    seleccionados = when {
                        numero in seleccionados -> seleccionados - numero
                        completa -> seleccionados // ya hay 6: primero hay que sacar uno
                        else -> (seleccionados + numero).sorted()
                    }
                },
                modifier = Modifier.padding(top = 8.dp),
            )

            Text("Modalidades", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 24.dp))
            Text(
                "Tradicional (Primer Sorteo y La Segunda) siempre está incluida.",
                style = MaterialTheme.typography.bodySmall,
            )
            OpcionConSwitch("Revancha", revancha) { revancha = it }
            OpcionConSwitch("Siempre Sale", siempreSale) { siempreSale = it }

            Button(
                onClick = {
                    onGuardar(
                        JugadaQuini6(
                            id = inicial?.id,
                            fechaSorteo = fecha,
                            numeros = seleccionados,
                            revancha = revancha,
                            siempreSale = siempreSale,
                        ),
                    )
                },
                enabled = completa,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            ) {
                Text(if (completa) "Guardar jugada" else "Elegí ${Quini6.CANTIDAD_NUMEROS - seleccionados.size} número(s) más")
            }
        }
    }

    if (confirmarBorrado && inicial?.id != null) {
        AlertDialog(
            onDismissRequest = { confirmarBorrado = false },
            title = { Text("Borrar jugada") },
            text = { Text("¿Seguro que querés borrar esta jugada?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarBorrado = false
                    onBorrar(inicial.id)
                }) { Text("Borrar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarBorrado = false }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun SelectorDeFecha(elegida: LocalDate, onElegir: (LocalDate) -> Unit) {
    // Si se edita una jugada vieja, su fecha puede no estar entre las sugeridas
    val fechas = remember(elegida) { (CalendarioQuini6.fechasParaElegir() + elegida).distinct().sorted() }
    val proximo = remember { CalendarioQuini6.proximoSorteo() }
    val estado = rememberLazyListState(initialFirstVisibleItemIndex = maxOf(0, fechas.indexOf(elegida) - 1))

    LazyRow(
        state = estado,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 8.dp),
    ) {
        items(fechas) { f ->
            FilterChip(
                selected = f == elegida,
                onClick = { onElegir(f) },
                label = { Text(if (f == proximo) "${f.formatoCorto()} (próximo)" else f.formatoCorto()) },
            )
        }
    }
}

/** Los números del 00 al 45 en filas de 8. */
@Composable
private fun GrillaDeNumeros(
    seleccionados: List<Int>,
    onTocar: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val porFila = 8
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Quini6.RANGO_NUMEROS.chunked(porFila).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                fila.forEach { numero ->
                    Bolilla(
                        numero = numero,
                        resaltada = numero in seleccionados,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .clickable { onTocar(numero) },
                    )
                }
                // Completa la última fila para que las bolillas queden alineadas
                repeat(porFila - fila.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun OpcionConSwitch(texto: String, activada: Boolean, onCambiar: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable { onCambiar(!activada) }.padding(vertical = 4.dp),
    ) {
        Text(texto, modifier = Modifier.weight(1f))
        Switch(checked = activada, onCheckedChange = onCambiar)
    }
}

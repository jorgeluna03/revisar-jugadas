package com.jluna.revisarjugadas.ui.jugadas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jluna.revisarjugadas.data.jugadas.JugadaQuiniela
import com.jluna.revisarjugadas.dominio.Jurisdiccion
import com.jluna.revisarjugadas.dominio.Quiniela
import com.jluna.revisarjugadas.dominio.Turno
import java.time.LocalDate

/** Pantalla para cargar una apuesta de Quiniela nueva ([inicial] null) o editar una existente. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditarQuinielaScreen(
    inicial: JugadaQuiniela?,
    onGuardar: (JugadaQuiniela) -> Unit,
    onBorrar: (String) -> Unit,
    onVolver: () -> Unit,
) {
    val proximo = remember { Quiniela.proximoSorteo() }

    // rememberSaveable solo guarda tipos simples: fechas y enums se guardan como texto
    var fechaTexto by rememberSaveable { mutableStateOf((inicial?.fechaSorteo ?: proximo.first).toString()) }
    var turnoTexto by rememberSaveable { mutableStateOf((inicial?.turno ?: proximo.second).name) }
    var jurisdiccionesTexto by rememberSaveable {
        mutableStateOf((inicial?.jurisdicciones ?: listOf(Jurisdiccion.NACIONAL)).map { it.name })
    }
    var numero by rememberSaveable { mutableStateOf(inicial?.numero.orEmpty()) }
    var ubicacion by rememberSaveable { mutableStateOf(inicial?.ubicacion ?: 1) }
    var importeTexto by rememberSaveable { mutableStateOf(inicial?.importe?.toLong()?.toString().orEmpty()) }
    var confirmarBorrado by remember { mutableStateOf(false) }

    val fecha = LocalDate.parse(fechaTexto)
    val turno = Turno.valueOf(turnoTexto)
    val jurisdicciones = Jurisdiccion.entries.filter { it.name in jurisdiccionesTexto }
    val importe = importeTexto.toDoubleOrNull()?.takeIf { it > 0 }
    val multiplicador = Quiniela.multiplicador(numero.length, ubicacion)
    val valida = numero.isNotEmpty() && jurisdicciones.isNotEmpty() && multiplicador != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (inicial == null) "Nueva Quiniela" else "Editar Quiniela") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (inicial?.id != null) {
                        IconButton(onClick = { confirmarBorrado = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Borrar apuesta")
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
            Titulo("Día")
            val fechas = remember(fecha) { (Quiniela.fechasParaElegir() + fecha).distinct().sorted() }
            val estadoFechas = rememberLazyListState(initialFirstVisibleItemIndex = maxOf(0, fechas.indexOf(fecha) - 1))
            LazyRow(state = estadoFechas, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(fechas) { f ->
                    FilterChip(
                        selected = f == fecha,
                        onClick = { fechaTexto = f.toString() },
                        label = { Text(if (f == LocalDate.now()) "Hoy" else f.formatoCorto()) },
                    )
                }
            }

            Titulo("Turno")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Turno.entries.forEach { t ->
                    FilterChip(
                        selected = t == turno,
                        onClick = { turnoTexto = t.name },
                        label = { Text("${t.nombre} ${t.hora}") },
                    )
                }
            }

            Titulo("Quinielas")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Jurisdiccion.entries.forEach { j ->
                    FilterChip(
                        selected = j in jurisdicciones,
                        onClick = {
                            jurisdiccionesTexto = if (j in jurisdicciones) jurisdiccionesTexto - j.name else jurisdiccionesTexto + j.name
                        },
                        label = { Text(j.nombre) },
                    )
                }
            }

            Titulo("Número")
            OutlinedTextField(
                value = numero,
                onValueChange = { nuevo ->
                    numero = nuevo.filter(Char::isDigit).take(4)
                    if (numero.length == 1) ubicacion = 1 // 1 cifra solo se juega a la cabeza
                },
                placeholder = { Text("De 1 a 4 cifras, ej. 23") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Titulo("Ubicación")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Quiniela.UBICACIONES.forEach { u ->
                    FilterChip(
                        selected = u == ubicacion,
                        onClick = { ubicacion = u },
                        enabled = numero.length != 1 || u == 1,
                        label = { Text(textoUbicacion(u).replaceFirstChar(Char::uppercase)) },
                    )
                }
            }

            Titulo("Importe por quiniela (opcional)")
            OutlinedTextField(
                value = importeTexto,
                onValueChange = { importeTexto = it.filter(Char::isDigit).take(9) },
                prefix = { Text("$ ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            if (multiplicador != null && numero.isNotEmpty()) {
                Text(
                    "Si sale, paga ${multiplicador.comoMultiplicador()} veces lo apostado" +
                        (importe?.let { " (${(it * multiplicador).enPesos()} por quiniela)" } ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Button(
                onClick = {
                    onGuardar(
                        JugadaQuiniela(
                            id = inicial?.id,
                            fechaSorteo = fecha,
                            turno = turno,
                            jurisdicciones = jurisdicciones,
                            numero = numero,
                            ubicacion = ubicacion,
                            importe = importe,
                        ),
                    )
                },
                enabled = valida,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            ) {
                Text(
                    when {
                        numero.isEmpty() -> "Escribí el número"
                        jurisdicciones.isEmpty() -> "Elegí al menos una quiniela"
                        else -> "Guardar apuesta"
                    },
                )
            }
        }
    }

    if (confirmarBorrado && inicial?.id != null) {
        AlertDialog(
            onDismissRequest = { confirmarBorrado = false },
            title = { Text("Borrar apuesta") },
            text = { Text("¿Seguro que querés borrar esta apuesta?") },
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
private fun Titulo(texto: String) {
    Text(texto, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))
}

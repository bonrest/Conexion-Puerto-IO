package com.incentiainversiones.recoleconexionio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    private lateinit var digitalIOManager: DigitalIOManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        digitalIOManager = DigitalIOManager(this)

        setContent {
            IOControlApp()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        digitalIOManager.cleanup()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun IOControlApp() {
        MaterialTheme {
            var totalPulses by remember { mutableStateOf(0) }
            var logText by remember { mutableStateOf("") }
            val scrollState = rememberScrollState()

            LaunchedEffect(Unit) {
                digitalIOManager.ioFlow.collect { status ->
                    totalPulses = status.totalPulses
                    logText = "Total de impulsos: ${status.totalPulses}\n" +
                            "Último evento [${status.timestamp}]: ${status.message}\n\n" +
                            logText
                }
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Contador de Impulsos Pin 1",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Display del contador
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Total de Impulsos",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "$totalPulses",
                                style = MaterialTheme.typography.displayLarge
                            )
                        }
                    }

                    // Controles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { digitalIOManager.startMonitoringPin1() },
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        ) {
                            Text("Iniciar")
                        }

                        Button(
                            onClick = { digitalIOManager.stopMonitoringPin1() },
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                        ) {
                            Text("Detener")
                        }

                        Button(
                            onClick = { digitalIOManager.resetPulseCount() },
                            modifier = Modifier.weight(1f).padding(start = 8.dp)
                        ) {
                            Text("Reiniciar")
                        }
                    }

                    // Registro de eventos
                    Text(
                        text = "Registro de Eventos",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = logText,
                        onValueChange = { },
                        readOnly = true,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                    )

                    Button(
                        onClick = { logText = "Total de impulsos: $totalPulses\n" },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Limpiar Registro")
                    }
                }
            }
        }
    }
}
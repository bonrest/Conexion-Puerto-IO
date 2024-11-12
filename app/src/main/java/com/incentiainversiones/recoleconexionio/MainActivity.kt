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
            var logText by remember { mutableStateOf("") }
            val scrollState = rememberScrollState()

            LaunchedEffect(Unit) {
                digitalIOManager.ioFlow.collect { evento ->
                    logText += "$evento\n"
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
                        text = "Control de Pin Digital 1",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Controles del Pin 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenlyill 
                    ) {
                        Button(
                            onClick = { digitalIOManager.startMonitoringPin1() },
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        ) {
                            Text("Iniciar Monitoreo")
                        }

                        Button(
                            onClick = { digitalIOManager.stopMonitoringPin1() },
                            modifier = Modifier.weight(1f).padding(start = 8.dp)
                        ) {
                            Text("Detener Monitoreo")
                        }
                    }

                    // Botón de lectura manual
                    Button(
                        onClick = { digitalIOManager.readInput(1) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Leer Pin 1")
                    }

                    // Log de eventos
                    OutlinedTextField(
                        value = logText,
                        onValueChange = { },
                        readOnly = true,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(scrollState),
                        label = { Text("Registro de Eventos") }
                    )

                    // Botón para limpiar el log
                    Button(
                        onClick = { logText = "" },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Limpiar Registro")
                    }
                }
            }
        }
    }
}
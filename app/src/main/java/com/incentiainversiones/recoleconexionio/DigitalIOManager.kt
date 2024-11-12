package com.incentiainversiones.recoleconexionio

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.hardware.digital.DigitalIO
import android.hardware.digital.DigitalIOInterruptMonitor

class DigitalIOManager(private val context: Context) {
    private val digitalIO = DigitalIO()
    private val _ioFlow = MutableSharedFlow<String>()
    val ioFlow = _ioFlow.asSharedFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private var isMonitoring = false

    private val digitalIOInterruptMonitor = object : DigitalIOInterruptMonitor {
        override fun onDigitalIO1() {
            if (isMonitoring) {
                val value = readInput(1)
                sendMessage("Pin 1 activado - Valor leído: $value")
            }
        }

        override fun onDigitalIO2() {
            sendMessage("Entrada Digital 2 activada")
        }

        override fun onDigitalIO3() {
            sendMessage("Entrada Digital 3 activada")
        }
    }

    init {
        try {
            setupDigitalIO()
        } catch (e: Exception) {
            Log.e(TAG, "Error al inicializar el IO Digital", e)
            sendMessage("Error de inicialización: ${e.message}")
        }
    }

    private fun setupDigitalIO() {
        try {
            // Registrar el monitor para recibir las interrupciones
            digitalIO.registerDigitalIOInterruptMonitor(digitalIOInterruptMonitor)
            sendMessage("IO Digital inicializado")
        } catch (e: Exception) {
            Log.e(TAG, "Error en setupDigitalIO", e)
            sendMessage("Error en la configuración: ${e.message}")
        }
    }

    fun startMonitoringPin1() {
        try {
            if (!isMonitoring) {
                // Configurar el pin 1 para detectar flanco ascendente
                digitalIO.digital_in_request_irq(1, DigitalIO.IRQF_TRIGGER_RISING)
                isMonitoring = true

                // Lectura inicial
                val initialValue = readInput(1)
                sendMessage("Monitoreo del Pin 1 iniciado - Valor inicial: $initialValue")
            } else {
                sendMessage("El Pin 1 ya está siendo monitoreado")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al iniciar monitoreo del pin 1", e)
            sendMessage("Error al iniciar monitoreo: ${e.message}")
        }
    }

    fun stopMonitoringPin1() {
        try {
            if (isMonitoring) {
                isMonitoring = false
                // Ya que no hay un método específico para detener el monitoreo de un pin individual,
                // solo desactivamos nuestro flag de monitoreo
                sendMessage("Monitoreo del Pin 1 detenido")
            } else {
                sendMessage("El Pin 1 no estaba siendo monitoreado")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al detener monitoreo del pin 1", e)
            sendMessage("Error al detener monitoreo: ${e.message}")
        }
    }

    fun readInput(pinNumber: Int): Int {
        return try {
            val value = digitalIO.digital_in_get_value(pinNumber)
            if (pinNumber == 1) {
                sendMessage("Lectura manual Pin 1: $value")
            }
            value
        } catch (e: Exception) {
            Log.e(TAG, "Error al leer el pin $pinNumber", e)
            sendMessage("Error de lectura pin $pinNumber: ${e.message}")
            -1
        }
    }

    private fun sendMessage(message: String) {
        val timestamp = dateFormat.format(Date())
        scope.launch {
            _ioFlow.emit("[$timestamp] $message")
        }
    }

    fun cleanup() {
        try {
            if (isMonitoring) {
                stopMonitoringPin1()
            }
            digitalIO.unregisterDigitalIOInterruptMonitor()
            sendMessage("IO Digital desconectado")
        } catch (e: Exception) {
            Log.e(TAG, "Error durante la limpieza", e)
        }
    }

    companion object {
        private const val TAG = "DigitalIOManager"
    }
}
package com.tnova.t_pesaje.features.simulation

import com.tnova.t_pesaje.features.weighing.WeightData
import com.tnova.t_pesaje.features.weighing.WeightSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

/**
 * Fuente de datos utilizada durante las pruebas de la Actividad 5.
 *
 * Genera lecturas continuas de una balanza simulada,
 * incluyendo identificación, peso, fecha, hora y
 * estado de estabilidad.
 */
class WeightSimulator : WeightSource {

    private var job: Job? = null

    private val scope =
        CoroutineScope(Dispatchers.Default)

    private val formatoFecha =
        SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        )

    private val formatoHora =
        SimpleDateFormat(
            "HH:mm:ss",
            Locale.getDefault()
        )

    override fun iniciar(
        onDatosPeso: (WeightData) -> Unit
    ) {
        job?.cancel()

        job = scope.launch {

            var peso = 0.0

            val pesoObjetivo = 1250.500

            while (isActive) {

                val pesoActual: Double
                val estable: Boolean

                if (peso < pesoObjetivo) {

                    val incremento =
                        Random.nextDouble(
                            20.0,
                            80.0
                        )

                    peso += incremento

                    if (peso > pesoObjetivo) {
                        peso = pesoObjetivo
                    }

                    pesoActual = peso
                    estable = false

                } else {

                    val variacion =
                        Random.nextDouble(
                            -0.300,
                            0.300
                        )

                    pesoActual =
                        pesoObjetivo + variacion

                    estable = true
                }

                val ahora = Date()

                onDatosPeso(
                    WeightData(
                        balanza = "Balanza 1",
                        peso = pesoActual,
                        fecha = formatoFecha.format(ahora),
                        hora = formatoHora.format(ahora),
                        estable = estable
                    )
                )

                delay(500)
            }
        }
    }

    override fun detener() {
        job?.cancel()
        job = null
    }
}
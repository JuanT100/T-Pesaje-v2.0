package com.tnova.t_pesaje.features.weighing

/**
 * Contrato para cualquier fuente de datos de pesaje.
 *
 * La implementación debe comenzar a proporcionar lecturas
 * mediante la función onDatosPeso y detenerlas cuando se solicite.
 *
 * En esta actividad la implementación será SimuladorPeso.
 *
 * Posteriormente se podrá implementar una fuente para el OWA450
 * utilizando este mismo contrato, una vez definido su mecanismo
 * real de comunicación con la aplicación.
 */
interface WeightSource {

    fun iniciar(
        onDatosPeso: (WeightData) -> Unit
    )

    fun detener()
}
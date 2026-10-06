package com.tnova.t_pesaje.features.weighing

/**
 * Representa una lectura obtenida desde la fuente de pesaje.
 *
 * Contiene la información necesaria para que la aplicación
 * pueda mostrar y procesar una lectura de peso.
 *
 * La identificación de la balanza se maneja como texto.
 * La correspondencia de esta identificación con la balanza
 * registrada en la base de datos se definirá durante la
 * integración real con el sistema OWA450.
 *
 * Durante esta actividad los valores son proporcionados
 * por SimuladorPeso.
 */
data class WeightData(
    val balanza: String,
    val peso: Double,
    val fecha: String,
    val hora: String,
    val estable: Boolean
)
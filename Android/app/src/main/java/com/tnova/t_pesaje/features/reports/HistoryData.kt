package com.tnova.t_pesaje.features.reports

/**
 * Representa un registro de pesaje obtenido del historial.
 *
 * Contiene los datos necesarios para mostrar un pesaje
 * registrado en la aplicación.
 */
data class HistoryData(
    val idPesaje: Long,
    val balanza: String,
    val peso: Double,
    val fecha: String,
    val hora: String,
    val producto: String?,
    val colaborador: String,
    val otros: String?
)
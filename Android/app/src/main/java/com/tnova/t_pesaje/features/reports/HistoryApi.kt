package com.tnova.t_pesaje.features.reports

import com.tnova.t_pesaje.features.security.AuthenticatedApiClient
import com.tnova.t_pesaje.features.security.SessionManager
import com.tnova.t_pesaje.features.security.SessionStorage
import org.json.JSONObject
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.SSLException

/**
 * Resultado de la consulta del historial.
 */
data class HistoryResult(
    val success: Boolean,
    val records: List<HistoryData> = emptyList(),
    val total: Long = 0,
    val hasMore: Boolean = false,
    val message: String
)

/**
 * Cliente utilizado para consultar el historial.
 */
object HistoryApi {

    /**
     * Dirección de la API utilizada durante el desarrollo.
     */
    private const val BASE_URL =
        "https://192.168.100.154:5443"

    /**
     * Consulta el historial de pesajes.
     */
    fun getHistory(
        sessionStorage: SessionStorage,
        sessionManager: SessionManager,
        from: String,
        to: String,
        employee: String?,
        offset: Int,
        limit: Int
    ): HistoryResult {

        val employeeParam =
            URLEncoder.encode(
                employee ?: "",
                "UTF-8"
            )

        val url =
            URL(
                "$BASE_URL/api/pesajes" +
                        "?desde=$from" +
                        "&hasta=$to" +
                        "&colaborador=$employeeParam" +
                        "&offset=$offset" +
                        "&limit=$limit"
            )

        val client =
            AuthenticatedApiClient(
                sessionStorage,
                sessionManager
            )

        return try {

            // Ejecuta la solicitud autenticada.
            val response =
                client.execute(
                    method = "GET",
                    endpoint = url.toString()
                )

            if (response.statusCode != 200) {

                HistoryResult(
                    success = false,
                    message =
                        "No fue posible consultar el historial."
                )

            } else {

                // Procesa la respuesta.
                val json =
                    JSONObject(response.body)

                val jsonRecords =
                    json.getJSONArray("registros")

                val records =
                    mutableListOf<HistoryData>()

                for (i in 0 until jsonRecords.length()) {

                    val item =
                        jsonRecords.getJSONObject(i)

                    records.add(
                        HistoryData(
                            idPesaje =
                                item.getLong("idPesaje"),
                            balanza =
                                item.getString("balanza"),
                            peso =
                                item.getDouble("peso"),
                            fecha =
                                item.getString("fecha"),
                            hora =
                                item.getString("hora"),
                            producto =
                                if (item.isNull("producto")) {
                                    null
                                } else {
                                    item.getString("producto")
                                },
                            colaborador =
                                item.getString("colaborador"),
                            otros =
                                if (item.isNull("otros")) {
                                    null
                                } else {
                                    item.getString("otros")
                                }
                        )
                    )
                }

                HistoryResult(
                    success = true,
                    records = records,
                    total =
                        json.getLong("total"),
                    hasMore =
                        json.getBoolean("hayMas"),
                    message =
                        "Consulta realizada correctamente."
                )
            }

        } catch (_: SocketTimeoutException) {

            HistoryResult(
                success = false,
                message =
                    "Tiempo de espera agotado."
            )

        } catch (_: ConnectException) {

            HistoryResult(
                success = false,
                message =
                    "No fue posible conectar con la API."
            )

        } catch (_: SSLException) {

            HistoryResult(
                success = false,
                message =
                    "No fue posible establecer una conexión HTTPS segura."
            )

        } catch (_: Exception) {

            HistoryResult(
                success = false,
                message =
                    "No fue posible procesar la consulta."
            )
        }
    }
}
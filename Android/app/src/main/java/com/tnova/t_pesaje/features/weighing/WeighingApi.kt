package com.tnova.t_pesaje.features.weighing

import com.tnova.t_pesaje.features.security.AuthenticatedApiClient
import com.tnova.t_pesaje.features.security.SessionManager
import com.tnova.t_pesaje.features.security.SessionStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URL
import javax.net.ssl.SSLHandshakeException

/**
 * Resultado del registro de un pesaje.
 */
data class WeighingResult(
    val success: Boolean,
    val weighingId: Long? = null,
    val message: String
)

/**
 * Cliente utilizado para registrar pesajes.
 */
object WeighingApi {

    /**
     * Dirección de la API utilizada durante el desarrollo.
     */
    private const val BASE_URL =
        "https://192.168.100.154:5443"

    /**
     * Registra un pesaje en la API.
     */
    suspend fun registerWeighing(
        sessionStorage: SessionStorage,
        sessionManager: SessionManager,
        scaleId: Int,
        weight: Double,
        date: String,
        time: String,
        productId: Int?,
        userId: Int,
        notes: String? = null
    ): WeighingResult = withContext(Dispatchers.IO) {

        try {

            val url =
                URL("$BASE_URL/api/pesajes")

            // Construye la solicitud de pesaje.
            val requestJson =
                JSONObject().apply {

                    put(
                        "idBalanza",
                        scaleId
                    )

                    put(
                        "peso",
                        weight
                    )

                    put(
                        "fecha",
                        date
                    )

                    put(
                        "hora",
                        time
                    )

                    if (productId != null) {

                        put(
                            "idProducto",
                            productId
                        )

                    } else {

                        put(
                            "idProducto",
                            JSONObject.NULL
                        )
                    }

                    put(
                        "idUsuario",
                        userId
                    )

                    if (notes != null) {

                        put(
                            "otros",
                            notes
                        )

                    } else {

                        put(
                            "otros",
                            JSONObject.NULL
                        )
                    }
                }

            val client =
                AuthenticatedApiClient(
                    sessionStorage,
                    sessionManager
                )

            // Ejecuta la solicitud autenticada.
            val response =
                client.execute(
                    method = "POST",
                    endpoint = url.toString(),
                    body = requestJson.toString()
                )

            if (response.statusCode == 200) {

                // Procesa la respuesta.
                val json =
                    JSONObject(response.body)

                WeighingResult(
                    success =
                        json.optBoolean(
                            "registrado",
                            false
                        ),

                    weighingId =
                        if (
                            json.has("idPesaje") &&
                            !json.isNull("idPesaje")
                        ) {
                            json.getLong(
                                "idPesaje"
                            )
                        } else {
                            null
                        },

                    message =
                        json.optString(
                            "mensaje",
                            "Pesaje registrado."
                        )
                )

            } else {

                WeighingResult(
                    success = false,
                    message =
                        "No fue posible registrar el pesaje."
                )
            }

        } catch (_: SocketTimeoutException) {

            WeighingResult(
                success = false,
                message =
                    "Tiempo de espera agotado."
            )

        } catch (_: ConnectException) {

            WeighingResult(
                success = false,
                message =
                    "No fue posible conectar con la API."
            )

        } catch (_: SSLHandshakeException) {

            WeighingResult(
                success = false,
                message =
                    "No fue posible establecer una conexión HTTPS segura."
            )

        } catch (_: Exception) {

            WeighingResult(
                success = false,
                message =
                    "No fue posible procesar el pesaje."
            )
        }
    }
}
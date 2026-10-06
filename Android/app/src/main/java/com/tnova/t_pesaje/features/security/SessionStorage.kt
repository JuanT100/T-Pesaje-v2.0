package com.tnova.t_pesaje.features.security

import android.content.Context
import android.util.Base64
import androidx.core.content.edit
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties

/**
 * Almacena de forma cifrada los datos de la sesión.
 */
class SessionStorage(
    context: Context
) {

    private val preferences =
        context.getSharedPreferences(
            "tpesaje_session",
            Context.MODE_PRIVATE
        )

    companion object {

        private const val KEY_ALIAS =
            "tpesaje_session_key"

        private const val ACCESS_TOKEN =
            "access_token"

        private const val REFRESH_TOKEN =
            "refresh_token"

        private const val USER_ID =
            "user_id"

        private const val USERNAME =
            "username"

        private const val FULL_NAME =
            "full_name"

        private const val MUST_CHANGE_PASSWORD =
            "must_change_password"

        private const val ANDROID_KEYSTORE =
            "AndroidKeyStore"

        private const val TRANSFORMATION =
            "AES/GCM/NoPadding"
    }

    /**
     * Guarda la sesión autenticada.
     */
    fun saveSession(
        userId: Int,
        username: String,
        fullName: String,
        mustChangePassword: Boolean,
        accessToken: String,
        refreshToken: String
    ) {
        preferences.edit {
            putString(ACCESS_TOKEN, encrypt(accessToken))
            putString(REFRESH_TOKEN, encrypt(refreshToken))
            putInt(USER_ID, userId)
            putString(USERNAME, username)
            putString(FULL_NAME, fullName)
            putBoolean(MUST_CHANGE_PASSWORD, mustChangePassword)
        }
    }

    /**
     * Obtiene el Access Token almacenado.
     */
    fun getAccessToken(): String? =
        preferences
            .getString(ACCESS_TOKEN, null)
            ?.let { decrypt(it) }

    /**
     * Obtiene el Refresh Token almacenado.
     */
    fun getRefreshToken(): String? =
        preferences
            .getString(REFRESH_TOKEN, null)
            ?.let { decrypt(it) }

    /**
     * Elimina la sesión almacenada.
     */
    fun clearSession() {
        preferences.edit {
            clear()
        }
    }

    /**
     * Actualiza los tokens de la sesión.
     */
    fun updateTokens(
        accessToken: String,
        refreshToken: String
    ) {
        preferences.edit {
            putString(ACCESS_TOKEN, encrypt(accessToken))
            putString(REFRESH_TOKEN, encrypt(refreshToken))
        }
    }

    /**
     * Obtiene la clave de cifrado del Android Keystore.
     */
    private fun getSecretKey(): SecretKey {

        val keyStore =
            KeyStore.getInstance(ANDROID_KEYSTORE).apply {
                load(null)
            }

        if (keyStore.containsAlias(KEY_ALIAS)) {
            return keyStore.getKey(
                KEY_ALIAS,
                null
            ) as SecretKey
        }

        val keyGenerator =
            KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )

        val keySpec =
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or
                        KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(
                    KeyProperties.BLOCK_MODE_GCM
                )
                .setEncryptionPaddings(
                    KeyProperties.ENCRYPTION_PADDING_NONE
                )
                .setKeySize(256)
                .build()

        keyGenerator.init(keySpec)

        return keyGenerator.generateKey()
    }

    /**
     * Cifra un valor antes de almacenarlo.
     */
    private fun encrypt(value: String): String {

        val cipher =
            Cipher.getInstance(TRANSFORMATION)

        cipher.init(
            Cipher.ENCRYPT_MODE,
            getSecretKey()
        )

        val encrypted =
            cipher.doFinal(
                value.toByteArray(Charsets.UTF_8)
            )

        val combined =
            cipher.iv + encrypted

        return Base64.encodeToString(
            combined,
            Base64.NO_WRAP
        )
    }

    /**
     * Descifra un valor almacenado.
     */
    private fun decrypt(value: String): String {

        val combined =
            Base64.decode(
                value,
                Base64.NO_WRAP
            )

        val iv =
            combined.copyOfRange(
                0,
                12
            )

        val encrypted =
            combined.copyOfRange(
                12,
                combined.size
            )

        val cipher =
            Cipher.getInstance(TRANSFORMATION)

        cipher.init(
            Cipher.DECRYPT_MODE,
            getSecretKey(),
            GCMParameterSpec(128, iv)
        )

        return String(
            cipher.doFinal(encrypted),
            Charsets.UTF_8
        )
    }
}

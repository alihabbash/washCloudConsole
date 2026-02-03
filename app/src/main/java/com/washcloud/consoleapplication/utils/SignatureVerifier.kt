package com.washcloud.consoleapplication.utils


import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import android.util.Base64


/**
 * QR / Backend Signature Verifier
 *
 * Backend signs using:
 *   payload = "apiKey=XXX&terminalSn=YYY"
 *   signature = HMAC_SHA256(payload, apiKey)
 *
 * Device verifies using the same logic.
 */

class SignatureVerifier {

    /**
     * Build payload exactly like backend
     */
    private fun buildPayload(apiKey: String, terminalSn: String): String {
        return "apiKey=$apiKey&terminalSn=$terminalSn"
    }

    /**
     * Compute HMAC-SHA256 (Android compatible)
     */
    private fun computeHmacSha256(data: String, secret: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val keySpec = SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256")
        mac.init(keySpec)

        val rawHmac = mac.doFinal(data.toByteArray(Charsets.UTF_8))

        // ✅ Android Base64 (NO_WRAP is important)
        return Base64.encodeToString(rawHmac, Base64.NO_WRAP)
    }

    /**
     * Verify backend signature
     */
    fun isSignatureValid(
        receivedSignature: String,
        apiKey: String,
        terminalSn: String
    ): Boolean {

        val payload = buildPayload(apiKey, terminalSn)
        val expectedSignature = computeHmacSha256(payload, apiKey)

        return expectedSignature == receivedSignature
    }

    /**
     * Check expiration (Unix time in seconds)
     */
    fun isNotExpired(expiresAt: Long): Boolean {
        val nowSeconds = System.currentTimeMillis() / 1000
        return nowSeconds <= expiresAt
    }

    /**
     * FINAL validation
     */
    fun isQrValid(
        receivedSignature: String,
        apiKey: String,
        terminalSn: String,
        expiresAt: Long
    ): Boolean {
        return isSignatureValid(receivedSignature, apiKey, terminalSn)
                && isNotExpired(expiresAt)
    }
}
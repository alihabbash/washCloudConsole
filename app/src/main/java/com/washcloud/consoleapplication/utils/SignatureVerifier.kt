package com.washcloud.consoleapplication.utils


import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import android.util.Base64


import com.washcloud.consoleapplication.remote.model.offline.QrActionPayload

/**
 * QR / Backend Signature Verifier
 *
 * Backend signs using:
 *   payload = "apiKey=${apiKey}&terminalSn=${terminalSn}&nonce=${payload.nonce}&expiresAt=${payload.expiresAt}&actionType=${payload.actionType}&doorNo=${payload.doorNo}"
 *   signature = HMAC_SHA256(payload, apiKey)
 *
 * Device verifies using the same logic.
 */

class SignatureVerifier {

    /**
     * Build payload exactly like backend
     */
    private fun buildPayload(apiKey: String, terminalSn: String, payload: QrActionPayload): String {
        return "apiKey=$apiKey&terminalSn=$terminalSn&nonce=${payload.nonce}&expiresAt=${payload.expiresAt}&actionType=${payload.actionType.name}&doorNo=${payload.doorNo}"
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
        payloadObj: QrActionPayload,
        apiKey: String,
        terminalSn: String
    ): Boolean {

        val payloadString = buildPayload(apiKey, terminalSn, payloadObj)
        val expectedSignature = computeHmacSha256(payloadString, apiKey)

        return expectedSignature == payloadObj.signature
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
        payloadObj: QrActionPayload,
        apiKey: String,
        terminalSn: String
    ): Boolean {
        return isSignatureValid(payloadObj, apiKey, terminalSn)
                && isNotExpired(payloadObj.expiresAt)
    }
}
package com.washcloud.consoleapplication.utils


import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import android.util.Base64


import com.washcloud.consoleapplication.remote.model.offline.QrActionPayload
import com.washcloud.consoleapplication.remote.model.offline.StaticQrPayload

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
        var basePayload = "apiKey=$apiKey&terminalSn=$terminalSn&nonce=${payload.nonce}&expiresAt=${payload.expiresAt}&actionType=${payload.actionType.name}"
        if (payload.doorNo != null) {
            basePayload += "&doorNo=${payload.doorNo}"
        }
        if (payload.customerId != null) {
            basePayload += "&customerId=${payload.customerId}"
        }
        return basePayload
    }

    /**
     * Compute HMAC-SHA256 (Android compatible)
     * Generates a signature for any arbitrary payload string.
     */
    fun generateSignature(data: String, secret: String): String {
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
        val expectedSignature = generateSignature(payloadString, apiKey)

        return expectedSignature == payloadObj.signature
    }

    /**
     * Generate backend signature for Static QR (useful for testing/mocking)
     */
    fun generateStaticQrSignature(
        customerId: Long,
        phoneNumber: String,
        terminalSn: String
    ): String {
        val payloadString = "id=$customerId&phoneNumber=$phoneNumber"
        return generateSignature(payloadString, terminalSn)
    }

    /**
     * Verify backend signature for Static QR
     */
    fun isStaticQrSignatureValid(
        payloadObj: StaticQrPayload,
        terminalSn: String
    ): Boolean {
        val expectedSignature = generateStaticQrSignature(payloadObj.customerId, payloadObj.phoneNumber, terminalSn)
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
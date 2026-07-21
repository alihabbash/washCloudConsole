package com.washcloud.consoleapplication.remote.model.offline

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class QrActionPayload(
    val actionType: ActionType,
    val wayBillNo: String? = null,
    val doorNo: String?,
    val operationType: String, // Pickup, DropOff
    val issuedAt: Long,
    val expiresAt: Long,
    val nonce: String,
    val signature: String // HMAC-SHA256
)

enum class ActionType {
    OPEN_BOX,
    OPEN_CONVEYOR_DOOR, // only open the conveyor belt door
    CLOSE_CONVEYOR,
    MOVE_CONVEYOR, // move and open and close
    REBOOT_DEVICE
}

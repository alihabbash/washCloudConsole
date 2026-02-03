package com.washcloud.consoleapplication.remote.model.offline

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class QrActionPayload(
    val actionType: ActionType,
    val wayBillNo: String,
    val doorNo: String?,
    val operationType: String, // Pickup, DropOff
    val issuedAt: Long,
    val expiresAt: Long,
    val nonce: String,
    val signature: String
)

enum class ActionType {
    OPEN_BOX,
    OPEN_CONVEYOR,
    CLOSE_CONVEYOR,
    MOVE_CONVEYOR,
    REBOOT_DEVICE
}

package com.washcloud.consoleapplication.remote.model.heartbeat
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HeartbeatRequest(
    @field:Json(name = "apiKey") val apiKey: String,
    @field:Json(name = "terminalSn") val terminalSn: String,
    @field:Json(name = "boxes") val boxes: List<Box>
) {
    @JsonClass(generateAdapter = true)
    data class Box(
        @field:Json(name = "no") val no: String,
        @field:Json(name = "status") val status: String,
        @field:Json(name = "occupied") val occupied: String,
        @field:Json(name = "size") val size: Int,
        @field:Json(name = "open") val open: String,
        @field:Json(name = "type") val type: Int,
        @field:Json(name = "date") val date: Long,
        @field:Json(name = "orderID") val orderID: Long

    )
}

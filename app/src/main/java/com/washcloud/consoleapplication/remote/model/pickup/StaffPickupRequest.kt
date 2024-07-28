package com.washcloud.consoleapplication.remote.model.pickup


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StaffPickupRequest(
    @field:Json(name = "Apikey") val apiKey: String,
    @field:Json(name = "WayBillNo") val wayBillNo: String,
    @field:Json(name = "TerminalSn") val terminalSn: String,
    @field:Json(name = "Type") val type: Int,
    @field:Json(name = "DoorNo") val doorNo: Int
)

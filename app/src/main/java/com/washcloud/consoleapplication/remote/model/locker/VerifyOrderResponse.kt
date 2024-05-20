package com.washcloud.consoleapplication.remote.model.locker

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VerifyOrderResponse(
    @field:Json(name = "operationType") val operationType: String,
    @field:Json(name = "doorNo") val doorNo: String,
    @field:Json(name = "terminalSn") val terminalSn: String,
    @field:Json(name = "wayBillNo") val wayBillNo: String,
    @field:Json(name = "dropOffUrl") val dropOffUrl: String,
    @field:Json(name = "type") val type: String,
)
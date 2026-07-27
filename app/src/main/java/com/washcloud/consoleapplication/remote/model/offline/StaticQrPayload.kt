package com.washcloud.consoleapplication.remote.model.offline

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StaticQrPayload(
    @field:Json(name = "id") val customerId: Long,
    @field:Json(name = "phoneNumber") val phoneNumber: String,
    @field:Json(name = "signature") val signature: String
)

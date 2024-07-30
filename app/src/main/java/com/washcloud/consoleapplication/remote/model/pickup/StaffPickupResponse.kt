package com.washcloud.consoleapplication.remote.model.pickup


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StaffPickupResponse(
    @field:Json(name = "Status") val status: String?,
    @field:Json(name = "message") val message: String?
)

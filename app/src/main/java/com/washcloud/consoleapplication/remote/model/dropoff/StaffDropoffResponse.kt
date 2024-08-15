package com.washcloud.consoleapplication.remote.model.dropoff

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StaffDropoffResponse(
    @field:Json(name = "Status") val status: String?,
    @field:Json(name = "message") val message: String?
)
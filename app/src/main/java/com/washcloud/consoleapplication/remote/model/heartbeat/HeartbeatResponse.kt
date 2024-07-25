package com.washcloud.consoleapplication.remote.model.heartbeat

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class HeartbeatResponse(
    @field:Json(name = "Status") val status: String,
    @field:Json(name = "message") val message: String?
)
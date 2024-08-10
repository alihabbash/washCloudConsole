package com.washcloud.consoleapplication.remote.model.login

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StaffLoginResponse(
    @field:Json(name = "message") val message: String?,
)
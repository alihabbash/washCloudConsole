package com.washcloud.consoleapplication.remote.model.login

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StaffLoginRequest(
    @field:Json(name = "Apikey") val apiKey: String,
    @field:Json(name = "Type") val type: String,
    @field:Json(name = "Account") val account: String,
    @field:Json(name = "Password") val password: String,
)

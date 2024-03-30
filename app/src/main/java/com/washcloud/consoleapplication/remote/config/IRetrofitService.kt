package com.washcloud.consoleapplication.remote.config

import com.washcloud.consoleapplication.remote.model.login.StaffLoginRequest
import com.washcloud.consoleapplication.remote.model.login.StaffLoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface IRetrofitService {
    @POST(STAFF_LOGIN)
    suspend fun staffLogin(@Body request: StaffLoginRequest): Response<StaffLoginResponse>
}
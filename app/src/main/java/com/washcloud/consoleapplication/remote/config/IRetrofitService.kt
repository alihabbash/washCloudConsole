package com.washcloud.consoleapplication.remote.config

import com.washcloud.consoleapplication.remote.model.login.StaffLoginRequest
import com.washcloud.consoleapplication.remote.model.login.StaffLoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface IRetrofitService {
    @GET(STAFF_LOGIN)
    suspend fun staffLogin(
        @Query("Apikey") apiKey: String,
        @Query("type") type: String,
        @Query("account") account: String,
        @Query("password") password: String,
    ): Response<StaffLoginResponse>
}
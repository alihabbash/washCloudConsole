package com.washcloud.consoleapplication.remote.config

import com.washcloud.consoleapplication.remote.model.dropoff.StaffDropoffResponse
import com.washcloud.consoleapplication.remote.model.heartbeat.HeartbeatRequest
import com.washcloud.consoleapplication.remote.model.heartbeat.HeartbeatResponse
import com.washcloud.consoleapplication.remote.model.locker.VerifyOrderResponse
import com.washcloud.consoleapplication.remote.model.login.StaffLoginResponse
import com.washcloud.consoleapplication.remote.model.pickup.StaffPickupResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface IRetrofitService {
    @GET(STAFF_LOGIN)
    suspend fun staffLogin(
        @Query("Apikey") apiKey: String,
        @Query("type") type: String,
        @Query("account") account: String,
        @Query("password") password: String,
    ): Response<StaffLoginResponse>

    @GET(VERIFY_ORDER)
    suspend fun verifyOrder(
        @Path("serial") serial: String,//order serial
        @Path("terminalSn") terminalSn: String,
        @Query("Apikey") apiKey: String,
        @Query("serial") serialQ: String,//order serial
        @Query("terminalSn") terminalSnQ: String,
    ): Response<List<VerifyOrderResponse>>

    @POST(HEART_BEAT)
    suspend fun sendHeartbeat(
        @Body heartbeatRequest: HeartbeatRequest
    ): Response<HeartbeatResponse>

    @GET(STAFF_PICKUP)
    suspend fun staffPickup(
        @Query("Apikey") apiKey: String,
        @Query("WayBillNo") wayBillNo: String,
        @Query("TerminalSn") terminalSn: String,
        @Query("Type") type: Int,
        @Query("DoorNo") doorNo: Int
    ): Response<StaffPickupResponse>

    @GET(STAFF_DROP_OFF)
    suspend fun staffDropOff(
        @Query("Apikey") apiKey: String,
        @Query("WayBillNo") wayBillNo: String,
        @Query("TerminalSn") terminalSn: String,
        @Query("Type") type: Int,
        @Query("DoorNo") doorNo: Int
    ): Response<StaffDropoffResponse>
}
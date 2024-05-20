package com.washcloud.consoleapplication.remote.datasource

import com.washcloud.consoleapplication.remote.model.locker.VerifyOrderResponse

interface ILockerRemoteDataSource {
    suspend fun verifyOrder(
        serial: String,//order serial
        terminalSn: String,
        apiKey: String,
    ): List<VerifyOrderResponse>

}
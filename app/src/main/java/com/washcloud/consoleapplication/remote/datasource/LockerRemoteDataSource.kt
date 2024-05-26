package com.washcloud.consoleapplication.remote.datasource

import com.washcloud.consoleapplication.local.preferences.API_KEY
import com.washcloud.consoleapplication.remote.config.IApiProvider
import com.washcloud.consoleapplication.remote.config.IRetrofitService
import com.washcloud.consoleapplication.remote.model.locker.VerifyOrderResponse
import javax.inject.Inject

class LockerRemoteDataSource @Inject constructor(
    private val retrofitService: IRetrofitService,
    private val apiProvider: IApiProvider
) : ILockerRemoteDataSource{
    override suspend fun verifyOrder(
        serial: String,
        terminalSn: String,
        apiKey: String,
    ): List<VerifyOrderResponse> {
        return apiProvider.proceedRequest {
            retrofitService.verifyOrder(
                serial = serial,
                terminalSn = terminalSn,
                apiKey = API_KEY,
                serialQ = serial,
                terminalSnQ = terminalSn
            )
        }
    }


}
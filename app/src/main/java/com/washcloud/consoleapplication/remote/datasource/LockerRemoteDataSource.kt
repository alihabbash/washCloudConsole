package com.washcloud.consoleapplication.remote.datasource

import android.content.Context
import com.washcloud.consoleapplication.local.preferences.PrefsManager
import com.washcloud.consoleapplication.remote.config.IApiProvider
import com.washcloud.consoleapplication.remote.config.IRetrofitService
import com.washcloud.consoleapplication.remote.model.locker.VerifyOrderResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LockerRemoteDataSource @Inject constructor(
    private val retrofitService: IRetrofitService,
    private val apiProvider: IApiProvider,
    @ApplicationContext private val context: Context
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
                apiKey = PrefsManager.getApiKey(context),
                serialQ = serial,
                terminalSnQ = terminalSn
            )
        }
    }


}
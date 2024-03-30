package com.washcloud.consoleapplication.remote.datasource

import com.washcloud.consoleapplication.local.preferences.API_KEY
import com.washcloud.consoleapplication.remote.config.IApiProvider
import com.washcloud.consoleapplication.remote.config.IRetrofitService
import com.washcloud.consoleapplication.remote.model.login.StaffLoginRequest
import com.washcloud.consoleapplication.remote.model.login.StaffLoginResponse
import javax.inject.Inject

class StaffRemoteDataSource @Inject constructor(
    private val retrofitService: IRetrofitService,
    private val apiProvider: IApiProvider
) : IStaffRemoteDataSource {
    override suspend fun login(account: String, password: String): StaffLoginResponse {
        return apiProvider.proceedRequest {
            retrofitService.staffLogin(
                StaffLoginRequest(
                    account = account,
                    password = password,
                    apiKey = API_KEY,
                    type = ""
                )
            )
        }
    }
}
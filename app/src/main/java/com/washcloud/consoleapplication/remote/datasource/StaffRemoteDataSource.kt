package com.washcloud.consoleapplication.remote.datasource

import android.content.Context
import com.washcloud.consoleapplication.local.preferences.PrefsManager

import com.washcloud.consoleapplication.remote.config.IApiProvider
import com.washcloud.consoleapplication.remote.config.IRetrofitService
import com.washcloud.consoleapplication.remote.model.dropoff.StaffDropoffRequest
import com.washcloud.consoleapplication.remote.model.dropoff.StaffDropoffResponse
import com.washcloud.consoleapplication.remote.model.dropoff.StaffRecallRequest
import com.washcloud.consoleapplication.remote.model.dropoff.StaffRecallResponse
import com.washcloud.consoleapplication.remote.model.login.StaffLoginRequest
import com.washcloud.consoleapplication.remote.model.login.StaffLoginResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class StaffRemoteDataSource @Inject constructor(
    private val retrofitService: IRetrofitService,
    private val apiProvider: IApiProvider,
    @ApplicationContext private val context: Context
) : IStaffRemoteDataSource {
    override suspend fun login(account: String, password: String): StaffLoginResponse {
        return apiProvider.proceedRequest {
            retrofitService.staffLogin(
                    account = account,
                    password = password,
                    apiKey = PrefsManager.getApiKey(context),
                    type = ""
            )
        }
    }

    override suspend fun staffDropOff(request: StaffDropoffRequest): StaffDropoffResponse {
        return apiProvider.proceedRequest {
            retrofitService.staffDropOff(
                apiKey = request.apiKey,
                wayBillNo = request.wayBillNo,
                terminalSn = request.terminalSn,
                type = request.type,
                doorNo = request.doorNo
            )
        }

    }

    override suspend fun staffRecall(request: StaffRecallRequest): StaffRecallResponse {
        return apiProvider.proceedRequest {
            retrofitService.staffRecall(
                apiKey = request.apiKey,
                wayBillNo = request.wayBillNo,
                terminalSn = request.terminalSn,
                type = request.type,
                doorNo = request.doorNo
            )
        }
    }
}
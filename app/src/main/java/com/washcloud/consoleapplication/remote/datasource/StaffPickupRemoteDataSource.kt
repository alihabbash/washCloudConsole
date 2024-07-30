package com.washcloud.consoleapplication.remote.datasource


import com.washcloud.consoleapplication.remote.config.ApiProvider
import com.washcloud.consoleapplication.remote.config.IApiProvider
import com.washcloud.consoleapplication.remote.model.pickup.StaffPickupRequest
import com.washcloud.consoleapplication.remote.model.pickup.StaffPickupResponse
import com.washcloud.consoleapplication.remote.config.IRetrofitService

import javax.inject.Inject

class StaffPickupRemoteDataSource @Inject constructor(
    private val retrofitService: IRetrofitService,
    private val apiProvider: IApiProvider
) : IStaffPickupRemoteDataSource {
    override suspend fun staffPickup(request: StaffPickupRequest): StaffPickupResponse {
        return apiProvider.proceedRequest {
            retrofitService.staffPickup(
                apiKey = request.apiKey,
                wayBillNo = request.wayBillNo,
                terminalSn = request.terminalSn,
                type = request.type,
                doorNo = request.doorNo
            )
        }
    }
}
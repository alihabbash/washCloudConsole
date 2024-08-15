package com.washcloud.consoleapplication.remote.datasource

import com.washcloud.consoleapplication.remote.config.IApiProvider
import com.washcloud.consoleapplication.remote.config.IRetrofitService
import com.washcloud.consoleapplication.remote.model.dropoff.StaffDropoffRequest
import com.washcloud.consoleapplication.remote.model.dropoff.StaffDropoffResponse
import com.washcloud.consoleapplication.remote.model.pickup.StaffPickupRequest
import com.washcloud.consoleapplication.remote.model.pickup.StaffPickupResponse
import javax.inject.Inject


class StaffDropoffRemoteDataSource @Inject constructor(
    private val retrofitService: IRetrofitService,
    private val apiProvider: IApiProvider
) : IStaffDropoffRemoteDataSource {
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
}
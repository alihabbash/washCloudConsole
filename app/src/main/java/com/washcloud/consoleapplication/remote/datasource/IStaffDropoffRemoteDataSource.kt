package com.washcloud.consoleapplication.remote.datasource

import com.washcloud.consoleapplication.remote.model.dropoff.StaffDropoffRequest
import com.washcloud.consoleapplication.remote.model.dropoff.StaffDropoffResponse
import com.washcloud.consoleapplication.remote.model.pickup.StaffPickupRequest


interface IStaffDropoffRemoteDataSource {
    suspend fun staffDropOff(request: StaffDropoffRequest): StaffDropoffResponse
}
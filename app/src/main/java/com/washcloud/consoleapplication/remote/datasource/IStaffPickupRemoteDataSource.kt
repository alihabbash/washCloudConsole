package com.washcloud.consoleapplication.remote.datasource

import com.washcloud.consoleapplication.remote.model.pickup.StaffPickupRequest
import com.washcloud.consoleapplication.remote.model.pickup.StaffPickupResponse


interface IStaffPickupRemoteDataSource {
    suspend fun staffPickup(request: StaffPickupRequest): StaffPickupResponse
}

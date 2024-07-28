package com.washcloud.consoleapplication.repository

import com.washcloud.consoleapplication.remote.model.pickup.StaffPickupRequest
import com.washcloud.consoleapplication.remote.model.pickup.StaffPickupResponse

interface IStaffPickupRepository {
    suspend fun staffPickup(request: StaffPickupRequest): StaffPickupResponse
}
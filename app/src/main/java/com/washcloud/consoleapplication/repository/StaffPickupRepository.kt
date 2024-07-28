package com.washcloud.consoleapplication.repository

import com.washcloud.consoleapplication.remote.datasource.StaffPickupRemoteDataSource
import com.washcloud.consoleapplication.remote.model.pickup.StaffPickupRequest
import com.washcloud.consoleapplication.remote.model.pickup.StaffPickupResponse
import javax.inject.Inject

class StaffPickupRepository @Inject constructor(
    private val remoteDataSource: StaffPickupRemoteDataSource
) : IStaffPickupRepository {
    override suspend fun staffPickup(request: StaffPickupRequest): StaffPickupResponse {
        return remoteDataSource.staffPickup(request)
    }
}
package com.washcloud.consoleapplication.remote.usecase

import com.washcloud.consoleapplication.remote.model.pickup.StaffPickupRequest
import com.washcloud.consoleapplication.remote.model.pickup.StaffPickupResponse
import com.washcloud.consoleapplication.repository.IStaffPickupRepository
import com.washcloud.consoleapplication.repository.StaffPickupRepository
import javax.inject.Inject


class StaffPickupUseCase @Inject constructor(
    private val repository: StaffPickupRepository
) {
    suspend operator fun invoke(request: StaffPickupRequest): StaffPickupResponse {
        return repository.staffPickup(request)
    }
}
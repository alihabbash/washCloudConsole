package com.washcloud.consoleapplication.remote.usecase


import com.washcloud.consoleapplication.remote.model.dropoff.StaffDropoffRequest
import com.washcloud.consoleapplication.remote.model.dropoff.StaffDropoffResponse
import com.washcloud.consoleapplication.repository.StaffRepository
import javax.inject.Inject

class StaffDropoffUseCase @Inject constructor(
    private val repository: StaffRepository
) {
    suspend operator fun invoke(request: StaffDropoffRequest): StaffDropoffResponse {
        return repository.staffDropOff(request)
    }
}
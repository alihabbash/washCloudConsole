package com.washcloud.consoleapplication.remote.usecase

import com.washcloud.consoleapplication.remote.datasource.IStaffRemoteDataSource
import com.washcloud.consoleapplication.remote.model.dropoff.StaffRecallRequest
import com.washcloud.consoleapplication.remote.model.dropoff.StaffRecallResponse
import com.washcloud.consoleapplication.repository.StaffPickupRepository
import com.washcloud.consoleapplication.repository.StaffRepository
import javax.inject.Inject

class StaffRecallUseCase @Inject constructor(
    private val repository: StaffRepository
) {

    suspend operator fun invoke(request: StaffRecallRequest): StaffRecallResponse {
        return repository.staffRecall(request)
    }
}
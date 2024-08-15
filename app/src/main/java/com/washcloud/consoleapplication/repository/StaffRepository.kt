package com.washcloud.consoleapplication.repository

import com.washcloud.consoleapplication.remote.datasource.StaffRemoteDataSource
import com.washcloud.consoleapplication.remote.model.dropoff.StaffDropoffRequest
import com.washcloud.consoleapplication.remote.model.dropoff.StaffDropoffResponse
import com.washcloud.consoleapplication.remote.model.login.StaffLoginResponse
import javax.inject.Inject

class StaffRepository @Inject constructor(
    private val staffRemoteDataSource: StaffRemoteDataSource
): IStaffRepository{
    override suspend fun login(account: String, password: String): StaffLoginResponse {
        return staffRemoteDataSource.login(account, password)
    }

    override suspend fun staffDropOff(request: StaffDropoffRequest): StaffDropoffResponse {
        return  staffRemoteDataSource.staffDropOff(request)
    }
}
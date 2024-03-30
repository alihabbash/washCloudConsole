package com.washcloud.consoleapplication.repository

import com.washcloud.consoleapplication.remote.datasource.StaffRemoteDataSource
import com.washcloud.consoleapplication.remote.model.login.StaffLoginResponse
import javax.inject.Inject

class StaffRepository @Inject constructor(
    private val staffRemoteDataSource: StaffRemoteDataSource
): IStaffRepository{
    override suspend fun login(account: String, password: String): StaffLoginResponse {
        return staffRemoteDataSource.login(account, password)
    }
}
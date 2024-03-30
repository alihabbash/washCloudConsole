package com.washcloud.consoleapplication.remote.datasource

import com.washcloud.consoleapplication.remote.model.login.StaffLoginResponse

interface IStaffRemoteDataSource {

    suspend fun login(account: String, password: String): StaffLoginResponse

}
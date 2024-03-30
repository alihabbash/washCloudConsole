package com.washcloud.consoleapplication.repository

import com.washcloud.consoleapplication.remote.model.login.StaffLoginResponse

interface IStaffRepository {
    suspend fun login(account: String, password: String): StaffLoginResponse

}
package com.washcloud.consoleapplication.repository

import com.washcloud.consoleapplication.remote.model.dropoff.StaffDropoffRequest
import com.washcloud.consoleapplication.remote.model.dropoff.StaffDropoffResponse
import com.washcloud.consoleapplication.remote.model.login.StaffLoginResponse

interface IStaffRepository {
    suspend fun login(account: String, password: String): StaffLoginResponse
    suspend fun staffDropOff(request: StaffDropoffRequest): StaffDropoffResponse

}
package com.washcloud.consoleapplication.remote.datasource

import com.washcloud.consoleapplication.remote.model.dropoff.StaffDropoffRequest
import com.washcloud.consoleapplication.remote.model.dropoff.StaffDropoffResponse
import com.washcloud.consoleapplication.remote.model.dropoff.StaffRecallRequest
import com.washcloud.consoleapplication.remote.model.dropoff.StaffRecallResponse
import com.washcloud.consoleapplication.remote.model.login.StaffLoginResponse

interface IStaffRemoteDataSource {

    suspend fun login(account: String, password: String): StaffLoginResponse
    suspend fun staffDropOff(request: StaffDropoffRequest): StaffDropoffResponse
    suspend fun staffRecall(request: StaffRecallRequest): StaffRecallResponse

}
package com.washcloud.consoleapplication.repository

import com.washcloud.consoleapplication.remote.datasource.HeartbeatRemoteDataSource
import com.washcloud.consoleapplication.remote.datasource.IHeartbeatRemoteDataSource
import com.washcloud.consoleapplication.remote.model.heartbeat.HeartbeatRequest
import com.washcloud.consoleapplication.remote.model.heartbeat.HeartbeatResponse
import javax.inject.Inject

class HeartbeatRepository @Inject constructor(
    private val heartbeatRemoteDataSource: HeartbeatRemoteDataSource
): IHeartbeatRepository {
    override suspend fun sendHeartbeat(request: HeartbeatRequest): HeartbeatResponse {
        return heartbeatRemoteDataSource.sendHeartbeat(request)
    }
}
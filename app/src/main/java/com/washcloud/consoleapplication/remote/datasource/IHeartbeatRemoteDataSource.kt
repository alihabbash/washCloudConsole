package com.washcloud.consoleapplication.remote.datasource

import com.washcloud.consoleapplication.remote.model.heartbeat.HeartbeatRequest
import com.washcloud.consoleapplication.remote.model.heartbeat.HeartbeatResponse

interface IHeartbeatRemoteDataSource {
    suspend fun sendHeartbeat(request: HeartbeatRequest): HeartbeatResponse
}
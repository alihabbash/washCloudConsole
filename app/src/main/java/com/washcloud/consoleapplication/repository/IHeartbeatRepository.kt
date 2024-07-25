package com.washcloud.consoleapplication.repository

import com.washcloud.consoleapplication.remote.model.heartbeat.HeartbeatRequest
import com.washcloud.consoleapplication.remote.model.heartbeat.HeartbeatResponse

interface IHeartbeatRepository {
    suspend fun sendHeartbeat(request: HeartbeatRequest): HeartbeatResponse
}
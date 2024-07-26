package com.washcloud.consoleapplication.remote.datasource

import com.washcloud.consoleapplication.remote.config.IApiProvider
import com.washcloud.consoleapplication.remote.config.IRetrofitService
import com.washcloud.consoleapplication.remote.model.heartbeat.HeartbeatRequest
import com.washcloud.consoleapplication.remote.model.heartbeat.HeartbeatResponse
import javax.inject.Inject


class HeartbeatRemoteDataSource @Inject constructor(
    private val retrofitService: IRetrofitService,
    private val apiProvider: IApiProvider
) : IHeartbeatRemoteDataSource {
    override suspend fun sendHeartbeat(request: HeartbeatRequest): HeartbeatResponse {
        return apiProvider.proceedRequest {
            retrofitService.sendHeartbeat(request)
        }
    }
}
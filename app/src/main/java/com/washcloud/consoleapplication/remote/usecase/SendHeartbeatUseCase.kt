package com.washcloud.consoleapplication.remote.usecase

import com.washcloud.consoleapplication.remote.model.heartbeat.HeartbeatRequest
import com.washcloud.consoleapplication.remote.model.heartbeat.HeartbeatResponse
import com.washcloud.consoleapplication.repository.HeartbeatRepository
import com.washcloud.consoleapplication.utils.ParamsUseCase
import javax.inject.Inject


class SendHeartbeatUseCase @Inject constructor(
    private val heartbeatRepository: HeartbeatRepository
) : ParamsUseCase<SendHeartbeatUseCase.Params, HeartbeatResponse> {

    override suspend fun invoke(params: Params): HeartbeatResponse {
        return heartbeatRepository.sendHeartbeat(
            HeartbeatRequest(
                apiKey = params.apiKey,
                terminalSn = params.terminalSn,
                boxes = params.boxes
            )
        )
    }

    data class Params(
        val apiKey: String,
        val terminalSn: String,
        val boxes: List<HeartbeatRequest.Box>
    )
}
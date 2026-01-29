package com.washcloud.consoleapplication.ui.mainad


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.washcloud.consoleapplication.di.DatabaseModule
import com.washcloud.consoleapplication.local.database.dao.BoxDao
import com.washcloud.consoleapplication.local.database.dto.BoxDto
import com.washcloud.consoleapplication.local.database.utils.BoxSizeType
import com.washcloud.consoleapplication.local.database.utils.BoxState
import com.washcloud.consoleapplication.local.database.utils.BoxType
import com.washcloud.consoleapplication.local.database.utils.TransactionType
import com.washcloud.consoleapplication.remote.model.heartbeat.HeartbeatRequest
import com.washcloud.consoleapplication.remote.usecase.SendHeartbeatUseCase
import dagger.hilt.android.lifecycle.HiltViewModel

import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class HeartbeatViewModel @Inject constructor(
    private val sendHeartbeatUseCase: SendHeartbeatUseCase,
    private val boxDao: BoxDao
) : ViewModel() {


    fun initialize(context: Context) {
        // Did nothing
    }

    fun sendHeartbeat(apiKey: String, terminalSn: String) {

       println("sendHeartbeat")
        viewModelScope.launch {

            val boxesDto = boxDao.getAllBoxes()
            val boxes = boxesDto.map { mapBoxDtoToHeartbeatBox(it) }

            runCatching {
                sendHeartbeatUseCase(
                    SendHeartbeatUseCase.Params(
                        apiKey = apiKey,
                        terminalSn = terminalSn,
                        boxes = boxes
                    )
                )
            }.onSuccess {
                println("sendHeartbeat success")

            }.onFailure {
                println("sendHeartbeat falied: ${it.message}")
            }
        }
    }

    private fun mapBoxDtoToHeartbeatBox(boxDto: BoxDto): HeartbeatRequest.Box {
        return HeartbeatRequest.Box(
            no = boxDto.boxId.toString(),
            status = if (boxDto.boxState == BoxState.AVAILABLE) "1" else "0",
            occupied = if (boxDto.trnasType == TransactionType.PICKUP) "1" else "0",
            size = when (boxDto.boxSize) {
                BoxSizeType.LARGE -> 0
                BoxSizeType.MEDIUM -> 1
                BoxSizeType.SMALL -> 2
                BoxSizeType.X_LARGE -> 4
                BoxSizeType.X_SMALL -> 5
                BoxSizeType.CONVEYOR -> 6
            },
            open = "1",
            type = if (boxDto.boxType == BoxType.BOX) 1 else 2
        )
    }
}

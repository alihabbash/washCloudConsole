package com.washcloud.consoleapplication

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.washcloud.consoleapplication.di.DatabaseModule
import com.washcloud.consoleapplication.local.database.dao.BoxDao
import com.washcloud.consoleapplication.local.database.dto.BoxDto
import com.washcloud.consoleapplication.local.database.utils.BoxSizeType
import com.washcloud.consoleapplication.local.database.utils.BoxState
import com.washcloud.consoleapplication.local.database.utils.BoxType
import com.washcloud.consoleapplication.local.database.utils.TransactionType
import com.washcloud.consoleapplication.local.preferences.PrefsManager
import com.washcloud.consoleapplication.remote.model.heartbeat.HeartbeatRequest
import com.washcloud.consoleapplication.remote.usecase.SendHeartbeatUseCase
import com.washcloud.consoleapplication.ui.mainad.HeartbeatViewModel
import com.washcloud.consoleapplication.utils.FileLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HeartbeatReceiver : BroadcastReceiver() {

    @Inject
    lateinit var sendHeartbeatUseCase: SendHeartbeatUseCase
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    override fun onReceive(context: Context, intent: Intent) {
        val apiKey = intent.getStringExtra("API_KEY")
        val terminalSn = intent.getStringExtra("TERMINAL_SN")

        if (apiKey != null && terminalSn != null) {
            sendHeartbeat(context)
        }
    }


    @Inject
    lateinit var boxDao: BoxDao

    private fun sendHeartbeat(context: Context) {
        try {

            scope.launch {
                try {
                    val boxesDto = boxDao.getAllBoxes()
            val boxes = boxesDto.map { mapBoxDtoToHeartbeatBox(it) }

                    val response = sendHeartbeatUseCase(
                        SendHeartbeatUseCase.Params(
                            apiKey = PrefsManager.getApiKey(context),
                            terminalSn = PrefsManager.getTerminalSN(context),
                            boxes = boxes
                        )
                    )

                    FileLogger.log(context, "HeartbeatReceiver", "Heartbeat sent successfully: $response")
                } catch (e: Exception) {
                    FileLogger.log(context, "HeartbeatReceiver", "Error sending heartbeat: ${e.message}")
                }
            }
        } catch (e: Exception) {
            FileLogger.log(context, "HeartbeatReceiver", "Error initializing database: ${e.message}")
        }
    }


    private fun mapBoxDtoToHeartbeatBox(boxDto: BoxDto): HeartbeatRequest.Box {
        return HeartbeatRequest.Box(
            no = boxDto.boxId.toString(),
            occupied = if (boxDto.boxState == BoxState.AVAILABLE) "0" else "1",
            status = "1",
            size = when (boxDto.boxSize) {
                BoxSizeType.LARGE -> 0
                BoxSizeType.MEDIUM -> 1
                BoxSizeType.SMALL -> 2
                BoxSizeType.X_LARGE -> 4
                BoxSizeType.X_SMALL -> 5
                BoxSizeType.CONVEYOR -> 6
            },
            open = "0",
            type = if (boxDto.boxType == BoxType.BOX) 1 else 2,
            date = boxDto.trnasDate.time,
            orderID = boxDto.orderId
        )
    }
}

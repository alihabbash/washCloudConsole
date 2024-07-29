package com.washcloud.consoleapplication

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.washcloud.consoleapplication.di.DatabaseModule
import com.washcloud.consoleapplication.local.database.dto.BoxDto
import com.washcloud.consoleapplication.local.database.utils.BoxSizeType
import com.washcloud.consoleapplication.local.database.utils.BoxState
import com.washcloud.consoleapplication.local.database.utils.BoxType
import com.washcloud.consoleapplication.local.database.utils.TransactionType
import com.washcloud.consoleapplication.local.preferences.API_KEY
import com.washcloud.consoleapplication.local.preferences.TERMINAL_SN
import com.washcloud.consoleapplication.remote.model.heartbeat.HeartbeatRequest
import com.washcloud.consoleapplication.remote.usecase.SendHeartbeatUseCase
import com.washcloud.consoleapplication.ui.mainad.HeartbeatViewModel
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


    private fun sendHeartbeat(context: Context) {
        val boxDao = DatabaseModule.provideConsoleDatabase(context.applicationContext).getBoxDao()

        scope.launch(Dispatchers.IO) {

            /*val boxesDto = boxDao.getAllBoxes()
            val boxes = boxesDto.map { mapBoxDtoToHeartbeatBox(it) }*/

            val boxes = listOf(
                HeartbeatRequest.Box(
                    no = "1",
                    occupied = "1",
                    status = "1",
                    size = 0,
                    open = "0",
                    type = 1
                ),
                HeartbeatRequest.Box(
                    no = "2",
                    occupied = "0",
                    status = "1",
                    size = 1,
                    open = "0",
                    type = 1
                ),
                HeartbeatRequest.Box(
                    no = "3",
                    occupied = "1",
                    status = "1",
                    size = 2,
                    open = "0",
                    type = 1
                ),
                HeartbeatRequest.Box(
                    no = "4",
                    occupied = "0",
                    status = "1",
                    size = 4,
                    open = "0",
                    type = 1
                ),
                HeartbeatRequest.Box(
                    no = "5",
                    occupied = "1",
                    status = "1",
                    size = 5,
                    open = "0",
                    type = 1
                ),
                HeartbeatRequest.Box(
                    no = "6",
                    occupied = "0",
                    status = "1",
                    size = 6,
                    open = "0",
                    type = 1
                ),

            )

            sendHeartbeatUseCase(
                SendHeartbeatUseCase.Params(
                    apiKey = API_KEY,
                    terminalSn = TERMINAL_SN,
                    boxes = boxes
                )
            )
        }
    }


    private fun mapBoxDtoToHeartbeatBox(boxDto: BoxDto): HeartbeatRequest.Box {
        return HeartbeatRequest.Box(
            no = boxDto.boxId.toString(),
            occupied = if (boxDto.boxState == BoxState.AVAILABLE) "1" else "0",
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
            type = if (boxDto.boxType == BoxType.BOX) 1 else 2
        )
    }
}

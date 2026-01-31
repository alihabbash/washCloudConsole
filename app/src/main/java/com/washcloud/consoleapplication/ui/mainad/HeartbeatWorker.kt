package com.washcloud.consoleapplication.ui.mainad

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.washcloud.consoleapplication.di.DatabaseModule
import com.washcloud.consoleapplication.local.database.dao.BoxDao
import com.washcloud.consoleapplication.local.database.dto.BoxDto
import com.washcloud.consoleapplication.local.database.utils.BoxSizeType
import com.washcloud.consoleapplication.local.database.utils.BoxState
import com.washcloud.consoleapplication.local.database.utils.BoxType
import com.washcloud.consoleapplication.local.database.utils.TransactionType
import com.washcloud.consoleapplication.remote.model.heartbeat.HeartbeatRequest
import com.washcloud.consoleapplication.remote.usecase.SendHeartbeatUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
@HiltWorker
class HeartbeatWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val sendHeartbeatUseCase: SendHeartbeatUseCase,
    private val boxDao: BoxDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val apiKey = inputData.getString("API_KEY") ?: return Result.failure()
        val terminalSn = inputData.getString("TERMINAL_SN") ?: return Result.failure()

        val boxDao = DatabaseModule.provideConsoleDatabase(applicationContext).getBoxDao()
        val boxesDto = boxDao.getAllBoxes()
        val boxes = boxesDto.map { mapBoxDtoToHeartbeatBox(it) }

        println("doWork ....")
        sendHeartbeatUseCase(
            SendHeartbeatUseCase.Params(
                apiKey = apiKey,
                terminalSn = terminalSn,
                boxes = boxes
            )
        )

        return Result.success()
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


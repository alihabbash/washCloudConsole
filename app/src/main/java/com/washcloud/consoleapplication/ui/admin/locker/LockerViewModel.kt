package com.washcloud.consoleapplication.ui.admin.locker

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.washcloud.consoleapplication.di.DatabaseModule
import com.washcloud.consoleapplication.hardware.SerialPortService
import com.washcloud.consoleapplication.local.database.dto.BoxDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tp.xmaihh.serialport.SerialHelper
import tp.xmaihh.serialport.bean.ComBean
import tp.xmaihh.serialport.utils.ByteUtil
import android_serialport_api.SerialPortFinder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.washcloud.consoleapplication.local.database.utils.BoxSizeType
import com.washcloud.consoleapplication.local.database.utils.BoxState
import com.washcloud.consoleapplication.local.database.utils.BoxType
import com.washcloud.consoleapplication.local.database.utils.TransactionType
import com.washcloud.consoleapplication.repository.BroadcastReceiverRepository
import com.washcloud.consoleapplication.utils.FileLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class LockerViewModel @Inject constructor(
    application: Application,
    private val broadcastReceiverRepository: BroadcastReceiverRepository
) : AndroidViewModel(application) {

    private val context: Context = getApplication<Application>().applicationContext

    private val boxDao = DatabaseModule.provideConsoleDatabase(context).getBoxDao()

    private val _lockers = MutableStateFlow<List<BoxDto>>(emptyList())
    val lockers: StateFlow<List<BoxDto>> = _lockers.asStateFlow()

    private val _boxesToInsert = MutableLiveData<List<BoxDto>>()
    val boxesToInsert: LiveData<List<BoxDto>> = _boxesToInsert

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage


    private val _lockerStatuses = MutableStateFlow<List<Pair<String, Boolean>>>(emptyList())
    val lockerStatuses: StateFlow<List<Pair<String, Boolean>>> = _lockerStatuses.asStateFlow()


    init {
        fetchLockers()
        registerLockerStatusReceiver()

    }

    private fun registerLockerStatusReceiver() {
        val filter = IntentFilter().apply {
            addAction("com.washcloud.door_status")
        }
        broadcastReceiverRepository.registerReceiver(filter)

        viewModelScope.launch {
            broadcastReceiverRepository.broadcastFlow.collectLatest { intent ->
                handleBroadcastIntent(intent)
            }
        }
    }

    private fun handleBroadcastIntent(intent: Intent) {
        println("LockerViewModel: Received broadcast intent: ${intent.action}")
        FileLogger.log(context, "LockerViewModel", "Received broadcast intent: ${intent.action}")
        when (intent.action) {
            "com.washcloud.door_status" -> {
                val isOpen = intent.getBooleanExtra("status", false)
                val stationId = intent.getStringExtra("stationId")
                val boxId = intent.getStringExtra("boxId")
              println("LockerViewModel: Station ID: $stationId, Box ID: $boxId, Door is open: $isOpen")
                FileLogger.log(context, "LocketViewModel", "LockerViewModel: Station ID: $stationId, Box ID: $boxId, Door is open: $isOpen")

                _lockerStatuses.value = _lockerStatuses.value.toMutableList().apply {
                    removeAll { it.first == boxId }
                    add((boxId to isOpen) as Pair<String, Boolean>)
                }

            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        broadcastReceiverRepository.unregisterReceiver()
    }


     fun sendMockDoorStatusBrodcast(boxId: String) {
        val intent = Intent("com.washcloud.door_status").apply {
            putExtra("stationId", "02")
            putExtra("boxId", boxId)
            putExtra("status", boxId.toInt()%2 ==0)
            putExtra("data", "900785010101")
        }
        Log.e("LockersViewModel", "sendDoorStatusBrodcast: ${intent.action}")
        context.sendBroadcast(intent)

    }

    fun checkAllLockerStatuses() {

        FileLogger.log(context, "LockerViewModel", "Checking all locker statuses")
        viewModelScope.launch {
            val allLockers = lockers.value.filter { it.boxType == BoxType.BOX }

            FileLogger.log(context, "LockerViewModel", "Checking all locker statuses: ${allLockers.size} lockers  ${allLockers.map { it.boxId }}")
            for (locker in allLockers) {
                delay(100)
                sendCommand("com.washcloud.check_door", locker.boxId.toString())

                delay(2000)
                sendMockDoorStatusBrodcast(locker.boxId.toString());
            }


        }
    }

    fun clearLockerStatuses() {
        _lockerStatuses.value = emptyList()
    }



    fun fetchLockers() {
        viewModelScope.launch {
            _lockers.value = boxDao.getAllBoxes()
        }
    }

    fun addLocker(
        boxId: Long,
        branchId: Long,
        stationId: Long,
        portId: String,
        boxSize: BoxSizeType,
        boxType: BoxType
    ) {

        val existingLocker = _lockers.value.find { it.boxId == boxId }

        viewModelScope.launch(Dispatchers.IO) {

            if (existingLocker == null) {
                val locker = BoxDto(
                    orderSerial = "",
                    orderId = 0L,
                    boxId = boxId,
                    trnasDate = Date(),
                    branchId = branchId,
                    trnasType = TransactionType.DROP_OFF,
                    boxSize = boxSize,
                    boxType = boxType,
                    boxState = BoxState.AVAILABLE,
                    stationId = stationId,
                    portId = portId
                )
                boxDao.insertBox(locker)
                fetchLockers()

            }else{
                _errorMessage.value = " تم الإضافة مسبقاً"
                FileLogger.log(context, "Locker with box ID $boxId already exists. Ignoring insertion.", "LockerViewModel")
            }

        }

    }

    fun resetInsertBoxes() {
        _boxesToInsert.value = emptyList()
    }

    fun deleteLocker(lockerNumber: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val box = boxDao.getBoxById(lockerNumber.toLong(), boxType = BoxType.BOX.toString())
            if (box?.boxState == BoxState.AVAILABLE && box.boxType == BoxType.BOX) {
                boxDao.deleteBox(lockerNumber.toLong())
                fetchLockers()
            } else {
            }
        }
    }
//
//    fun loadCsvFile(uri: Uri) {
//        try {
//            val inputStream = context.contentResolver.openInputStream(uri)
//            val reader = BufferedReader(InputStreamReader(inputStream))
//
//            val boxes = mutableListOf<BoxDto>()
//            reader.forEachLine { line ->
//                val fields = line.split(",")
//                if (fields.size == 6) {
//                    val boxDto = BoxDto(
//                        orderSerial = "",
//                        orderId = 0,
//                        boxId =  fields[0].toLong(),
//                        trnasDate = Date(),
//                        branchId = 1L,
//                        trnasType = TransactionType.DROP_OFF,
//                        boxSize = BoxSizeType.valueOf(fields[4]),
//                        boxType = BoxType.valueOf(fields[3]),
//                        boxState = BoxState.valueOf(fields[5]),
//                        stationId = fields[1].toLong(),
//                        portId = fields[2],
//                    )
//                    boxes.add(boxDto)
//                } else {
//                    _errorMessage.value = "Invalid CSV format."
//
//                }
//            }
//
//            _boxesToInsert.value = boxes
//        } catch (e: Exception) {
//            _errorMessage.value = "Error loading CSV: ${e.message}"
//        }
//    }


    fun loadCsvFile(context: Context, fileUri: Uri) {
        val boxes = mutableListOf<BoxDto>()


        try {
            val existingBoxIds = _lockers.value.map { it.boxId }.toSet()
            val inputStream = context.contentResolver.openInputStream(fileUri)
            val bufferedReader = inputStream?.bufferedReader()
            bufferedReader?.useLines { lines ->
                lines.drop(1)
                    .forEach { line ->
                        val columns = line.split(";")
                        if (columns.size == 12) {
                            val boxId = columns[3].toLong()

                            if (boxId !in existingBoxIds) {
                                val box = BoxDto(
                                    orderSerial = "",
                                    orderId = 0L,
                                    boxId = boxId,
                                    trnasDate = Date(),
                                    branchId = columns[5].toLong(),
                                    trnasType = TransactionType.valueOf(columns[6]),
                                    boxSize = BoxSizeType.valueOf(columns[7]),
                                    boxType = BoxType.valueOf(columns[8]),
                                    boxState = BoxState.valueOf(columns[9]),
                                    stationId = columns[10].toLong(),
                                    portId = columns[11]
                                )
                                boxes.add(box)
                            } else {
                                FileLogger.log(context, "Duplicate box ID $boxId found. Ignoring.", "LockerViewModel")
                            }
                        }else{
                            _errorMessage.value = "Invalid CSV format."
                            FileLogger.log(context, "Invalid CSV format.", "LockerViewModel")
                        }
                    }
            }
        } catch (e: Exception) {
            _errorMessage.value = "Invalid CSV format."
            FileLogger.log(context, "Error loading CSV: ${e.message}", "LockerViewModel")
            e.printStackTrace()
        }

        if (boxes.isEmpty()) {
            _errorMessage.value = "لم يتم العثور على صناديق في ملف CSV او تم الإضافة مسبقاً"
            FileLogger.log(context, "No boxes found in the CSV file.", "LockerViewModel")
        }else{
            FileLogger.log(context, "Boxes found in the CSV file: ${boxes.size}", "LockerViewModel")
            _boxesToInsert.value = boxes
        }
    }



    fun insertBoxes(boxes: List<BoxDto>) {
        viewModelScope.launch(Dispatchers.IO) {

            boxes.forEach { box ->
                boxDao.insertBox(box)
            }
        }
    }
    fun sendCommand(action: String, boxId: String) {
        val intent = Intent(action).apply {
            val stationId =   lockers.value
                .filter {it.boxType == BoxType.BOX}
                .filter { it.boxId == boxId.toLong() }
                .map { it.stationId }
                .firstOrNull()
            FileLogger.log(context, "LockerViewModel", "Sending command to open door: stationId: 0$stationId, boxId: 0$boxId")
            putExtra("stationId", "0"+stationId.toString())
            putExtra("boxId", "0"+boxId)
        }
        context.sendBroadcast(intent)
    }

    fun openAllEmptyLockers() {
        viewModelScope.launch {
            lockers.value.filter { it.boxState == BoxState.AVAILABLE && it.boxType == BoxType.BOX}.forEach { locker ->
                sendCommand("com.washcloud.open_door", locker.boxId.toString())
            }
        }
    }

    fun openAllOccupiedLockers() {
        viewModelScope.launch {
            lockers.value.filter { it.boxState == BoxState.OCCUPIED && it.boxType == BoxType.BOX }.forEach { locker ->
                sendCommand("com.washcloud.open_door", locker.boxId.toString())
            }
        }
    }


     fun openConveyor(boxID: String) {
     FileLogger.log(context, "DropOffViewModel", "Sending command to open conveyor")
        val intent = Intent("com.washcloud.conveyor_open").apply {
            putExtra("conveyorNumber","0${boxID}");
        }

        context.sendBroadcast(intent)
    }

    fun openConveyorDoor() {
       FileLogger.log(context, "LockerViewModel", "Sending command to open conveyor")
        val intent = Intent("com.washcloud.conveyor_open_door").apply {
        }

        context.sendBroadcast(intent)
    }

    fun closeConveyorDoor() {
        FileLogger.log(context, "LockerViewModel", "Sending command to close conveyor")
        val intent = Intent("com.washcloud.conveyor_close").apply {
        }
        context.sendBroadcast(intent)
    }




}

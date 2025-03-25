package com.washcloud.consoleapplication.ui.dropoff
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.washcloud.consoleapplication.local.database.dao.BoxDao
import com.washcloud.consoleapplication.local.database.dao.TransactionDao
import com.washcloud.consoleapplication.local.database.dto.BoxDto
import com.washcloud.consoleapplication.local.database.dto.TransactionDto
import com.washcloud.consoleapplication.local.database.utils.BoxState
import com.washcloud.consoleapplication.local.database.utils.BoxType
import com.washcloud.consoleapplication.local.database.utils.TransactionType
import com.washcloud.consoleapplication.local.preferences.PrefsManager
import com.washcloud.consoleapplication.remote.model.dropoff.StaffDropoffRequest
import com.washcloud.consoleapplication.remote.model.dropoff.StaffDropoffResponse
import com.washcloud.consoleapplication.remote.model.dropoff.StaffRecallRequest
import com.washcloud.consoleapplication.remote.model.dropoff.StaffRecallResponse
import com.washcloud.consoleapplication.remote.usecase.StaffDropoffUseCase
import com.washcloud.consoleapplication.remote.usecase.StaffRecallUseCase
import com.washcloud.consoleapplication.repository.BroadcastReceiverRepository
import com.washcloud.consoleapplication.utils.FileLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class DropOffViewModel @Inject constructor(
    private val transactionDao: TransactionDao,
    private val staffDropoffUseCase: StaffDropoffUseCase,
    private val staffRecallUseCase: StaffRecallUseCase,
    private val boxDao: BoxDao,
    private val broadcastReceiverRepository: BroadcastReceiverRepository,
    application: Application
) : AndroidViewModel(application)  {

    private  val context: Context = getApplication<Application>().applicationContext
    private val _transactions = MutableStateFlow<List<BoxDto>>(emptyList())
    val transactions: StateFlow<List<BoxDto>> get() = _transactions
    private val _lockers = MutableStateFlow<List<BoxDto>>(emptyList())
    val lockers: StateFlow<List<BoxDto>> get() = _lockers

    val _staffDropoffResponse = MutableStateFlow<StaffDropoffResponse?>(null)
    val staffDropoffResponse: StateFlow<StaffDropoffResponse?> get() = _staffDropoffResponse

    val _staffRecallResponse = MutableStateFlow<StaffRecallResponse?>(null)
    val staffRecallResponse: StateFlow<StaffRecallResponse?> get() = _staffRecallResponse

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    private val _isSuccessed = MutableStateFlow<Boolean>(false)
    val isSuccessed: StateFlow<Boolean> get() = _isSuccessed


    private val _scannedWaybill = MutableStateFlow("")
    val scannedWaybill: StateFlow<String> get() = _scannedWaybill


    private val _showAlert = MutableStateFlow(false)
    val showAlert: StateFlow<Boolean> get() = _showAlert
    private var conveyorStatusReceived: Boolean = false
    private var isConveyorDoorOpen: Boolean = false


    init {
        fetchTransactions()
        fetchLockers()
        registerBroadcasts()

    }


    private fun registerBroadcasts() {
        val filter = IntentFilter().apply {
            addAction("com.washcloud.scanner_data")
            addAction("com.washcloud.conveyor_move")
            addAction("com.washcloud.conveyor_door_status")
        }
        broadcastReceiverRepository.registerReceiver(filter)

        viewModelScope.launch {
            broadcastReceiverRepository.broadcastFlow.collectLatest { intent ->
                FileLogger.log(context, "DropOffViewModel", "Received broadcast: $intent")

                when (intent.action) {
                    "com.washcloud.scanner_data" -> {
                        val scannerData = intent.getStringExtra("scannerData")
                        FileLogger.log(context, "DropOffViewModel", "Received scanner data: $scannerData")
                        _scannedWaybill.value = scannerData ?: ""
                    }

                    "com.washcloud.conveyor_move" -> {
                        val status = intent.getStringExtra("status")
                        FileLogger.log(context, "DropOffViewModel", "Conveyor move status: $status")
                        conveyorStatusReceived = true

                        if (status == "open") {
                            openConveyorDoor()
                        }
                    }

                    "com.washcloud.conveyor_door_status" -> {
                        val status = intent.getStringExtra("status")
                        FileLogger.log(context, "DropOffViewModel", "Conveyor door status: $status")
                        if (status == "open") {
                            isConveyorDoorOpen = true
                        }
                    }
                }
            }
        }
    }



    private fun openConveyorDoor() {
        FileLogger.log(context, "DropOffViewModel", "Sending command to open conveyor door")
        isConveyorDoorOpen = false
        context.sendBroadcast(Intent("com.washcloud.conveyor_open_door"))

        viewModelScope.launch {
            while (!isConveyorDoorOpen) {
                delay(3000)
                if (!isConveyorDoorOpen) {
                    FileLogger.log(context, "DropOffViewModel", "Retrying openConveyorDoor()")
                    context.sendBroadcast(Intent("com.washcloud.conveyor_open_door"))
                }
            }
        }
    }


    fun clearScannedWaybill() {
        _scannedWaybill.value = ""
    }


    fun setShowAlert(show: Boolean = true) {

        _showAlert.value = show
    }

     fun fetchLockers() {
        viewModelScope.launch(Dispatchers.IO) {
            val lockerList = boxDao.getAllBoxes()
            _lockers.value = lockerList
        }
    }

     fun fetchTransactions() {
        viewModelScope.launch(Dispatchers.IO) {
            val transactionsList = boxDao.getAllBoxes().filter { it.boxState == BoxState.OCCUPIED && it.trnasType == TransactionType.PICKUP }
            _transactions.value = transactionsList
        }
    }


    fun dropoff(orderSerial: String, boxID: String, boxType: String, stationId: String){

        println("orderSerial: $orderSerial")
        FileLogger.log(context, "DropOffViewModel", "confirm Staff Drop-off button clicked: $orderSerial")
        //println("doorNo: ${_transactions.value.first { it.orderSerial == orderSerial }.boxId}")
        FileLogger.log(context, "DropOffViewModel", "doorNo: $boxID  stationId: $stationId")



        val request = StaffDropoffRequest(
            apiKey = PrefsManager.getApiKey(context),
            wayBillNo = orderSerial,
            terminalSn = PrefsManager.getApiKey(context),
            type = 1,
            doorNo = boxID.toInt()
        )

        FileLogger.log(context, "DropOffViewModel", "Staff DropOff request: $request")
        viewModelScope.launch {
            try {
                val response = staffDropoffUseCase(request)

                Log.e("drop-off", response.status.toString());
                _staffDropoffResponse.value = response
                _isSuccessed.value = true
                setShowAlert()

                if(boxType == BoxType.BOX.name){

                    sendCommand(stationId, "0$boxID")
                }else{
                    openConveyor(boxID)
                }


                FileLogger.log(context, "DropOffViewModel", "Staff Dropoff successful: $response")
                updateBoxState(boxID, orderSerial, BoxState.OCCUPIED, TransactionType.PICKUP, boxType)
            } catch (e: Exception) {
                _error.value = e.message
                FileLogger.log(context, "DropOffViewModel", "Error in Staff Dropoff: ${e.message}")

            }
        }
    }

    fun recall(orderSerial: String, boxID: String, boxType: String, stationId: String){
        println("orderSerial: $orderSerial")
        FileLogger.log(context, "DropOffViewModel", "confirm Staff Recall button clicked: $orderSerial")
        FileLogger.log(context, "DropOffViewModel", "doorNo: $boxID")

        val request = StaffRecallRequest(
            apiKey = PrefsManager.getApiKey(context),
            wayBillNo = orderSerial,
            terminalSn = PrefsManager.getTerminalSN(context),
            type = 2,
            doorNo = boxID.toInt()
        )

        FileLogger.log(context, "DropOffViewModel", "Staff Recall request: $request")
        viewModelScope.launch {
            try {
                val response = staffRecallUseCase(request)
                _staffRecallResponse.value = response
                _isSuccessed.value = true
                sendCommand(stationId, "0$boxID")
                FileLogger.log(context, "DropOffViewModel", "Staff Recall successful: $response")
                updateBoxState(boxID, orderSerial, BoxState.AVAILABLE, TransactionType.DROP_OFF, boxType)
            } catch (e: Exception) {
                _error.value = e.message
                FileLogger.log(context, "DropOffViewModel", "Error in Staff Recall: ${e.message}")

            }
        }
    }

    private fun sendCommand(stationId: String, boxId: String) {
        FileLogger.log(context, "DropOffViewModel", "Sending command to open door: stationId: $stationId, boxId: $boxId")
        val intent = Intent("com.washcloud.open_door").apply {
            putExtra("stationId", stationId)
            putExtra("boxId", boxId)
        }
        context.sendBroadcast(intent)
    }

    private fun openConveyor(boxID: String) {
     FileLogger.log(context, "DropOffViewModel", "Sending command to open conveyor")
        val intent = Intent("com.washcloud.conveyor_open").apply {
            putExtra("conveyorNumber","0${boxID}");
        }

        context.sendBroadcast(intent)
    }


    private fun updateBoxState(boxId: String, orderSerial: String, boxState: BoxState, trnasType: TransactionType, boxType: String) {

        viewModelScope.launch(Dispatchers.IO) {


            val box = boxDao.getBoxById(boxId.toLong(), boxType)
            Log.e("box before updated ", box.toString());
            if (box != null) {
                val updatedBox = box.copy(boxState = boxState, trnasType = trnasType, orderSerial = orderSerial)
                boxDao.updateBox(updatedBox)
                FileLogger.log(context,  "insertTransaction"   ,"Updated box status to ${updatedBox.boxState} for boxId: $boxId")
                Log.e("DropOffViewModel", "Updated box status to ${updatedBox.boxState} for boxId: $boxId and orderSerial: ${updatedBox.orderSerial}")
                fetchLockers()
                fetchTransactions()
            } else {
                Log.e("DropOffViewModel", "Box with ID $boxId not found.")
                FileLogger.log(context,  "insertTransaction"   ,"Box with ID $boxId not found.")
            }
        }
    }

}


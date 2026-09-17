package com.washcloud.consoleapplication.ui.dropoff
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.washcloud.consoleapplication.local.database.dao.BoxDao
import com.washcloud.consoleapplication.local.database.dao.TransactionDao
import com.washcloud.consoleapplication.local.database.dto.BoxDto
import com.washcloud.consoleapplication.local.database.dto.TransactionDto
import com.washcloud.consoleapplication.local.database.utils.BoxState
import com.washcloud.consoleapplication.local.database.utils.BoxType
import com.washcloud.consoleapplication.local.database.utils.TransactionType
import com.washcloud.consoleapplication.local.preferences.IS_RECALL_CLOTHES_ENABLED_KEY
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
import retrofit2.HttpException
import java.io.IOException

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

    private val sharedPreferences = getDefaultSharedPreferences(application)
    val isRecallClothesEnabled = mutableStateOf(sharedPreferences.getBoolean(IS_RECALL_CLOTHES_ENABLED_KEY, true))


    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    private val _isSuccessed = MutableStateFlow<Boolean>(false)
    val isSuccessed: StateFlow<Boolean> get() = _isSuccessed


    private val _scannedWaybill = MutableStateFlow("")
    val scannedWaybill: StateFlow<String> get() = _scannedWaybill


    private val _showAlert = MutableStateFlow(false)
    val showAlert: StateFlow<Boolean> get() = _showAlert
    private var isConveyorDoorOpen: Boolean = false


    init {
        fetchTransactions()
        fetchLockers()

    }


    fun unregisterBroadcasts() {

        FileLogger.log(context, "DropOffViewModel", "unregisterBroadcasts");
        broadcastReceiverRepository.unregisterReceiver()
    }

    fun registerBroadcasts() {
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

                        _scannedWaybill.value = "";
                        FileLogger.log(context, "DropOffViewModel", "_scannedWaybill reset to empty and holding 1 seconds...")
                        delay(2000L);
                        _scannedWaybill.value = scannerData?.trim() ?: ""
                        FileLogger.log(context,"DropOffViewModel", "_scannedWaybill has assigned with new value: ${_scannedWaybill}")
                    }

                    "com.washcloud.conveyor_move" -> {
                        val status = intent.getStringExtra("status")
                        FileLogger.log(context, "DropOffViewModel", "Conveyor move status: $status")


                        if (status == "open") {
                            FileLogger.log(context, "DropOffViewModel", "Conveyor is arrived to DES Please open the door and status is  ${status}")

                            if(isConveyorDoorOpen){
                                FileLogger.log(context, "DropOffViewModel", "Sending command to open conveyor but conveyor is already opened then just no thing to do....");
                            }else{
                                openConveyorDoor()
                            }

                        }
                    }

                    "com.washcloud.conveyor_door_status" -> {
                        val status = intent.getStringExtra("status")
                        FileLogger.log(context, "DropOffViewModel", "Conveyor door status: $status")
                        if (status == "open") {
                            isConveyorDoorOpen = true
                            FileLogger.log(context, "DropOffViewModel", "Conveyor door is open AND isConveyorDoorOpen IS:  ${isConveyorDoorOpen}")
                        }else if (status == "close") {
                            isConveyorDoorOpen = false
                            FileLogger.log(context, "DropOffViewModel", "Conveyor door is closed AND isConveyorDoorOpen IS:  ${isConveyorDoorOpen}")
                        }
                    }
                }
            }
        }
    }



    private fun openConveyorDoor() {
        FileLogger.log(context, "DropOffViewModel", "Sending command to open conveyor door")
        FileLogger.log(context, "DropOffViewModel", "isConveyorDoorOpen IS:  ${isConveyorDoorOpen}")
        context.sendBroadcast(Intent("com.washcloud.conveyor_open_door"))
       /* viewModelScope.launch {

            var attempt = 1
            val maxAttempts = 5
            val delayMillis = 3000L

            context.sendBroadcast(Intent("com.washcloud.conveyor_open_door"))
            FileLogger.log(context, "DropOffViewModel", "Initial conveyor_open_door broadcast sent")

            while (attempt <= maxAttempts) {
                delay(delayMillis)

                if (isConveyorDoorOpen) {
                    FileLogger.log(context, "DropOffViewModel", "Conveyor door opened on attempt $attempt")
                    break
                }

                FileLogger.log(context, "DropOffViewModel", "Retrying conveyor door open, attempt $attempt")
                context.sendBroadcast(Intent("com.washcloud.conveyor_open_door"))
                attempt++
            }

            if (!isConveyorDoorOpen) {
                FileLogger.log(context, "DropOffViewModel", "Failed to open conveyor door after $maxAttempts attempts")
            }


        }*/
    }

     fun closeConveyorDoor() {
       FileLogger.log(context, "DropOffViewModel", "Sending command to close conveyor door")
        context.sendBroadcast(Intent("com.washcloud.conveyor_close"))
         isConveyorDoorOpen = false
    }

    fun updateConveyorDoorStatus(isOpen: Boolean) {
        isConveyorDoorOpen = isOpen
        FileLogger.log(context, "DropOffViewModel", "Conveyor door status updated. isConveyorDoorOpen IS:  ${isConveyorDoorOpen}")
    }



    fun clearScannedWaybill() {

        FileLogger.log(context, "DropOffViewModel", "Tying to clearing scanned waybill")
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
            val transactionsList = boxDao.getAllBoxes().filter { it.boxState == BoxState.OCCUPIED  && it.trnasType == TransactionType.PICKUP }
            _transactions.value = transactionsList

        }
    }


    fun dropoff(orderSerial: String, boxID: String, boxType: String, stationId: String, onSuccess: () -> Unit){

        println("orderSerial: $orderSerial")
        FileLogger.log(context, "DropOffViewModel", "confirm Staff Drop-off button clicked: $orderSerial")
        //println("doorNo: ${_transactions.value.first { it.orderSerial == orderSerial }.boxId}")
        FileLogger.log(context, "DropOffViewModel", "doorNo: $boxID  stationId: $stationId")



        val request = StaffDropoffRequest(
            apiKey = PrefsManager.getApiKey(context),
            wayBillNo = orderSerial,
            terminalSn = PrefsManager.getTerminalSN(context),
            type = if(boxType == BoxType.BOX.name) 1 else 2,
            doorNo = boxID.toInt()
        )

        FileLogger.log(context, "DropOffViewModel", "Staff DropOff request: $request")
        viewModelScope.launch {



                    try {
                        val response = staffDropoffUseCase(request)
                        _staffDropoffResponse.value = response
                        _isSuccessed.value = true

                        if(boxType == BoxType.BOX.name){
                            setShowAlert()
                            sendCommand(stationId, "0$boxID")
                        } else {
                            openConveyor(boxID)
                        }

                        FileLogger.log(context, "DropOffViewModel", "Staff Dropoff successful: $response")
                        updateBoxState(boxID, orderSerial, BoxState.OCCUPIED, TransactionType.PICKUP, boxType, response.customerId)
                        delay(3000)
                        onSuccess()

                    } catch (e: HttpException) {
                        val errorBody = e.response()?.errorBody()?.string()
                       // _error.value = "HTTP ${e.code()}: $errorBody"
                        FileLogger.log(context, "DropOffViewModel", "HTTP error in Staff Dropoff: $errorBody")

                    } catch (e: IOException) {
                      //  _error.value = "Network error: ${e.localizedMessage}"
                        FileLogger.log(context, "DropOffViewModel", "Network error in Staff Dropoff: ${e.localizedMessage}")

                    } catch (e: Exception) {
                      //  _error.value = "Unexpected error: ${e.localizedMessage ?: "Unknown"}"
                        FileLogger.log(context, "DropOffViewModel", "Unknown error in Staff Dropoff: ${e.localizedMessage}")
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
            type =   if(boxType == BoxType.BOX.name) 1 else 2,
            doorNo = boxID.toInt()
        )

        FileLogger.log(context, "DropOffViewModel", "Staff Recall request: $request")
        viewModelScope.launch {
            try {
                val response = staffRecallUseCase(request)
                _staffRecallResponse.value = response
                _isSuccessed.value = true

                if(boxType == BoxType.BOX.name){
                   sendCommand(stationId, "0$boxID")
                }else{
                    openConveyor(boxID)
                }
                FileLogger.log(context, "DropOffViewModel", "Staff Recall successful: $response")
                updateBoxState(boxID, orderSerial, BoxState.AVAILABLE, TransactionType.DROP_OFF, boxType)

            } catch (e: Exception) {
                _error.value = e.message
                FileLogger.log(context, "DropOffViewModel", "Error in Staff Recall: ${e.message}")

            }
        }
    }

     suspend fun  sendCommand(stationId: String, boxId: String) {


        val box = boxDao.getBoxById(boxId.toLong(), BoxType.BOX.name) ?: run {
            FileLogger.log(context, "DropOffViewModel", "Box not found for ID: $boxId")
            return
        }

        FileLogger.log(context, "DropOffViewModel", "Sending command to open door: stationId: $stationId, boxId: ${box.boxNumber}")
        val intent = Intent("com.washcloud.open_door").apply {
            putExtra("stationId", "0$stationId")
            putExtra("boxId", "0${box.boxNumber}")
        }
        context.sendBroadcast(intent)
    }

    private fun openConveyor(boxID: String) {


        FileLogger.log(context, "DropOffViewModel", "Sending command to open conveyor")
        val intent = Intent("com.washcloud.conveyor_open").apply {
            putExtra("conveyorNumber","${boxID}");
        }
        context.sendBroadcast(intent)



      /* if(isConveyorDoorOpen){
           FileLogger.log(context, "DropOffViewModel", "Sending command to open conveyor but conveyor is already opened then just send move command to ${boxID}");

       }else{
           FileLogger.log(context, "DropOffViewModel", "Sending command to open conveyor")
           val intent = Intent("com.washcloud.conveyor_open").apply {
               putExtra("conveyorNumber","${boxID}");
           }
           context.sendBroadcast(intent)
       }*/



    }


    private fun  updateBoxState(boxId: String, orderSerial: String, boxState: BoxState, trnasType: TransactionType, boxType: String, customerId: Long? = null) {

        viewModelScope.launch(Dispatchers.IO) {


            val box = boxDao.getBoxById(boxId.toLong(), boxType)
            Log.e("box before updated ", box.toString());
            if (box != null) {
                val updatedBox = box.copy(
                    boxState = boxState, 
                    trnasType = trnasType, 
                    orderSerial = orderSerial,
                    customerId = customerId ?: box.customerId
                )
                boxDao.updateBox(updatedBox)
                customerId?.let { 
                    transactionDao.updateTransactionCustomerId(orderSerial, it) 
                }
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


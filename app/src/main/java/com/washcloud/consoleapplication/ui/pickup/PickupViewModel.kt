package com.washcloud.consoleapplication.ui.pickup

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.washcloud.consoleapplication.hardware.CustomPrinterHelper
import com.washcloud.consoleapplication.hardware.PrintQR
import com.washcloud.consoleapplication.local.database.dao.BoxDao
import com.washcloud.consoleapplication.local.database.dao.TransactionDao
import com.washcloud.consoleapplication.local.database.dto.BoxDto
import com.washcloud.consoleapplication.local.database.dto.TransactionDto
import com.washcloud.consoleapplication.local.database.utils.BoxState
import com.washcloud.consoleapplication.local.database.utils.BoxType
import com.washcloud.consoleapplication.local.database.utils.TransactionType
import com.washcloud.consoleapplication.local.preferences.PrefsManager
import com.washcloud.consoleapplication.remote.model.pickup.StaffPickupRequest
import com.washcloud.consoleapplication.remote.model.pickup.StaffPickupResponse
import com.washcloud.consoleapplication.remote.usecase.StaffPickupUseCase
import com.washcloud.consoleapplication.repository.BroadcastReceiverRepository
import com.washcloud.consoleapplication.utils.FileLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class PickupViewModel @Inject constructor(
    private val transactionDao: TransactionDao,
    private val staffPickupUseCase: StaffPickupUseCase,
    private val boxDao: BoxDao,
    private val broadcastReceiverRepository: BroadcastReceiverRepository,
    application: Application
) : AndroidViewModel(application)  {

    private  val context: Context = getApplication<Application>().applicationContext
    private val _transactions = MutableStateFlow<List<BoxDto>>(emptyList())
    val transactions: StateFlow<List<BoxDto>> get() = _transactions

    private val _staffPickupResponse = MutableStateFlow<StaffPickupResponse?>(null)
    val staffPickupResponse: StateFlow<StaffPickupResponse?> get() = _staffPickupResponse

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    private val _isSuccessed = MutableStateFlow<Boolean>(false)
    val isSuccessed: StateFlow<Boolean> get() = _isSuccessed

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading


    private lateinit var customPrinterHelper: PrintQR
    init {
        fetchTransactions()
        initializePrinterHelper()
    }


    fun registerScannerDataReceiver() {
        println("Registering scanner data receiver PickupViewModel");
        val filter = IntentFilter().apply {
            addAction("com.washcloud.scanner_data")
        }
        broadcastReceiverRepository.registerReceiver(filter)
        FileLogger.log(context, "PickupViewModel", "Scanner receiver registered. Current transactions: ${_transactions.value.map { it.orderSerial }}")

        viewModelScope.launch {
            broadcastReceiverRepository.broadcastFlow.collectLatest { intent ->

                FileLogger.log(context, "pickupViewModel", "Received broadcast: $intent")
                when (intent.action) {
                    "com.washcloud.scanner_data" -> {
                        val scannerData = intent.getStringExtra("scannerData")

                        Log.e("PickupViewModel", "Received scanner data: $scannerData")

                        FileLogger.log(context, "pickupViewModel", "Received scanner data: $scannerData")

                        val extractedData = scannerData?.trim() ?: ""
                        if (extractedData.startsWith("http://", ignoreCase = true) || extractedData.startsWith("https://", ignoreCase = true)) {
                            FileLogger.log(context, "PickupViewModel", "Ignoring URL scanned in Staff Pickup mode: $extractedData")
                            return@collectLatest
                        }
                        FileLogger.log(context, "PickupViewModel", "Passing to staffPickup: '$extractedData'")
                        staffPickup(extractedData)

                    }
                }
            }
        }
    }

    fun unregisterScannerDataReceiver() {
        println("Unregistering scanner data receiver PickupViewModel");
        broadcastReceiverRepository.unregisterReceiver()
        FileLogger.log(context, "PickupViewModel", "Unregistered scanner data receiver")
    }

    fun sendMockDoorScannerDataBrodcast() {
        val intent = Intent("com.washcloud.scanner_data").apply {
            putExtra("scannerData", "900785010101")
        }
        Log.e("PickupViewModel", "sendMockDoorScannerDataBrodcast: ${intent.action}")
        context.sendBroadcast(intent)

    }


  private fun initializePrinterHelper() {
        customPrinterHelper = PrintQR(context)
    }

    fun printTransaction(transaction: BoxDto) {
        viewModelScope.launch {
            if (customPrinterHelper.OpenDevice()) {
                FileLogger.log(context, "PickupViewModel", "Printing transaction: $transaction")
                customPrinterHelper.PrintOrderQr(transaction.orderSerial, PrefsManager.getTerminalSN(context))
                //customPrinterHelper.closeDevice()
            } else {
              FileLogger.log(context, "PickupViewModel", "Error opening print device")
            }
        }
    }

    private fun fetchTransactions() {
        viewModelScope.launch(Dispatchers.IO) {
          /*  val transactionsList = transactionDao.getAllTransactions()
            _transactions.value = transactionsList*/

            val boxes = boxDao.getAllBoxes().filter { it.boxState == BoxState.OCCUPIED && it.trnasType == TransactionType.DROP_OFF }
            _transactions.value = boxes
        }
    }

    fun staffPickup(orderSerial: String) {

        println("orderSerial: $orderSerial")
        println("transactions: ${_transactions.value}")
        FileLogger.log(context, "PickupViewModel", "confirm Staff Pickup button clicked: $orderSerial")
        FileLogger.log(context, "PickupViewModel", "transactions: ${_transactions.value}")
        FileLogger.log(context, "PickupViewModel", "Matching '$orderSerial' against: ${_transactions.value.map { "'${it.orderSerial}'" }}")
        //println("doorNo: ${_transactions.value.first { it.orderSerial == orderSerial }.boxId}")
        FileLogger.log(context, "PickupViewModel", "doorNo: ${_transactions.value.firstOrNull { it.orderSerial == orderSerial }?.boxId}")
        val transaction = _transactions.value.firstOrNull { tx ->
            tx.orderSerial.equals(orderSerial, ignoreCase = true)
                    || tx.orderSerial.startsWith("$orderSerial-", ignoreCase = true)
        }

        if (transaction == null) {
            _error.value = "Transaction not found"
            FileLogger.log(
                context,
                "PickupViewModel",
                "Error in Staff Pickup: Transaction not found"
            );

            return;
        }
            FileLogger.log(context, "PickupViewModel", "Transaction found: $transaction")

        val doorNo    = transaction.boxId
        val stationId = transaction.stationId


        /*if (doorNo == null) {
            _error.value = "Door number not found"
//            Toast.makeText(context, "Order number not found", Toast.LENGTH_SHORT).show()
            FileLogger.log(context, "PickupViewModel", "Error in Staff Pickup: Door number not found")
            return
        }*/
        val request = StaffPickupRequest(
            apiKey = PrefsManager.getApiKey(context),
            wayBillNo = orderSerial,
            terminalSn = PrefsManager.getTerminalSN(context),
            type = 1,
            doorNo = doorNo.toInt()
        )

        FileLogger.log(context, "PickupViewModel", "Staff Pickup request: $request")
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = staffPickupUseCase(request)
                _staffPickupResponse.value = response
                _isSuccessed.value = true
                sendCommand(stationId.toString(), "0$doorNo")
                FileLogger.log(context, "PickupViewModel", "Staff Pickup successful: $response")
                deleteTransactionsByOrderSerial(transaction)
            } catch (e: Exception) {
                _error.value = e.message
                FileLogger.log(context, "PickupViewModel", "Error in Staff Pickup: ${e.message}")

            } finally {
                _isLoading.value = false

            }
        }
    }

   private suspend fun sendCommand(stationId: String, boxId: String) {

        val box = boxDao.getBoxById(boxId.toLong(), boxType = BoxType.BOX.name);
        FileLogger.log(context, "PickupViewModel", "Sending command to open door: stationId: $stationId, boxId: ${box?.boxNumber}")
        val intent = Intent("com.washcloud.open_door").apply {
            putExtra("stationId", stationId)
            putExtra("boxId", box?.boxNumber.toString())
        }
        context.sendBroadcast(intent)
    }

     fun deleteTransactionsByOrderSerial(order: BoxDto) {
        viewModelScope.launch {
            transactionDao.deleteTransaction(order.id);
            updateBoxState(order.boxId, order.boxType.name)
            FileLogger.log(context, "PickupViewModel", "Deleted transactions by id: $order")

        }
    }

    private fun updateBoxState(boxId: Long, boxType: String) {

        viewModelScope.launch(Dispatchers.IO) {


            val box = boxDao.getBoxById(boxId, boxType = boxType)
            if (box != null) {
                val updatedBox = box.copy(boxState = BoxState.AVAILABLE, orderSerial = "-1")
                boxDao.updateBox(updatedBox)
                fetchTransactions()
                FileLogger.log(context,  "insertTransaction"   ,"Updated box status to ${updatedBox.boxState} for boxId: $boxId")
                Log.d("PickupViewModel", "Updated box status to ${updatedBox.boxState} for boxId: $boxId")
            } else {
                Log.e("PickupViewModel", "Box with ID $boxId not found.")
                FileLogger.log(context,  "insertTransaction"   ,"Box with ID $boxId not found.")
            }
        }
    }
}
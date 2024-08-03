package com.washcloud.consoleapplication.ui.pickup

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.washcloud.consoleapplication.hardware.CustomPrinterHelper
import com.washcloud.consoleapplication.local.database.dao.TransactionDao
import com.washcloud.consoleapplication.local.database.dto.TransactionDto
import com.washcloud.consoleapplication.local.preferences.API_KEY
import com.washcloud.consoleapplication.local.preferences.TERMINAL_SN
import com.washcloud.consoleapplication.remote.model.pickup.StaffPickupRequest
import com.washcloud.consoleapplication.remote.model.pickup.StaffPickupResponse
import com.washcloud.consoleapplication.remote.usecase.StaffPickupUseCase
import com.washcloud.consoleapplication.utils.FileLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class PickupViewModel @Inject constructor(
    private val transactionDao: TransactionDao,
    private val staffPickupUseCase: StaffPickupUseCase,
    application: Application
) : AndroidViewModel(application)  {

    private  val context: Context = getApplication<Application>().applicationContext
    private val _transactions = MutableStateFlow<List<TransactionDto>>(emptyList())
    val transactions: StateFlow<List<TransactionDto>> get() = _transactions

    private val _staffPickupResponse = MutableStateFlow<StaffPickupResponse?>(null)
    val staffPickupResponse: StateFlow<StaffPickupResponse?> get() = _staffPickupResponse

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    private lateinit var customPrinterHelper: CustomPrinterHelper
    init {
        fetchTransactions()
        initializePrinterHelper()
    }


  private fun initializePrinterHelper() {
        customPrinterHelper = CustomPrinterHelper(context)
    }

    fun printTransaction(transaction: TransactionDto) {
        viewModelScope.launch {
            if (customPrinterHelper.openDevice(0)) {
                FileLogger.log(context, "PickupViewModel", "Printing transaction: $transaction")
                customPrinterHelper.printText(transaction.orderSerial.toString())
                customPrinterHelper.closeDevice()
            } else {
              FileLogger.log(context, "PickupViewModel", "Error opening print device")
            }
        }
    }

    private fun fetchTransactions() {
        viewModelScope.launch(Dispatchers.IO) {
            val transactionsList = transactionDao.getAllTransactions()
            _transactions.value = transactionsList
        }
    }

    fun staffPickup(orderSerial: String) {

        println("orderSerial: $orderSerial")
        println("transactions: ${_transactions.value}")
        //println("doorNo: ${_transactions.value.first { it.orderSerial == orderSerial }.boxId}")
        val doorNo = _transactions.value.first { it.orderSerial == orderSerial }.boxId

        val request = StaffPickupRequest(
            apiKey = API_KEY,
            wayBillNo = orderSerial,
            terminalSn = TERMINAL_SN,
            type = 1,
            doorNo = doorNo.toInt()
        )
        viewModelScope.launch {
            try {
                val response = staffPickupUseCase(request)
                _staffPickupResponse.value = response
                sendCommand("02", "0$doorNo")
                FileLogger.log(context, "PickupViewModel", "Staff Pickup successful: $response")
                deleteTransactionsByOrderSerial(_transactions.value.first { it.orderSerial == orderSerial })
            } catch (e: Exception) {
                _error.value = e.message
                FileLogger.log(context, "PickupViewModel", "Error in Staff Pickup: ${e.message}")

            }
        }
    }

    private fun sendCommand(stationId: String, boxId: String) {
        val intent = Intent("com.washcloud.open_door").apply {
            putExtra("stationId", stationId)
            putExtra("boxId", boxId)
        }
        context.sendBroadcast(intent)
    }

    private fun deleteTransactionsByOrderSerial(order: TransactionDto) {
        viewModelScope.launch {
            transactionDao.deleteTransaction(order.id);
            FileLogger.log(context, "PickupViewModel", "Deleted transactions by id: $order")
            fetchTransactions()
        }
    }
}
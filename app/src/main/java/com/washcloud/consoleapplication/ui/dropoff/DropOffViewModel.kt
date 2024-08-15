package com.washcloud.consoleapplication.ui.dropoff
import android.app.Application
import android.content.Context
import android.content.Intent
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
import com.washcloud.consoleapplication.local.preferences.API_KEY
import com.washcloud.consoleapplication.local.preferences.TERMINAL_SN
import com.washcloud.consoleapplication.remote.model.dropoff.StaffDropoffRequest
import com.washcloud.consoleapplication.remote.model.dropoff.StaffDropoffResponse
import com.washcloud.consoleapplication.remote.usecase.StaffDropoffUseCase
import com.washcloud.consoleapplication.utils.FileLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class DropOffViewModel @Inject constructor(
    private val transactionDao: TransactionDao,
    private val staffDropoffUseCase: StaffDropoffUseCase,
    private val boxDao: BoxDao,
    application: Application
) : AndroidViewModel(application)  {

    private  val context: Context = getApplication<Application>().applicationContext
    private val _transactions = MutableStateFlow<List<TransactionDto>>(emptyList())
    val transactions: StateFlow<List<TransactionDto>> get() = _transactions
    private val _lockers = MutableStateFlow<List<BoxDto>>(emptyList())
    val lockers: StateFlow<List<BoxDto>> get() = _lockers

    val _staffDropoffResponse = MutableStateFlow<StaffDropoffResponse?>(null)
    val staffDropoffResponse: StateFlow<StaffDropoffResponse?> get() = _staffDropoffResponse

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    private val _isSuccessed = MutableStateFlow<Boolean>(false)
    val isSuccessed: StateFlow<Boolean> get() = _isSuccessed

    init {
        fetchTransactions()
        fetchLockers()

    }

     fun fetchLockers() {
        viewModelScope.launch(Dispatchers.IO) {
            val lockerList = boxDao.getAllBoxes()
            _lockers.value = lockerList
        }
    }

     fun fetchTransactions() {
        viewModelScope.launch(Dispatchers.IO) {
            val transactionsList = transactionDao.getAllTransactions()
            _transactions.value = transactionsList
        }
    }

    fun dropoff(orderSerial: String, boxID: String){

        println("orderSerial: $orderSerial")
        FileLogger.log(context, "DropOffViewModel", "confirm Staff Drop-off button clicked: $orderSerial")
        //println("doorNo: ${_transactions.value.first { it.orderSerial == orderSerial }.boxId}")
        FileLogger.log(context, "DropOffViewModel", "doorNo: $boxID")



        val request = StaffDropoffRequest(
            apiKey = API_KEY,
            wayBillNo = orderSerial,
            terminalSn = TERMINAL_SN,
            type = 1,
            doorNo = boxID.toInt()
        )

        FileLogger.log(context, "DropOffViewModel", "Staff DropOff request: $request")
        viewModelScope.launch {
            try {
                val response = staffDropoffUseCase(request)
                _staffDropoffResponse.value = response
                _isSuccessed.value = true
                sendCommand("02", "0$boxID")
                FileLogger.log(context, "DropOffViewModel", "Staff Dropoff successful: $response")
                updateBoxState(boxID)
            } catch (e: Exception) {
                _error.value = e.message
                FileLogger.log(context, "DropOffViewModel", "Error in Staff Dropoff: ${e.message}")

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


    private fun updateBoxState(boxId: String) {

        viewModelScope.launch(Dispatchers.IO) {


            val box = boxDao.getBoxById(boxId.toLong())
            if (box != null) {
                val updatedBox = box.copy(boxState = BoxState.OCCUPIED)
                boxDao.updateBox(updatedBox)
                FileLogger.log(context,  "insertTransaction"   ,"Updated box status to ${updatedBox.boxState} for boxId: $boxId")
                Log.d("DropOffViewModel", "Updated box status to ${updatedBox.boxState} for boxId: $boxId")
            } else {
                Log.e("DropOffViewModel", "Box with ID $boxId not found.")
                FileLogger.log(context,  "insertTransaction"   ,"Box with ID $boxId not found.")
            }
        }
    }

}


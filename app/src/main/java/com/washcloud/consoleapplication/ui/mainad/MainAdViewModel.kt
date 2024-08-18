package com.washcloud.consoleapplication.ui.mainad

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.URLUtil
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.washcloud.consoleapplication.local.database.dao.BoxDao
import com.washcloud.consoleapplication.local.database.dao.TransactionDao
import com.washcloud.consoleapplication.local.database.dto.TransactionDto
import com.washcloud.consoleapplication.local.database.utils.BoxSizeType
import com.washcloud.consoleapplication.local.database.utils.BoxState
import com.washcloud.consoleapplication.local.database.utils.TransactionType
import com.washcloud.consoleapplication.local.preferences.API_KEY
import com.washcloud.consoleapplication.local.preferences.BRANCH_ID
import com.washcloud.consoleapplication.local.preferences.TERMINAL_SN
import com.washcloud.consoleapplication.remote.config.BASE_URL
import com.washcloud.consoleapplication.remote.config.CUSTOMER_DROP_OFF
import com.washcloud.consoleapplication.remote.config.CUSTOMER_PICKUP
import com.washcloud.consoleapplication.utils.FileLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url
import java.util.Date
import javax.inject.Inject


object RetrofitClient {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttpClient)
            .build()
            .create(ApiService::class.java)
    }
}

interface ApiService {
    @GET
    suspend fun fetchData(@Url url: String): Response<ApiResponse>

    @GET(CUSTOMER_DROP_OFF)
    suspend fun customerDropOff(
        @Query("Apikey") apiKey: String,
        @Query("WayBillNo") wayBillNo: String,
        @Query("TerminalSn") terminalSn: String,
        @Query("DoorNo") doorNo: String,
        @Query("Type") type: Int
    ): Response<ApiResponse>

    @GET(CUSTOMER_PICKUP)
    suspend fun customerPickup(
        @Query("Apikey") apiKey: String,
        @Query("WayBillNo") wayBillNo: String,
        @Query("TerminalSn") terminalSn: String,
        @Query("DoorNo") doorNo: String,
        @Query("Type") type: Int
    ): Response<ApiResponse>
}

@JsonClass(generateAdapter = true)
data class ApiResponse(
    @Json(name = "Status") val status: String?,
    @Json(name = "message") val message: String?,
    @Json(name = "data") val data: List<ApiData>?
)

@JsonClass(generateAdapter = true)
data class ApiData(
    @Json(name = "operationType") val operationType: String,
    @Json(name = "doorNo") val doorNo: String,
    @Json(name = "terminalSn") val terminalSn: String,
    @Json(name = "wayBillNo") val wayBillNo: String,
    @Json(name = "dropOffUrl") val dropOffUrl: String,
    @Json(name = "type") val type: String
)

@HiltViewModel
class MainAdViewModel @Inject constructor(
    private val transactionDao: TransactionDao,
    private val boxDao: BoxDao,
    application: Application
) : AndroidViewModel(application)  {

    private  val context: Context = getApplication<Application>().applicationContext

    private val apiService: ApiService = RetrofitClient.apiService
    private val _apiResponse = MutableLiveData<ApiResponse>()
    val apiResponse: LiveData<ApiResponse> get() = _apiResponse

    private val _showDialog = MutableLiveData<ApiData>()
    val showDialog: LiveData<ApiData> get() = _showDialog

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error


    private val _isDoorOpen = MutableStateFlow(false)
    val isDoorOpen: StateFlow<Boolean> = _isDoorOpen.asStateFlow()

   /* private val handler = Handler(Looper.getMainLooper())
    private lateinit var checkDoorRunnable: Runnable

    fun startRepeatingCheck(stationId: String, boxId: String) {
        checkDoorRunnable = object : Runnable {
            override fun run() {
                sendCheckDoorStatusCommand(stationId, boxId)
                handler.postDelayed(this, 3000L)  // Repeat every 3 seconds
            }
        }
        handler.post(checkDoorRunnable)
    }*/



    fun handCheckDoorStatusResponse(stationId: String?, boxId: String?,  isOpen: Boolean) {
        _isDoorOpen.value = isOpen
        FileLogger.log(context,  "onReceive"   ,"Door status: ${_isDoorOpen.value}")
        Toast.makeText(context, "Door status received: $stationId  ${boxId} status: ${isDoorOpen.value}", Toast.LENGTH_LONG).show();
        if(!_isDoorOpen.value){
            checkOperationType()

        }
    }



    fun checkOperationType() {
        setCloseDoor()
        if (_apiResponse.value?.data?.firstOrNull()?.operationType == "PickUp") {
            requestCustomerPickup()
        } else {

            requestCustomerDropOff()
        }
    }

    private fun requestCustomerDropOff() {

        setCloseDoor()

        viewModelScope.launch {
            try {
                FileLogger.log(context,  "setCustomerDropOff"   ,"Fetching data from ${CUSTOMER_DROP_OFF}")
                val response: Response<ApiResponse> = apiService.customerDropOff(
                    apiKey = API_KEY,
                    wayBillNo = _apiResponse.value?.data?.firstOrNull()?.wayBillNo ?: "",
                    terminalSn = TERMINAL_SN,
                    doorNo = _apiResponse.value?.data?.firstOrNull()?.doorNo ?: "",
                    type = 1
                )

                if (response.isSuccessful) {
                    Log.d("MainAdViewModel", "Response: ${response.body()}")

                    FileLogger.log(context,  "setCustomerDropOff"   ,"Response: ${response.body()}")
                    insertTransaction(apiResponse.value?.data?.firstOrNull()!!)
                    response.body()?.let {
                     //TODO
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    _error.value = "Error fetching data: $errorBody"
                    FileLogger.log(context,  "setCustomerDropOff"   ,"Error fetching data: $errorBody")
                }
            } catch (e: Exception) {
                _error.value = "Error fetching data: ${e.message ?: "An error occurred"}"
                FileLogger.log(context,  "setCustomerDropOff"   ,"Error fetching data: ${e.message ?: "An error occurred"}")
            }
        }
    }


    private fun requestCustomerPickup() {
        viewModelScope.launch {
            try {
                FileLogger.log(context,  "setCustomerPickup"   ,"Fetching data from ${CUSTOMER_PICKUP}")
                val response: Response<ApiResponse> = apiService.customerPickup(
                    apiKey = API_KEY,
                    wayBillNo = _apiResponse.value?.data?.firstOrNull()?.wayBillNo ?: "",
                    terminalSn = TERMINAL_SN,
                    doorNo = _apiResponse.value?.data?.firstOrNull()?.doorNo ?: "",
                    type = 1
                )

                if (response.isSuccessful) {
                    Log.d("MainAdViewModel", "Response: ${response.body()}")
                    updateBoxStats( _apiResponse.value?.data?.firstOrNull()?.doorNo!!.toLong() , BoxState.AVAILABLE, "-1")
                    FileLogger.log(context,  "setCustomerPickup"   ,"Response: ${response.body()}")
                    response.body()?.let {

                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    _error.value = "Error fetching data: $errorBody"
                    FileLogger.log(context,  "setCustomerPickup"   ,"Error fetching data: $errorBody")
                }
            } catch (e: Exception) {
                _error.value = "Error fetching data: ${e.message ?: "An error occurred"}"
                FileLogger.log(context,  "setCustomerPickup"   ,"Error fetching data: ${e.message ?: "An error occurred"}")
            }
        }
    }

    fun handleBarcode(barcode: String) {


        if(isDoorOpen.value){
            FileLogger.log(context,  "handleBarcode"   ,"rejected handleBarcode: $barcode")
            return
        }

        FileLogger.log(context,  "handleBarcode"   ,"handleBarcode: $barcode")
        if (URLUtil.isValidUrl(barcode)) {
            viewModelScope.launch {
                try {
                    val fullUrl = "$barcode?apiKey=$API_KEY"



                    FileLogger.log(context,  "handleBarcode"   ,"Fetching data from $fullUrl")
                    val response: Response<ApiResponse> = apiService.fetchData(fullUrl)
                    if (response.isSuccessful) {
                        Log.d("MainAdViewModel", "Response: ${response.body()}")
                        FileLogger.log(context,  "handleBarcode"   ,"Response: ${response.body()}")
                        response.body()?.let {
                            _apiResponse.value = it
                            _showDialog.value = it.data?.firstOrNull()
                            _isDoorOpen.value = true
                            sendCommand("02", "0"+it.data?.firstOrNull()?.doorNo)
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        _error.value = "Error fetching data from $fullUrl: $errorBody"
                        FileLogger.log(context,  "handleBarcode"   ,"Error fetching data from $fullUrl: $errorBody")
                    }
                } catch (e: Exception) {
                    val fullUrl = "$barcode?apiKey=$API_KEY"
                    _error.value = "Error fetching data from $fullUrl: ${e.message ?: "An error occurred"}"
                    FileLogger.log(context,  "handleBarcode"   ,"Error fetching data from $fullUrl: ${e.message ?: "An error occurred"}")
                }
            }
        } else {
            _error.value = "Invalid URL"
            FileLogger.log(context,  "handleBarcode"   ,"Invalid URL")
        }
    }

    fun setCloseDoor() {
        FileLogger.log(context,  "setCloseDoor"   ,"setCloseDoor")
        _isDoorOpen.value = false
    }


    fun fetchDirectly(url: String) {
        viewModelScope.launch {
            try {
                val fullUrl = "$url?apiKey=$API_KEY"
                Log.d("MainAdViewModel", "Fetching data from $fullUrl")

                val response: Response<ApiResponse> = apiService.fetchData(fullUrl)
                if (response.isSuccessful) {
                    Log.d("MainAdViewModel", "Response: ${response.body()}")
                    response.body()?.let {
                        _apiResponse.value = it
                        _showDialog.value = it.data?.firstOrNull()
                        _isDoorOpen.value = true

                        delay(20000L)
                        sendMockDoorStatusBrodcast()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    _error.value = "Error fetching data from $fullUrl: $errorBody"
                }
            } catch (e: Exception) {
                val fullUrl = "$url?apiKey=$API_KEY"
                _error.value = "Error fetching data from $fullUrl: ${e.message ?: "An error occurred"}"
            }
        }
    }

    fun insertTransaction(data: ApiData) {

        viewModelScope.launch(Dispatchers.IO) {
            val transaction = TransactionDto(
                orderSerial = data.wayBillNo,
                orderId = data.doorNo.toLong(),
                boxId = data.doorNo.toLong(),
                trnasDate = Date(),
                branchId = BRANCH_ID,
                trnasType =  if (data.operationType == "DropOff") TransactionType.DROP_OFF else TransactionType.PICKUP,
                boxSize = BoxSizeType.MEDIUM
            )
            transactionDao.insertTransaction(transaction)
            Log.d("MainAdViewModel", "Inserted transaction: $transaction")
            FileLogger.log(context,  "insertTransaction"   ,"Inserted transaction: $transaction")


            val boxId = data.doorNo.toLong()
            updateBoxStats(boxId, BoxState.OCCUPIED, data.wayBillNo)

        }
    }


    private fun  updateBoxStats(boxId: Long, state: BoxState, order_serial: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val box = boxDao.getBoxById(boxId)
            if (box != null) {
                val updatedBox = box.copy(boxState = state, orderSerial = order_serial, trnasType = TransactionType.DROP_OFF)
                boxDao.updateBox(updatedBox)
                FileLogger.log(context,  "insertTransaction"   ,"Updated box status to ${updatedBox.boxState} for boxId: $boxId")
                Log.d("MainAdViewModel", "Updated box status to ${updatedBox.boxState} for boxId: $boxId")
            } else {
                Log.e("MainAdViewModel", "Box with ID $boxId not found.")
                FileLogger.log(context,  "insertTransaction"   ,"Box with ID $boxId not found.")
            }
        }
    }

   private fun sendCommand(stationId: String, boxId: String) {

        viewModelScope.launch {

            delay(1000L)
            FileLogger.log(context,  "sendCommand"   ,"sendCommand stationId: $stationId, boxId: $boxId")
            val intent = Intent("com.washcloud.open_door").apply {
                putExtra("stationId", stationId)
                putExtra("boxId", boxId)
            }

            context.sendBroadcast(intent)

        }

    }


    fun sendCheckDoorStatusCommand(stationId: String, boxId: String) {
        val intent = Intent("com.washcloud.check_door").apply {
            putExtra("stationId", stationId)
            putExtra("boxId", boxId)
        }
        context.sendBroadcast(intent)
    }


   private fun sendMockDoorStatusBrodcast() {
        val intent = Intent("com.washcloud.door_status").apply {
            putExtra("stationId", "02")
            putExtra("boxId", "03")
            putExtra("status", false)
            putExtra("data", "900785010101")
        }
       Log.e("MainAdViewModel", "sendDoorStatusBrodcast: ${intent.action}")
        context.sendBroadcast(intent)

    }




//    override fun onCleared() {
//        super.onCleared()
//        unregisterReceiver()
//    }

}

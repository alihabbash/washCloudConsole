package com.washcloud.consoleapplication.ui.mainad

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.webkit.URLUtil
import android.widget.Toast
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.toUpperCase
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.washcloud.consoleapplication.di.DatabaseModule
import com.washcloud.consoleapplication.local.database.dao.BoxDao
import com.washcloud.consoleapplication.local.database.dao.TransactionDao
import com.washcloud.consoleapplication.local.database.dto.BoxDto
import com.washcloud.consoleapplication.local.database.dto.TransactionDto
import com.washcloud.consoleapplication.local.database.utils.BoxSizeType
import com.washcloud.consoleapplication.local.database.utils.BoxState
import com.washcloud.consoleapplication.local.database.utils.BoxType
import com.washcloud.consoleapplication.local.database.utils.TransactionType
import com.washcloud.consoleapplication.local.preferences.ADS_ARRAY
import com.washcloud.consoleapplication.local.preferences.BRANCH_ID
import com.washcloud.consoleapplication.local.preferences.PrefsManager
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
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import java.util.Locale
import java.util.jar.Manifest
import javax.inject.Inject


object RetrofitClient {
    private var apiServiceInstance: ApiService? = null

    fun getApiService(context: Context): ApiService {
        if (apiServiceInstance == null) {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build()

            val baseUrl = MainAdActivity.getBaseUrl(context)

            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .client(okHttpClient)
                .build()

            apiServiceInstance = retrofit.create(ApiService::class.java)
        }

        return apiServiceInstance!!
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
    private val apiService: ApiService = RetrofitClient.getApiService(context)

    private val _apiResponse = MutableLiveData<ApiResponse>()
    val apiResponse: LiveData<ApiResponse> get() = _apiResponse

    private val _showDialog = MutableLiveData<ApiData?>()
    val showDialog: LiveData<ApiData?> get() = _showDialog

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error


    private val _isDoorOpen = MutableStateFlow(false)
    val isDoorOpen: StateFlow<Boolean> = _isDoorOpen.asStateFlow()

    private val _isloading = MutableStateFlow(false)

    private var lastScannedBarcode: String? = null
    private var lastScannedTime: Long = 0L
    private val debounceInterval = 5000L
    private var isConveyorDoorOpen: Boolean = false

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

    private val _adsList = MutableStateFlow<List<Uri>>(emptyList())
    val adsList: StateFlow<List<Uri>> = _adsList.asStateFlow()




    init {
        loadAdsFromStorage()
    }

    fun loadAdsFromStorage() {
        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

        val adsString: Set<String>? = try {
            sharedPreferences.getStringSet(ADS_ARRAY, emptySet())
        } catch (e: Exception) {
            Log.e("MainAdViewModel", "Error retrieving ADS_ARRAY from SharedPreferences", e)
            FileLogger.log(context,  "MainAdViewModel"   ,"Error retrieving ADS_ARRAY from SharedPreferences")
            emptySet()
        }

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("MainAdViewModel", "Storage permission not granted")
            return
        }

        adsString?.let {
            val adsUris = it.map { uriString -> Uri.parse(uriString) }

            Log.e("MainAdViewModel", "Resolved URIs: $adsUris")
            _adsList.value = adsUris
        }
    }

    /*  fun getRealPathFromURI(context: Context, uri: Uri): String? {
          val projection = arrayOf(MediaStore.Images.Media.DATA)
          val cursor = context.contentResolver.query(uri, projection, null, null, null)

          cursor?.use {
              if (it.moveToFirst()) {
                  val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                  return it.getString(columnIndex)
              }
          }
          return null
      }

      fun copyFileToInternalStorage(context: Context, uri: Uri): String? {
          val inputStream = context.contentResolver.openInputStream(uri) ?: return null
          val file = File(context.filesDir, "tempFile_${System.currentTimeMillis()}")

          inputStream.use { input ->
              FileOutputStream(file).use { output ->
                  input.copyTo(output)
              }
          }
          return file.absolutePath
      }*/


    suspend fun getBox(boxId: String): BoxDto? {
        val box = boxDao.getBoxById(boxId.toLong(), BoxType.BOX.name)
        return box
    }

    fun handCheckDoorStatusResponse(stationId: String? = "", boxId: String? = "",  isOpen: Boolean) {
        _isDoorOpen.value = isOpen
        FileLogger.log(context,  "handCheckDoorStatusResponse with order"   ,"apiResponse.value?.data?.firstOrNull() ${apiResponse.value?.data?.firstOrNull()}")
        FileLogger.log(context,  "onReceive"   ,"Door status: ${_isDoorOpen.value}")
       // Toast.makeText(context, "Door status received: $stationId  ${boxId} status: ${isDoorOpen.value}", Toast.LENGTH_LONG).show();
        if(!_isDoorOpen.value && apiResponse.value?.data?.firstOrNull()?.operationType != null){
            checkOperationType()

        }
    }



    fun checkOperationType() {
        setCloseDoor()

        val data = _apiResponse.value?.data?.firstOrNull()

        if (data?.operationType == "PickUp") {
            val type = data.type.uppercase(Locale.ENGLISH)
            requestCustomerPickup(type)
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
                    apiKey = PrefsManager.getApiKey(context),
                    wayBillNo = _apiResponse.value?.data?.firstOrNull()?.wayBillNo ?: "",
                    terminalSn =  PrefsManager.getTerminalSN(context),
                    doorNo = _apiResponse.value?.data?.firstOrNull()?.doorNo ?: "",
                    type = 1
                )

                FileLogger.log(context,  "setCustomerDropOff"   ,"Fetching data from ${CUSTOMER_DROP_OFF} wayBillNo: ${_apiResponse.value?.data?.firstOrNull()?.wayBillNo} terminalSn: ${PrefsManager.getTerminalSN(context)} doorNo: ${_apiResponse.value?.data?.firstOrNull()?.doorNo} type: 1");

                if (response.isSuccessful) {
                    Log.d("MainAdViewModel", "Response: ${response.body()}")

                    FileLogger.log(context,  "setCustomerDropOff"   ,"Response: ${response.body()}")
                    insertTransaction(apiResponse.value?.data?.firstOrNull()!!)
                    _showDialog.value = null
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


    private fun requestCustomerPickup(boxType: String) {
        viewModelScope.launch {
            try {
                FileLogger.log(context,  "setCustomerPickup"   ,"Fetching data from ${CUSTOMER_PICKUP}")
                val response: Response<ApiResponse> = apiService.customerPickup(
                    apiKey = PrefsManager.getApiKey(context),
                    wayBillNo = _apiResponse.value?.data?.firstOrNull()?.wayBillNo ?: "",
                    terminalSn = PrefsManager.getTerminalSN(context),
                    doorNo = _apiResponse.value?.data?.firstOrNull()?.doorNo ?: "",
                    type = if(boxType == BoxType.CONVEYOR.name) 2 else 1
                )

                if (response.isSuccessful) {
                    Log.d("MainAdViewModel", "Response: ${response.body()}")
                    updateBoxStats( _apiResponse.value?.data?.firstOrNull()?.doorNo!!.toLong() , BoxState.AVAILABLE, "-1", boxType)
                    FileLogger.log(context,  "setCustomerPickup"   ,"Response: ${response.body()}")
                    _showDialog.value = null
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
        val currentTime = System.currentTimeMillis()

        if (_isloading.value) {
            FileLogger.log(context, "handleBarcode", "rejected (isLoading) handleBarcode: $barcode")
            return
        }

        if (barcode == lastScannedBarcode && currentTime - lastScannedTime < debounceInterval) {
            FileLogger.log(context, "handleBarcode", "rejected (debounce) handleBarcode: $barcode")
            return
        }

        lastScannedBarcode = barcode
        lastScannedTime = currentTime

        _isloading.value = true

        viewModelScope.launch {
            try {

                FileLogger.log(context, "handleBarcode", "handleBarcode: $barcode")

                if (URLUtil.isValidUrl(barcode)) {
                    val fullUrl = "$barcode?apiKey=${PrefsManager.getApiKey(context)}"
                    FileLogger.log(context, "handleBarcode", "Fetching data from $fullUrl")

                    val response: Response<ApiResponse> = apiService.fetchData(fullUrl)

                    if (response.isSuccessful) {
                        val body = response.body()
                        Log.d("MainAdViewModel", "Response: $body")
                        FileLogger.log(context, "MainAdViewModel handleBarcode", "Response: $body")

                        body?.let {
                            _apiResponse.value = it
                            _showDialog.value = it.data?.firstOrNull()
                            _isDoorOpen.value = true



                            val boxType = it.data?.firstOrNull()?.type?.uppercase(Locale.ENGLISH);


                            val doorNo = "0${it.data?.firstOrNull()?.doorNo}"

                            if (boxType == BoxType.CONVEYOR.name) {
                                FileLogger.log(context, "MainAdViewModel handleBarcode", "openConveyor: $doorNo")
                                openConveyor(doorNo)

                            } else {
                                FileLogger.log(context, "MainAdViewModel handleBarcode", "sendCommand: $doorNo")
                                sendCommand(doorNo)
                            }
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        _error.value = "Error fetching data from $fullUrl: $errorBody"
                        FileLogger.log(context, "handleBarcode", "Error fetching data from $fullUrl: $errorBody")
                    }
                } else {
                    _error.value = "Invalid URL"
                    FileLogger.log(context, "handleBarcode", "Invalid URL")
                }

            } catch (e: Exception) {
                val fullUrl = "$barcode?apiKey=${PrefsManager.getApiKey(context)}"
                _error.value = "Error fetching data from $fullUrl: ${e.message ?: "An error occurred"}"
                FileLogger.log(context, "handleBarcode", "Exception: ${e.message}")
            } finally {
                _isloading.value = false
            }
        }
    }

    fun setCloseDoor() {
        FileLogger.log(context,  "setCloseDoor"   ,"setCloseDoor")
        _isDoorOpen.value = false
    }


    fun closeConveyorDoor() {
        FileLogger.log(context, "DropOffViewModel", "Sending command to close conveyor door")
        context.sendBroadcast(Intent("com.washcloud.conveyor_close"))
        isConveyorDoorOpen = false

    }

 /*   fun fetchDirectly(url: String) {
        viewModelScope.launch {
            try {
                val fullUrl = "$url?apiKey=${PrefsManager.getApiKey(context)}"
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
                val fullUrl = "$url?apiKey=${PrefsManager.getApiKey(context)}"
                _error.value = "Error fetching data from $fullUrl: ${e.message ?: "An error occurred"}"
            }
        }
    }*/

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
            updateBoxStats(boxId, BoxState.OCCUPIED, data.wayBillNo, boxType = BoxType.BOX.name)

        }
    }


    private fun  updateBoxStats(boxId: Long, state: BoxState, order_serial: String, boxType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val box = boxDao.getBoxById(boxId, boxType)
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

   private fun sendCommand(boxId: String) {

//       FileLogger.log(context,  "sendCommand fun"   ,"sendCommand stationId: ${_apiResponse.value?.data?.firstOrNull()?.wayBillNo}, boxId: $boxId");

        viewModelScope.launch {

            val box = boxDao.getBoxById(boxId.toLong(), BoxType.BOX.name)
            FileLogger.log(context,  "sendCommand"   ,"sendCommand stationId: 0${box?.stationId}, boxId: $boxId")
            val intent = Intent("com.washcloud.open_door").apply {
                FileLogger.log(context,  "MainAdViewModel"  ,"sendCommand stationId: 0${box?.stationId}, boxId: $boxId")
                putExtra("stationId", "0" + box?.stationId.toString())
                putExtra("boxId", box?.boxNumber.toString())
            }

            context.sendBroadcast(intent)

        }

    }
    private fun openConveyor(boxID: String) {
      FileLogger.log(context,  "openConveyor"   ,"Sending command to open conveyor")
        val intent = Intent("com.washcloud.conveyor_open").apply {
            putExtra("conveyorNumber", boxID)
        }

        context.sendBroadcast(intent)


        /*viewModelScope.launch {
            delay(20000)
            context.sendBroadcast(Intent("com.washcloud.conveyor_open_door"))
        }*/
    }


    fun openConveyorDoor() {

        if(isConveyorDoorOpen) {
            Log.e("MainAdViewModel", "Conveyor door is already open")
            FileLogger.log(context,  "MainAdViewModel"   ,"Conveyor door is already open")
            return
        }
        FileLogger.log(context,  "MainAdViewModel"   ,"openConveyorDoor")

        viewModelScope.launch {
            delay(200)
            isConveyorDoorOpen = true
            context.sendBroadcast(Intent("com.washcloud.conveyor_open_door"))

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

    fun sendMockConveyorStatusBroadcast(context: Context) {
        val intent = Intent("com.washcloud.conveyor_door_status")
        intent.putExtra("status", "open")
        context.sendBroadcast(intent)
    }



//    override fun onCleared() {
//        super.onCleared()
//        unregisterReceiver()
//    }

}

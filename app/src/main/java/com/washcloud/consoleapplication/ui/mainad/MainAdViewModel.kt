package com.washcloud.consoleapplication.ui.mainad

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.webkit.URLUtil
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.washcloud.consoleapplication.local.database.dao.BoxDao
import com.washcloud.consoleapplication.local.database.dao.CustomerUserDao
import com.washcloud.consoleapplication.local.database.dao.TransactionDao
import com.washcloud.consoleapplication.local.database.dto.BoxDto
import com.washcloud.consoleapplication.local.database.dto.CustomerUserDto
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
import com.washcloud.consoleapplication.remote.config.SYNC_CUSTOMER_DATA
import com.washcloud.consoleapplication.utils.FileLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Query
import retrofit2.http.Url
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import androidx.core.net.toUri
import com.washcloud.consoleapplication.remote.config.HeadersInterceptors
import com.washcloud.consoleapplication.remote.model.offline.ActionType
import com.washcloud.consoleapplication.remote.model.offline.QrActionPayload
import com.washcloud.consoleapplication.remote.model.offline.StaticQrPayload
import com.washcloud.consoleapplication.R
import com.washcloud.consoleapplication.utils.SignatureVerifier
import com.washcloud.consoleapplication.workmanager.OfflineSyncWorker
import kotlinx.coroutines.withContext
import org.json.JSONObject


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

            val headersInterceptor = HeadersInterceptors(context)

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(headersInterceptor)
                .addInterceptor(loggingInterceptor)
                .build()

            val baseUrl = MainAdActivity.getBaseUrl(context)

            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .client(okHttpClient)
                .build()


            FileLogger.log(context,  "RetrofitClient"   ,"Base URL: $baseUrl")

            apiServiceInstance = retrofit.create(ApiService::class.java)
        }

        return apiServiceInstance!!
    }
}

interface ApiService {
    @GET
    suspend fun fetchData(@Url url: String): Response<ApiResponse>

    @GET
    suspend fun checkExternalDriverLogin(@Url url: String): Response<Unit>

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

    @POST(SYNC_CUSTOMER_DATA)
    suspend fun syncCustomerData(
        @Body request: CustomerSyncRequest
    ): Response<CustomerSyncResponse>
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
    @Json(name = "type") val type: String,
    @Json(name = "boxes") val boxes: List<Box>
)

@JsonClass(generateAdapter = true)
data class Box(
    @Json(name = "doorNo") val doorNo: String,
    @Json(name = "type") val type: String
)

@JsonClass(generateAdapter = true)
data class CustomerSyncRequest(
    @Json(name = "apiKey") val apiKey: String,
    @Json(name = "terminalSn") val terminalSn: String,
    @Json(name = "customers") val customers: List<CustomerData>
)

@JsonClass(generateAdapter = true)
data class CustomerSyncResponse(
    @Json(name = "Status") val status: String?,
    @Json(name = "message") val message: String?,
    @Json(name = "customers") val customers: List<CustomerData>?
)

@JsonClass(generateAdapter = true)
data class CustomerData(
    @Json(name = "id") val customerId: Long,
    @Json(name = "customerName") val customerName: String?,
    @Json(name = "customerPhoneNumber") val phoneNumber: String?,
    @Json(name = "consolePassword") val password: String?,
    @Json(name = "consoleLastUpdate") val lastUpdate: String?
)

@HiltViewModel
class MainAdViewModel @Inject constructor(
    private val transactionDao: TransactionDao,
    private val boxDao: BoxDao,
    private val customerUserDao: CustomerUserDao,
    application: Application
) : AndroidViewModel(application)  {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val qrAdapter = moshi.adapter(QrActionPayload::class.java)
    private val staticQrAdapter = moshi.adapter(StaticQrPayload::class.java)

    private  val context: Context = getApplication<Application>().applicationContext
    private val apiService: ApiService = RetrofitClient.getApiService(context)

    private val _apiResponse = MutableLiveData<ApiResponse>()
    val apiResponse: LiveData<ApiResponse> get() = _apiResponse

    private val _showDialog = MutableLiveData<ApiData?>()
    val showDialog: LiveData<ApiData?> get() = _showDialog

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error
    
    fun clearError() {
        _error.value = ""
    }

    private val _isloading = MutableStateFlow(false)
    val isloading: StateFlow<Boolean> get() = _isloading

    private var lastScannedBarcode: String? = null

    private val _offlineQrLoginCustomerId = MutableStateFlow<Long?>(null)
    val offlineQrLoginCustomerId: StateFlow<Long?> get() = _offlineQrLoginCustomerId

    private val _offlineQrSetPinCustomerId = MutableStateFlow<Long?>(null)
    val offlineQrSetPinCustomerId: StateFlow<Long?> get() = _offlineQrSetPinCustomerId

    private val _isDoorOpen = MutableStateFlow(false)
    val isDoorOpen: StateFlow<Boolean> = _isDoorOpen.asStateFlow()

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

    private val _isBagScanMode = MutableStateFlow(false)
    val isBagScanMode: StateFlow<Boolean> = _isBagScanMode.asStateFlow()


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


        if ((apiResponse.value?.data?.firstOrNull()?.boxes?.size ?: 0) > 1) {
            return
        }


        _isDoorOpen.value = isOpen
        isConveyorDoorOpen = isOpen
        FileLogger.log(context,  "handCheckDoorStatusResponse with order"   ,"apiResponse.value?.data?.firstOrNull() ${apiResponse.value?.data?.firstOrNull()}")
        FileLogger.log(context,  "onReceive"   ,"Door status: ${_isDoorOpen.value}")
       // Toast.makeText(context, "Door status received: $stationId  ${boxId} status: ${isDoorOpen.value}", Toast.LENGTH_LONG).show();
        if(!_isDoorOpen.value && apiResponse.value?.data?.firstOrNull()?.operationType != null){
            checkOperationType()
        }
    }



    fun checkOperationType(boxType: String = "", doorNumber: String  = "", hideDialog: Boolean = true) {

        if(hideDialog){
            setCloseDoor()
        }



        FileLogger.log(context,  "checkOperationType"   ,"Order ${apiResponse.value?.data?.firstOrNull()}");
        val data = _apiResponse.value?.data?.firstOrNull()

        if (data?.operationType == "PickUp") {
            val type = if (boxType == "") data.type.uppercase(Locale.ENGLISH) else boxType.uppercase(Locale.ENGLISH)
            requestCustomerPickup(type,doorNumber,hideDialog)
        } else {
            if (PrefsManager.isDropOffDisabled(context)) {
                FileLogger.log(context, "checkOperationType", "Drop-Off is disabled in settings. Aborting.")
                Toast.makeText(context, context.getString(R.string.drop_off_disabled_toast), Toast.LENGTH_SHORT).show()
                return
            }
            requestCustomerDropOff()
        }
    }


    private fun requestCustomerDropOff() {
        setCloseDoor()
        viewModelScope.launch {
            try {
                insertTransaction(apiResponse.value?.data?.firstOrNull()!!)
                val wayBillNo = _apiResponse.value?.data?.firstOrNull()?.wayBillNo ?: ""
                val terminalSn = PrefsManager.getTerminalSN(context)
                val doorNo = _apiResponse.value?.data?.firstOrNull()?.doorNo ?: ""
                val type = 1
                val apiKey = PrefsManager.getApiKey(context)
                val baseUrl = MainAdActivity.getBaseUrl(context)
                val fullUrl = "$baseUrl$CUSTOMER_DROP_OFF" +
                        "?Apikey=$apiKey" +
                        "&WayBillNo=$wayBillNo" +
                        "&TerminalSn=$terminalSn" +
                        "&DoorNo=$doorNo" +
                        "&Type=$type"
                FileLogger.log(context, "requestCustomerDropOff", "FULL URL: $fullUrl")
                val response: Response<ApiResponse> = apiService.customerDropOff(
                    apiKey = apiKey,
                    wayBillNo = wayBillNo,
                    terminalSn = terminalSn,
                    doorNo = doorNo,
                    type = type
                )

                FileLogger.log(context, "requestCustomerDropOff", "Response from server: ${response.body()}")
                if (response.isSuccessful) {
                    Log.d("MainAdViewModel", "Response: ${response.body()}")

                    _showDialog.value = null
                } else {
                    val errorBody = response.errorBody()?.string()
                    _error.value = "Error fetching data: $errorBody"
                    FileLogger.log(context, "requestCustomerDropOff", "Error fetching data: $errorBody")
                }
            } catch (e: Exception) {
                _error.value = "Error fetching data: ${e.message ?: "An error occurred"}"
                FileLogger.log(context, "requestCustomerDropOff", "Exception: ${e.message}")
            }
        }
    }


    private fun requestCustomerPickup(boxType: String, doorNumber: String, hideDialog: Boolean ) {
        viewModelScope.launch {
            try {
                FileLogger.log(context,  "setCustomerPickup"   ,"Fetching data from ${CUSTOMER_PICKUP}")

                val selectedDoorId = if (doorNumber.isEmpty()) {
                    _apiResponse.value?.data?.firstOrNull()?.doorNo?.toLongOrNull()
                } else {
                    doorNumber.toLongOrNull()
                }


                updateBoxStats(
                    selectedDoorId!!,
                    BoxState.AVAILABLE,
                    "-1",
                    boxType
                )

                val response: Response<ApiResponse> = apiService.customerPickup(
                    apiKey = PrefsManager.getApiKey(context),
                    wayBillNo = _apiResponse.value?.data?.firstOrNull()?.wayBillNo ?: "",
                    terminalSn = PrefsManager.getTerminalSN(context),
                    doorNo = if(doorNumber == "")  _apiResponse.value?.data?.firstOrNull()?.doorNo ?: "" else doorNumber,
                    type = if(boxType == BoxType.CONVEYOR.name) 2 else 1
                )

                if (response.isSuccessful) {
                    Log.d("MainAdViewModel", "Response: ${response.body()}")
                    FileLogger.log(context,  "setCustomerPickup"   ,"Response: ${response.body()}")
                    if(hideDialog) {
                        _showDialog.value = null
                        response.body()?.let {
                        }
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

        // If we’re in bag mode, ignore all locker logic
        if (_isBagScanMode.value) {
            FileLogger.log(
                context,
                "handleBarcode",
                "BagScanMode active, ignoring locker logic for barcode: $barcode"
            )
            // We still broadcast in MainAdActivity → BagCounterViewModel picks it up.
            return
        }

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

                executeBarcodeAction(barcode, context)

            } catch (e: Exception) {
                val fullUrl = "$barcode?apiKey=${PrefsManager.getApiKey(context)}"
                _error.value =
                    "Error fetching data from $fullUrl: ${e.message ?: "An error occurred"}"
                FileLogger.log(context, "handleBarcode", "Exception: ${e.message}")
            } finally {
                _isloading.value = false
            }
        }
    }

    suspend fun executeBarcodeAction(barcode: String, context: Context) {
        FileLogger.log(context, "handleBarcode", "Received barcode: $barcode")

        if (!URLUtil.isValidUrl(barcode)) {
            _error.value = "Invalid URL"
            FileLogger.log(context, "handleBarcode", "Invalid URL: $barcode")
            
            try {
                val jsonObject = org.json.JSONObject(barcode)
                if (jsonObject.has("phoneNumber")) {
                    handleStaticQrBarcode(barcode, context)
                } else {
                    handleUrlOrQrJsonBarcode(barcode, context)
                }
            } catch (e: Exception) {
                // Fallback if not valid JSON
                handleUrlOrQrJsonBarcode(barcode, context)
            }
            return
        }

        val uri = barcode.toUri()
        val path = uri.path.orEmpty()

        val isCheckLogin = path.contains("ExternalDrivers/CheckLogin", ignoreCase = true)

        if (isCheckLogin) {
            executeExternalDriversCheckLogin(barcode, uri, context)
        }
        else {
            executeLockerBarcodeAction(barcode, context)
        }
    }


    /**
     *
     * Expected Static QR JSON format example:
     *
     * {
     *   "customerId": 319,
     *   "signature": "9f3a1c88b7e2d4a5c9b1a77e..."
     * }
     */
    private fun handleStaticQrBarcode(barcode: String, context: Context) {
        try {
            val staticQrPayload = staticQrAdapter.fromJson(barcode)

            if (staticQrPayload == null) {
                _error.value = "Invalid Static QR payload"
                FileLogger.log(context, "handleStaticQrBarcode", "Static QR JSON parsed as null")
                return
            }

            FileLogger.log(
                context,
                "handleStaticQrBarcode",
                "Static QR JSON parsed successfully: $staticQrPayload"
            )

            val verifier = SignatureVerifier()
            val isSignatureValid = verifier.isStaticQrSignatureValid(
                payloadObj = staticQrPayload,
                terminalSn = PrefsManager.getTerminalSN(context)
            )

            if (!isSignatureValid) {
                _error.value = "Invalid Static QR signature"
                FileLogger.log(context, "handleStaticQrBarcode", "Static QR signature validation failed")
                return
            }

            FileLogger.log(context, "handleStaticQrBarcode", "Static QR successfully validated for customer: ${staticQrPayload.customerId}")
            
            viewModelScope.launch {
                val customer = customerUserDao.getCustomerUser(staticQrPayload.customerId)
                if (customer == null) {
                    _error.value = "Customer not found locally. Please sync first."
                    FileLogger.log(context, "handleStaticQrBarcode", "Customer ${staticQrPayload.customerId} not found in DB.")
                    return@launch
                }

                if (customer.consolePassword.isNullOrBlank()) {
                    // No PIN set, navigate to Set PIN screen
                    _offlineQrSetPinCustomerId.value = customer.userId
                } else {
                    // PIN is set, navigate to Login screen
                    _offlineQrLoginCustomerId.value = customer.userId
                }
            }

        } catch (e: Exception) {
            _error.value = "Error parsing Static QR: ${e.message}"
            FileLogger.log(context, "handleStaticQrBarcode", "Exception parsing Static QR: ${e.message}")
        }
    }


    /**
     *
     * Expected QR JSON format example:
     *
     * {
     *   "actionType": "OPEN_BOX",
     *   "wayBillNo": "WB123456789",
     *   "doorNo": "05",
     *   "type": 1,
     *   "issuedAt": 1712345600,
     *   "expiresAt": 1712345660,
     *   "nonce": "A7X9Q",
     *   "signature": "9f3a1c88b7e2d4a5c9b1a77e"
     * }
     */

    private fun handleUrlOrQrJsonBarcode(barcode: String, context: Context) {


        try {
            val qrPayload = qrAdapter.fromJson(barcode)

            if (qrPayload == null) {
                _error.value = "Invalid QR payload"
                FileLogger.log(context, "handleUrlOrQrJsonBarcode", "QR JSON parsed as null")
                return
            }

            FileLogger.log(
                context,
                "handleUrlOrQrJsonBarcode",
                "QR JSON parsed successfully: $qrPayload"
            )

            // ===============================
            // ✅ STEP 2: validate signature
            // ===============================
            val verifier = SignatureVerifier()

            val isSignatureValid = verifier.isSignatureValid(
                payloadObj = qrPayload,
                apiKey = PrefsManager.getApiKey(context),
                terminalSn = PrefsManager.getTerminalSN(context)
            )

            if (!isSignatureValid) {
                _error.value = "Invalid signature"
                FileLogger.log(context, "handleUrlOrQrJsonBarcode", "Signature validation failed")
                return
            }

            // ===============================
            // ✅ STEP 3: check expiration
            // ===============================
            val isNotExpired = verifier.isNotExpired(qrPayload.expiresAt)

            if (!isNotExpired) {
                _error.value = "QR code expired"
                FileLogger.log(context, "handleUrlOrQrJsonBarcode", "QR expired")
                return
            }


            if (PrefsManager.isQrAlreadyScanned(context, qrPayload.nonce)) {
                //TODO show Dialog only once
                Toast.makeText(context, "QR already scanned", Toast.LENGTH_SHORT).show()
                return
            }

            PrefsManager.saveScannedQr(context, qrPayload.nonce)

            // ===============================
            // ✅ STEP 4: execute action (SWITCH ONLY)
            // ===============================
            when (qrPayload.actionType) {

                ActionType.OPEN_BOX -> {
                    FileLogger.log(
                        context,
                        "handleUrlOrQrJsonBarcode",
                        "Opening box door as per QR code request");
                    sendCommand(qrPayload.doorNo ?: "0");

                    if (qrPayload.operationType.equals("PickUp", ignoreCase = true)) {
                        updateBoxStats(
                            qrPayload.doorNo!!.toLong(),
                            BoxState.AVAILABLE,
                            "-1",
                            BoxType.BOX.name.uppercase(Locale.ENGLISH)
                        )
                    } else {
                        updateBoxStats(
                            qrPayload.doorNo!!.toLong(),
                            BoxState.OCCUPIED,
                            qrPayload.wayBillNo ?: "",
                            BoxType.BOX.name.uppercase(Locale.ENGLISH)
                        )
                    }
                    
                    // Enqueue background sync
                    insertOfflineEmergencyTransaction(qrPayload, context)
                }

                ActionType.OPEN_CONVEYOR_DOOR -> {
                    FileLogger.log(
                        context,
                        "handleUrlOrQrJsonBarcode",
                        "Opening conveyor door as per QR code request");
                    openConveyorDoor();


                }

                ActionType.CLOSE_CONVEYOR -> {
                    closeConveyorDoor();
                    FileLogger.log(
                        context,
                        "handleUrlOrQrJsonBarcode",
                        "Closing conveyor door as per QR code request");
                }

                ActionType.MOVE_CONVEYOR -> {
                    FileLogger.log(
                        context,
                        "handleUrlOrQrJsonBarcode",
                        "Moving conveyor to doorNo: ${qrPayload.doorNo ?: "0"}"
                    );
                    openConveyor(qrPayload.doorNo ?: "0");

                    updateBoxStats(
                        qrPayload.doorNo!!.toLong(),
                        BoxState.AVAILABLE,
                        "-1",
                        BoxType.CONVEYOR.name.uppercase(Locale.ENGLISH)
                    )
                }

                ActionType.REBOOT_DEVICE -> {

                    FileLogger.log(
                        context,
                        "handleUrlOrQrJsonBarcode",
                        "Rebooting device as per QR code request");
                    try {
                        val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "reboot"))
                        process.waitFor()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                ActionType.RESET_CUSTOMER_PASSWORD -> {
                    FileLogger.log(
                        context,
                        "handleUrlOrQrJsonBarcode",
                        "Resetting customer password as per QR code request");
                        
                    viewModelScope.launch {
                        qrPayload.customerId?.let { custId ->
                            val customer = customerUserDao.getCustomerUser(custId)
                            if (customer != null) {
                                val currentDateTime = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }.format(java.util.Date())
                                customerUserDao.updateCustomerUser(customer.copy(
                                    consolePassword = null,
                                    consoleLastUpdate = currentDateTime
                                ))
                                Toast.makeText(context, context.getString(R.string.password_reset_success), Toast.LENGTH_LONG).show()
                            } else {
                                FileLogger.log(
                                    context,
                                    "handleUrlOrQrJsonBarcode",
                                    "Customer ID $custId not found for password reset"
                                )
                            }
                        }
                    }
                }
            }

        } catch (e: Exception) {
            _error.value = "Invalid barcode format"
            FileLogger.log(
                context,
                "handleUrlOrQrJsonBarcode",
                "Barcode is neither URL nor valid QR JSON: ${e.message}"
            )
        }
    }




    private suspend fun executeExternalDriversCheckLogin(
        barcode: String,
        uri: Uri,
        context: Context,
    ) {
        val token = uri.getQueryParameter("access_token")

        if (token.isNullOrBlank()) {
            _error.value = "Invalid login link: missing access_token"
            FileLogger.log(
                context,
                "MainAdViewModel handleBarcode",
                "Invalid CheckLogin URL (no access_token): $barcode"
            )
            return
        }

        FileLogger.log(
            context,
            "MainAdViewModel handleBarcode",
            "Checking external driver login at $barcode"
        )

        val response: Response<Unit> = apiService.checkExternalDriverLogin(barcode)

        val code = response.code()
        FileLogger.log(
            context,
            "MainAdViewModel handleBarcode",
            "CheckLogin HTTP status: $code"
        )

        when (code) {
            200 -> {
                navigateToBagCounter()
            }
            401 -> {
                _error.value = "Login failed: not authorized."
            }
            else -> {
                _error.value = "Login failed (code: $code). Please try again."
            }
        }
    }

    private suspend fun executeLockerBarcodeAction(barcode: String, context: Context) {

        val fullUrl = "$barcode?apiKey=${PrefsManager.getApiKey(context)}"
        FileLogger.log(context, "MainAdViewModel handleBarcode", "Fetching data from $fullUrl")

        try {
            val response: Response<ApiResponse> = apiService.fetchData(fullUrl)

            if (response.isSuccessful) {
                val body = response.body()
                Log.d("MainAdViewModel", "Response: $body")
                FileLogger.log(context, "MainAdViewModel handleBarcode", "Response: $body")

                body?.let {
                    val opType = it.data?.firstOrNull()?.operationType
                    if (opType != "PickUp" && PrefsManager.isDropOffDisabled(context)) {
                        FileLogger.log(context, "MainAdViewModel handleBarcode", "Drop-Off is disabled in settings. Aborting.")
                        Toast.makeText(context, context.getString(R.string.drop_off_disabled_toast), Toast.LENGTH_SHORT).show()
                        return@let
                    }

                    _apiResponse.value = it

                    Log.i("boxes number is", "${it.data?.firstOrNull()?.boxes?.size}")
                    _showDialog.value = it.data?.firstOrNull()

                    _isDoorOpen.value = true
                    val boxType = it.data?.firstOrNull()?.type?.uppercase(Locale.ENGLISH)
                    val doorNo = "0${it.data?.firstOrNull()?.doorNo}"

                    //send customer pickup to server
                    if(it.data?.firstOrNull()?.boxes?.size == 1){
                        FileLogger.log(context, "MainAdViewModel handleBarcode", "Single box detected, checking operation type");
                        checkOperationType(hideDialog = false);
                    }


                    if (boxType == BoxType.CONVEYOR.name) {
                        FileLogger.log(
                            context,
                            "MainAdViewModel handleBarcode",
                            "openConveyor: $doorNo",
                        )
                        openConveyor(doorNo)
                    } else {
                        FileLogger.log(
                            context,
                            "MainAdViewModel handleBarcode",
                            "sendCommand: $doorNo",
                        )
                        sendCommand(doorNo)
                    }
                }
            } else {
                val errorBody = response.errorBody()?.string()
                FileLogger.log(
                    context,
                    "MainAdViewModel handleBarcode",
                    "Error fetching data from $fullUrl: $errorBody"
                )
                
                var finalErrorMessage = "Error fetching data from $fullUrl: $errorBody"

                // Parse the error JSON for a specific message
                try {
                    if (!errorBody.isNullOrBlank()) {
                        val jsonObject = JSONObject(errorBody)
                        if (jsonObject.has("message")) {
                            val msg = jsonObject.getString("message")
                            if (msg.isNotBlank()) {
                                finalErrorMessage = msg
                            }
                        }
                    }
                } catch (e: Exception) {
                    FileLogger.log(context, "MainAdViewModel handleBarcode", "Failed to parse error message: ${e.message}")
                }
                
                // Only post to LiveData ONCE so it doesn't trigger two Dialogs
                _error.postValue(finalErrorMessage)
            }
        } catch (e: Exception) {
            FileLogger.log(
                context,
                "MainAdViewModel handleBarcode",
                "Network exception: ${e.message}. Triggering Primary Offline Mode."
            )
            handlePrimaryOfflineMode(barcode, context)
        }
    }

    private suspend fun handlePrimaryOfflineMode(barcode: String, context: Context) {
        try {
            val uri = barcode.toUri()
            val pathSegments = uri.pathSegments
            if (pathSegments.size >= 2) {
                // Extract serial from standard URL format: /api/LockerIntegration/Verification/{serial}/{terminalSn}
                val serial = pathSegments[pathSegments.size - 2]
                FileLogger.log(context, "handlePrimaryOfflineMode", "Extracted serial: $serial")
                
                val boxes = boxDao.getBoxesByOrderSerial(serial)
                
                if (boxes.isNotEmpty()) {
                    FileLogger.log(context, "handlePrimaryOfflineMode", "Found ${boxes.size} boxes for serial: $serial")
                    for (box in boxes) {
                        val doorNo = box.boxId.toString().padStart(2, '0')
                        val boxTypeStr = box.boxType.name.uppercase(Locale.ENGLISH)
                        
                        if (boxTypeStr == BoxType.CONVEYOR.name) {
                            FileLogger.log(context, "handlePrimaryOfflineMode", "openConveyor: $doorNo")
                            openConveyor(doorNo)
                        } else {
                            FileLogger.log(context, "handlePrimaryOfflineMode", "sendCommand: $doorNo")
                            sendCommand(doorNo)
                        }
                        
                        // Update box state to AVAILABLE and clear serial
                        updateBoxStats(
                            box.boxId,
                            BoxState.AVAILABLE,
                            "", 
                            boxTypeStr
                        )
                        
                        // Queue transaction for background sync
                        insertOfflinePickupTransaction(box)
                    }
                    _error.value = "Offline Pickup Successful"
                } else {
                    _error.value = "Order not found in this terminal"
                    FileLogger.log(context, "handlePrimaryOfflineMode", "No boxes found for serial: $serial")
                }
            } else {
                _error.value = "Invalid QR Format for Offline Pickup"
                FileLogger.log(context, "handlePrimaryOfflineMode", "Could not extract serial from path segments")
            }
        } catch (e: Exception) {
            _error.value = "Offline Mode Error: ${e.message}"
            FileLogger.log(context, "handlePrimaryOfflineMode", "Error: ${e.message}")
        }
    }

    private fun insertOfflinePickupTransaction(box: BoxDto) {
        viewModelScope.launch(Dispatchers.IO) {
            val transaction = TransactionDto(
                orderSerial = box.orderSerial,
                orderId = box.orderId,
                boxId = box.boxId,
                trnasDate = Date(),
                branchId = box.branchId,
                trnasType = TransactionType.PICKUP,
                boxSize = box.boxSize
            )
            transactionDao.insertTransaction(transaction)
            FileLogger.log(context, "insertOfflinePickupTransaction", "Inserted offline pickup sync transaction: $transaction")
            
            // Queue sync worker
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
                
            val syncRequest = OneTimeWorkRequestBuilder<OfflineSyncWorker>()
                .setConstraints(constraints)
                .build()
                
            WorkManager.getInstance(context).enqueueUniqueWork(
                "OfflineSyncWork",
                ExistingWorkPolicy.REPLACE,
                syncRequest
            )
        }
    }

    private fun insertOfflineEmergencyTransaction(qrPayload: QrActionPayload, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val transType = if (qrPayload.operationType.equals("PickUp", ignoreCase = true)) {
                TransactionType.PICKUP
            } else {
                TransactionType.DROP_OFF
            }

            val boxSize = BoxSizeType.MEDIUM // default since it's missing in emergency JSON
            
            val transaction = TransactionDto(
                orderSerial = qrPayload.wayBillNo ?: "",
                orderId = 0L,
                boxId = qrPayload.doorNo?.toLongOrNull() ?: 0L,
                trnasDate = Date(),
                branchId = 0L,
                trnasType = transType,
                boxSize = boxSize
            )
            transactionDao.insertTransaction(transaction)
            FileLogger.log(context, "insertOfflineEmergencyTransaction", "Inserted offline emergency sync transaction: $transaction")
            
            // Queue sync worker
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
                
            val syncRequest = OneTimeWorkRequestBuilder<OfflineSyncWorker>()
                .setConstraints(constraints)
                .build()
                
            WorkManager.getInstance(context).enqueueUniqueWork(
                "OfflineSyncWork",
                ExistingWorkPolicy.REPLACE,
                syncRequest
            )
        }
    }

    fun navigateToBagCounter() {
        FileLogger.log(context, "MainAdViewModel", "navigateToBagCounter called")
        _isBagScanMode.value = true
    }

    fun exitBagScanMode() {
        FileLogger.log(context, "MainAdViewModel", "exitBagScanMode called")
        _isBagScanMode.value = false
    }

    fun setCloseDoor() {
        FileLogger.log(context,  "setCloseDoor"   ,"setCloseDoor")
        _isDoorOpen.value = false
    }


    fun closeConveyorDoor() {
        FileLogger.log(context, "MainAdViewModel", "Sending command to close conveyor door")
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

    fun resetLocker(lockerNumber: Int,  boxType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val box = boxDao.getBoxById(lockerNumber.toLong(), boxType)
            if (box != null) {
                val updatedBox = box.copy(
                    boxState = BoxState.AVAILABLE,
                    trnasType = TransactionType.DROP_OFF,
                    orderSerial = "",
                    orderId = 0L,
                    boxType = if (boxType == "CONVEYOR") BoxType.CONVEYOR else BoxType.BOX
                )
                boxDao.insertBox(updatedBox)
            }
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
    fun sendCommand(boxId: String) {

//       FileLogger.log(context,  "sendCommand fun"   ,"sendCommand stationId: ${_apiResponse.value?.data?.firstOrNull()?.wayBillNo}, boxId: $boxId");

        viewModelScope.launch {

            val box = boxDao.getBoxById(boxId.toLong(), BoxType.BOX.name)
            FileLogger.log(context,  "sendCommand"   ,"sendCommand stationId: 0${box?.stationId}, boxId: $boxId")
            val intent = Intent("com.washcloud.open_door").apply {
                FileLogger.log(context,  "MainAdViewModel"  ,"sendCommand stationId: 0${box?.stationId}, boxId: $boxId  && the box number is  ${box?.boxNumber.toString()}")
                putExtra("stationId", "0" + box?.stationId.toString())
                putExtra("boxId", box?.boxNumber.toString())
            }

            context.sendBroadcast(intent)

        }

    }

     fun openConveyor(boxID: String) {
      FileLogger.log(context,  "openConveyor"   ,"Sending command to open conveyor")
        isConveyorDoorOpen = false
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

        FileLogger.log(context, "MainAdViewModel", "openConveyorDoor initial broadcast")
        context.sendBroadcast(Intent("com.washcloud.conveyor_open_door"))

       /* viewModelScope.launch {

            var attempt = 1
            val maxAttempts = 5
            val delayMillis = 3000L

            while (attempt <= maxAttempts) {
                delay(delayMillis)

                if (isConveyorDoorOpen) {
                    FileLogger.log(context, "MainAdViewModel", "Conveyor door opened on attempt $attempt")
                    break
                }

                FileLogger.log(context, "MainAdViewModel", "Retrying conveyor door open, attempt $attempt")
                context.sendBroadcast(Intent("com.washcloud.conveyor_open_door"))
                attempt++
            }

            if (!isConveyorDoorOpen) {
                FileLogger.log(context, "MainAdViewModel", "Failed to open conveyor door after $maxAttempts attempts")
            }

        }*/
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

    fun clearOfflineQrMode() {
        _offlineQrLoginCustomerId.value = null
        _offlineQrSetPinCustomerId.value = null
    }

    fun verifyPinAndUnlockBoxes(customerId: Long, enteredPhone: String, enteredPin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val customer = customerUserDao.getCustomerUser(customerId)
            if (customer != null && customer.consolePassword == enteredPin) {
                val expectedPhone = customer.phoneNumber
                val fullEnteredPhone = "+966$enteredPhone"
                if (expectedPhone == fullEnteredPhone) {
                    FileLogger.log(context, "OfflineStaticQr", "Customer $customerId successfully verified offline PIN and phone number.")
                    unlockDropOffBoxesForCustomer(customerId)
                    onResult(true)
                } else {
                    FileLogger.log(context, "OfflineStaticQr", "Customer $customerId failed offline verification. Phone number mismatch (Entered: $fullEnteredPhone, Expected: $expectedPhone).")
                    onResult(false)
                }
            } else {
                FileLogger.log(context, "OfflineStaticQr", "Customer $customerId failed offline PIN verification.")
                onResult(false)
            }
        }
    }

    fun validatePhoneForOfflineQr(customerId: Long, enteredPhone: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val customer = customerUserDao.getCustomerUser(customerId)
            if (customer != null) {
                val expectedPhone = customer.phoneNumber
                val fullEnteredPhone = "+966$enteredPhone"
                if (expectedPhone == fullEnteredPhone) {
                    onResult(true)
                } else {
                    FileLogger.log(context, "OfflineStaticQr", "Phone validation failed: mismatch (Entered: $fullEnteredPhone, Expected: $expectedPhone).")
                    onResult(false)
                }
            } else {
                onResult(false)
            }
        }
    }

    fun savePinAndUnlockBoxes(customerId: Long, enteredPhone: String, newPin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val customer = customerUserDao.getCustomerUser(customerId)
            if (customer != null) {
                val expectedPhone = customer.phoneNumber
                val fullEnteredPhone = "+966$enteredPhone"
                if (expectedPhone == fullEnteredPhone) {
                    FileLogger.log(context, "OfflineStaticQr", "Setting initial offline PIN for customer $customerId")
                    val updatedCustomer = customer.copy(
                        consolePassword = newPin,
                        consoleLastUpdate = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }.format(Date())
                    )
                    customerUserDao.updateCustomerUser(updatedCustomer)
                    unlockDropOffBoxesForCustomer(customerId)
                    onResult(true)
                } else {
                    FileLogger.log(context, "OfflineStaticQr", "Failed to set PIN: Phone number mismatch (Entered: $fullEnteredPhone, Expected: $expectedPhone).")
                    onResult(false)
                }
            } else {
                FileLogger.log(context, "OfflineStaticQr", "Failed to set PIN: Customer $customerId not found in local DB.")
                onResult(false)
            }
        }
    }

    private suspend fun unlockDropOffBoxesForCustomer(customerId: Long) {
        // Find all boxes assigned to this customer that are OCCUPIED
        val customerBoxes = boxDao.getBoxesByCustomerId(customerId, BoxState.OCCUPIED)
        
        for (box in customerBoxes) {
            if (box.boxType == BoxType.CONVEYOR) {
                openConveyor(box.boxNumber.toString())
            } else {
                // Send hardware broadcast to open the door (sendCommand expects boxId as String)
                sendCommand(box.boxId.toString())
            }
            
            // Update local box state to AVAILABLE
            val updatedBox = box.copy(boxState = BoxState.AVAILABLE)
            boxDao.updateBox(updatedBox)
            
            // Queue transaction for background sync
            insertOfflinePickupTransaction(box)
            
            FileLogger.log(context, "OfflineStaticQr", "Successfully unlocked boxId ${box.boxId} (Type: ${box.boxType}) for customer $customerId")
        }
        
        // After processing, clear the UI state to return to Ad screen
        clearOfflineQrMode()
    }
}

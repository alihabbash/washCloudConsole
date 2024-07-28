package com.washcloud.consoleapplication.ui.mainad
import android.app.Application
import android.content.Context
import android.content.Intent
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

import android.util.Log
import android.webkit.URLUtil
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.washcloud.consoleapplication.local.database.dao.BoxDao
import com.washcloud.consoleapplication.local.database.dao.TransactionDao
import com.washcloud.consoleapplication.local.database.dto.TransactionDto
import com.washcloud.consoleapplication.local.database.utils.BoxSizeType
import com.washcloud.consoleapplication.local.database.utils.BoxState
import com.washcloud.consoleapplication.local.database.utils.TransactionType
import com.washcloud.consoleapplication.local.preferences.API_KEY
import com.washcloud.consoleapplication.remote.config.BASE_URL
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
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
}

@JsonClass(generateAdapter = true)
data class ApiResponse(
    @Json(name = "Status") val status: String,
    @Json(name = "message") val message: String,
    @Json(name = "data") val data: List<ApiData>
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

    private val context: Context = getApplication<Application>().applicationContext

    private val apiService: ApiService = RetrofitClient.apiService
    private val _apiResponse = MutableLiveData<ApiResponse>()
    val apiResponse: LiveData<ApiResponse> get() = _apiResponse

    private val _showDialog = MutableLiveData<ApiData>()
    val showDialog: LiveData<ApiData> get() = _showDialog

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error



    fun handleBarcode(barcode: String) {
        if (URLUtil.isValidUrl(barcode)) {
            viewModelScope.launch {
                try {
                    val fullUrl = "$barcode?apiKey=$API_KEY"
                    Log.d("MainAdViewModel", "Fetching data from $fullUrl")

                    val response: Response<ApiResponse> = apiService.fetchData(fullUrl)
                    if (response.isSuccessful) {
                        Log.d("MainAdViewModel", "Response: ${response.body()}")
                        response.body()?.let {
                            _apiResponse.value = it
                            _showDialog.value = it.data.firstOrNull()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        _error.value = "Error fetching data from $fullUrl: $errorBody"
                    }
                } catch (e: Exception) {
                    val fullUrl = "$barcode?apiKey=$API_KEY"
                    _error.value = "Error fetching data from $fullUrl: ${e.message ?: "An error occurred"}"
                }
            }
        } else {
            _error.value = "Invalid URL"
        }
    }

   /* fun fetchDirectly(url: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://devwashcloud.azurewebsites.net/api/LockerIntegration/Verification/4442407280004-1/21222213701A-001?apiKey=cb71a12703264742b5b8")
            .build()
        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()
        Log.d("MainAdViewModel", "Response: $responseBody")

    }*/

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
                        _showDialog.value = it.data.firstOrNull()
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
                orderId = data.wayBillNo.toLong(),
                boxId = data.doorNo.toLong(),
                trnasDate = Date(),
                branchId = 28,
                trnasType = TransactionType.DROP_OFF,
                boxSize = BoxSizeType.MEDIUM
            )
            transactionDao.insertTransaction(transaction)
            Log.d("MainAdViewModel", "Inserted transaction: $transaction")


            val boxId = data.doorNo.toLong()
            val box = boxDao.getBoxById(boxId)
            if (box != null) {
                val updatedBox = box.copy(boxState = BoxState.OCCUPIED)
                boxDao.updateBox(updatedBox)
                Log.d("MainAdViewModel", "Updated box status to ${updatedBox.boxState} for boxId: $boxId")
            } else {
                Log.e("MainAdViewModel", "Box with ID $boxId not found.")
            }

        }
    }

    fun sendCommand(stationId: String, boxId: String) {
        val intent = Intent("com.washcloud.open_door").apply {
            putExtra("stationId", stationId)
            putExtra("boxId", boxId)
        }
        context.sendBroadcast(intent)
    }

}

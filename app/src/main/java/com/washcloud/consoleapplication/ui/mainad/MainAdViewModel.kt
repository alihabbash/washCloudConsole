package com.washcloud.consoleapplication.ui.mainad

import android.util.Log
import android.webkit.URLUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.washcloud.consoleapplication.remote.config.BASE_URL
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import javax.inject.Inject


object RetrofitClient {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}

interface ApiService {
    @GET
    suspend fun fetchData(@Url url: String): ApiResponse
}

data class ApiResponse(
    val Status: String,
    val message: String,
    val data: List<ApiData>
)

data class ApiData(
    val operationType: String,
    val doorNo: String,
    val terminalSn: String,
    val wayBillNo: String,
    val dropOffUrl: String,
    val type: String
)


@HiltViewModel
class MainAdViewModel @Inject constructor() : ViewModel() {

    private val apiService: ApiService = RetrofitClient.apiService
    private val _apiResponse = MutableLiveData<ApiResponse>()
    val apiResponse: LiveData<ApiResponse> get() = _apiResponse

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun handleBarcode(barcode: String) {
        if (URLUtil.isValidUrl(barcode)) {
            viewModelScope.launch {
                try {
                    val response = apiService.fetchData(barcode)
                    _apiResponse.value = response
                } catch (e: Exception) {
                    _error.value = e.message ?: "An error occurred"
                }
            }
        } else {
            _error.value = "Invalid URL"
        }
    }

    fun fetchDirectly(url: String) {
        viewModelScope.launch {
            try {
                val response = apiService.fetchData(url)
                _apiResponse.value = response
            } catch (e: Exception) {
                _error.value = "Error fetching data from $url: ${e.message ?: "An error occurred"}"
                Log.w("MainAdViewModel", "Error fetching data from $url: ${e.message ?: "An error occurred"}")
            }
        }
    }
}

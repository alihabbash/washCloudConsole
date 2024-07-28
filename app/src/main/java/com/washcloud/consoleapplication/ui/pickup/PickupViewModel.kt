package com.washcloud.consoleapplication.ui.pickup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.washcloud.consoleapplication.local.database.dao.TransactionDao
import com.washcloud.consoleapplication.local.database.dto.TransactionDto
import com.washcloud.consoleapplication.remote.model.pickup.StaffPickupRequest
import com.washcloud.consoleapplication.remote.model.pickup.StaffPickupResponse
import com.washcloud.consoleapplication.remote.usecase.StaffPickupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class PickupViewModel @Inject constructor(
    private val transactionDao: TransactionDao
//    private val staffPickupUseCase: StaffPickupUseCase
) : ViewModel() {
    private val _transactions = MutableStateFlow<List<TransactionDto>>(emptyList())
    val transactions: StateFlow<List<TransactionDto>> get() = _transactions

    /*private val _staffPickupResponse = MutableStateFlow<StaffPickupResponse?>(null)
    val staffPickupResponse: StateFlow<StaffPickupResponse?> get() = _staffPickupResponse

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error*/
    init {
        fetchTransactions()
    }

    private fun fetchTransactions() {
        viewModelScope.launch(Dispatchers.IO) {
            val transactionsList = transactionDao.getAllTransactions()
            _transactions.value = transactionsList
        }
    }

    /*fun staffPickup(request: StaffPickupRequest) {
        viewModelScope.launch {
            try {
                val response = staffPickupUseCase(request)
                _staffPickupResponse.value = response
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }*/
}
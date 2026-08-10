package com.washcloud.consoleapplication.ui.admin.customers

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.washcloud.consoleapplication.local.database.ConsoleDatabase
import com.washcloud.consoleapplication.local.database.dto.CustomerUserDto
import com.washcloud.consoleapplication.utils.FileLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoredCustomersViewModel @Inject constructor(
    private val database: ConsoleDatabase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _customers = MutableStateFlow<List<CustomerUserDto>>(emptyList())
    val customers: StateFlow<List<CustomerUserDto>> = _customers.asStateFlow()

    init {
        fetchCustomers()
    }

    private fun fetchCustomers() {
        viewModelScope.launch {
            try {
                val customerList = database.getCustomerUserDao().getAllCustomerUsers()
                _customers.value = customerList
            } catch (e: Exception) {
                FileLogger.log(
                    context,
                    "StoredCustomersViewModel",
                    "Error fetching customers: ${e.message}"
                )
                _customers.value = emptyList()
            }
        }
    }
}

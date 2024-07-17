package com.washcloud.consoleapplication.ui.admin.phoneNumber

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.washcloud.consoleapplication.local.preferences.PHONE_NUMBER
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdatePhoneNumberViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val _currentPhoneNumber = MutableStateFlow("")
    val currentPhoneNumber: StateFlow<String> = _currentPhoneNumber

    private val _newPhoneNumber = MutableStateFlow("")
    val newPhoneNumber: StateFlow<String> = _newPhoneNumber

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _phoneNumberHasChanged = MutableStateFlow(false)
    val phoneNumberHasChanged: StateFlow<Boolean> = _phoneNumberHasChanged

    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)

    init {
        loadCurrentPhoneNumber()
    }

    private fun loadCurrentPhoneNumber() {

        _currentPhoneNumber.value = sharedPreferences.getString(PHONE_NUMBER, "") ?: ""
    }

    fun onNewPhoneNumberChange(phoneNumber: String) {
        _newPhoneNumber.value = phoneNumber
    }

    fun updatePhoneNumber() {
        viewModelScope.launch {


            if (_newPhoneNumber.value.isBlank()) {
                _errorMessage.value = "New phone number cannot be empty"
                return@launch
            }

            sharedPreferences.edit().putString(PHONE_NUMBER, _newPhoneNumber.value).apply()
            _errorMessage.value = null
            _phoneNumberHasChanged.value = true
        }
    }
}
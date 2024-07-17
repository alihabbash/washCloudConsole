
package com.washcloud.consoleapplication.ui.admin.login
import android.app.Application

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


import androidx.preference.PreferenceManager
import com.washcloud.consoleapplication.local.preferences.ADMIN_PASSWORD
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AdminLoginViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val _isPasswordCorrect = MutableStateFlow(false)
    val isPasswordCorrect: StateFlow<Boolean> = _isPasswordCorrect
    val passwordText = MutableStateFlow("")

    private val _showAlert = MutableStateFlow(false)
    val showAlert: StateFlow<Boolean> = _showAlert

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)

    fun verifyPassword() {
        viewModelScope.launch {
            val storedPassword = sharedPreferences.getString(ADMIN_PASSWORD, "")
            if (passwordText.value == storedPassword) {
                _isPasswordCorrect.value = true
            } else {
                _showAlert.value = true
            }
        }
    }

    fun dismissAlert() {
        _showAlert.value = false
    }

    fun resetPasswordCorrectState() {
        _isPasswordCorrect.value = false
        passwordText.value = ""
    }


}

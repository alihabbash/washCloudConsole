package com.washcloud.consoleapplication.ui.admin.password

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.washcloud.consoleapplication.local.preferences.ADMIN_PASSWORD
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChangePasswordViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
    private val _currentPassword = MutableStateFlow("")
    val currentPassword: StateFlow<String> = _currentPassword

    private val _newPassword = MutableStateFlow("")
    val newPassword: StateFlow<String> = _newPassword

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _passwordHasChanged = MutableStateFlow(false)
    val passwordHasChanged: StateFlow<Boolean> = _passwordHasChanged

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)

    fun onCurrentPasswordChange(password: String) {
        _currentPassword.value = password
    }

    fun onNewPasswordChange(password: String) {
        _newPassword.value = password
    }

    fun onConfirmPasswordChange(password: String) {
        _confirmPassword.value = password
    }

    fun changePassword(onSave: () -> Unit) {
        viewModelScope.launch {
            val storedPassword = sharedPreferences.getString(ADMIN_PASSWORD, "")

            when {
                _currentPassword.value != storedPassword -> {
                    _errorMessage.value = "Current password is incorrect"
                }
                _newPassword.value != _confirmPassword.value -> {
                    _errorMessage.value = "New passwords do not match"
                }
                else -> {
                    sharedPreferences.edit().putString(ADMIN_PASSWORD, _newPassword.value).apply()
                    _errorMessage.value = null
                    _passwordHasChanged.value = true
                    onSave()
                }
            }
        }
    }
}
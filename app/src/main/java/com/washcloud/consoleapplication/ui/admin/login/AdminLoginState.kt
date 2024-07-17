package com.washcloud.consoleapplication.ui.admin.login

sealed class AdminLoginState {
    object Initial: AdminLoginState()
    object Loading: AdminLoginState()
    object Success: AdminLoginState()
    data class Error(val message: String): AdminLoginState()
}
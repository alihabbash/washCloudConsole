package com.washcloud.consoleapplication.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.washcloud.consoleapplication.remote.usecase.StaffLoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StaffLoginViewModel @Inject constructor(
    private val loginUseCase: StaffLoginUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<LoginState?>(null)
    val uiState: StateFlow<LoginState?> = _uiState.asStateFlow()

    val accountText = MutableStateFlow("")
    val passwordText = MutableStateFlow("")

    fun login() {
        viewModelScope.launch {
            if (_uiState.value is LoginState.Loading || !validateFields()) return@launch
            _uiState.value = LoginState.Loading
            runCatching {
                loginUseCase(StaffLoginUseCase.Params(accountText.value, passwordText.value))
            }.fold(
                onSuccess = {
                    _uiState.value =
                            LoginState.Success

                },
                onFailure = {
                    _uiState.value =
                        LoginState.Error(it.message?:"" )
                }
            )
        }
    }

    private fun validateFields(): Boolean {
        return when {
            !isValidAccount() -> {
//                accountError.value = R.string.error_email
                false
            }
            !isValidPassword() -> {
//                passwordError.value = R.string.error_password
                false
            }
            else -> true
        }
    }

    private fun isValidAccount(): Boolean {
        return accountText.value.isNotBlank()
    }

    private fun isValidPassword(): Boolean {
        return passwordText.value.isNotBlank()
    }
}
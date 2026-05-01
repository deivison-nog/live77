package com.live77.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

sealed class LoginResult {
    object Success : LoginResult()
    object InvalidFormat : LoginResult()
    object InvalidCredentials : LoginResult()
}

/**
 * ViewModel for the login screen.
 *
 * NOTE: Because no backend authentication service is provided, credentials are validated
 * locally against hardcoded values:
 *   Login:  123456
 *   Password: 123456
 *
 * Replace this logic with a real API call when a backend becomes available.
 */
class LoginViewModel : ViewModel() {

    // Hardcoded demo credentials (documented assumption — no backend available)
    private val validLogin = "123456"
    private val validPassword = "123456"

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(login: String, password: String) {
        if (!login.matches(Regex("\\d{6}")) || !password.matches(Regex("\\d{6}"))) {
            _loginResult.value = LoginResult.InvalidFormat
            return
        }
        _loginResult.value = if (login == validLogin && password == validPassword) {
            LoginResult.Success
        } else {
            LoginResult.InvalidCredentials
        }
    }
}

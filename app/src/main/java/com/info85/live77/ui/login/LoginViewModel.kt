package com.info85.live77.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.info85.live77.data.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class LoginResult {
    object Success : LoginResult()
    object InvalidFormat : LoginResult()
    object InvalidCredentials : LoginResult()
    object Loading : LoginResult()
    data class NetworkError(val message: String) : LoginResult()
}

/**
 * ViewModel for the login screen.
 *
 * Credentials are validated by POSTing to the backend API at [ApiClient.API_URL].
 * The server queries the MySQL database and returns {"success": true/false}.
 */
class LoginViewModel : ViewModel() {

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(login: String, password: String) {
        if (!login.matches(Regex("\\d{6}")) || !password.matches(Regex("\\d{6}"))) {
            _loginResult.value = LoginResult.InvalidFormat
            return
        }

        _loginResult.value = LoginResult.Loading
        viewModelScope.launch {
            try {
                val authenticated = withContext(Dispatchers.IO) {
                    ApiClient.login(login, password)
                }
                _loginResult.value = if (authenticated) {
                    LoginResult.Success
                } else {
                    LoginResult.InvalidCredentials
                }
            } catch (e: Exception) {
                _loginResult.value = LoginResult.NetworkError(e.message ?: "Erro de conexão")
            }
        }
    }
}

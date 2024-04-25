package com.calmscient.viewmodels

import android.content.DialogInterface
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calmscient.ApiService
import com.calmscient.di.remote.request.LoginRequest
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.repository.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val loginRepository: LoginRepository) : ViewModel() {

    val loginResultLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val loadingLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val responseData: MutableLiveData<LoginResponse?> = MutableLiveData()
    val failureResponseData: MutableLiveData<LoginResponse?> = MutableLiveData()
    val errorLiveData: MutableLiveData<String> = MutableLiveData()

    private var lastUsername: String? = null
    private var lastPassword: String? = null

    fun loginUser(username: String, password: String) {
        lastUsername = username
        lastPassword = password
        loadingLiveData.value = true // Show loader
        viewModelScope.launch {
            try {
                val loginRequest = LoginRequest(username, password)
                val response = loginRepository.loginUser(loginRequest)
                handleResponse(response)
            }
            catch (e: SocketTimeoutException) {
                // Handle timeout exception
                Log.e("LoginViewModel", "Timeout Exception: ${e.message}")
                errorLiveData.postValue("Timeout Exception: ${e.message}")
                loginResultLiveData.postValue(false) // Indicate login failure
            }
            catch (e: Exception) {
                e.printStackTrace()
                loginResultLiveData.value = false
                errorLiveData.postValue("Timeout Exception: ${e.message}")
            } finally {
                loadingLiveData.value = false
            }
        }
    }

    fun retryLogin() {
        lastUsername?.let { username ->
            lastPassword?.let { password ->
                loginUser(username, password)
            }
        }
    }

    private suspend fun handleResponse(call: Call<LoginResponse>) {
        withContext(Dispatchers.IO) {
            try {
                val response = call.execute()
                if (response.isSuccessful) {
                    val isValidLogin = response.body()?.statusResponse?.responseCode == 200
                    if (isValidLogin) {
                        responseData.postValue(response.body()) // Store response data
                        Log.d("LoginViewModel", "Response Data: ${response.body()}")
                    } else {
                        failureResponseData.postValue(response.body())
                    }
                    loginResultLiveData.postValue(isValidLogin)
                } else {
                    loginResultLiveData.postValue(false)
                }
            } catch (e: SocketTimeoutException) {
                // Handle timeout exception
                Log.e("LoginViewModel", "Timeout Exception: ${e.message}")
                errorLiveData.postValue("Timeout Exception: ${e.message}")
                loginResultLiveData.postValue(false) // Indicate login failure
            } catch (e: Exception) {
                e.printStackTrace()
                errorLiveData.postValue("Timeout Exception: ${e.message}")
                loginResultLiveData.postValue(false)
            }
        }
    }
}


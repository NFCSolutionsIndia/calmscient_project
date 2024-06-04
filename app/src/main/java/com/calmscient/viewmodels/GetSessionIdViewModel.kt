/*
 *
 *      Copyright (c) 2023- NFC Solutions, - All Rights Reserved
 *      All source code contained herein remains the property of NFC Solutions Incorporated
 *      and protected by trade secret or copyright law of USA.
 *      Dissemination, De-compilation, Modification and Distribution are strictly prohibited unless
 *      there is a prior written permission or license agreement from NFC Solutions.
 *
 *      Author : @Pardha Saradhi
 */
package com.calmscient.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calmscient.di.remote.request.SessionIdRequest
import com.calmscient.di.remote.response.SessionIdResponse
import com.calmscient.repository.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Response
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class GetSessionIdViewModel @Inject constructor(private val repository: LoginRepository) : ViewModel() {

    val saveResponseLiveData: MutableLiveData<SessionIdResponse?> = MutableLiveData()
    val loadingLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val successLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val successNotAnsweredData: MutableLiveData<Boolean> = MutableLiveData()
    val errorLiveData: MutableLiveData<String?> = MutableLiveData()
    val failureLiveData: MutableLiveData<String?> = MutableLiveData()
    val failureResponseData: MutableLiveData<SessionIdResponse?> = MutableLiveData()

    private var lastRequestAccessToken: String = ""
    private var lastAccessToken: String = ""

    fun getSessionId(accessToken: String) {
        loadingLiveData.value = true
        lastAccessToken = accessToken
        Log.d("Access Token", accessToken)
        viewModelScope.launch {
            try {
               // val request = SessionIdRequest(requestBodyAccessToken)
                val response = repository.getSessionId(accessToken)
                handleResponse(response)
            } catch (e: SocketTimeoutException) {
                Log.e("GetSessionIdViewModel", "Timeout Exception: ${e.message}")
                failureLiveData.postValue("Timeout Exception: ${e.message}")
            } catch (e: Exception) {
                Log.e("GetSessionIdViewModel", "Exception: ${e.message}", e)
                failureLiveData.value = "Failed to save patient answers."
            } finally {
                loadingLiveData.value = false
            }
        }
    }

    private suspend fun handleResponse(call: Call<SessionIdResponse>) {
        withContext(Dispatchers.IO) {
            try {
                val response: Response<SessionIdResponse> = call.execute()
                if (response.isSuccessful) {
                    saveResponseLiveData.postValue(response.body())
                    Log.d("GetSessionIdViewModel", "Success: ${response.body()}")
                } else {
                    failureResponseData.postValue(response.body())
                    Log.e("GetSessionIdViewModel", "Failure: ${response.errorBody()?.string()}")
                }
            } catch (e: SocketTimeoutException) {
                Log.e("GetSessionIdViewModel", "Timeout Exception: ${e.message}")
                failureLiveData.postValue("Timeout Exception: ${e.message}")
                errorLiveData.postValue("Timeout Exception: ${e.message}")
            } catch (e: Exception) {
                Log.e("GetSessionIdViewModel", "Exception: ${e.message}", e)
                failureLiveData.postValue("Failed to get data.")
            }
        }
    }

    fun retryGetSessionId() {
        getSessionId(lastAccessToken)
    }

    fun clear() {
        saveResponseLiveData.value = null
        loadingLiveData.value = false
        successLiveData.value = false
        successNotAnsweredData.value = false
        errorLiveData.value = null
        failureLiveData.value = null
        failureResponseData.value = null
    }
}


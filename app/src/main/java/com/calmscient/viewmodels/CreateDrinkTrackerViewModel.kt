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
import com.calmscient.di.remote.request.CreateDrinkTrackerRequest
import com.calmscient.di.remote.request.PatientAnswersWrapper
import com.calmscient.di.remote.response.CreateDrinkTrackerResponse
import com.calmscient.di.remote.response.DrinkTrackerResponse
import com.calmscient.di.remote.response.PatientAnswerSaveResponse
import com.calmscient.repository.ScreeningQuestionnaireRepository
import com.calmscient.repository.TakingControlRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class CreateDrinkTrackerViewModel @Inject constructor(private val repository: TakingControlRepository) : ViewModel() {

    val saveResponseLiveData: MutableLiveData<CreateDrinkTrackerResponse?> = MutableLiveData()
    val loadingLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val successLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val successNotAnsweredData: MutableLiveData<Boolean> = MutableLiveData()
    val errorLiveData: MutableLiveData<String?> = MutableLiveData()
    val failureLiveData: MutableLiveData<String?> = MutableLiveData()
    val failureResponseData: MutableLiveData<CreateDrinkTrackerResponse?> = MutableLiveData()

    private var lastRequestBody: CreateDrinkTrackerRequest? = null
    private var lastAccessToken: String = ""

    fun createDrinkTrackerList(requestBody: CreateDrinkTrackerRequest, accessToken: String) {
        loadingLiveData.value = true
        lastRequestBody = requestBody
        lastAccessToken = accessToken
        viewModelScope.launch {
            try {
                val response = repository.createDrinkTrackerList(requestBody, accessToken)
                handleResponse(response)
            } catch (e: SocketTimeoutException) {
                failureLiveData.postValue("Timeout Exception: ${e.message}")
            } catch (e: Exception) {
                failureLiveData.value = "Failed to save patient answers."
            } finally {
                loadingLiveData.value = false
            }
        }
    }

    private suspend fun handleResponse(call: Call<CreateDrinkTrackerResponse>) {
        withContext(Dispatchers.IO) {
            try {
                val response = call.execute()
                if (response.isSuccessful) {
                    val isSuccess = response.body()?.statusResponse?.responseCode == 200
                    val isNotAnswered = response.body()?.statusResponse?.responseCode == 300
                    if (isSuccess) {
                        saveResponseLiveData.postValue(response.body())
                        successLiveData.postValue(isSuccess)
                        Log.d("GetDrinkTrackerViewModel ", "Success: ${response.body()}")
                    } else if (isNotAnswered) {
                        saveResponseLiveData.postValue(response.body())
                        successNotAnsweredData.postValue(isNotAnswered)
                    } else {
                        failureResponseData.postValue(response.body())
                        val res = response.body()?.statusResponse?.responseMessage
                        errorLiveData.postValue("Error Occurred: $res")
                        successNotAnsweredData.postValue(false)
                    }
                } else {
                    errorLiveData.postValue("Failed to get data.")
                }
            } catch (e: SocketTimeoutException) {
                // Handle timeout exception
                failureLiveData.postValue("Timeout Exception: ${e.message}")
                errorLiveData.postValue("Timeout Exception: ${e.message}")
                Log.e("GetDrinkTrackerViewModel", "Timeout Exception: ${e.message}")
            } catch (e: Exception) {
                e.printStackTrace()
                failureLiveData.postValue("Failed to get data.")
            }
        }
    }


    fun retryCreateDrinkTrackerList() {
        lastRequestBody?.let { createDrinkTrackerList(it, lastAccessToken) }
    }


    fun clear() {
        // Reset LiveData objects to their initial state
        saveResponseLiveData.value = null
        loadingLiveData.value = false
        successLiveData.value = false
        successNotAnsweredData.value = false
        errorLiveData.value = null
        failureLiveData.value = null
        failureResponseData.value = null

    }

}


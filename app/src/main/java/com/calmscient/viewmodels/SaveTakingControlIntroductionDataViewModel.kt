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
import com.calmscient.di.remote.request.SavePatientMoodWrapper
import com.calmscient.di.remote.request.SaveTakingControlIntroductionRequest
import com.calmscient.di.remote.request.SaveTakingControlIntroductionWrapper
import com.calmscient.di.remote.response.PatientMoodResponse
import com.calmscient.di.remote.response.SavePatientMoodResponse
import com.calmscient.di.remote.response.SaveTakingControlIntroductionResponse
import com.calmscient.repository.TakingControlRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class SaveTakingControlIntroductionDataViewModel @Inject constructor(private val repository: TakingControlRepository) : ViewModel() {

    val saveResponseLiveData: MutableLiveData<SaveTakingControlIntroductionResponse?> = MutableLiveData()
    val loadingLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val successLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val successNotAnsweredData: MutableLiveData<Boolean> = MutableLiveData()
    val errorLiveData: MutableLiveData<String?> = MutableLiveData()
    val failureLiveData: MutableLiveData<String?> = MutableLiveData()
    val failureResponseData: MutableLiveData<SaveTakingControlIntroductionResponse?> = MutableLiveData()



    private var lastRequestBody: SaveTakingControlIntroductionRequest? = null
    private var lastAccessToken: String = ""

    // Function to save patient answers
    fun saveTakingControlIntroductionData( requestBody: SaveTakingControlIntroductionRequest, accessToken:String) {
        loadingLiveData.value = true // Show loader

        lastRequestBody = requestBody
        lastAccessToken = accessToken

        viewModelScope.launch {
            try {

                val response = repository.saveTakingControlIntroductionData(requestBody,accessToken)
                Log.d("SaveTakingControlIntroductionDataViewModel", "Response: $response")
                handleResponse(response)
            } catch (e: SocketTimeoutException) {
                // Handle timeout exception
                failureLiveData.postValue("Timeout Exception: ${e.message}")
                Log.e("GetDrinkTrackerViewModel", "Timeout Exception: ${e.message}")
            } catch (e: Exception) {
                e.printStackTrace()
                failureLiveData.value = "Failed to get Data."
            } finally {
                loadingLiveData.value = false
            }
        }
    }

    // Function to handle API response
    private suspend fun handleResponse(call: Call<SaveTakingControlIntroductionResponse>) {
        withContext(Dispatchers.IO) {
            try {
                val response = call.execute()
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        // Check if the response body contains the expected data
                        if (responseBody.statusResponse != null) {
                            saveResponseLiveData.postValue(responseBody)
                            successLiveData.postValue(true)
                            Log.d("GetPatientMoodViewModel ", "Success: $responseBody")
                        } else {
                            errorLiveData.postValue("Response body doesn't contain expected data.")
                            successLiveData.postValue(false)
                        }
                    } else {
                        // Handle case where response body is null
                        errorLiveData.postValue("Response body is null.")
                        successLiveData.postValue(false)
                    }
                } else {
                    // Handle case where response is not successful
                    errorLiveData.postValue("Failed to get data.")
                }
            } catch (e: SocketTimeoutException) {
                // Handle timeout exception
                failureLiveData.postValue("Timeout Exception: ${e.message}")
                errorLiveData.postValue("Timeout Exception: ${e.message}")
                Log.e("GetPatientMoodViewModel", "Timeout Exception: ${e.message}")
            } catch (e: Exception) {
                e.printStackTrace()
                failureLiveData.postValue("Failed to get data.")
            }
        }
    }

    fun retrySaveTakingControlIntroductionData() {
        if(lastAccessToken.isNotEmpty())
        {
            lastRequestBody?.let { saveTakingControlIntroductionData(it,lastAccessToken) }
        }
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

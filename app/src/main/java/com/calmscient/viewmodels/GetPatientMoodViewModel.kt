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
import com.calmscient.di.remote.request.AppointmentDetailsRequestData
import com.calmscient.di.remote.request.PatientMoodRequest
import com.calmscient.di.remote.response.AppointmentDetailsResponseData
import com.calmscient.di.remote.response.PatientMoodResponse
import com.calmscient.repository.LoginRepository
import com.calmscient.repository.MedicationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class GetPatientMoodViewModel @Inject constructor(private val repository: LoginRepository) : ViewModel() {

    val saveResponseLiveData: MutableLiveData<PatientMoodResponse?> = MutableLiveData()
    val loadingLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val successLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val successNotAnsweredData: MutableLiveData<Boolean> = MutableLiveData()
    val errorLiveData: MutableLiveData<String?> = MutableLiveData()
    val failureLiveData: MutableLiveData<String?> = MutableLiveData()
    val failureResponseData: MutableLiveData<PatientMoodResponse?> = MutableLiveData()



    private var lastPatientLocationId: Int = -1
    private var lastClientId: Int = -1
    private var lastPatientId: Int = -1
    private var lastDateTime: String = ""

    // Function to save patient answers
    fun getPatientMood(  patientLocationId: Int,clientId: Int, patientId: Int,  dateTime:String) {
        loadingLiveData.value = true // Show loader

        lastPatientLocationId = patientLocationId
        lastDateTime= dateTime
        lastClientId = clientId
        lastPatientId = patientId

        viewModelScope.launch {
            try {
                val request = PatientMoodRequest(patientLocationId,clientId,patientId,dateTime)
                val response = repository.getPatientMood(request)
                Log.d("GetPatientMoodViewModel", "Response: $response")
                handleResponse(response)
            } catch (e: SocketTimeoutException) {
                // Handle timeout exception
                failureLiveData.postValue("Timeout Exception: ${e.message}")
                Log.e("GetPatientMoodViewModel", "Timeout Exception: ${e.message}")
            } catch (e: Exception) {
                e.printStackTrace()
                failureLiveData.value = "Failed to get Data."
            } finally {
                loadingLiveData.value = false
            }
        }
    }

    // Function to handle API response
    private suspend fun handleResponse(call: Call<PatientMoodResponse>) {
        withContext(Dispatchers.IO) {
            try {
                val response = call.execute()
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        // Check if the response body contains the expected data
                        if (responseBody.moodData != null) {
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


    // Function to retry get
    fun retryGetPatientMood() {
        if(lastPatientLocationId>0 && lastPatientId>0 && lastClientId>0 && lastDateTime.isNotEmpty())
        {
            getPatientMood(lastPatientLocationId,lastClientId,lastPatientId,lastDateTime)
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

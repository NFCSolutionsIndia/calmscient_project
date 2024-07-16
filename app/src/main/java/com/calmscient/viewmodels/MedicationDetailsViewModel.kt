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
import com.calmscient.di.remote.request.MedicationDetailsRequest
import com.calmscient.di.remote.request.ScreeningsResultsRequest
import com.calmscient.di.remote.response.MedicationDetailsResponse
import com.calmscient.di.remote.response.ScreeningResultsResponse
import com.calmscient.repository.MedicationsRepository
import com.calmscient.repository.ScreeningQuestionnaireRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class MedicationDetailsViewModel @Inject constructor(private val repository: MedicationsRepository) : ViewModel() {

    val saveResponseLiveData: MutableLiveData<MedicationDetailsResponse?> = MutableLiveData()
    val loadingLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val successLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val successNotAnsweredData: MutableLiveData<Boolean> = MutableLiveData()
    val errorLiveData: MutableLiveData<String> = MutableLiveData()
    val failureLiveData: MutableLiveData<String> = MutableLiveData()
    val failureResponseData: MutableLiveData<MedicationDetailsResponse?> = MutableLiveData()

    private var lastPatientLocationId: Int = -1
    private var lastPatientId: Int = -1
    private var lastClientId: Int = -1
    private var lastFromDate: String = "04/19/2024"
    private var lastToDate: String = "04/25/2024"
    private var lastAccessToken: String =""

    // Function to save patient answers
    fun getMedicationDetails(
        patientLocationId: Int,
        patientId: Int,
        clientId: Int,
        fromDate:String,
        toDate : String,
        accessToken : String
    ) {
        loadingLiveData.value = true // Show loader

        lastFromDate = fromDate
        lastToDate = toDate
        lastClientId = clientId
        lastPatientId = patientId
        lastPatientLocationId = patientLocationId
        lastAccessToken = accessToken

        viewModelScope.launch {
            try {
                val request = MedicationDetailsRequest(patientId, patientLocationId,fromDate,toDate,clientId)
                val response = repository.getMedicationDetails(request,accessToken)
                Log.d("Medication Response - 2", "Response: $response")
                handleResponse(response)
            } catch (e: SocketTimeoutException) {
                // Handle timeout exception
                failureLiveData.postValue("Timeout Exception: ${e.message}")
                Log.e("MedicationDetailsViewModel", "Timeout Exception: ${e.message}")
            } catch (e: Exception) {
                e.printStackTrace()
                failureLiveData.value = "Failed to get Data."
            } finally {
                loadingLiveData.value = false
            }
        }
    }

    // Function to handle API response
    private suspend fun handleResponse(call: Call<MedicationDetailsResponse>) {
        withContext(Dispatchers.IO) {
            try {
                val response = call.execute()
                if (response.isSuccessful) {
                    val isSuccess = response.body()?.response?.responseCode == 200
                    val isNotAnswered = response.body()?.response?.responseCode == 300
                    if (isSuccess) {
                        saveResponseLiveData.postValue(response.body())
                        successLiveData.postValue(isSuccess)
                        Log.d("Medications Response - 1", "Success: ${response.body()}")
                    } else if (isNotAnswered) {
                        saveResponseLiveData.postValue(response.body())
                        successNotAnsweredData.postValue(isNotAnswered)
                    } else {
                        failureResponseData.postValue(response.body())
                        val res = response.body()?.response?.responseMessage
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
                Log.e("MedicationDetailsViewModel", "Timeout Exception: ${e.message}")
            } catch (e: Exception) {
                e.printStackTrace()
                failureLiveData.postValue("Failed to get data.")
            }
        }
    }

    // Function to retry get
    fun retryGetMedicationDetails() {
        if (lastPatientLocationId > 0 && lastPatientId > 0 && lastClientId > 0 && lastAccessToken.isNotEmpty())
            getMedicationDetails(lastPatientLocationId, lastPatientId,lastClientId,lastFromDate,lastToDate,lastAccessToken)
    }

    fun clear() {
        // Reset LiveData objects to their initial state
        saveResponseLiveData.value = null
        loadingLiveData.value = false
        successLiveData.value = false
        successNotAnsweredData.value = false
        errorLiveData.value = ""
        failureLiveData.value = ""
        failureResponseData.value = null

    }
}

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
import com.calmscient.di.remote.request.DrinkTrackerRequest
import com.calmscient.di.remote.request.GetBasicKnowledgeIndexRequest
import com.calmscient.di.remote.request.ManageAnxietyIndexRequest
import com.calmscient.di.remote.request.PatientMoodRequest
import com.calmscient.di.remote.response.AppointmentDetailsResponseData
import com.calmscient.di.remote.response.DrinkTrackerResponse
import com.calmscient.di.remote.response.GetBasicKnowledgeIndexResponse
import com.calmscient.di.remote.response.ManageAnxietyIndexResponse
import com.calmscient.di.remote.response.PatientMoodResponse
import com.calmscient.di.remote.response.SummaryOfDASTResponse
import com.calmscient.repository.LoginRepository
import com.calmscient.repository.ManageAnxietyRepository
import com.calmscient.repository.MedicationsRepository
import com.calmscient.repository.TakingControlRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class GetBasicKnowledgeIndexDataViewModel @Inject constructor(private val repository: TakingControlRepository) : ViewModel() {

    val saveResponseLiveData: MutableLiveData<GetBasicKnowledgeIndexResponse?> = MutableLiveData()
    val loadingLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val successLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val successNotAnsweredData: MutableLiveData<Boolean> = MutableLiveData()
    val errorLiveData: MutableLiveData<String?> = MutableLiveData()
    val failureLiveData: MutableLiveData<String?> = MutableLiveData()
    val failureResponseData: MutableLiveData<GetBasicKnowledgeIndexResponse?> = MutableLiveData()


    private var lastClientId: Int = -1
    private var lastPatientId: Int = -1
    private var lastAccessToken: String = ""

    // Function to save patient answers
    fun getBasicKnowledgeIndexData(clientId:Int, patientId: Int, accessToken:String) {
        loadingLiveData.value = true // Show loader

        lastClientId = clientId
        lastPatientId = patientId
        lastAccessToken = accessToken

        viewModelScope.launch {
            try {
                val request = GetBasicKnowledgeIndexRequest(clientId,patientId)
                val response = repository.getBasicKnowledgeIndexData(request,accessToken)
                Log.d("GetBasicKnowledgeIndexDataViewModel", "Response: $response")
                handleResponse(response)
            } catch (e: SocketTimeoutException) {
                // Handle timeout exception
                failureLiveData.postValue("Timeout Exception: ${e.message}")
                Log.e("GetBasicKnowledgeIndexDataViewModel", "Timeout Exception: ${e.message}")
            } catch (e: Exception) {
                e.printStackTrace()
                failureLiveData.value = "Failed to get Data."
            } finally {
                loadingLiveData.value = false
            }
        }
    }

    // Function to handle API response
    private suspend fun handleResponse(call: Call<GetBasicKnowledgeIndexResponse>) {
        withContext(Dispatchers.IO) {
            try {
                val response = call.execute()
                if (response.isSuccessful) {
                    val isSuccess = response.body()?.statusResponse?.responseCode == 200
                    val isNotAnswered = response.body()?.statusResponse?.responseCode == 300
                    if (isSuccess) {
                        saveResponseLiveData.postValue(response.body())
                        successLiveData.postValue(isSuccess)
                        Log.d("GetBasicKnowledgeIndexDataViewModel ", "Success: ${response.body()}")
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
                Log.e("GetBasicKnowledgeIndexDataViewModel", "Timeout Exception: ${e.message}")
            } catch (e: Exception) {
                e.printStackTrace()
                failureLiveData.postValue("Failed to get data.")
            }
        }
    }


    // Function to retry get
    fun retryGetBasicKnowledgeIndexData() {
        if(lastPatientId>0 && lastClientId>0  && lastAccessToken.isNotEmpty())
        {
            getBasicKnowledgeIndexData(lastClientId,lastPatientId,lastAccessToken)
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
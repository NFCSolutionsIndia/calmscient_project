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
import com.calmscient.di.remote.request.AddMedicationDetailsRequest
import com.calmscient.di.remote.request.AlarmUpdateRequest
import com.calmscient.di.remote.request.AlarmWrapper
import com.calmscient.di.remote.request.MedicationDetailsRequest
import com.calmscient.di.remote.request.ScreeningsResultsRequest
import com.calmscient.di.remote.request.UpdatePatientThemeRequest
import com.calmscient.di.remote.response.AddMedicationResponse
import com.calmscient.di.remote.response.AlarmUpdateResponse
import com.calmscient.di.remote.response.MedicationDetailsResponse
import com.calmscient.di.remote.response.Response
import com.calmscient.di.remote.response.ScreeningResultsResponse
import com.calmscient.repository.MedicationsRepository
import com.calmscient.repository.ScreeningQuestionnaireRepository
import com.calmscient.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class UpdatePatientThemeViewModel @Inject constructor(private val repository: UserProfileRepository) : ViewModel() {

    val saveResponseLiveData: MutableLiveData<Response?> = MutableLiveData()
    val loadingLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val successLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val successNotAnsweredData: MutableLiveData<Boolean> = MutableLiveData()
    val errorLiveData: MutableLiveData<String?> = MutableLiveData()
    val failureLiveData: MutableLiveData<String?> = MutableLiveData()
    val failureResponseData: MutableLiveData<Response?> = MutableLiveData()


    private lateinit var  lastRequest : UpdatePatientThemeRequest
    private lateinit var  lastAccessToken : String


    // Function to save patient answers
    fun updatePatientTheme(request : UpdatePatientThemeRequest,accessToken : String) {
        loadingLiveData.value = true // Show loader
        lastRequest= request
        lastAccessToken = accessToken
        viewModelScope.launch {
            try {

                this@UpdatePatientThemeViewModel.lastRequest = request
                val response = repository.updatePatientTheme(accessToken,request)
                Log.d("Add Medication Response - 2", "Response: $response")
                handleResponse(response)
            } catch (e: SocketTimeoutException) {
                // Handle timeout exception
                failureLiveData.postValue("Timeout Exception: ${e.message}")
                Log.e("AddMedicationDetailsViewModel", "Timeout Exception: ${e.message}")
            } catch (e: Exception) {
                e.printStackTrace()
                failureLiveData.value = "Failed to get Data."
            } finally {
                loadingLiveData.value = false
            }
        }
    }

    // Function to handle API response
    private suspend fun handleResponse(call: Call<Response>) {
        withContext(Dispatchers.IO) {
            try {
                val response = call.execute()
                if (response.isSuccessful) {
                    val isSuccess = response.body()?.responseCode == 200
                    val isNotAnswered = response.body()?.responseCode == 300
                    if (isSuccess) {
                        saveResponseLiveData.postValue(response.body())
                        successLiveData.postValue(isSuccess)
                        Log.d("Add Medication Response ", "Success: ${response.body()}")
                    } else if (isNotAnswered) {
                        saveResponseLiveData.postValue(response.body())
                        successNotAnsweredData.postValue(isNotAnswered)
                    } else {
                        failureResponseData.postValue(response.body())
                        val res = response.body()?.responseMessage
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
                Log.e("AddMedicationDetailsViewModel", "Timeout Exception: ${e.message}")
            } catch (e: Exception) {
                e.printStackTrace()
                failureLiveData.postValue("Failed to get data.")
            }
        }
    }

    // Function to retry get
    fun retryUpdatePatientTheme() {


        updatePatientTheme(lastRequest,lastAccessToken)
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

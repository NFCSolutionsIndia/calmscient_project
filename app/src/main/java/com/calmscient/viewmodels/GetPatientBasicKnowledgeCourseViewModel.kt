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

import android.util.JsonToken
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calmscient.di.remote.request.MyDrinkingHabitRequest
import com.calmscient.di.remote.request.SummaryOfAUDITRequest
import com.calmscient.di.remote.response.MyDrinkingHabitResponse
import com.calmscient.di.remote.response.SummaryOfAUDITResponse
import com.calmscient.repository.TakingControlRepository
import com.calmscient.repository.WeeklySummaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class GetPatientBasicKnowledgeCourseViewModel @Inject constructor(private val repository: TakingControlRepository) : ViewModel() {

    var saveResponseLiveData: MutableLiveData<MyDrinkingHabitResponse?> = MutableLiveData()
    var loadingLiveData: MutableLiveData<Boolean> = MutableLiveData()
    var successLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val successNotAnsweredData: MutableLiveData<Boolean> = MutableLiveData()
    val errorLiveData: MutableLiveData<String> = MutableLiveData()
    val failureLiveData: MutableLiveData<String> = MutableLiveData()
    val failureResponseData: MutableLiveData<MyDrinkingHabitResponse?> = MutableLiveData()

    private var lastPatientLocationId: Int = -1
    private var lastPatientId: Int = -1
    private var lastClientId: Int = -1
    private var lastAssessmentId: Int = -1

    private var lastAccessToken: String = ""

    // Function to myDrinkingHabitQuestions
     fun myDrinkingHabitQuestions(assessmentId:Int, clientId: Int,patientId: Int,patientLocationId: Int, accessToken: String ) {
        loadingLiveData.value = true // Show loader

        lastClientId = clientId
        lastPatientId = patientId
        lastPatientLocationId = patientLocationId
        lastAssessmentId = assessmentId
        lastAccessToken = accessToken
        viewModelScope.launch {
            try {
                val request = MyDrinkingHabitRequest(assessmentId,clientId,patientId,patientLocationId)
                val response = repository.getPatientBasicKnowledgeCourse(request,accessToken)
                Log.d("GetPatientBasicKnowledgeCourseViewModel ", "Response: $response")
                handleResponse(response)
            } catch (e: SocketTimeoutException) {
                // Handle timeout exception
                failureLiveData.postValue("Timeout Exception: ${e.message}")
                Log.e("GetPatientBasicKnowledgeCourseViewModel ", "Timeout Exception: ${e.message}")
            } catch (e: Exception) {
                e.printStackTrace()
                failureLiveData.value = "Failed to get Data."
            } finally {
                loadingLiveData.value = false
            }
        }
    }

    // Function to handle API response
    private suspend fun handleResponse(call: Call<MyDrinkingHabitResponse>) {
        withContext(Dispatchers.IO) {
            try {
                val response = call.execute()
                if (response.isSuccessful) {
                    val isSuccess = response.body()?.statusResponse?.responseCode == 200
                    val isNotSuccess = response.body()?.statusResponse?.responseCode == 400
                    if (isSuccess) {
                        saveResponseLiveData.postValue(response.body())
                        successLiveData.postValue(isSuccess)
                        Log.d("GetPatientBasicKnowledgeCourseViewModel ", "Success: ${response.body()}")
                    } else if (isNotSuccess) {
                        saveResponseLiveData.postValue(response.body())
                        successNotAnsweredData.postValue(isNotSuccess)
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
                Log.e("GetPatientBasicKnowledgeCourseViewModel", "Timeout Exception: ${e.message}")
            } catch (e: Exception) {
                e.printStackTrace()
                failureLiveData.postValue("Failed to get data.")
            }
        }
    }

    // Function to retry get
    fun retryMyDrinkingHabitQuestions() {
        if (lastPatientLocationId > 0 && lastPatientId > 0 && lastClientId > 0 )
            myDrinkingHabitQuestions(lastAssessmentId,lastClientId,lastPatientId,lastPatientLocationId,lastAccessToken)
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

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
import com.calmscient.ApiService
import com.calmscient.di.remote.request.ScreeningRequest
import com.calmscient.di.remote.request.ScreeningsAssessmentRequest
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.di.remote.response.QuestionnaireItem
import com.calmscient.di.remote.response.ScreeningAssignmentResponse
import com.calmscient.di.remote.response.ScreeningItem
import com.calmscient.di.remote.response.ScreeningResponse
import com.calmscient.repository.ScreeningQuestionnaireRepository
import com.calmscient.repository.ScreeningsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class ScreeningQuestionnaireViewModel @Inject constructor(private val screeningQuestionnaireRepository: ScreeningQuestionnaireRepository) : ViewModel() {
    val screeningQuestionListLiveData: MutableLiveData<List<QuestionnaireItem>> = MutableLiveData()
    val screeningsQuestionResultLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val loadingLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val failureLiveData: MutableLiveData<String> = MutableLiveData()
    val failureResponseData: MutableLiveData<ScreeningAssignmentResponse?> = MutableLiveData()
    val errorLiveData: MutableLiveData<String> = MutableLiveData()



    private var fromDate: String = ""
    private var patientLocationId: Int = -1
    private var toDate: String = ""
    private var screeningId: Int = -1
    private var patientId: Int = -1
    private var clientId: Int = -1
    private var assessmentId: Int = -1


    fun getScreeningQuestionsList(patientId: Int, clientId: Int, patientLocationId: Int, toDate: String?, fromDate: String?, assessmentId :Int, screeningId :Int) {
        loadingLiveData.value = true // Show loader
        viewModelScope.launch {
            try {
                val request = ScreeningsAssessmentRequest(fromDate,patientLocationId,toDate,screeningId,patientId,clientId,assessmentId)
                val response = screeningQuestionnaireRepository.fetchScreeningsMenuItems(request)
                handleResponse(response)
            }
            catch (e: SocketTimeoutException) {
                // Handle timeout exception
                errorLiveData.postValue("Timeout Exception: ${e.message}")
                Log.e("ScreeningQuestionnaireViewModel", "Timeout Exception: ${e.message}")
                screeningsQuestionResultLiveData.postValue(false) // Indicate login failure
            }
            catch (e: Exception) {
                e.printStackTrace()
                failureLiveData.value = "Failed to fetch screening list."
            } finally {
                loadingLiveData.value = false
            }
        }
    }

    private suspend fun handleResponse(call: Call<ScreeningAssignmentResponse>) {
        withContext(Dispatchers.IO) {
            try {
                val response = call.execute()

                if (response.isSuccessful) {
                    val isSuccess = response.body()?.statusResponse?.responseCode == 200
                    val screeningItems = response.body()
                    if(isSuccess && screeningItems != null) {
                        screeningQuestionListLiveData.postValue(screeningItems.questionnaire)
                        screeningsQuestionResultLiveData.postValue(isSuccess)
                        Log.d("ScreeningQuestionnaireViewModel", "Response Data: ${response.body()}")
                    }
                    else {
                        failureResponseData.postValue(response.body())
                        val res = response.body()?.statusResponse?.responseMessage
                        errorLiveData.postValue("Error Occure: $res")
                    }
                    screeningsQuestionResultLiveData.postValue(false)
                } else {
                    failureLiveData.postValue("Failed to fetch screening list.")
                }
            }
            catch (e: SocketTimeoutException) {
                // Handle timeout exception
                Log.e("ScreeningQuestionnaireViewModel", "Timeout Exception: ${e.message}")
                errorLiveData.postValue("Timeout Exception: ${e.message}")
                screeningsQuestionResultLiveData.postValue(false) // Indicate login failure
            }
            catch (e: Exception) {
                e.printStackTrace()
                failureLiveData.postValue("Failed to fetch screening list.")
                screeningsQuestionResultLiveData.postValue(false)
            }
        }
    }

    // Function to retry fetching menu items
    fun retryScreeningsFetchMenuItems() {
        getScreeningQuestionsList(patientId,clientId,patientLocationId,toDate,fromDate,assessmentId, screeningId)
    }
}

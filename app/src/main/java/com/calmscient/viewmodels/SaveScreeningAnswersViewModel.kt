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
import com.calmscient.di.remote.request.PatientAnswerSaveRequest
import com.calmscient.di.remote.request.PatientAnswersWrapper
import com.calmscient.di.remote.response.PatientAnswerSaveResponse
import com.calmscient.di.remote.response.PatientAnswersStatusResponse
import com.calmscient.repository.ScreeningQuestionnaireRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class SaveScreeningAnswersViewModel @Inject constructor(private val repository: ScreeningQuestionnaireRepository) : ViewModel() {

    val saveResponseLiveData: MutableLiveData<PatientAnswerSaveResponse?> = MutableLiveData()
    val loadingLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val successLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val successNotAnsweredData: MutableLiveData<Boolean> = MutableLiveData()
    val errorLiveData: MutableLiveData<String> = MutableLiveData()
    val failureLiveData: MutableLiveData<String> = MutableLiveData()
    val failureResponseData: MutableLiveData<PatientAnswersStatusResponse?> = MutableLiveData()


    val requestBody: PatientAnswersWrapper? = null
    // Function to save patient answers
    fun savePatientAnswers(requestBody: PatientAnswersWrapper) {
        loadingLiveData.value = true // Show loader
        viewModelScope.launch {
            try {
                val response = repository.saveScreeningQuestionAnswers(requestBody)
                handleResponse(response)
            } catch (e: SocketTimeoutException) {
                // Handle timeout exception
                failureLiveData.postValue("Timeout Exception: ${e.message}")
                Log.e("SaveScreeningAnswersViewModel", "Timeout Exception: ${e.message}")
            } catch (e: Exception) {
                e.printStackTrace()
                failureLiveData.value = "Failed to save patient answers."
            } finally {
                loadingLiveData.value = false
            }
        }
    }

    // Function to handle API response
    private suspend fun handleResponse(call: Call<PatientAnswerSaveResponse>) {
        withContext(Dispatchers.IO) {
            try {
                val response = call.execute()
                if (response.isSuccessful) {
                    val isSuccess = response.body()?.statusResponse?.responseCode == 200
                    val isNotAnswered = response.body()?.statusResponse?.responseCode == 300
                    if (isSuccess) {
                        saveResponseLiveData.postValue(response.body())
                        successLiveData.postValue(isSuccess)
                    }
                   else if(isNotAnswered)
                    {
                        saveResponseLiveData.postValue(response.body())
                        successNotAnsweredData.postValue(isNotAnswered)
                    }
                    else
                    {
                        failureResponseData.postValue(response.body()?.statusResponse)
                        val res = response.body()?.statusResponse?.responseMessage
                        errorLiveData.postValue("Error Occure: $res")
                        successNotAnsweredData.postValue(false)

                    }
                }
                else {
                    errorLiveData.postValue("Failed to save patient answers.")
                }
            } catch (e: SocketTimeoutException) {
                // Handle timeout exception
                failureLiveData.postValue("Timeout Exception: ${e.message}")
                errorLiveData.postValue("Timeout Exception: ${e.message}")
                Log.e("SaveScreeningAnswersViewModel", "Timeout Exception: ${e.message}")
            } catch (e: Exception) {
                e.printStackTrace()
                failureLiveData.postValue("Failed to save patient answers.")
            }
        }
    }

    // Function to retry saving patient answers
    fun retrySavePatientAnswers() {
        if (requestBody != null) {
            savePatientAnswers(requestBody)
        }
    }
}

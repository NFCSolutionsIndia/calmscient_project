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
import com.calmscient.di.remote.request.PatientAnswersWrapper
import com.calmscient.di.remote.response.PatientAnswerSaveResponse
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
    val successNotAnsweredDataMessage: MutableLiveData<String> = MutableLiveData()
    val errorLiveData: MutableLiveData<String> = MutableLiveData()
    val failureLiveData: MutableLiveData<String> = MutableLiveData()

    private var lastRequestBody: PatientAnswersWrapper? = null
    private var lastAccessToken: String = ""

    fun savePatientAnswers(requestBody: PatientAnswersWrapper, accessToken: String) {
        loadingLiveData.value = true
        lastRequestBody = requestBody
        lastAccessToken = accessToken
        viewModelScope.launch {
            try {
                val response = repository.saveScreeningQuestionAnswers(requestBody, accessToken)
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

    private suspend fun handleResponse(call: Call<PatientAnswerSaveResponse>) {
        withContext(Dispatchers.IO) {
            try {
                val response = call.execute()
                if (response.isSuccessful) {
                    val responseCode = response.body()?.statusResponse?.responseCode
                    val responseMessage = response.body()?.statusResponse?.responseMessage ?: "Unknown error"
                    when (responseCode) {
                        200 -> {
                            saveResponseLiveData.postValue(response.body())
                            successLiveData.postValue(true)
                        }
                        300 -> {
                            saveResponseLiveData.postValue(response.body())
                            successNotAnsweredData.postValue(true)
                            successNotAnsweredDataMessage.postValue(responseMessage)
                        }
                        else -> {
                            errorLiveData.postValue("Error: ${response.body()?.statusResponse?.responseMessage}")
                        }
                    }
                } else {
                    errorLiveData.postValue("Failed to save patient answers.")
                }
            } catch (e: SocketTimeoutException) {
                failureLiveData.postValue("Timeout Exception: ${e.message}")
                errorLiveData.postValue("Timeout Exception: ${e.message}")
            } catch (e: Exception) {
                failureLiveData.postValue("Failed to save patient answers.")
            }
        }
    }

    fun retrySavePatientAnswers() {
        lastRequestBody?.let { savePatientAnswers(it, lastAccessToken) }
    }
}


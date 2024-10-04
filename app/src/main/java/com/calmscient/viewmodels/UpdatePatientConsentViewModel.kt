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
import com.calmscient.di.remote.request.UpdatePatientConsentRequest
import com.calmscient.di.remote.request.UpdateUserLanguageRequest
import com.calmscient.di.remote.response.UpdatePatientConsentResponse
import com.calmscient.di.remote.response.UpdateUserLanguageResponse
import com.calmscient.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class UpdatePatientConsentViewModel @Inject constructor(private val repository: UserProfileRepository) : ViewModel() {

    val saveResponseLiveData: MutableLiveData<UpdatePatientConsentResponse?> = MutableLiveData()
    val loadingLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val successLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val successNotAnsweredData: MutableLiveData<Boolean> = MutableLiveData()
    val errorLiveData: MutableLiveData<String?> = MutableLiveData()
    val failureLiveData: MutableLiveData<String?> = MutableLiveData()
    val failureResponseData: MutableLiveData<UpdatePatientConsentResponse?> = MutableLiveData()



    private var lastClientId: Int = -1
    private var lastPatientId: Int = -1
    private var lastConsentListId: Int = -1
    private var lastFlag: Int = -1
    private var lastPlId: Int = -1
    private var lastAccessToken: String = ""

    // Function to save patient answers
    fun updatePatientConsent(clientId:Int, consentListId: Int,flag: Int, patientId:Int, plId: Int, accessToken:String) {
        loadingLiveData.value = true // Show loader

        lastClientId = clientId
        lastPatientId = patientId
        lastAccessToken = accessToken
        lastConsentListId = consentListId
        lastFlag = flag
        lastPlId = plId

        viewModelScope.launch {
            try {
                val request = UpdatePatientConsentRequest(clientId,consentListId,flag,patientId,plId)
                val response = repository.updatePatientConsent(accessToken,request)
                Log.d("UpdatePatientConsentViewModel", "Response: $response")
                handleResponse(response)
            } catch (e: SocketTimeoutException) {
                // Handle timeout exception
                failureLiveData.postValue("Timeout Exception: ${e.message}")
                Log.e("UpdatePatientConsentViewModel", "Timeout Exception: ${e.message}")
            } catch (e: Exception) {
                e.printStackTrace()
                failureLiveData.value = "Failed to Update Data."
            } finally {
                loadingLiveData.value = false
            }
        }
    }

    // Function to handle API response
    private suspend fun handleResponse(call: Call<UpdatePatientConsentResponse>) {
        withContext(Dispatchers.IO) {
            try {
                val response = call.execute()
                if (response.isSuccessful) {
                    val isSuccess = response.body()?.statusResponse?.responseCode == 200
                    val isNotAnswered = response.body()?.statusResponse?.responseCode == 300
                    if (isSuccess) {
                        saveResponseLiveData.postValue(response.body())
                        successLiveData.postValue(isSuccess)
                        Log.d("UpdatePatientConsentViewModel ", "Success: ${response.body()}")
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
                Log.e("UpdatePatientConsentViewModel", "Timeout Exception: ${e.message}")
            } catch (e: Exception) {
                e.printStackTrace()
                failureLiveData.postValue("Failed to Update data.")
            }
        }
    }


    // Function to retry get
    fun retryUpdatePatientConsent() {
        if( lastPatientId>0 && lastClientId>0  && lastAccessToken.isNotEmpty())
        {
            updatePatientConsent(lastClientId,lastConsentListId,lastFlag,lastPatientId,lastPlId,lastAccessToken)
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

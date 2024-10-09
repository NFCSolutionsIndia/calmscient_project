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
import com.calmscient.di.remote.request.UpdateUserLanguageRequest
import com.calmscient.di.remote.response.Response
import com.calmscient.di.remote.response.UpdateUserLanguageResponse
import com.calmscient.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import java.io.File
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class UploadProfileImageViewModel @Inject constructor(private val repository: UserProfileRepository) : ViewModel() {

    val saveResponseLiveData: MutableLiveData<Response?> = MutableLiveData()
    val loadingLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val successLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val successNotAnsweredData: MutableLiveData<Boolean> = MutableLiveData()
    val errorLiveData: MutableLiveData<String?> = MutableLiveData()
    val failureLiveData: MutableLiveData<String?> = MutableLiveData()
    val failureResponseData: MutableLiveData<Response?> = MutableLiveData()


/*
    private var lastClientId: Int = -1
    private var lastPatientId: Int = -1
    private var lastLanguageId: Int = -1
    private var lastFlag: Int = -1
    private var lastAccessToken: String = ""*/

    // Function to save patient answers
    fun uploadProfileImage(accessToken: String, patientId: String, clientId: String,  profileImage: MultipartBody.Part) {
        loadingLiveData.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Prepare data for the request
                val patientIdBody = patientId.toRequestBody("text/plain".toMediaTypeOrNull())
                val clientIdBody = clientId.toRequestBody("text/plain".toMediaTypeOrNull())

                // Call repository to upload image
                val response = repository.uploadProfileImage(
                    accessToken = accessToken,
                    patientId = patientIdBody,
                    clientId = clientIdBody,
                    file = profileImage
                )

                handleResponse(response)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    failureLiveData.postValue("Failed to upload image: ${e.message}")
                }
            } finally {
                loadingLiveData.postValue(false)
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
                        Log.d("UploadProfileImageViewModel ", "Success: ${response.body()}")
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
                Log.e("UploadProfileImageViewModel", "Timeout Exception: ${e.message}")
            } catch (e: Exception) {
                e.printStackTrace()
                failureLiveData.postValue("Failed to get data.")
            }
        }
    }


    // Function to retry get
   /* fun retryUploadProfileImage() {
        if( lastPatientId>0 && lastClientId>0  && lastAccessToken.isNotEmpty())
        {
            uploadProfileImage(lastClientId,lastPatientId,lastLanguageId,lastFlag,lastAccessToken)
        }
    }*/

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

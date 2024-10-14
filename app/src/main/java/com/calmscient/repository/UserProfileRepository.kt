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

package com.calmscient.repository

import com.calmscient.ApiService
import com.calmscient.di.remote.request.GetPatientPrivacyRequest
import com.calmscient.di.remote.request.GetUserProfileRequest
import com.calmscient.di.remote.request.SavePatientExercisesFavoritesRequest
import com.calmscient.di.remote.request.UpdatePatientConsentRequest
import com.calmscient.di.remote.request.UpdatePatientThemeRequest
import com.calmscient.di.remote.request.UpdateUserLanguageRequest
import com.calmscient.di.remote.response.GetPatientPrivacyResponse
import com.calmscient.di.remote.response.GetUserLanguagesResponse
import com.calmscient.di.remote.response.GetUserProfileResponse
import com.calmscient.di.remote.response.Response
import com.calmscient.di.remote.response.UpdatePatientConsentResponse
import com.calmscient.di.remote.response.UpdateUserLanguageResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import javax.inject.Inject

class UserProfileRepository @Inject constructor(private val apiService: ApiService)
{
    fun getUserProfile(getUserProfileRequest: GetUserProfileRequest, accessToken:String): Call<GetUserProfileResponse> {
       return apiService.getUserProfile("Bearer $accessToken",getUserProfileRequest)
    }

    fun getUserLanguages(getUserProfileRequest: GetUserProfileRequest, accessToken:String): Call<GetUserLanguagesResponse> {
        return apiService.getUserLanguages("Bearer $accessToken",getUserProfileRequest)
    }

    fun updateUserLanguage(updateUserLanguageRequest: UpdateUserLanguageRequest, accessToken:String): Call<UpdateUserLanguageResponse> {
        return apiService.updateUserLanguage("Bearer $accessToken",updateUserLanguageRequest)
    }

    fun getPatientPrivacyDetails(getPatientPrivacyRequest: GetPatientPrivacyRequest,accessToken: String): Call<GetPatientPrivacyResponse>
    {
        return apiService.getPatientPrivacyDetails("Bearer $accessToken",getPatientPrivacyRequest)
    }

    fun logoutUser(accessToken: String): Call<Response>{
        return apiService.logoutUser("Bearer $accessToken")
    }

    fun updatePatientConsent(accessToken: String, @Body request: UpdatePatientConsentRequest): Call<UpdatePatientConsentResponse>{
        return apiService.updatePatientConsent("Bearer $accessToken",request)
    }

    fun uploadProfileImage(accessToken: String, patientId: RequestBody, clientId: RequestBody, file: MultipartBody.Part): Call<Response> {
        return apiService.uploadProfileImage("Bearer $accessToken", patientId, clientId, file)
    }

    fun updatePatientTheme(accessToken: String, request: UpdatePatientThemeRequest): Call<Response>{
        return apiService.updatePatientTheme("Bearer $accessToken", request)
    }

    // For Exercise
    fun savePatientExercisesFavorites(accessToken: String, request: SavePatientExercisesFavoritesRequest): Call<Response>{
        return  apiService.savePatientExercisesFavorites("Bearer $accessToken",request)
    }
}
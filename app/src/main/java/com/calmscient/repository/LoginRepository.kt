package com.calmscient.repository

import com.calmscient.ApiService
import com.calmscient.di.remote.request.LoginRequest
import com.calmscient.di.remote.request.PatientMoodRequest
import com.calmscient.di.remote.request.SavePatientMoodRequest
import com.calmscient.di.remote.request.SavePatientMoodWrapper
import com.calmscient.di.remote.request.SessionIdRequest
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.di.remote.response.PatientMoodResponse
import com.calmscient.di.remote.response.SavePatientMoodResponse
import com.calmscient.di.remote.response.SessionIdResponse
import retrofit2.Call
import javax.inject.Inject

class LoginRepository @Inject constructor(private val apiService: ApiService)
{
    fun loginUser(loginRequest: LoginRequest): Call<LoginResponse> {
        return apiService.loginUser(loginRequest)
    }

    fun getPatientMood(patientMoodRequest: PatientMoodRequest,accessToken: String): Call<PatientMoodResponse> {
        return apiService.getPatientMood("Bearer $accessToken",patientMoodRequest)
    }

    fun savePatientMoodData(savePatientMoodRequest: SavePatientMoodWrapper, accessToken: String): Call<SavePatientMoodResponse> {
        return apiService.savePatientMoodData("Bearer $accessToken",savePatientMoodRequest)
    }

    fun getSessionId(accessToken: String): Call<SessionIdResponse> {
        return apiService.getSessionId("Bearer $accessToken")
    }
}

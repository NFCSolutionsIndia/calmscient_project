package com.calmscient.repository

import com.calmscient.ApiService
import com.calmscient.di.remote.request.LoginRequest
import com.calmscient.di.remote.request.PatientMoodRequest
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.di.remote.response.PatientMoodResponse
import retrofit2.Call
import javax.inject.Inject

class LoginRepository @Inject constructor(private val apiService: ApiService)
{
    fun loginUser(loginRequest: LoginRequest): Call<LoginResponse> {
        return apiService.loginUser(loginRequest)
    }

    fun getPatientMood(patientMoodRequest: PatientMoodRequest): Call<PatientMoodResponse> {
        return apiService.getPatientMood(patientMoodRequest)
    }
}

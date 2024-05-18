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
import com.calmscient.di.remote.request.AddMedicationDetailsRequest
import com.calmscient.di.remote.request.AlarmUpdateRequest
import com.calmscient.di.remote.request.AlarmWrapper
import com.calmscient.di.remote.request.AppointmentDetailsRequestData
import com.calmscient.di.remote.request.LoginRequest
import com.calmscient.di.remote.request.MedicationDetailsRequest
import com.calmscient.di.remote.response.AddMedicationResponse
import com.calmscient.di.remote.response.AlarmUpdateResponse
import com.calmscient.di.remote.response.AppointmentDetailsResponseData
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.di.remote.response.MedicationDetailsResponse
import retrofit2.Call
import javax.inject.Inject

class MedicationsRepository @Inject constructor(private val apiService: ApiService)
{
    fun getMedicationDetails(medicationDetailsRequest: MedicationDetailsRequest,accessToken: String): Call<MedicationDetailsResponse> {
        return apiService.getMedicationDetails("Bearer $accessToken",medicationDetailsRequest)
    }

    fun addMedicationDetails(medicationDetailsRequest: AddMedicationDetailsRequest, accessToken: String): Call<AddMedicationResponse> {
        return apiService.addMedicationDetails("Bearer $accessToken",medicationDetailsRequest)
    }

    fun getAppointmentDetails(appointmentDetailsRequest: AppointmentDetailsRequestData, accessToken: String): Call<AppointmentDetailsResponseData> {
        return apiService.getAppointmentDetails("Bearer $accessToken",appointmentDetailsRequest)
    }

    fun updatePatientMedicationDetails(appointmentDetailsRequest: AlarmWrapper, accessToken: String): Call<AlarmUpdateResponse> {
        return apiService.updatePatientMedicationDetails("Bearer $accessToken",appointmentDetailsRequest)
    }
}


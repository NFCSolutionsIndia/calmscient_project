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
import com.calmscient.di.remote.request.ManageAnxietyIndexRequest
import com.calmscient.di.remote.request.MedicationDetailsRequest
import com.calmscient.di.remote.response.ManageAnxietyIndexResponse
import com.calmscient.di.remote.response.MedicationDetailsResponse
import retrofit2.Call
import javax.inject.Inject

class ManageAnxietyRepository @Inject constructor(private val apiService: ApiService)
{
    fun getManageAnxietyIndexData(manageAnxietyIndexRequest: ManageAnxietyIndexRequest, accessToken: String): Call<ManageAnxietyIndexResponse> {
        return apiService.getManageAnxietyIndexData("Bearer $accessToken",manageAnxietyIndexRequest)
    }
}
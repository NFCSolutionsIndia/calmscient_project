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
import com.calmscient.di.remote.request.LoginRequest
import com.calmscient.di.remote.request.ScreeningHistoryRequest
import com.calmscient.di.remote.request.ScreeningRequest
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.di.remote.response.ScreeningHistoryResponse
import com.calmscient.di.remote.response.ScreeningHistoryResponseData
import com.calmscient.di.remote.response.ScreeningResponse
import retrofit2.Call
import javax.inject.Inject

class ScreeningsRepository  @Inject constructor(private val apiService: ApiService)
{
    fun fetchScreeningsMenuItems(screeningRequest: ScreeningRequest): Call<ScreeningResponse> {
        return apiService.fetchScreeningsMenuItems(screeningRequest)
    }

    fun getScreeningsHistory(screeningRequest: ScreeningHistoryRequest): Call<ScreeningHistoryResponseData> {
        return apiService.getScreeningsHistory(screeningRequest)
    }
}
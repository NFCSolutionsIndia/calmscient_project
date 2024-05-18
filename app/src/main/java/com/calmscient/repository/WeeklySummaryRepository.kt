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
import com.calmscient.di.remote.request.SummaryOfAUDITRequest
import com.calmscient.di.remote.request.SummaryOfDASTRequest
import com.calmscient.di.remote.request.SummaryOfGADRequest
import com.calmscient.di.remote.request.SummaryOfPHQ9Request
import com.calmscient.di.remote.response.SummaryOfAUDITResponse
import com.calmscient.di.remote.response.SummaryOfDASTResponse
import com.calmscient.di.remote.response.SummaryOfGADResponse
import com.calmscient.di.remote.response.SummaryOfPHQ9
import com.calmscient.di.remote.response.SummaryOfPHQ9Response
import retrofit2.Call
import javax.inject.Inject

class WeeklySummaryRepository @Inject constructor(private val apiService: ApiService)
{

    fun getSummaryOfPHQ(summaryOfPHQ9Request: SummaryOfPHQ9Request,accessToken: String): Call<SummaryOfPHQ9Response>
    {
        return  apiService.getSummaryOfPHQ("Bearer $accessToken",summaryOfPHQ9Request)
    }

    fun getSummaryOfGAD(summaryOfPHQ9Request: SummaryOfGADRequest,accessToken: String): Call<SummaryOfGADResponse>
    {
        return  apiService.getSummaryOfGAD("Bearer $accessToken",summaryOfPHQ9Request)
    }

    fun getSummaryOfAUDIT(summaryOfAUDITRequest: SummaryOfAUDITRequest,accessToken: String): Call<SummaryOfAUDITResponse>
    {
        return  apiService.getSummaryOfAUDIT("Bearer $accessToken",summaryOfAUDITRequest)
    }

    fun getSummaryOfDAST(summaryOfDASTRequest: SummaryOfDASTRequest,accessToken: String): Call<SummaryOfDASTResponse>
    {
        return  apiService.getSummaryOfDAST("Bearer $accessToken",summaryOfDASTRequest)
    }

}
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
import com.calmscient.di.remote.request.AddPatientJournalEntryRequest
import com.calmscient.di.remote.request.GetPatientJournalByPatientIdRequest
import com.calmscient.di.remote.request.SummaryOfAUDITRequest
import com.calmscient.di.remote.request.SummaryOfCourseWorkRequest
import com.calmscient.di.remote.request.SummaryOfDASTRequest
import com.calmscient.di.remote.request.SummaryOfGADRequest
import com.calmscient.di.remote.request.SummaryOfMoodRequest
import com.calmscient.di.remote.request.SummaryOfPHQ9Request
import com.calmscient.di.remote.request.SummaryOfSleepRequest
import com.calmscient.di.remote.response.AddPatientJournalEntryResponse
import com.calmscient.di.remote.response.GetPatientJournalByPatientIdResponse
import com.calmscient.di.remote.response.SummaryOfAUDITResponse
import com.calmscient.di.remote.response.SummaryOfCourseWorkResponse
import com.calmscient.di.remote.response.SummaryOfDASTResponse
import com.calmscient.di.remote.response.SummaryOfGADResponse
import com.calmscient.di.remote.response.SummaryOfMoodResponse
import com.calmscient.di.remote.response.SummaryOfPHQ9
import com.calmscient.di.remote.response.SummaryOfPHQ9Response
import com.calmscient.di.remote.response.SummaryOfSleepResponse
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

    fun getSummaryOfSleep(summaryOfSleepRequest: SummaryOfSleepRequest,accessToken: String): Call<SummaryOfSleepResponse>
    {
        return  apiService.getSummaryOfSleep("Bearer $accessToken",summaryOfSleepRequest)
    }
    fun getSummaryOfMood(summaryOfMoodRequest: SummaryOfMoodRequest,accessToken: String): Call<SummaryOfMoodResponse>
    {
        return  apiService.getSummaryOfMood("Bearer $accessToken",summaryOfMoodRequest)
    }

    fun getSummaryOfCourseWork(summaryOfCourseWorkRequest: SummaryOfCourseWorkRequest,accessToken: String): Call<SummaryOfCourseWorkResponse>
    {
        return  apiService.getSummaryOfCourseWork("Bearer $accessToken",summaryOfCourseWorkRequest)
    }

    fun getPatientJournalByPatientId(getPatientJournalByPatientIdRequest: GetPatientJournalByPatientIdRequest, accessToken: String): Call<GetPatientJournalByPatientIdResponse>
    {
        return  apiService.getPatientJournalByPatientId("Bearer $accessToken",getPatientJournalByPatientIdRequest)
    }

    fun addPatientJournalEntry(addPatientJournalEntryRequest: AddPatientJournalEntryRequest,accessToken: String): Call<AddPatientJournalEntryResponse>{
        return apiService.addPatientJournalEntry("Bearer $accessToken",addPatientJournalEntryRequest)
    }

}
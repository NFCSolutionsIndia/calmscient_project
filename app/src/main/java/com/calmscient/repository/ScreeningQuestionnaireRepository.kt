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
import com.calmscient.di.remote.request.PatientAnswerSaveRequest
import com.calmscient.di.remote.request.PatientAnswersWrapper
import com.calmscient.di.remote.request.ScreeningRequest
import com.calmscient.di.remote.request.ScreeningsAssessmentRequest
import com.calmscient.di.remote.request.ScreeningsResultsRequest
import com.calmscient.di.remote.response.PatientAnswerSaveResponse
import com.calmscient.di.remote.response.ScreeningAssignmentResponse
import com.calmscient.di.remote.response.ScreeningResponse
import com.calmscient.di.remote.response.ScreeningResultsResponse
import retrofit2.Call
import javax.inject.Inject

class ScreeningQuestionnaireRepository @Inject constructor(private val apiService: ApiService)
{
    fun fetchScreeningsMenuItems(screeningAssignmentRequest: ScreeningsAssessmentRequest,accessToken: String): Call<ScreeningAssignmentResponse> {
        return apiService.fetchScreeningsQuestionItems("Bearer $accessToken",screeningAssignmentRequest)
    }

    fun saveScreeningQuestionAnswers(patientAnswerSaveRequest: PatientAnswersWrapper,accessToken: String): Call<PatientAnswerSaveResponse>
    {
        return  apiService.saveScreeningQuestionAnswers("Bearer $accessToken",patientAnswerSaveRequest)
    }

    fun getScreeningsResults(screeningResultsRequest: ScreeningsResultsRequest,accessToken: String):Call<ScreeningResultsResponse>{
        return  apiService.getScreeningsResults("Bearer $accessToken",screeningResultsRequest)
    }
}
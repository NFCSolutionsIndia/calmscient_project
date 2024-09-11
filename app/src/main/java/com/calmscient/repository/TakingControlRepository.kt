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
import com.calmscient.di.remote.request.CreateDrinkTrackerRequest
import com.calmscient.di.remote.request.CreateEventTrackerRequest
import com.calmscient.di.remote.request.DrinkTrackerRequest
import com.calmscient.di.remote.request.GetAlcoholFreeDayRequest
import com.calmscient.di.remote.request.GetBasicKnowledgeIndexRequest
import com.calmscient.di.remote.request.GetEventsListRequest
import com.calmscient.di.remote.request.GetTakingControlIndexRequest
import com.calmscient.di.remote.request.GetTakingControlIntroductionRequest
import com.calmscient.di.remote.request.GetTakingControlSummaryRequest
import com.calmscient.di.remote.request.ManageAnxietyIndexRequest
import com.calmscient.di.remote.request.MyDrinkingHabitRequest
import com.calmscient.di.remote.request.SaveAlcoholFreeDayRequest
import com.calmscient.di.remote.request.SaveCourseJournalEntryMakeAPlanRequest
import com.calmscient.di.remote.request.SaveGoalSetupRequest
import com.calmscient.di.remote.request.SaveMyDrinkingHabitAnswerRequest
import com.calmscient.di.remote.request.SavePatientMoodRequest
import com.calmscient.di.remote.request.SaveTakingControlIntroductionRequest
import com.calmscient.di.remote.request.SaveTakingControlIntroductionWrapper
import com.calmscient.di.remote.request.SendNotificationToDoctorMakeAPlanRequest
import com.calmscient.di.remote.request.UpdateBasicKnowledgeIndexRequest
import com.calmscient.di.remote.request.UpdateTakingControlIndexRequest
import com.calmscient.di.remote.response.CreateDrinkTrackerResponse
import com.calmscient.di.remote.response.CreateEventTrackerResponse
import com.calmscient.di.remote.response.DrinkTrackerResponse
import com.calmscient.di.remote.response.GetAlcoholFreeDayResponse
import com.calmscient.di.remote.response.GetBasicKnowledgeIndexResponse
import com.calmscient.di.remote.response.GetEventsListResponse
import com.calmscient.di.remote.response.GetTakingControlIndexResponse
import com.calmscient.di.remote.response.GetTakingControlIntroductionResponse
import com.calmscient.di.remote.response.GetTakingControlSummaryResponse
import com.calmscient.di.remote.response.ManageAnxietyIndexResponse
import com.calmscient.di.remote.response.MyDrinkingHabitResponse
import com.calmscient.di.remote.response.SaveAlcoholFreeDayResponse
import com.calmscient.di.remote.response.SaveCourseJournalEntryMakeAPlanResponse
import com.calmscient.di.remote.response.SaveGoalSetupResponse
import com.calmscient.di.remote.response.SaveMyDrinkingHabitAnswerResponse
import com.calmscient.di.remote.response.SavePatientMoodResponse
import com.calmscient.di.remote.response.SaveTakingControlIntroductionResponse
import com.calmscient.di.remote.response.SendNotificationToDoctorMakeAPlanResponse
import com.calmscient.di.remote.response.TakingControlIntroduction
import com.calmscient.di.remote.response.UpdateBasicKnowledgeIndexResponse
import com.calmscient.di.remote.response.UpdateTakingControlIndexResponse
import retrofit2.Call
import javax.inject.Inject

class TakingControlRepository @Inject constructor(private val apiService: ApiService)
{
    fun getDrinkTackerData(drinkTrackerRequest: DrinkTrackerRequest, accessToken: String): Call<DrinkTrackerResponse> {
        return apiService.getDrinkTackerData("Bearer $accessToken",drinkTrackerRequest)
    }

    fun createDrinkTrackerList(createDrinkTrackerRequest: CreateDrinkTrackerRequest, accessToken: String) : Call<CreateDrinkTrackerResponse>{
        return apiService.createDrinkTrackerList("Bearer $accessToken",createDrinkTrackerRequest)
    }

    fun createEventTrackingList(createEventTrackerRequest: CreateEventTrackerRequest, accessToken: String) : Call<CreateEventTrackerResponse>{
        return apiService.createEventTracking("Bearer $accessToken",createEventTrackerRequest)
    }

    fun getEventTackerData(getEventsListRequest: GetEventsListRequest, accessToken: String): Call<GetEventsListResponse> {
        return apiService.getEventTackerData("Bearer $accessToken",getEventsListRequest)
    }
    fun getTakingControlIndex(getTakingControlIndexRequest: GetTakingControlIndexRequest, accessToken: String) : Call<GetTakingControlIndexResponse>{
        return apiService.getTakingControlIndex("Bearer $accessToken",getTakingControlIndexRequest)
    }

    fun getTakingControlIntroductionData(getTakingControlIntroductionRequest: GetTakingControlIntroductionRequest,accessToken: String) : Call<GetTakingControlIntroductionResponse>{
        return  apiService.getTakingControlIntroductionData("Bearer $accessToken",getTakingControlIntroductionRequest)
    }
    fun saveTakingControlIntroductionData(saveTakingControlIntroductionRequest: SaveTakingControlIntroductionRequest, accessToken: String) : Call<SaveTakingControlIntroductionResponse>{
        return  apiService.saveTakingControlIntroductionData("Bearer $accessToken",saveTakingControlIntroductionRequest)
    }

    fun updateTakingControlIndexData(updateTakingControlIndexRequest: UpdateTakingControlIndexRequest, accessToken: String): Call<UpdateTakingControlIndexResponse>
    {
        return apiService.updateTakingControlIndexData("Bearer $accessToken",updateTakingControlIndexRequest)
    }

    fun getPatientBasicKnowledgeCourse(myDrinkingHabitRequest: MyDrinkingHabitRequest,accessToken: String) : Call<MyDrinkingHabitResponse>
    {
        return apiService.getPatientBasicKnowledgeCourse("Bearer $accessToken",myDrinkingHabitRequest)
    }

    fun getBasicKnowledgeIndexData(getBasicKnowledgeIndexRequest: GetBasicKnowledgeIndexRequest,accessToken: String): Call<GetBasicKnowledgeIndexResponse>
    {
        return apiService.getBasicKnowledgeIndexData("Bearer $accessToken",getBasicKnowledgeIndexRequest)
    }

    fun updateBasicKnowledgeIndexData(updateBasicKnowledgeIndexRequest: UpdateBasicKnowledgeIndexRequest,accessToken: String): Call<UpdateBasicKnowledgeIndexResponse>
    {
        return apiService.updateBasicKnowledgeIndexData("Bearer $accessToken",updateBasicKnowledgeIndexRequest)
    }


    fun sendNotificationToDoctorMakeAPlan(sendNotificationToDoctorMakeAPlanRequest: SendNotificationToDoctorMakeAPlanRequest, accessToken: String):Call<SendNotificationToDoctorMakeAPlanResponse>
    {
        return apiService.sendNotificationToDoctorMakeAPlan("Bearer $accessToken",sendNotificationToDoctorMakeAPlanRequest)
    }

    fun savePatientAlcoholFreeDays(saveAlcoholFreeDayRequest: SaveAlcoholFreeDayRequest,accessToken: String) :Call<SaveAlcoholFreeDayResponse>
    {
        return apiService.saveAlcoholFreeDay("Bearer $accessToken",saveAlcoholFreeDayRequest)
    }

    fun getAlcoholFreeDay(getAlcoholFreeDayRequest: GetAlcoholFreeDayRequest,accessToken: String): Call<GetAlcoholFreeDayResponse>
    {
        return apiService.getAlcoholFreeDay("Bearer $accessToken",getAlcoholFreeDayRequest)
    }

    fun getTakingControlSummaryData(getTakingControlSummaryRequest: GetTakingControlSummaryRequest,accessToken: String): Call<GetTakingControlSummaryResponse>
    {
        return apiService.getTakingControlSummaryData("Bearer $accessToken", getTakingControlSummaryRequest)
    }

    fun saveCourseJournalEntryMakeAPlan(saveCourseJournalEntryMakeAPlanRequest: SaveCourseJournalEntryMakeAPlanRequest, accessToken: String): Call<SaveCourseJournalEntryMakeAPlanResponse>
    {
        return apiService.saveCourseJournalEntry("Bearer $accessToken",saveCourseJournalEntryMakeAPlanRequest)
    }

    fun saveMyDrinkHabitAnswer(saveMyDrinkingHabitAnswerRequest: SaveMyDrinkingHabitAnswerRequest, accessToken: String): Call<SaveMyDrinkingHabitAnswerResponse>
    {
        return apiService.saveMyDrinkHabitAnswer("Bearer $accessToken",saveMyDrinkingHabitAnswerRequest)
    }

    fun saveDrinkCountGoalSetup(saveGoalSetupRequest: SaveGoalSetupRequest, accessToken: String): Call<SaveGoalSetupResponse>{
        return apiService.saveGoalSetupMakeAPlan("Bearer $accessToken",saveGoalSetupRequest)
    }

}
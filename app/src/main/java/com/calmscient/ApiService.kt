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

package com.calmscient

import com.calmscient.di.remote.request.AddMedicationDetailsRequest
import com.calmscient.di.remote.request.AlarmUpdateRequest
import com.calmscient.di.remote.request.AlarmWrapper
import com.calmscient.di.remote.request.AppointmentDetailsRequestData
import com.calmscient.di.remote.request.CreateDrinkTrackerRequest
import com.calmscient.di.remote.request.DrinkTrackerRequest
import com.calmscient.di.remote.request.GetEventsListRequest
import com.calmscient.di.remote.request.GetTakingControlIndexRequest
import com.calmscient.di.remote.request.GetTakingControlIntroductionRequest
import com.calmscient.di.remote.request.MenuItemRequest
import com.calmscient.di.remote.request.LoginRequest
import com.calmscient.di.remote.request.ManageAnxietyIndexRequest
import com.calmscient.di.remote.request.MedicationDetailsRequest
import com.calmscient.di.remote.request.PatientAnswerSaveRequest
import com.calmscient.di.remote.request.PatientAnswersWrapper
import com.calmscient.di.remote.request.PatientMoodRequest
import com.calmscient.di.remote.request.SavePatientMoodRequest
import com.calmscient.di.remote.request.SavePatientMoodWrapper
import com.calmscient.di.remote.request.SaveTakingControlIntroductionRequest
import com.calmscient.di.remote.request.SaveTakingControlIntroductionWrapper
import com.calmscient.di.remote.request.ScreeningHistoryRequest
import com.calmscient.di.remote.request.ScreeningRequest
import com.calmscient.di.remote.request.ScreeningsAssessmentRequest
import com.calmscient.di.remote.request.ScreeningsResultsRequest
import com.calmscient.di.remote.request.SessionIdRequest
import com.calmscient.di.remote.request.SummaryOfAUDITRequest
import com.calmscient.di.remote.request.SummaryOfCourseWorkRequest
import com.calmscient.di.remote.request.SummaryOfDASTRequest
import com.calmscient.di.remote.request.SummaryOfGADRequest
import com.calmscient.di.remote.request.SummaryOfMoodRequest
import com.calmscient.di.remote.request.SummaryOfPHQ9Request
import com.calmscient.di.remote.request.SummaryOfSleepRequest
import com.calmscient.di.remote.request.UpdateTakingControlIndexRequest
import com.calmscient.di.remote.response.AddMedicationResponse
import com.calmscient.di.remote.response.AlarmUpdateResponse
import com.calmscient.di.remote.response.AppointmentDetailsResponseData
import com.calmscient.di.remote.response.CreateDrinkTrackerResponse
import com.calmscient.di.remote.response.DrinkTrackerResponse
import com.calmscient.di.remote.response.GetEventsListResponse
import com.calmscient.di.remote.response.GetTakingControlIndexResponse
import com.calmscient.di.remote.response.GetTakingControlIntroductionResponse
import com.calmscient.di.remote.response.MenuItemsResponse
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.di.remote.response.ManageAnxietyIndexResponse
import com.calmscient.di.remote.response.MedicationDetailsResponse
import com.calmscient.di.remote.response.PatientAnswerSaveResponse
import com.calmscient.di.remote.response.PatientMoodResponse
import com.calmscient.di.remote.response.SavePatientMoodResponse
import com.calmscient.di.remote.response.SaveTakingControlIntroductionResponse
import com.calmscient.di.remote.response.ScreeningAssignmentResponse
import com.calmscient.di.remote.response.ScreeningHistoryResponse
import com.calmscient.di.remote.response.ScreeningHistoryResponseData
import com.calmscient.di.remote.response.ScreeningResponse
import com.calmscient.di.remote.response.ScreeningResultsResponse
import com.calmscient.di.remote.response.SessionIdResponse
import com.calmscient.di.remote.response.SummaryOfAUDITResponse
import com.calmscient.di.remote.response.SummaryOfCourseWorkResponse
import com.calmscient.di.remote.response.SummaryOfDASTResponse
import com.calmscient.di.remote.response.SummaryOfGADResponse
import com.calmscient.di.remote.response.SummaryOfMoodResponse
import com.calmscient.di.remote.response.SummaryOfPHQ9Response
import com.calmscient.di.remote.response.SummaryOfSleepResponse
import com.calmscient.di.remote.response.UpdateTakingControlIndexResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url


interface ApiService {

    //Login
    @POST("identity/api/v1/settings/userLogin")
    fun loginUser(@Body requestBody: LoginRequest): Call<LoginResponse>

    //sessionId
    @GET("identity/api/v1/user/getSession")
    fun getSessionId(@Header("Authorization") accessToken: String) : Call<SessionIdResponse>

    //Moods Screen
    @POST("patients/api/v1/patientDetails/getPatientStartupScreen")
    fun getPatientMood(@Header("Authorization") accessToken: String,@Body requestBody: PatientMoodRequest): Call<PatientMoodResponse>


    @POST("patients/api/v1/patientDetails/savePatientStartupScreen")
    fun savePatientMoodData(@Header("Authorization") accessToken: String,@Body requestBody: SavePatientMoodWrapper): Call<SavePatientMoodResponse>




//    @POST("identity/menu/fetchMenus")
//    fun fetchMenuItems(@Body requestBody: MenuItemRequest): Call<MenuItemsResponse>

    //Menu
    @POST("identity/api/v1/menu/fetchMenus")
    fun fetchMenuItems(@Header("Authorization") accessToken: String, @Body requestBody: MenuItemRequest): Call<MenuItemsResponse>


    //ScreeningsList
    @POST("patients/api/v1/screening/getScreeningListForMobile")
    fun fetchScreeningsMenuItems(@Header("Authorization") accessToken: String,@Body requestBody: ScreeningRequest): Call<ScreeningResponse>

    @POST("patients/api/v1/screening/getScreeningQuestionnaireForMobile")
    fun fetchScreeningsQuestionItems(@Header("Authorization") accessToken: String,@Body requestBody: ScreeningsAssessmentRequest): Call<ScreeningAssignmentResponse>

    @POST("patients/api/v1/screening/savePatientAnswersForMobile")
    fun saveScreeningQuestionAnswers(@Header("Authorization") accessToken: String,@Body requestBody: PatientAnswersWrapper): Call<PatientAnswerSaveResponse>

    @POST("patients/api/v1/screening/getScreeningHistoryForMobile")
    fun getScreeningsHistory(@Header("Authorization") accessToken: String,@Body requestBody: ScreeningHistoryRequest): Call<ScreeningHistoryResponseData>


    @POST("patients/api/v1/screening/getScreeningResultsForMobile")
    fun getScreeningsResults(@Header("Authorization") accessToken: String,@Body requestBody: ScreeningsResultsRequest): Call<ScreeningResultsResponse>

    //Medications
    @POST("patients/api/v1/medications/getMedications")
    fun getMedicationDetails(@Header("Authorization") accessToken: String,@Body requestBody: MedicationDetailsRequest): Call<MedicationDetailsResponse>

    @POST("patients/api/v1/medications/addMedications")
    fun addMedicationDetails(@Header("Authorization") accessToken: String,@Body requestBody: AddMedicationDetailsRequest): Call<AddMedicationResponse>


    @POST("patients/api/v1/medications/addPatientMedicationAlarms")
    fun updatePatientMedicationDetails(@Header("Authorization") accessToken: String,@Body requestBody: AlarmWrapper): Call<AlarmUpdateResponse>


    //Medical Appointments
    @POST("patients/api/v1/patientDetails/getMedicalAppointmentsByPatientId")
    fun getAppointmentDetails(@Header("Authorization") accessToken: String,@Body requestBody: AppointmentDetailsRequestData): Call<AppointmentDetailsResponseData>


    //WeeklySummary
    @POST("patients/api/v1/patientDetails/getPHQDashboardByDateRange")
    fun getSummaryOfPHQ(@Header("Authorization") accessToken: String,@Body requestBody: SummaryOfPHQ9Request): Call<SummaryOfPHQ9Response>

    @POST("patients/api/v1/patientDetails/getGADDashboardByDateRange")
    fun getSummaryOfGAD(@Header("Authorization") accessToken: String,@Body requestBody: SummaryOfGADRequest): Call<SummaryOfGADResponse>

    @POST("patients/api/v1/patientDetails/getSummaryOfAUDITByDateRange")
    fun getSummaryOfAUDIT(@Header("Authorization") accessToken: String,@Body requestBody: SummaryOfAUDITRequest): Call<SummaryOfAUDITResponse>

    @POST("patients/api/v1/patientDetails/getSummaryOfDASTByDateRange")
    fun getSummaryOfDAST(@Header("Authorization") accessToken: String,@Body requestBody: SummaryOfDASTRequest): Call<SummaryOfDASTResponse>

    @POST("patients/api/v1/patientDetails/getPatientMoodByPatientId")
    fun getSummaryOfMood(@Header("Authorization") accessToken: String,@Body requestBody: SummaryOfMoodRequest): Call<SummaryOfMoodResponse>

    @POST("patients/api/v1/patientDetails/getPatientSleepMonitoringById")
    fun getSummaryOfSleep(@Header("Authorization") accessToken: String,@Body requestBody: SummaryOfSleepRequest): Call<SummaryOfSleepResponse>

    @POST("patients/api/v1/course/getPatientCourseWorkPercentageDetails")
    fun getSummaryOfCourseWork(@Header("Authorization") accessToken: String,@Body requestBody: SummaryOfCourseWorkRequest): Call<SummaryOfCourseWorkResponse>

    //ManageAnxiety
    @POST("patients/api/v1/course/getPatientCourseIndex")
    fun getManageAnxietyIndexData(@Header("Authorization") accessToken: String,@Body request: ManageAnxietyIndexRequest) :Call<ManageAnxietyIndexResponse>


    //Taking Control
    @POST("patients/api/v1/alcohol/getDrinksList")
    fun getDrinkTackerData(@Header("Authorization") accessToken: String,@Body request: DrinkTrackerRequest) :Call<DrinkTrackerResponse>

    @POST("patients/api/v1/alcohol/createDrinkTracking")
    fun createDrinkTrackerList(@Header("Authorization") accessToken: String,@Body request: CreateDrinkTrackerRequest) : Call<CreateDrinkTrackerResponse>

    @POST("patients/api/v1/alcohol/getEventsList")
    fun getEventTackerData(@Header("Authorization") accessToken: String,@Body request: GetEventsListRequest) :Call<GetEventsListResponse>


    @POST("patients/api/v1/takingControl/getTakingControlIndex")
    fun getTakingControlIndex(@Header("Authorization")accessToken: String,@Body request: GetTakingControlIndexRequest) : Call<GetTakingControlIndexResponse>

    @POST("patients/api/v1/takingControl/updateTakingControlIndex")
    fun updateTakingControlIndexData(@Header("Authorization")accessToken: String,@Body request: UpdateTakingControlIndexRequest) :Call<UpdateTakingControlIndexResponse>

    @POST("patients/api/v1/takingControl/getTakingControlIntroduction")
    fun getTakingControlIntroductionData(@Header("Authorization")accessToken: String,@Body request: GetTakingControlIntroductionRequest) : Call<GetTakingControlIntroductionResponse>


    @POST("patients/api/v1/takingControl/saveTakingControlIntroduction")
    fun saveTakingControlIntroductionData(@Header("Authorization")accessToken: String,@Body request: SaveTakingControlIntroductionRequest) : Call<SaveTakingControlIntroductionResponse>


}


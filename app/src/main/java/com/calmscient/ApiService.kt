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

import com.calmscient.di.remote.request.MenuItemRequest
import com.calmscient.di.remote.request.LoginRequest
import com.calmscient.di.remote.request.ScreeningRequest
import com.calmscient.di.remote.request.ScreeningsAssessmentRequest
import com.calmscient.di.remote.response.MenuItemsResponse
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.di.remote.response.ScreeningAssignmentResponse
import com.calmscient.di.remote.response.ScreeningResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url


interface ApiService {

    @POST("patients/patientDetails/userLogin")
    fun loginUser(@Body requestBody: LoginRequest): Call<LoginResponse>


//    @POST("identity/menu/fetchMenus")
//    fun fetchMenuItems(@Body requestBody: MenuItemRequest): Call<MenuItemsResponse>

    @POST
    fun fetchMenuItems(@Url url: String, @Body requestBody: MenuItemRequest): Call<MenuItemsResponse>

    @POST("patients/screening/getScreeningListForMobile")
    fun fetchScreeningsMenuItems(@Body requestBody: ScreeningRequest): Call<ScreeningResponse>

    @POST("patients/screening/getScreeningQuestionnaireForMobile")
    fun fetchScreeningsQuestionItems(@Body requestBody: ScreeningsAssessmentRequest): Call<ScreeningAssignmentResponse>
}


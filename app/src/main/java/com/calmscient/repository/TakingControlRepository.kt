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
import com.calmscient.di.remote.request.DrinkTrackerRequest
import com.calmscient.di.remote.request.GetEventsListRequest
import com.calmscient.di.remote.request.GetTakingControlIndexRequest
import com.calmscient.di.remote.request.GetTakingControlIntroductionRequest
import com.calmscient.di.remote.request.ManageAnxietyIndexRequest
import com.calmscient.di.remote.request.SaveTakingControlIntroductionRequest
import com.calmscient.di.remote.request.SaveTakingControlIntroductionWrapper
import com.calmscient.di.remote.request.UpdateTakingControlIndexRequest
import com.calmscient.di.remote.response.CreateDrinkTrackerResponse
import com.calmscient.di.remote.response.DrinkTrackerResponse
import com.calmscient.di.remote.response.GetEventsListResponse
import com.calmscient.di.remote.response.GetTakingControlIndexResponse
import com.calmscient.di.remote.response.GetTakingControlIntroductionResponse
import com.calmscient.di.remote.response.ManageAnxietyIndexResponse
import com.calmscient.di.remote.response.SaveTakingControlIntroductionResponse
import com.calmscient.di.remote.response.TakingControlIntroduction
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


}
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
import com.calmscient.di.remote.request.GetPatientFavoritesRequest
import com.calmscient.di.remote.request.SavePatientExercisesFavoritesRequest
import com.calmscient.di.remote.response.GetPatientFavoritesResponse
import com.calmscient.di.remote.response.Response
import retrofit2.Call
import javax.inject.Inject

class ExerciseRepository @Inject constructor(private val apiService: ApiService) {

    fun savePatientExercisesFavorites(accessToken: String, request: SavePatientExercisesFavoritesRequest): Call<Response> {
        return  apiService.savePatientExercisesFavorites("Bearer $accessToken",request)
    }

    fun getPatientFavourites(accessToken: String, request: GetPatientFavoritesRequest): Call<GetPatientFavoritesResponse>
    {
        return apiService.getPatientFavourites("Bearer $accessToken", request)
    }
}
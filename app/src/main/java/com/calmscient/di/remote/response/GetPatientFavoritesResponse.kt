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

package com.calmscient.di.remote.response

data class GetPatientFavoritesResponse(
    val favorites: List<Favorite>,
    val statusResponse: StatusResponse
)

data class Favorite(
    val chapterId: Int,
    val darkTheme: Int,
    val favoritesId: Int,
    val isFavorite: Int,
    val isFromExercises: Int,
    val language: Int,
    val lessonId: Int,
    val navigateURL: String,
    val pageNo: Int,
    val patientId: Int,
    val thumbnail: String,
    val title: String,
    val url: String
)

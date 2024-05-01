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

data class ScreeningResultStatusResponse(
    val responseMessage: String,
    val responseCode: Int
)

data class ScreeningResults(
    val patientID: Int,
    val firstName: String,
    val lastName: String,
    val patientAccountNumber: Int,
    val screeningName: String,
    val screeningId: Int,
    val screeningDate: String,
    val score: Int,
    val total: Int,
    val averageResult: String,
    val screeningReminder: String,
    val assessmentId: Int
)

data class ScreeningResultsResponse(
    val statusResponse: ScreeningResultStatusResponse,
    val screeningResults: ScreeningResults
)

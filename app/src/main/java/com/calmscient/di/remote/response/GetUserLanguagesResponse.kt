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

data class GetUserLanguagesResponse(
    val patientLanguages: List<PatientLanguage>,
    val statusResponse: StatusResponse
)

data class PatientLanguage(
    val flagUrl: String,
    val languageId: Int,
    val languageName: String,
    val preferred: Int
)
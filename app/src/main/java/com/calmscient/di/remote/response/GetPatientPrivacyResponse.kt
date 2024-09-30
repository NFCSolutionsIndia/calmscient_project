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

data class GetPatientPrivacyResponse(
    val patientConsent: List<PatientConsent>,
    val statusResponse: StatusResponse
)

data class PatientConsent(
    val clientId: Int,
    val consentFlag: Int,
    val consentId: Int,
    val consentListId: Int,
    val consentListName: String,
    val patientId: Int,
    val plId: Int
)

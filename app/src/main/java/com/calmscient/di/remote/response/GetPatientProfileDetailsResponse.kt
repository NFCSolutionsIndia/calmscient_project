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

data class GetPatientProfileDetailsResponse(
    val patientProfileDetails: PatientProfileDetails,
    val statusResponse: StatusResponse
)

data class PatientProfileDetails(
    val emailAddress: String,
    val firstName: String,
    val lastName: String,
    val phone: String
)

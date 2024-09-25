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

data class LoginResponse(
    val token: Token,
    val statusResponse: StatusResponse,
    val loginDetails: LoginDetails
)

data class Token(
    val access_token: String,
    val expires_in: Int,
    val refresh_expires_in: Int,
    val refresh_token: String,
    val token_type: String,
    val id_token: String?,
    val not_before_policy: Int,
    val session_state: String,
    val scope: String,
    val error: String?,
    val error_description: String?,
    val error_uri: String?
)

data class StatusResponse(
    val responseCode: Int,
    val responseMessage: String
)

data class LoginDetails(
    val patientID: Int,
    val patientLocationID: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val clientID: Int,
    val userID: Int,
    val userTypeID: Int,
    val securityGroupID: Int,
    val userCode: String,
    val lastActivity: String,
    val lastLogin: String?,
    val loginCount: Int,
    val userType: String,
    val locationName: String,
    val address: String,
    val city: Int,
    val providerID: Int,
    val providerCode: String?,
    val providerName: String?
)

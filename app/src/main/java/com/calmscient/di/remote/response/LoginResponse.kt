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
    val statusResponse: StatusResponse,
    val authorizationCode: Int,
    val authorizationMessage: String,
    val loginDetails: LoginDetails
)

data class StatusResponse(
    val responseMessage: String,
    val responseCode: Int?
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
    val lastLogin: String,
    val loginCount: Int,
    val userType: String,
    val locationName: String,
    val address: String,
    val city: Int,
    val providerName:String,
    val providerID:Int,
    val providerCode:String,

)


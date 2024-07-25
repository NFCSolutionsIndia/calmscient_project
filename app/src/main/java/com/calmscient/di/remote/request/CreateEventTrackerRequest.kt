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

package com.calmscient.di.remote.request

data class CreateEventTrackerRequest(
    val alcohol: List<CreateAlcohol>
)

data class CreateAlcohol(
    val activityDate: String,
    val clientId: Int,
    val eventFlag: Int,
    val eventId: Int,
    val flag: String,
    val patientId: Int,
    val plId: Int
)
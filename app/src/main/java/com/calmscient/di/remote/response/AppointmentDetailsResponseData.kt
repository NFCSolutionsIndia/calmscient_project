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

data class AppointmentDetailsResponseDataByDate(
    val hospitalName: String,
    val appointmentDetails: AppointmentDetails
)
data class AppointmentDetails(
    val providerName: String,
    val hospitalName: String,
    val dateAndTime: String,
    val contact: String,
    val address: String,
    val appointmentDetails: String
)

data class Appointment(
    val date: String,
    val appointmentDetailsByDate: List<AppointmentDetailsResponseDataByDate>
)

data class AppointmentDetailsResponseData(
    val statusResponse: AppointmentStatusResponse,
    val appointmentDetailsList: List<Appointment>
)

data class AppointmentStatusResponse(
    val responseMessage: String,
    val responseCode: Int
)
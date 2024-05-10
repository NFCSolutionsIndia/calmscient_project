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

data class MedicationDetailsResponse(
    val response: Response,
    val totalRecords: Int,
    val medicineDetails: List<MedicineDetail>
)

data class Response(
    val responseMessage: String,
    val responseCode: Int
)

data class MedicineDetail(
    val date: String,
    val medicationDetailsByDate: List<MedicationDetailByDate>
)

data class MedicationDetailByDate(
    val medicineName: String,
    val numberOfTablets: Int,
    val medicalDetails: MedicalDetails,
    val dosageTime: List<String>
)

data class MedicalDetails(
    val medicationId :Int,
    val medicineName: String,
    val medicineDosage: String,
    val providerName: String,
    val providerId: Int,
    val directions: String,
    val scheduledTimeList: List<ScheduledTime>
)

data class ScheduledTime(
    val scheduledTimes: List<ScheduledTimeDetails>
)

data class ScheduledTimeDetails(
    val medicineTime: String,
    val alarmTime: String,
    val alarmId :Int,
    val alarmEnabled: String,
    val alarmInterval: String?,
    val repeat: List<String>
)


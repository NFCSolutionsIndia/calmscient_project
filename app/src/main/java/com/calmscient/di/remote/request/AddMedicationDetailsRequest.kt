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

data class Alarm(
    val alarmId: Int,
    val alarmDate: String,
    val repeat: List<String>,
    val alarmInterval: Int,
    val flag: String,
    var isEnabled: Int?,
    val medicineTime: String,
    val plId: Int,
)

data class AlarmInternal(
    val alarmId: Int,
    val alarmDate: String,
    val repeat: List<String>,
    val alarmInterval: Int,
    val flag: String,
    var isEnabled: Int?,
    val medicineTime: String,
    var scheduleType :String?
)

data class AddMedicationDetailsRequest(
    val pvcFlag: String,
    val plId: Int,
    val prescriptionId: Int,
    val medicationName: String,
    val startDate: String,
    val endDate: String,
    val medicineTime :String,
    val providerId: Int,
    val direction: String,
    val dosage: String,
    val withMeal: Int,
    val quantity: Int,
    val isActive: Int,
    val alarms: List<Alarm>
)


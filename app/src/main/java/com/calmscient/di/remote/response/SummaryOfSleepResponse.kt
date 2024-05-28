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

data class SummaryOfSleepResponse(
    val avgTime: Int,
    val sleepMonitorList: List<SleepMonitor>,
    val statusResponse: StatusResponse
)

data class SleepMonitor(
    val createdAt: String,
    val plId: Int,
    val sleepHours: Double,
    val sno: Int
)


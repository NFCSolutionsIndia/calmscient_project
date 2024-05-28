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

data class SummaryOfMoodResponse(
    val averageMood: String,
    val moodMonitorList: List<MoodMonitor>,
    val response: Response
)

data class MoodMonitor(
    val createdAt: String,
    val mood: String,
    val moodScore: Int,
    val moodStatusId: Int,
    val plId: Int,
    val sno: Int
)

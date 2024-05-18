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

data class summaryOfAUDIT(
    val screeningId: Int,
    val plId: Int,
    val score: Int,
    val screening: String,
    val firstName: String,
    val lastName: String,
    val scoreTitle: String,
    val startDate: String,
    val completionDate: String,
    val pscreeningId: Int
)

data class auditByDateRange(
    val date: String,
    val score: Int,
    val scoreTitle: String?
)

data class SummaryOfAUDITResponse(
    val statusResponse: StatusResponse,
    val summaryOfAUDIT: List<summaryOfAUDIT>,
    val auditByDateRange: List<auditByDateRange>
)

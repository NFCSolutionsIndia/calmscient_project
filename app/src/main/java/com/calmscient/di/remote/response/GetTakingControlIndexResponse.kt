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

data class GetTakingControlIndexResponse(
    val courseLists: List<CourseLists>,
    val index: List<Index>,
    val statusResponse: StatusResponse
)

data class CourseLists(
    val clientId: Int,
    val courseId: Int,
    val courseName: String,
    val id: Int,
    val isCompleted: Int,
    val isEnable: Int,
    val patientId: Int,
    val plId: Int,
    val skipTutorialFlag: Int
)

data class Index(
    val goal: Int,
    val goalDeadline: Any,
    val goalDescription: Any,
    val goalType: String,
    val now: Int
)

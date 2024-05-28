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

import java.io.Serializable

data class SummaryOfCourseWorkResponse(
    val patientcourseWorkList: List<PatientcourseWork>,
    val statusResponse: StatusResponse
)
data class PatientcourseWork(
    val completedPer: Double,
    val courseName: String,
    val sectionsList: List<Sections>
): Serializable
data class Sections(
    val completions: String,
    val sectionName: String,
    val subSectionList: List<SubSection>
)
data class SubSection(
    val completion: String,
    val subSectionName: String
)
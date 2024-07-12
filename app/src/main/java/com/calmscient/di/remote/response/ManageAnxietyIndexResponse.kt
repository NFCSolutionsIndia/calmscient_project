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

data class ManageAnxietyIndexResponse(
    val managingAnxiety: List<ManagingAnxiety>,
    val statusResponse: StatusResponse,
    val patientSessionDetails: PatientSessionDetails
)

data class ManagingAnxiety(
    val chapters: List<Chapter>,
    val lessonId: Int,
    val lessonName: String
)

data class Chapter(
    val chapterId: Int,
    val chapterName: String,
    val chapterOnlyReading: Boolean,
    val chapterUrl: String,
    val imageUrl: String,
    val isCourseCompleted: Int,
    val pageCount: Int
)


data class PatientSessionDetails(
    val userSessionID: String,
    val languageId: Int,
    val languageName: String
)
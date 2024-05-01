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

data class PatientAnswerSaveRequest(
    val flag: String?,
    val answerId: String?,
    val screeningId: Int,
    val patientLocationId: Int,
    val questionnaireId: Int,
    val optionId: Int?,
    val score: Int,
    val clientId: Int,
    val patientId: Int,
    val assessmentId: Int
)

data class PatientAnswersWrapper(
    val patientAnswers: List<PatientAnswerSaveRequest>
)


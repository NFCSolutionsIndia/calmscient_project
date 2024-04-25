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

data class ScreeningAssignmentResponse(
    val statusResponse: ScreeningStatusResponse,
    val questionnaire: List<QuestionnaireItem>
)

data class ScreeningStatusResponse(
    val responseMessage: String,
    val responseCode: Int
)

data class AnswerResponse(
    val optionLabelId: String,
    val optionLabel: String,
    val optionScore: String,
    val answer: String,
    val answerId: Any?,
    var selected: String
)

data class QuestionnaireItem(
    val questionNo: Int,
    val questionId: Int,
    val questionName: String,
    val optionTypeId: String,
    val optionType: String,
    val answerResponse: List<AnswerResponse>,
    var selectedOption: Int? = null
)
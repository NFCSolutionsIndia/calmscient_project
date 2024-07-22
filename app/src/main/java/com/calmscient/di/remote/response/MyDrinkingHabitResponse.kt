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

data class MyDrinkingHabitResponse(
    val answersList: List<Answers>,
    val statusResponse: StatusResponse
)

data class Answers(
    val answerId: Int,
    val answerText: String,
    val description: String,
    val optionScore: Any,
    val options: List<Option>,
    val orderId: Any,
    val patientAnswer: String,
    val question: String,
    val questionnaireId: Int
)
data class Option(
    val optionId: Int,
    val optionType: String,
    val optionTypeId: Int,
    val optionValue: String
)
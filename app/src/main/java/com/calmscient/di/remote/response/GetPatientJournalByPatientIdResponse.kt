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

data class GetPatientJournalByPatientIdResponse(
    val dailyJournal: List<DailyJournal>,
    val discoveryExercises: List<Any>,
    val quiz: List<Quiz>,
    val response: Response
)

data class DailyJournal(
    val createdAt: String,
    val entry: String,
    val entryType: String?,
    val journalId: Int,
    val plId: Int,
    val questionType: String?,
    val sno: Int,
    val title: String?
)

data class Quiz(
    val completionDateTime: String,
    val quizTitle: Any,
    val score: Int,
    val title: String,
    val totalScore: Int
)

data class QuizDataForAdapter(
    val date: String?,
    val time: String?,
    val progressBarValue: Int?,
    val score: Int?,
    val total: Int?,
    var title: String?
)
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

data class PatientMoodResponse(
    val wish: String,
    val moodData: MoodData,
    val sleepData: SleepData? = null,
    val timeSpendData: TimeSpendData? = null,
    val medicineData: MedicineData? = null
)

data class MoodOption(
    val optionType: String,
    val optionTypeID: Int,
    val image: String
)

data class MoodData(
    val moodQuestion: String,
    val options: List<MoodOption>
)

data class SleepData(
    val sleepQuestion: String,
    val option1: String,
    val option2: String,
    val option3: String,
    val option4: String,
    val option5: String,
    val option6: String,
    val option7: String,
    val option8: String,
    val option9: String
)

data class TimeSpendData(
    val timeSpendQuestion: String,
    val option1: String,
    val option2: String,
    val option3: String,
    val option4: String,
    val option5: String
)

data class MedicineData(
    val medicineQuestion: String,
    val option1: String,
    val option2: String
)

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

package com.calmscient.di.remote

import androidx.annotation.DrawableRes

data class CardItemDataClass(val availableContentTypes: List<ItemType>,
                             val audioResourceId: String?, // Nullable Int for audio resource ID
                             val videoResourceId: String?,
                             @DrawableRes val contentIcons: List<Int>,
                             val description: String?,
                             val heading: String?,
                             var summary: String?,
                             var dialogText: String?,
                             val isCompleted: Boolean
)

data class ChapterDataClass(
    val chapterId: Int,
    val chapterName: String?,
    val chapterUrl: String?,
    val isCourseCompleted: Int,
    val pageCount: Int,
    val imageUrl: String,
    val chapterOnlyReading: Boolean
)

enum class ItemType {
    VIDEO, AUDIO, QUIZ, LESSON
}

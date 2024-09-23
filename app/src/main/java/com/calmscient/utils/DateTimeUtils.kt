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

package com.calmscient.utils

import java.text.SimpleDateFormat
import java.util.Locale

object DateTimeUtils {
    fun formatDateTime(dateTime: String):  Pair<String, String> {

        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        val outputDateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

        val outputTimeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        return try {
            val parsedDate = inputFormat.parse(dateTime)

            val formattedDate = outputDateFormat.format(parsedDate!!)
            val formattedTime = outputTimeFormat.format(parsedDate)

            Pair(formattedDate, formattedTime)
        } catch (e: Exception) {
            e.printStackTrace()
            Pair("Invalid Date", "Invalid Time")
        }
    }
}

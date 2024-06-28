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

import android.app.DatePickerDialog
import android.content.Context
import java.time.LocalDate
import java.util.Calendar

object DatePickerUtil {
    fun showDatePickerDialog(
        context: Context,
        initialDate: LocalDate,
        onDateSelected: (LocalDate) -> Unit
    ) {
        val currentDate = Calendar.getInstance()
        val currentYear = currentDate.get(Calendar.YEAR)
        val currentMonth = currentDate.get(Calendar.MONTH)
        val currentDay = currentDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(context, { _, year, month, day ->
            val selectedDate = LocalDate.of(year, month + 1, day)
            onDateSelected(selectedDate)
        }, currentYear, currentMonth, currentDay)

        datePickerDialog.show()
    }
}



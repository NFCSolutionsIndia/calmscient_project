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

package com.calmscient.utils.common

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.calmscient.R
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("StaticFieldLeak")
object DatePickerDialogUtil {

    private lateinit var calendar: Calendar
    private lateinit var calendarView: CalendarView

    fun showDatePickerDialog(context: Context, onDateSelected: (Date) -> Unit) {
        val dialog = Dialog(context)
        val dialogView = layoutInflater(context).inflate(R.layout.dialog_date_picker, null)

        dialog.setContentView(dialogView)

        calendarView = dialogView.findViewById(R.id.calendarView)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnOk = dialogView.findViewById<Button>(R.id.btnOk)

        calendar = Calendar.getInstance()



        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnOk.setOnClickListener {
            onDateSelected(calendar.time)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun layoutInflater(context: Context) =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private fun updateCalendarView() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)


        try {
            val cv = calendarView
            val vg = cv.getChildAt(0) as ViewGroup

            val subView = vg.getChildAt(0)

            if (subView is TextView) {
                subView.visibility = View.GONE
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Set selected date color to purple_100
        val purple100 = ContextCompat.getColor(calendarView.context, R.color.purple_100)
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val dateInMillis = calendar.timeInMillis
            calendarView.setDateTextAppearance(dateInMillis.toInt())
        }

        calendarView.date = Calendar.getInstance().apply {
            set(year, month, day)
        }.timeInMillis
    }


}


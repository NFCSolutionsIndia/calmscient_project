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

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.calmscient.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CustomCalendarDialog : DialogFragment() {

    private var onCancelClickListener: (() -> Unit)? = null
    private var onOkClickListener: (() -> Unit)? = null

    interface OnDateSelectedListener {
        fun onDateSelected(date: CalendarDay)
    }

    private var listener: OnDateSelectedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_date_picker, container, false)

        val calendarView: MaterialCalendarView = view.findViewById(R.id.calendarView)

        // Get the selected date string from arguments
        val selectedDateString = arguments?.getString("selected_date")

        // If no date is passed, default to today's date
        val calendar = Calendar.getInstance()
        if (selectedDateString != null) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val selectedDate = dateFormat.parse(selectedDateString)
            selectedDate?.let {
                calendar.time = it // Use the passed date
            }
        }

        // Set the selected date (either the passed date or today)
        //calendarView.setSelectedDate(calendar.time)

        // Set the selected date (either the passed date or today)
        val selectedDay = CalendarDay.from(calendar.time)
        calendarView.selectedDate = selectedDay

        // Scroll directly to the month of the selected date
        calendarView.currentDate = selectedDay


        calendarView.setOnDateChangedListener { widget, date, selected ->
            if (selected) {
                listener?.onDateSelected(date)
            }
        }

        val btnCancel: View = view.findViewById(R.id.btnCancel)
        btnCancel.setOnClickListener {
            dismiss()
            onCancelClickListener?.invoke()
        }

        val btnOk: View = view.findViewById(R.id.btnOk)
        btnOk.setOnClickListener {
            dismiss()
            onOkClickListener?.invoke()
        }

        return view
    }

    fun setOnDateSelectedListener(listener: OnDateSelectedListener) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), theme).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
        }
    }

    fun setOnOkClickListener(listener: () -> Unit) {
        onOkClickListener = listener
    }

    fun setOnCancelClickListener(listener: () -> Unit) {
        onCancelClickListener = listener
    }

    companion object {
        // Method to create the dialog and pass the selected date string
        fun newInstance(selectedDate: String): CustomCalendarDialog {
            val dialog = CustomCalendarDialog()
            val args = Bundle()
            args.putString("selected_date", selectedDate)
            dialog.arguments = args
            return dialog
        }
    }
}

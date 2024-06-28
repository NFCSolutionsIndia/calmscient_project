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

class CustomCalendarDialog : DialogFragment() {

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
        calendarView.setOnDateChangedListener { widget, date, selected ->
            if (selected) {
                listener?.onDateSelected(date)
                dismiss()
            }
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
}

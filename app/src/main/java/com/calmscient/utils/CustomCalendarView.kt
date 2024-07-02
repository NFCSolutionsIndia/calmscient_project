package com.calmscient.utils

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.calmscient.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener
import java.util.*

class CustomCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var calendarView: MaterialCalendarView

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.view_custom_calendar, this, true)

        calendarView = findViewById(R.id.calendarView)

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        setupCalendar(currentYear, 1, currentYear, 12)
        setCurrentMonth()
    }

    private fun setupCalendar(minYear: Int, minMonth: Int, maxYear: Int, maxMonth: Int) {
        calendarView.state().edit()
            .setFirstDayOfWeek(Calendar.SUNDAY)
            .setMinimumDate(CalendarDay.from(minYear, minMonth - 1, 1))
            .setMaximumDate(CalendarDay.from(maxYear, maxMonth - 1, 31))
            .setCalendarDisplayMode(CalendarMode.MONTHS)
            .commit()
    }

    private fun setCurrentMonth() {
        val currentDate = CalendarDay.today()
        calendarView.setCurrentDate(currentDate)
        calendarView.setSelectedDate(currentDate)
    }

    fun navigateToMonth(year: Int, month: Int) {
        val targetDate = CalendarDay.from(year, month - 1, 1)
        calendarView.setCurrentDate(targetDate)
    }

    fun setOnDateSelectedListener(listener: OnDateSelectedListener) {
        calendarView.setOnDateChangedListener(listener)
    }

    fun setOnMonthChangedListener(listener: OnMonthChangedListener) {
        calendarView.setOnMonthChangedListener(listener)
    }

    fun getSelectedDates(): List<CalendarDay> {
        return calendarView.selectedDates
    }

    fun getSelectedDatesSize() : Int{
        return calendarView.selectedDates.size
    }

    fun clearSelections() {
        calendarView.clearSelection()
    }

    fun setSelectionMode(selectionMode: Int) {
        calendarView.selectionMode = selectionMode
    }

    fun updateDateRange(minYear: Int, minMonth: Int, maxYear: Int, maxMonth: Int) {
        setupCalendar(minYear, minMonth, maxYear, maxMonth)
    }
}
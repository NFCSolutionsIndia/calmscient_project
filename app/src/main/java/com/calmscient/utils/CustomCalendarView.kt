package com.calmscient.utils

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.calmscient.Interface.OnSelectionDateChangeListener
import com.calmscient.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener
import java.text.SimpleDateFormat
import java.util.*

class CustomCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var calendarView: MaterialCalendarView
    private var onSelectionChangeListener: OnSelectionDateChangeListener? = null
    private var maxDay: Int = 31

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.view_custom_calendar, this, true)

        calendarView = findViewById(R.id.calendarView)

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        setupCalendar(currentYear, 1, currentYear, 12)
        setCurrentMonth()

        calendarView.setOnDateChangedListener(OnDateSelectedListener { widget, date, selected ->
            onSelectionChangeListener?.onSelectionChanged(calendarView.selectedDates.size)
        })
    }

    private fun setupCalendar(minYear: Int, minMonth: Int, maxYear: Int, maxMonth: Int) {
        val calendar = Calendar.getInstance()

        calendar.set(Calendar.YEAR, maxYear)
        calendar.set(Calendar.MONTH, maxMonth - 1)
        maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        calendarView.state().edit()
            .setFirstDayOfWeek(Calendar.SUNDAY)
            .setMinimumDate(CalendarDay.from(minYear, minMonth - 1, 1))
            .setMaximumDate(CalendarDay.from(maxYear, maxMonth - 1, maxDay)) // Use maxDay here
            .setCalendarDisplayMode(CalendarMode.MONTHS)
            .commit()
    }

    private fun setCurrentMonth() {
        val currentDate = CalendarDay.today()
        calendarView.currentDate = currentDate
        calendarView.selectedDate = currentDate
    }

    fun navigateToMonth(year: Int, month: Int) {
        val targetDate = CalendarDay.from(year, month - 1, 1)
        calendarView.currentDate = targetDate
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
    fun getSelectedDatesSizeForCurrentMonth(): Int {
        val currentMonth = calendarView.currentDate.month  // Get the current month (0-based)
        val currentYear = calendarView.currentDate.year    // Get the current year

        // Filter the selected dates to include only those in the current month and year
        return calendarView.selectedDates.filter { date ->
            date.year == currentYear && date.month == currentMonth
        }.size
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

    fun setOnSelectionChangeListener(listener: OnSelectionDateChangeListener) {
        this.onSelectionChangeListener = listener
    }

    fun getFormattedSelectedDates(): List<String> {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        return calendarView.selectedDates.map { date ->
            dateFormat.format(date.date)
        }
    }

    // set selected dates
    fun setSelectedDates(dates: List<Date?>) {
        val calendarDays = dates.filterNotNull().map { date -> CalendarDay.from(date) }
        clearSelections()
        calendarDays.forEach { day ->
            calendarView.setDateSelected(day, true)
        }
    }

    // New method to enable/disable interactions
    fun setInteractionsEnabled(enabled: Boolean) {
        calendarView.isEnabled = enabled
    }

    public fun getUnselectedDaysInMonth(): Int {
        val totalDaysInMonth = getTotalDaysInCurrentMonth()
        val selectedDaysCount = getSelectedDatesSizeForCurrentMonth()
        return totalDaysInMonth - selectedDaysCount
    }

    private fun getTotalDaysInCurrentMonth(): Int {
        val currentDate = calendarView.currentDate

        val year = currentDate.year
        val month = currentDate.month + 1

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)

        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun lockToCurrentMonth() {
        val currentDate = calendarView.currentDate
        val currentYear = currentDate.year
        val currentMonth = currentDate.month

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, currentYear)
        calendar.set(Calendar.MONTH, currentMonth)

        val firstDayOfMonth = CalendarDay.from(currentYear, currentMonth, 1)
        val lastDayOfMonth = CalendarDay.from(currentYear, currentMonth, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))

        calendarView.state().edit()
            .setMinimumDate(firstDayOfMonth)   // Lock to the start of the current month
            .setMaximumDate(lastDayOfMonth)    // Lock to the end of the current month
            .commit()
    }


}

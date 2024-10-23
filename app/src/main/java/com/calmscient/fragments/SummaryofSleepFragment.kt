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

package com.calmscient.fragments

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.calmscient.R
import com.calmscient.adapters.SummaryofMoodFragmentAdapter
import com.calmscient.data.remote.WeeklySummaryMoodTask
import com.calmscient.databinding.CalendarDayLayoutBinding
import com.calmscient.databinding.FragmentWeeklysummarymoodBinding
import com.calmscient.databinding.SummaryofsleepFragmentBinding
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.di.remote.response.SummaryOfPHQ9Response
import com.calmscient.di.remote.response.SummaryOfSleepResponse
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomCalendarDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.CustomMarkerView
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.utils.getColorCompat
import com.calmscient.viewmodels.GetSummaryOfPHQViewModel
import com.calmscient.viewmodels.GetSummaryOfSleepViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.kizitonwose.calendar.core.Week
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.core.yearMonth
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekDayBinder
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SummaryofSleepFragment : Fragment() , CustomCalendarDialog.OnDateSelectedListener{
    private lateinit var binding: SummaryofsleepFragmentBinding
    private lateinit var dateView: TextView
    private lateinit var calenderDateView: TextView
    private var selectedDate = LocalDate.now()
    private val dateFormatter = DateTimeFormatter.ofPattern("dd")
    private val summaryCardViewItems = mutableListOf<WeeklySummaryMoodTask>()
    private lateinit var summaryOfMoodAdapter: SummaryofMoodFragmentAdapter

    private val getSummaryOfSleepViewModel : GetSummaryOfSleepViewModel by activityViewModels()
    private lateinit var commonAPICallDialog: CommonAPICallDialog
    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var summaryOfSleepResponse: SummaryOfSleepResponse
    private var loginResponse : LoginResponse? = null
    private lateinit var lineChart: LineChart
    private  lateinit var accessToken : String

    private lateinit var averageSleepTextView: TextView
    private lateinit var averageSleepTextViewWithHours: TextView
    private lateinit var maxSleepTextView: TextView
    private lateinit var minSleepTextView: TextView
    private lateinit var averageSleepProBar: ProgressBar


    override fun onDateSelected(date: CalendarDay) {

        // Convert CalendarDay to LocalDate
        val localDate = date.toLocalDate()

        // Remove the selection indicator from the previously selected date
        val previousSelectedDate = selectedDate
        previousSelectedDate?.let {
            binding.exSevenCalendar.notifyDateChanged(it)
        }

        // Update the selectedDate variable
        selectedDate = localDate

        // Scroll the WeekCalendarView to the selected month and day
        binding.exSevenCalendar.scrollToDate(localDate)

        // Notify the WeekCalendarView to update the selected date UI
        binding.exSevenCalendar.notifyDateChanged(localDate)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            loadFragment(WeeklySummaryFragment())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SummaryofsleepFragmentBinding.inflate(inflater, container, false)
        binding.backIcon.setOnClickListener {
            loadFragment(WeeklySummaryFragment())
        }
        /*binding.calenderview.setOnClickListener {
            binding.newbackIcon.visibility = View.VISIBLE
            binding.graphScreen.visibility = View.GONE
            binding.datesScreen.visibility = View.VISIBLE
            binding.backIcon.visibility = View.GONE
            binding.scrollViewScreen.visibility = View.GONE
        }
        binding.newbackIcon.setOnClickListener {
            binding.backIcon.visibility = View.VISIBLE
            binding.graphScreen.visibility = View.VISIBLE
            binding.datesScreen.visibility = View.GONE
            binding.newbackIcon.visibility = View.GONE
            binding.scrollViewScreen.visibility = View.VISIBLE

        }*/

        accessToken = SharedPreferencesUtil.getData(requireContext(), "accessToken", "")
        customProgressDialog = CustomProgressDialog(requireContext())
        commonAPICallDialog = CommonAPICallDialog(requireContext())
        val loginJsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString<LoginResponse>(loginJsonString)


        lineChart = binding.lineChartViewSleep

        if (CommonClass.isNetworkAvailable(requireContext())) {
            apiCall(null)
            observeViewModel()
        }
        else{
            CommonClass.showInternetDialogue(requireContext())
        }


        averageSleepTextView = binding.averageSleepHours
        maxSleepTextView = binding.maxHoursSleep
        minSleepTextView = binding.minHoursSleep
        averageSleepTextViewWithHours = binding.tvAverageHoursSleep
        averageSleepProBar = binding.averageSleepProgressBar


        binding.needToTalkWithSomeOne.setOnClickListener {
            loadFragment(EmergencyResourceFragment())
        }

        binding.calenderView.setOnClickListener{
            val dialog = CustomCalendarDialog()
            dialog.setOnDateSelectedListener(this)
            dialog.show(parentFragmentManager, "CustomCalendarDialog")
            //customCalendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_SINGLE)

             dialog.setOnOkClickListener {
                 apiCall(selectedDate)
             }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Ensure that dateView is part of the FragmentWeeklysummarymoodBinding layout
        val dateView: TextView = binding.dateView
        val calenderDateView: TextView = binding.calenderDateView

        // Get the current date from the calendar
        val calendar = Calendar.getInstance()
        val currentDate: Date = calendar.time

        // Format the current date and calculate the date for next month
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val currentDateString: String = dateFormat.format(currentDate)

        // Calculate the date for next month
        //calendar.add(Calendar.MONTH, -1)
        // Subtract one week (7 days) from today's date
        calendar.add(Calendar.DATE, -7)
        val previousMonthDate: Date = calendar.time
        val previousMonthDateString: String = dateFormat.format(previousMonthDate)

        // Create the final date string
        val finalDateString = "$previousMonthDateString - $currentDateString"

        // Set the date in the TextView
        dateView.text = finalDateString
        calenderDateView.text = finalDateString
        class DayViewContainer(view: View) : ViewContainer(view) {
            val bind = CalendarDayLayoutBinding.bind(view)
            lateinit var day: WeekDay

            init {
                view.setOnClickListener {
                    if (selectedDate != day.date) {
                        val oldDate = selectedDate
                        selectedDate = day.date
                        binding.exSevenCalendar.notifyDateChanged(day.date)
                        oldDate?.let { binding.exSevenCalendar.notifyDateChanged(it) }
                    }
                }
            }

            fun bind(day: WeekDay) {
                this.day = day
                bind.exSevenDateText.text = dateFormatter.format(day.date)
                bind.exSevenDayText.text = day.date.dayOfWeek.displayText()
                val colorRes = if (day.date == selectedDate) {
                    R.color.white
                } else {
                    R.color.black
                }
                bind.exSevenDateText.setTextColor(view.context.getColorCompat(colorRes))
                bind.exSevenDayText.setTextColor(view.context.getColorCompat(colorRes))
                val colorResLayout = if (day.date == selectedDate) {
                    R.drawable.calendar_custom_border
                } else {
                    R.color.example_7_calendar
                }
                bind.layoutDate.setBackgroundResource(colorResLayout)
                //bind.exSevenSelectedView.isVisible = day.date == selectedDate
            }
        }
        binding.exSevenCalendar.dayBinder = object : WeekDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: WeekDay) = container.bind(data)
        }
        binding.exSevenCalendar.weekScrollListener = { weekDays ->
            val text = binding.exSevenToolbar.title
            binding.exSevenToolbar.setTitleTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.black
                )
            )
        }
        val currentMonth = YearMonth.now()
        binding.exSevenCalendar.setup(
            currentMonth.minusMonths(24).atStartOfMonth(),
            currentMonth.plusMonths(24).atEndOfMonth(),
            firstDayOfWeekFromLocale(),
        )
        binding.exSevenCalendar.scrollToDate(LocalDate.now())
        binding.recyclerViewSummaryMood.layoutManager = LinearLayoutManager(requireContext())
        summaryOfMoodAdapter = SummaryofMoodFragmentAdapter(summaryCardViewItems)
        binding.recyclerViewSummaryMood.adapter = summaryOfMoodAdapter
        summaryOfMoodAdapter.updateTasks(summaryCardViewItems)
        displayCardViewsForSelectedDate()
    }

    private fun displayCardViewsForSelectedDate() {
        // Format the selected date as "MM/dd/yyyy" and set it as the first dateview1
        val dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        summaryCardViewItems.clear()
        // Get the selected date
        val selectedDate = selectedDate
        for (i in 0 until 7) {
            val description = getCardDescription(i)
            val description1 = getCardDescription1(i)
            val nextDate = selectedDate.plusDays(i.toLong())
            val nextDateStr = nextDate.format(dateFormatter)
            summaryCardViewItems.add(
                WeeklySummaryMoodTask(
                    nextDateStr,
                    description,
                    description1
                )
            )
        }
        summaryOfMoodAdapter.notifyDataSetChanged()
    }

    private fun getCardDescription(cardPosition: Int): String {
        return when (cardPosition) {
            1 -> getString(R.string.eight_hours)
            2 -> getString(R.string.six_hours)
            3 -> getString(R.string.eight_hours)
            4 -> getString(R.string.five_hours)
            5 -> getString(R.string.three_hours)
            6 -> getString(R.string.eight_hours)
            else -> getString(R.string.eight_hours)
        }
    }

    private fun getCardDescription1(cardPosition: Int): String {
        return when (cardPosition) {
            0 -> getString(R.string.hours_sleep1)
            1 -> getString(R.string.hours_sleep2)
            2 -> getString(R.string.hours_sleep3)
            3 -> getString(R.string.hours_sleep4)
            4 -> getString(R.string.hours_sleep5)
            5 -> getString(R.string.hours_sleep6)
            else -> getString(R.string.hours_sleep1)
        }
    }

    fun getWeekPageTitle(week: Week): String {
        val firstDate = week.days.first().date
        val lastDate = week.days.last().date
        return when {
            firstDate.yearMonth == lastDate.yearMonth -> {
                firstDate.yearMonth.displayText()
            }

            firstDate.year == lastDate.year -> {
                "${firstDate.month.displayText(short = false)} - ${lastDate.yearMonth.displayText()}"
            }

            else -> {
                "${firstDate.yearMonth.displayText()} - ${lastDate.yearMonth.displayText()}"
            }
        }
    }

    fun YearMonth.displayText(short: Boolean = false): String {
        return "${this.month.displayText(short = short)} ${this.year}"
    }

    fun Month.displayText(short: Boolean = true): String {
        val style = if (short) TextStyle.SHORT else TextStyle.FULL
        return getDisplayName(style, Locale.ENGLISH)
    }

    fun DayOfWeek.displayText(uppercase: Boolean = false): String {
        return getDisplayName(TextStyle.SHORT, Locale.ENGLISH).let { value ->
            if (uppercase) value.uppercase(Locale.ENGLISH) else value
        }
    }

    /*private fun apiCall()
    {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        val calendar = Calendar.getInstance()

        // Get today's date
        val toDate = dateFormat.format(calendar.time)

        // Subtract one month from today's date
        *//*calendar.add(Calendar.MONTH, -1)
        val fromDate = dateFormat.format(calendar.time)*//*

        // Subtract one week (7 days) from today's date
        calendar.add(Calendar.DATE, -7)
        val fromDate = dateFormat.format(calendar.time)

        loginResponse?.loginDetails?.let { getSummaryOfSleepViewModel.getSummaryOfSleep(it.patientLocationID,it.patientID,it.clientID,fromDate,toDate,it.userID, accessToken) }

    }*/
    private fun apiCall(selectedDate: LocalDate?)
    {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        val calendar = Calendar.getInstance()

        // Check if selectedDate is provided
        val toDate: String
        val fromDate: String

        if (selectedDate != null) {
            // If selectedDate is not null, set toDate as selectedDate
            toDate = dateFormat.format(Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))

            // Set fromDate as 7 days back from selectedDate
            val startDateCalendar = Calendar.getInstance().apply {
                time = Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                add(Calendar.DATE, -6)
            }
            fromDate = dateFormat.format(startDateCalendar.time)
        } else {
            // If selectedDate is null, set toDate as today's date
            toDate = dateFormat.format(calendar.time)

            // Set fromDate as 7 days back from today's date
            calendar.add(Calendar.DATE, -6)
            fromDate = dateFormat.format(calendar.time)
        }
        // Create the final date string
        val finalDateString = "$fromDate - $toDate"

        // Set the date in the TextView
        binding.dateView.text = finalDateString
        getSummaryOfSleepViewModel.clear()
        loginResponse?.loginDetails?.let { getSummaryOfSleepViewModel.getSummaryOfSleep(it.patientLocationID,it.patientID,it.clientID,fromDate,toDate,it.userID, accessToken) }
    }

    private fun observeViewModel()
    {

        getSummaryOfSleepViewModel.loadingLiveData.observe(viewLifecycleOwner, Observer { isLoading->
            if(isLoading)
            {
                customProgressDialog.dialogDismiss()
                customProgressDialog.show("Loading...")
            }
            else
            {
                customProgressDialog.dialogDismiss()
            }
        })

        getSummaryOfSleepViewModel.saveResponseLiveData.observe(viewLifecycleOwner, Observer { successDate->
            if(successDate != null)
            {
                summaryOfSleepResponse = successDate

                Log.d("Sleep Response","$successDate")

                handleApiResponse(summaryOfSleepResponse)
            }
        })

    }
    private fun handleApiResponse(response: SummaryOfSleepResponse) {
        if (response.statusResponse.responseCode == 200) {
            val sleepByDateRange = response.sleepMonitorList

            if (sleepByDateRange.isEmpty() || response.statusResponse.responseMessage.trim() == getString(R.string.no_records)) {
                showNoDataMessage()
                return
            }
            val entries = ArrayList<Entry>()
            val dateLabels = ArrayList<String>()
            val sleepHoursList = sleepByDateRange.map { it.sleepHours.toFloat() }

            val sortedSleepByDateRange = sleepByDateRange.sortedBy {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.createdAt)
            }

            // Assuming SleepByDateRange has a date and score
            for (i in sleepByDateRange.indices) {
                val phqData = sortedSleepByDateRange[i]
                val sleepHours = phqData.sleepHours.toInt()
                val entry = Entry(i.toFloat(), sleepHours.toFloat())
                entry.data = sleepHours
                entries.add(entry)


                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(phqData.createdAt)
                val formattedDate = SimpleDateFormat("dd", Locale.getDefault()).format(date)
                dateLabels.add(formattedDate)
            }

            val dataSet = LineDataSet(entries, "Sleep Hours")
            dataSet.color = Color.parseColor("#6E6BB3")
            dataSet.valueTextColor = Color.BLACK
            dataSet.setDrawCircles(true)
            dataSet.setCircleColor(Color.parseColor("#6E6BB3"))
            dataSet.setDrawValues(true)

            // Setting the custom ValueFormatter to remove .00 from data points
            dataSet.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }

            val lineData = LineData(dataSet)
            lineChart.data = lineData

            // Customize the chart
            val xAxis = lineChart.xAxis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.labelCount = dateLabels.size
            xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return if (value.toInt() >= 0 && value.toInt() < dateLabels.size) {
                        dateLabels[value.toInt()]
                    } else {
                        ""
                    }
                }
            }
            xAxis.setDrawGridLines(false) // Disable vertical grid lines

            val yAxisLeft = lineChart.axisLeft
            yAxisLeft.setDrawGridLines(true)
            yAxisLeft.enableGridDashedLine(10f, 10f, 0f)
            yAxisLeft.axisMinimum = 0f // Ensure Y-axis starts from 0
            yAxisLeft.axisMaximum = 12f
            yAxisLeft.granularity = 1f // Set the interval to 2
            yAxisLeft.labelCount = 12 // Ensure 15 intervals from 0 to 30

            lineChart.axisRight.isEnabled = false

            // Hide the axis lines
            xAxis.setDrawAxisLine(false)
            yAxisLeft.setDrawAxisLine(false)

            // Set custom marker
            val markerView = CustomMarkerView(requireContext(), R.layout.custom_marker_view)
            lineChart.marker = markerView

            // Disable zooming
            lineChart.setScaleEnabled(false)
            lineChart.isDragEnabled = false

            lineChart.description.isEnabled = false

            // Adding animations
            lineChart.animateX(1000, Easing.EaseInOutQuad)
            lineChart.animateY(1000, Easing.EaseInOutQuad)

            lineChart.invalidate() // Refresh the chart

            // Calculate average, max, and min sleep hours
            val averageSleep = sleepHoursList.average().toFloat()
            val maxSleep = sleepHoursList.maxOrNull() ?: 0f
            val minSleep = sleepHoursList.minOrNull() ?: 0f

            val formattedAverageSleep = String.format("%.2f", averageSleep)

            // Display these values in the TextViews
            averageSleepTextView.text = formattedAverageSleep+" /"
            averageSleepTextViewWithHours.text = formattedAverageSleep+ getString(R.string.hrs)
            maxSleepTextView.text = maxSleep.toString()+getString(R.string.hrs)
            minSleepTextView.text = minSleep.toString()+getString(R.string.hrs)

            binding.averageSleepProgressBar.progress = averageSleep.toInt()

        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun showNoDataMessage() {
        lineChart.invalidate()
        lineChart.clear()
        lineChart.setNoDataText("No data available")
        lineChart.setNoDataTextColor(Color.parseColor("#6E6BB3"))

        averageSleepTextView.text = 0.00 .toString()+" /"
        averageSleepTextViewWithHours.text = 0.0.toString()+ getString(R.string.hrs)
        maxSleepTextView.text = 0.0.toString()+getString(R.string.hrs)
        minSleepTextView.text = 0.0.toString()+getString(R.string.hrs)
        binding.moreSleepText.text = "         "

    }

   /* private fun resultPercent(maxValue: Int, securedMarks: Int) {
        // binding.progressbarResult.max = maxValue
        val progressPercentage = (securedMarks.toFloat() / maxValue.toFloat() * 100).toInt()
        val progressAnimator = ValueAnimator.ofInt(0, progressPercentage)
        progressAnimator.addUpdateListener { valueAnimator ->
           averageSleepProBar.progress = valueAnimator.animatedValue as Int
        }
        progressAnimator.interpolator = LinearInterpolator()
        progressAnimator.duration = 2000 // Adjust duration as needed for the desired animation speed
        progressAnimator.start()
    }*/

    fun CalendarDay.toLocalDate(): LocalDate {
        return LocalDate.of(this.year, this.month + 1, this.day) // Note: CalendarDay month is 0-based, LocalDate is 1-based
    }
}
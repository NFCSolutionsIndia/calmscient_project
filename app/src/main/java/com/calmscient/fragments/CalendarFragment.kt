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

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.calmscient.Interface.CellClickListener
import com.calmscient.Interface.OnCheckedChangeListener
import com.calmscient.R
import com.calmscient.adapters.MedicationsCardAdapter
import com.calmscient.databinding.CalendarDayLayoutBinding
import com.calmscient.databinding.CalendarFragmentLayoutBinding
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.di.remote.response.MedicalDetails
import com.calmscient.di.remote.response.ScheduledTimeDetails
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomCalendarDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.DatePickerUtil
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SavePreferences
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.utils.getColorCompat
import com.calmscient.viewmodels.MedicationDetailsViewModel
import com.kizitonwose.calendar.core.Week
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.core.yearMonth
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekDayBinder
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale


// Define the CardViewItem data class
data class CardViewItem(
    val title: String,
    val dosage: String,
    val timeMorning: String?,
    val timeEvening: String?,
    val sunImageResource: Int?,
    val moonImageResource: Int?,
    var medicalDetails :MedicalDetails,
    var isChecked: Boolean
)

class CalendarFragment : Fragment(), CellClickListener ,CustomCalendarDialog.OnDateSelectedListener,
    OnCheckedChangeListener {
    private lateinit var binding: CalendarFragmentLayoutBinding
    private var selectedDate = LocalDate.now()
    private val dateFormatter = DateTimeFormatter.ofPattern("dd")
    private lateinit var cardViewAdapter: MedicationsCardAdapter
    private val cardViewItems = mutableListOf<CardViewItem>()
    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonDialog: CommonAPICallDialog

    private val medicationDetailsViewModel: MedicationDetailsViewModel by activityViewModels()

    private var loginResponse: LoginResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (CommonClass.isNetworkAvailable(requireContext())) {
                loadFragment(MedicalRecordsFragment())
            } else {
                CommonClass.showInternetDialogue(requireContext())
            }
        }
    }

    // for the checkbox state in recyclerview
    @SuppressLint("ResourceAsColor")
    override fun onCheckedChanged(isAnyChecked: Boolean) {
       if(isAnyChecked){
           binding.saveButton.isEnabled = true
           binding.saveButton.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_border)
           binding.saveButton.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
       }else{
           binding.saveButton.isEnabled = false
           binding.saveButton.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_background_disabled)
           binding.saveButton.setTextColor(ContextCompat.getColor(requireContext(),R.color.grey_light))
       }
    }
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = CalendarFragmentLayoutBinding.inflate(inflater, container, false)
        binding.backIcon.setOnClickListener {

            if (CommonClass.isNetworkAvailable(requireContext())) {
                loadFragment(MedicalRecordsFragment())
            } else {
                CommonClass.showInternetDialogue(requireContext())
            }
        }
        Log.d("Selected Date :","$selectedDate")
        binding.saveButton.setOnClickListener {
            if (CommonClass.isNetworkAvailable(requireContext())) {
                commonDialog.showDialog(getString(R.string.saved_successfully),R.drawable.ic_success_dialog)
                commonDialog.setOnDismissListener {
                    loadFragment(MedicalRecordsFragment())
                }
            } else {
                CommonClass.showInternetDialogue(requireContext())
            }
        }
        binding.plusIcon.setOnClickListener {
            if (CommonClass.isNetworkAvailable(requireContext())) {
                loadFragment(AddMedicationsFragment())
            } else {
                CommonClass.showInternetDialogue(requireContext())
            }
        }

        customProgressDialog = CustomProgressDialog(requireContext())

        commonDialog = CommonAPICallDialog(requireContext())

        val jsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString<LoginResponse>(jsonString)

        observeViewModel()

        binding.exSevenToolbar.setOnClickListener{
          /*  val dialog = CustomCalendarDialog()
            dialog.setOnDateSelectedListener(this)
            dialog.show(parentFragmentManager, "CustomCalendarDialog")*/
            //customCalendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_SINGLE)

            // Create and show the dialog, passing the selected date string
            val dialog = CustomCalendarDialog.newInstance(selectedDate.toString())
            dialog.setOnDateSelectedListener(this)
            dialog.show(parentFragmentManager, "CustomCalendarDialog")

            dialog.setOnOkClickListener {

                medicationDetailsViewModel.clear()
                apiCall(selectedDate.toString())
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val savePreferences = SavePreferences(requireContext())

        // Set Locale based on the saved language preference
        val locale: Locale = when (savePreferences.getLanguageMode()) {
            "english" -> Locale("en")
            "spanish" -> Locale("es")
            "asl" -> Locale("en") // Assuming ASL is a valid locale, otherwise handle it accordingly.
            else -> Locale.getDefault() // Fallback to device default
        }
        // Set the locale for date formatting
        Locale.setDefault(locale)

        class DayViewContainer(view: View) : ViewContainer(view) {
            val bind = CalendarDayLayoutBinding.bind(view)
            lateinit var day: WeekDay

            init {
                view.setOnClickListener {
                    if (selectedDate != day.date) {
                        val oldDate = selectedDate
                        selectedDate = day.date
                        Log.d("Selected Date :","$selectedDate")
                        incrementDateByOne()
                        binding.exSevenCalendar.notifyDateChanged(day.date)
                        oldDate?.let { binding.exSevenCalendar.notifyDateChanged(it) }

                        // Call the function to update medication details for the new selected date
                        medicationDetailsViewModel.clear()
                        apiCall(selectedDate.toString())
                    }
                }
            }

            fun bind(day: WeekDay,locale: Locale) {
                this.day = day
                bind.exSevenDateText.text = dateFormatter.format(day.date)
                bind.exSevenDayText.text = day.date.dayOfWeek.displayText(uppercase = false, locale = locale)
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
            override fun bind(container: DayViewContainer, data: WeekDay) = container.bind(data,locale)
        }
        binding.exSevenCalendar.weekScrollListener = { weekDays ->
            val text = binding.exSevenToolbar.title
            binding.exSevenToolbar.title = getWeekPageTitle(weekDays, locale)
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
        binding.recyclerViewMedications.layoutManager = LinearLayoutManager(requireContext())
        cardViewAdapter = MedicationsCardAdapter(cardViewItems,this)
        cardViewAdapter.setCellClickListener(this)
        binding.recyclerViewMedications.adapter = cardViewAdapter

        // Call this function when the date is selected
        // displayCardViewsForSelectedDate()
    }

    fun incrementDateByOne() {
        /* val c = Calendar.getInstance()
        c.time = date
        c.add(Calendar.DATE, 1)
        return c.time*/

        val sdf = SimpleDateFormat("MM/dd/yyyy")
        for (i in 0..6) {
            val calendar: Calendar = GregorianCalendar()
            calendar.add(Calendar.DATE, i)
            val day: String = sdf.format(calendar.time)
            Log.i(TAG, day)
        }
    }

    fun getWeekPageTitle(week: Week, locale: Locale): String {
        val firstDate = week.days.first().date
        val lastDate = week.days.last().date
        return when {
            firstDate.yearMonth == lastDate.yearMonth -> {
                firstDate.yearMonth.displayText(locale = locale)
            }
            firstDate.year == lastDate.year -> {
                "${firstDate.month.displayText(false, locale)} - ${lastDate.yearMonth.displayText(false,locale)}"
            }
            else -> {
                "${firstDate.yearMonth.displayText(false,locale)} - ${lastDate.yearMonth.displayText(false,locale)}"
            }
        }
    }

    fun YearMonth.displayText(short: Boolean = false, locale: Locale): String {
        return "${this.month.displayText(short = short, locale = locale)} ${this.year}"
    }


    fun Month.displayText(short: Boolean = false, locale: Locale): String {
        val style = if (short) TextStyle.SHORT else TextStyle.FULL
        return getDisplayName(style, locale)
    }


    fun DayOfWeek.displayText(uppercase: Boolean = false, locale: Locale): String {
        return getDisplayName(TextStyle.SHORT, locale).let { value ->
            if (uppercase) value.uppercase(locale) else value
        }
    }


    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        if (fragment is AddMedicationsFragment) {
            // If the fragment being loaded is AddMedicationsFragment,
            // pass the selectedDate as an argument
            val bundle = Bundle()
            bundle.putSerializable("selectedDate", selectedDate)
            fragment.arguments = bundle
        }
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }


    override fun onCellClickListener(position: Int, item: CardViewItem) {
        // Handle item click event here
        // Create an instance of MedicationDetailFragment and pass the data to it
        Log.d("Medical Details : ","${item.medicalDetails}")
        val medicationDetailFragment = MedicationDetailFragment.newInstance(item.medicalDetails,selectedDate)
        // Load the MedicationDetailFragment
        loadFragment(medicationDetailFragment)
    }

    private fun apiCall(selectedDate: String) {
        // Formatter for the incoming date format
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        // Formatter for the desired output date format
        val outputFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")

        // Parse the selected date using the input formatter
        val selectedLocalDate = LocalDate.parse(selectedDate, inputFormatter)
        // Calculate the date seven days before
        val sevenDaysBefore = selectedLocalDate.minusDays(7)

        // Format both dates using the output formatter
        val formattedSelectedDate = selectedLocalDate.format(outputFormatter)
        val formattedSevenDaysBefore = sevenDaysBefore.format(outputFormatter)

        medicationDetailsViewModel.clear()
        // Pass the formatted dates to the API call
        medicationDetailsViewModel.getMedicationDetails(
            loginResponse!!.loginDetails.patientLocationID,
            loginResponse!!.loginDetails.patientID,
            loginResponse!!.loginDetails.clientID,
            formattedSelectedDate,
            formattedSelectedDate,
            loginResponse!!.token.access_token
        )
        observeViewModelOne()
    }



    private fun observeViewModel() {
        // Get today's date
        val today = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MM/dd/yyyy")
        val todayDate = dateFormat.format(today.time)

        // Calculate 7 days before today
        val sevenDaysBefore = Calendar.getInstance()
        sevenDaysBefore.add(Calendar.DAY_OF_YEAR, -7)
        val formattedSevenDaysBefore = dateFormat.format(sevenDaysBefore.time)

        medicationDetailsViewModel.clear()

        medicationDetailsViewModel.getMedicationDetails(
            loginResponse!!.loginDetails.patientLocationID,
            loginResponse!!.loginDetails.patientID,
            loginResponse!!.loginDetails.clientID,
            todayDate,
            todayDate,
            loginResponse!!.token.access_token
        )
        observeViewModelOne()

    }

    private fun observeViewModelOne()
    {
        medicationDetailsViewModel.loadingLiveData.observe(
            viewLifecycleOwner,
            Observer { isLoading ->
                if (isLoading) {
                    customProgressDialog.show("Loading...")
                } else {
                    customProgressDialog.dialogDismiss()
                }
            })

        medicationDetailsViewModel.successLiveData.observe(viewLifecycleOwner, Observer { isSuccess ->
            filterData(isSuccess)
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterData(isSuccess: Boolean) {
        var sunImageResource :Int?
        var moonImageResource :Int?
        var morningTime :String?
        var eveningTime :String?
        if (isSuccess) {
            customProgressDialog.dialogDismiss()
            commonDialog.dismiss()
            medicationDetailsViewModel.saveResponseLiveData.observe(
                viewLifecycleOwner,
                Observer { successData ->
                    if (successData != null) {
                        // Clear the cardViewItems list
                        cardViewItems.clear()
                        cardViewItems.removeFirstOrNull()
                        sunImageResource = null
                        moonImageResource = null
                        morningTime = null
                        eveningTime = null


                        val selectedDateMedication =
                            successData.medicineDetails.filter { it.date == selectedDate.toString() }

                        if (selectedDateMedication.isNotEmpty()) {
                            // Medications exist for the selected date
                            binding.noMedicationsTextView.visibility = View.GONE
                            binding.saveButton.visibility = View.VISIBLE

                            selectedDateMedication.forEach { medicineDetail ->
                                medicineDetail.medicationDetailsByDate.forEach { medicationDetailByDate ->
                                    // Add medication details to the RecyclerView
                                    morningTime =
                                        getMorningTime(medicationDetailByDate.medicalDetails.scheduledTimeList.flatMap { it.scheduledTimes })
                                    eveningTime =
                                        getEveningTime(medicationDetailByDate.medicalDetails.scheduledTimeList.flatMap { it.scheduledTimes })

                                     sunImageResource =  if (morningTime!!.isNotEmpty()) R.drawable.sunset else null
                                     moonImageResource = if (eveningTime!!.isNotEmpty()) R.drawable.moon else null

                                    // Add card with null morning and evening times if no times available
                                    cardViewItems.add(
                                        CardViewItem(
                                            medicationDetailByDate.medicineName,
                                            "${medicationDetailByDate.numberOfTablets} tablets",
                                            if (morningTime!!.isNotEmpty()) morningTime else null,
                                            if (eveningTime!!.isNotEmpty()) eveningTime else null,
                                            sunImageResource,
                                            moonImageResource,
                                            medicationDetailByDate.medicalDetails,
                                            false
                                        )
                                    )
                                }
                            }
                        } else {
                            // No medications for the selected date
                            binding.noMedicationsTextView.visibility = View.VISIBLE
                            binding.saveButton.visibility = View.GONE
                        }

                        Log.d("Data in cardViewItems","$cardViewItems")
                        // Notify the adapter after updating the cardViewItems list
                        cardViewAdapter.notifyDataSetChanged()
                    }
                })
        }
    }


    private fun convertTo12HourFormat(time24Hour: String): String {
        val timeParts = time24Hour.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1]
        val period = if (hour >= 12) "PM" else "AM"
        val hour12 = if (hour == 0 || hour == 12) 12 else hour % 12 // Handle midnight (0) and noon (12)
        return String.format("%02d:%s %s", hour12, minute, period)
    }



   private fun getMorningTime(scheduledTimes: List<ScheduledTimeDetails>): String {
        val morningTimes = mutableListOf<String>()
        scheduledTimes.forEach { scheduledTime ->
            val time24HourFormat = scheduledTime.medicineTime
            try {
                val timeParts = time24HourFormat.split(":")
                if (timeParts.size >= 2) {
                    val time24Hour = timeParts[0].toInt()
                    if (time24Hour in 6..11) {
                        val time12HourFormat = convertTo12HourFormat(time24HourFormat)
                        if (time12HourFormat.contains("AM")) {
                            morningTimes.add(time12HourFormat)
                        }
                    }
                }
            } catch (e: NumberFormatException) {
                // Handle invalid time format
                e.printStackTrace()
            }
        }
        return if (morningTimes.isNotEmpty()) morningTimes.first() else ""
    }


   private fun getEveningTime(scheduledTimes: List<ScheduledTimeDetails>): String {
        val eveningTimes = mutableListOf<String>()
        scheduledTimes.forEach { scheduledTime ->
            val time24HourFormat = scheduledTime.medicineTime
            try {
                val timeParts = time24HourFormat.split(":")
                if (timeParts.size >= 2) {
                    val time24Hour = timeParts[0].toInt()
                    if (time24Hour in 18..23) {
                        val time12HourFormat = convertTo12HourFormat(time24HourFormat)
                        if (time12HourFormat.contains("PM")) {
                            eveningTimes.add(time12HourFormat)
                        }
                    }
                }
            } catch (e: NumberFormatException) {
                // Handle invalid time format
                e.printStackTrace()
            }
        }
        return if (eveningTimes.isNotEmpty()) eveningTimes.last() else ""
    }

    private fun CalendarDay.toLocalDate(): LocalDate {
        return LocalDate.of(this.year, this.month + 1, this.day) // Note: CalendarDay month is 0-based, LocalDate is 1-based
    }

}
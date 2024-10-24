
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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.calmscient.Interface.CellClickListenerAppointments
import com.calmscient.R
import com.calmscient.adapters.NextAppointmentsAdapter
import com.calmscient.databinding.CalendarDayLayoutBinding
import com.calmscient.databinding.FragmentNextappointmentsBinding
import com.calmscient.di.remote.response.Appointment
import com.calmscient.di.remote.response.AppointmentDetails
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomCalendarDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SavePreferences
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.utils.getColorCompat
import com.calmscient.utils.network.ServerTimeoutHandler
import com.calmscient.viewmodels.AppointmentDetailsViewModel
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
import java.time.LocalDateTime
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale



data class CardViewItems(
    val dateview1: String,
    val doctor_logo: Int?,
    var DoctorNameTextView: String?,
    var noAppointmentsTextView: String?,
    var HosptailNameTextView: String?,
    val img_arrow1: Int?,
    var appointmentDetails: AppointmentDetails?
)
class NextAppointmentsFragment : Fragment() , CellClickListenerAppointments, CustomCalendarDialog.OnDateSelectedListener{
    private lateinit var binding: FragmentNextappointmentsBinding
    private var selectedDate = LocalDate.now()
    private val dateFormatter = DateTimeFormatter.ofPattern("dd")
    private lateinit var cardViewAdapter: NextAppointmentsAdapter
    private val cardViewItems = mutableListOf<CardViewItems>()


    private var loginResponse : LoginResponse? = null
    private val appointmentDetailsViewModel : AppointmentDetailsViewModel by activityViewModels()

    private lateinit var commonDialog: CommonAPICallDialog
    private lateinit var customProgressDialog: CustomProgressDialog
    private  var appointmentDetailsResponseData: List<Appointment> = emptyList()


    override fun onCellClickListener(position: Int, item: CardViewItems) {
        // Handle item click event here
        // Create an instance of MedicationDetailFragment and pass the data to it
        val medicationDetailFragment = item.appointmentDetails?.let {
            AppointmentdetailsFragment.newInstance(
                it
            )
        }
        // Load the MedicationDetailFragment
        if (medicationDetailFragment != null) {
            loadFragment(medicationDetailFragment)
        }
        else
        {
            Toast.makeText(requireContext(),getString(R.string.no_appointments),Toast.LENGTH_SHORT).show()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this){
            if(CommonClass.isNetworkAvailable(requireContext()))
            {
                loadFragment(MedicalRecordsFragment())
            }
            else
            {
                CommonClass.showInternetDialogue(requireContext())
            }
        }

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNextappointmentsBinding.inflate(inflater, container, false)
        binding.mainlayout.setOnClickListener{
            loadFragment(AppointmentdetailsFragment())
        }
        binding.backIcon.setOnClickListener {
            loadFragment(MedicalRecordsFragment())
        }
        binding.plusIcon.setOnClickListener {
            loadFragment(AddAppointmentFragment())
        }


        customProgressDialog = CustomProgressDialog(requireContext())
        commonDialog = CommonAPICallDialog(requireContext())

        val jsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString<LoginResponse>(jsonString)

        if(CommonClass.isNetworkAvailable(requireContext()))
        {
            observeViewModel()
        }
        else
        {
            CommonClass.showInternetDialogue(requireContext())
        }

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
                appointmentDetailsViewModel.clear()
                apiCall(selectedDate.toString())
            }

            dialog.setOnOkClickListener {
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

        class DayViewContainer(view: View, private val onDayClicked: (LocalDate) -> Unit) : ViewContainer(view) {
            val bind = CalendarDayLayoutBinding.bind(view)
            lateinit var day: WeekDay

            init {
                view.setOnClickListener {
                    if (selectedDate != day.date) {
                        val oldDate = selectedDate
                        selectedDate = day.date
                        binding.exSevenCalendar.notifyDateChanged(day.date)
                        oldDate?.let { binding.exSevenCalendar.notifyDateChanged(it) }
                        onDayClicked(selectedDate) // Trigger callback when day is clicked
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
            override fun create(view: View) = DayViewContainer(view) { selectedDate ->
                this@NextAppointmentsFragment.selectedDate = selectedDate
                displayCardViewsForSelectedDate()
            }

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
        binding.recyclerViewAppointments.layoutManager = LinearLayoutManager(requireContext())
        cardViewAdapter = context?.let { NextAppointmentsAdapter(it,cardViewItems) }!!
        cardViewAdapter.setCellClickListener(this)
        binding.recyclerViewAppointments.adapter = cardViewAdapter


        // Call this function when the date is selected
        // displayCardViewsForSelectedDate()


    }
    /* private fun displayCardViewsForSelectedDate() {
         cardViewItems.clear()

         // Get the selected date
         val selectedDate = selectedDate

         // Format the selected date as "MM/dd/yyyy" and set it as the first dateview1
         val dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
         val dateStr = selectedDate.format(dateFormatter)
         val doctorName = "Dr.Hannah Johnson"
         val hospitalName = "SCD Hospital"
         cardViewItems.add(
             CardViewItems(
                 dateStr,
                 R.drawable.ic_doctor_logo,
                 "Dr.Hannah Johnson",
                 null,
                 "SCD Hospital",
                 R.drawable.ic_next
             )
         )

         // Calculate and set the next 6 days' dates as dateview1
         for (i in 1..6) {
             val nextDate = selectedDate.plusDays(i.toLong())
             val nextDateStr = nextDate.format(dateFormatter)
             cardViewItems.add(
                 CardViewItems(
                     nextDateStr,
                     R.drawable.ic_appointments,
                     null,
                     getString(R.string.no_appointments),
                     null,
                     null
                 )
             )
         }

         // Set visibility to GONE for DoctorNameTextView and HosptailNameTextView and noAppointmentsTextView
         cardViewItems.forEach { item ->
             if (item.DoctorNameTextView.isNullOrEmpty()) {
                 item.DoctorNameTextView = null
             }
             if (item.HosptailNameTextView.isNullOrEmpty()) {
                 item.HosptailNameTextView = null
             }
             if (item.noAppointmentsTextView.isNullOrEmpty()) {
                 item.noAppointmentsTextView = null
             }
         }
         cardViewAdapter.notifyDataSetChanged()
     }*/

    private fun apiCall(selectedDate: String){

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

        appointmentDetailsViewModel.clear()

        loginResponse?.loginDetails?.let {
            //appointmentDetailsViewModel.getAppointmentDetails(4,4,1,"05/04/2024","05/09/2024",loginResponse!!.token.access_token)
            appointmentDetailsViewModel.getAppointmentDetails(it.patientLocationID,it.patientID,it.clientID,formattedSevenDaysBefore, formattedSelectedDate,loginResponse!!.token.access_token)
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
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null) // This ensures that the previous fragment is added to the back stack
        transaction.commit()
    }

    private fun observeViewModel()
    {
        // Get today's date
        val today = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MM/dd/yyyy")
        val todayDate = dateFormat.format(today.time)

        // Calculate 7 days before today
        val sevenDaysBefore = Calendar.getInstance()
        sevenDaysBefore.add(Calendar.DAY_OF_YEAR, -7)
        val formattedSevenDaysBefore = dateFormat.format(sevenDaysBefore.time)

        appointmentDetailsViewModel.clear()

        loginResponse?.loginDetails?.let {
            //appointmentDetailsViewModel.getAppointmentDetails(4,4,1,"05/04/2024","05/09/2024",loginResponse!!.token.access_token)
            appointmentDetailsViewModel.getAppointmentDetails(it.patientLocationID,it.patientID,it.clientID,formattedSevenDaysBefore,todayDate,loginResponse!!.token.access_token)
        }

        appointmentDetailsViewModel.loadingLiveData.observe(viewLifecycleOwner, Observer { isLoading->
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

        appointmentDetailsViewModel.successLiveData.observe(viewLifecycleOwner, Observer { isSuccess->
            if(isSuccess)
            {
                ServerTimeoutHandler.clearRetryListener()
                ServerTimeoutHandler.dismissDialog()
                appointmentDetailsViewModel.saveResponseLiveData.observe(viewLifecycleOwner, Observer { successDate->
                    if(successDate != null)
                    {
                        appointmentDetailsResponseData = successDate.appointmentDetailsList

                        /*cardViewItems.clear()

                        // Populate cardViewItems with appointment details
                        successDate.appointmentDetailsList.forEach { appointment ->
                            appointment.appointmentDetailsByDate.forEach { appointmentByDate ->
                                appointmentByDate.appointmentDetails.apply {
                                    cardViewItems.add(
                                        CardViewItems(
                                            dateAndTime,
                                            R.drawable.ic_doctor_logo,
                                            providerName,
                                            null,
                                            hospitalName,
                                            R.drawable.ic_next
                                        )
                                    )
                                }
                            }
                        }*/

                        //cardViewAdapter.notifyDataSetChanged()
                    }
                })

                displayCardViewsForSelectedDate()
            }
            else
            {
                appointmentDetailsViewModel.failureLiveData.observe(viewLifecycleOwner, Observer { failureData->
                    failureData?.let {
                        commonDialog.dismiss()
                        commonDialog.showDialog(it,R.drawable.ic_failure)
                    }
                })
            }
        })

        appointmentDetailsViewModel.errorLiveData.observe(viewLifecycleOwner, Observer { failureMessage ->
            failureMessage?.let {
                if (CommonClass.isNetworkAvailable(requireContext())) {
                    ServerTimeoutHandler.handleTimeoutException(requireContext()) {
                        // Retry logic when the retry button is clicked

                        appointmentDetailsViewModel.retryGetAppointmentDetails()
                    }
                } else {
                    CommonClass.showInternetDialogue(requireContext())
                }
            }
        })
    }


    @SuppressLint("NotifyDataSetChanged")
    internal fun displayCardViewsForSelectedDate() {
        cardViewItems.clear()

        // Get the selected date
        val selectedDate = selectedDate

        // Format the selected date as "MM/dd/yyyy"
        val dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        val dateFormatterResponse = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        // Calculate and set the next 6 days' dates as dateview1
        for (i in 0 until 7) { // Iterate for 7 days including the selected date
            val currentDate = selectedDate.plusDays(i.toLong())
            val currentDateStr = currentDate.format(dateFormatter)

            // Check if there are appointments for the current date
            val appointmentsForDate = appointmentDetailsResponseData.filter { appointment ->
                val appointmentDate = LocalDate.parse(appointment.date.substring(0, 10))
                appointmentDate.isEqual(currentDate)
            }

            if (appointmentsForDate.isNotEmpty()) {
                // If appointments are available, add them to cardViewItems
                appointmentsForDate.forEach { appointment ->
                    appointment.appointmentDetailsByDate.forEach { appointmentByDate ->
                        appointmentByDate.appointmentDetails.apply {
                            val formattedDate = LocalDateTime.parse(dateAndTime, dateFormatterResponse).format(dateFormatter)
                            cardViewItems.add(
                                CardViewItems(
                                    formattedDate, // Use formatted date
                                    R.drawable.ic_doctor_logo,
                                    providerName,
                                    null,
                                    hospitalName,
                                    R.drawable.ic_next, // Set the arrow when appointments are available
                                    appointmentByDate.appointmentDetails
                                )
                            )
                        }
                    }
                }
            } else {
                // If no appointments are available, add default data with null arrow
                cardViewItems.add(
                    CardViewItems(
                        currentDateStr,
                        R.drawable.ic_appointments,
                        null,
                        getString(R.string.no_appointments),
                        null,
                        null, // Set the arrow to null when no appointments are available
                        null
                    )
                )
            }
        }

        // Notify the adapter that the data set has changed
        cardViewAdapter.notifyDataSetChanged()
    }

    fun CalendarDay.toLocalDate(): LocalDate {
        return LocalDate.of(this.year, this.month + 1, this.day) // Note: CalendarDay month is 0-based, LocalDate is 1-based
    }

}

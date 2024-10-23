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

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.calmscient.Interface.OnAlarmSelectedListenerUpdate
import com.calmscient.R
import com.calmscient.databinding.FragmnetUpdateBottomSheetBinding
import com.calmscient.di.remote.request.AlarmUpdateRequest
import com.calmscient.di.remote.request.AlarmUpdateRequestInternal
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.di.remote.response.ScheduledTimeDetails
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UpdateMedicationDetailsBottomSheetFragment(private val selectedSchedule: String?, private val scheduleTiming: ScheduledTimeDetails?, private val medicationId:Int): BottomSheetDialogFragment() {

    private var onAlarmSelectedListener: OnAlarmSelectedListenerUpdate? = null

    lateinit var save: ImageView
    private lateinit var binding: FragmnetUpdateBottomSheetBinding
    private val selectedTextViews: MutableList<TextView> = mutableListOf()
    private var selectedTimeInterval: TextView? = null
    private lateinit var commonDialog: CommonAPICallDialog

    private lateinit var textFive: TextView
    private lateinit var textTen: TextView
    private lateinit var textFifteen: TextView
    private lateinit var textTwenty: TextView
    private lateinit var textTwentyFive: TextView
    private lateinit var textThirty: TextView

    private lateinit var sundayText: TextView
    private  lateinit var mondayText: TextView
    private lateinit var tuesdayText: TextView
    private lateinit var wednesdayText: TextView
    private lateinit var thursdayText: TextView
    private lateinit var fridayText: TextView
    private  lateinit var saturdayText: TextView

    private var loginResponse : LoginResponse? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        commonDialog = CommonAPICallDialog(requireContext())

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmnetUpdateBottomSheetBinding.inflate(inflater, container, false)


        val jsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
         loginResponse = JsonUtil.fromJsonString<LoginResponse>(jsonString)

        // Initialize all TextViews representing time intervals or days using view binding
        textFive = binding.textFive
        textTen = binding.textTen
        textFifteen = binding.textFifteen
        textTwenty = binding.textTwenty
        textTwentyFive = binding.textTwentyfive
        textThirty = binding.textThirty

        sundayText = binding.textSunday
        mondayText = binding.textMonday
        tuesdayText = binding.textTuesday
        wednesdayText = binding.textWednesday
        thursdayText = binding.textThuresady
        fridayText = binding.textFriday
        saturdayText = binding.textSaturday


        // Set click listener for all TextViews
        val clickListener = View.OnClickListener { clickedView ->
            // Toggle the selection of the clicked TextView
            toggleSelection(clickedView as TextView)
        }


        // Set click listener for all TextViews
        textFive.setOnClickListener(clickListener)
        textTen.setOnClickListener(clickListener)
        textFifteen.setOnClickListener(clickListener)
        textTwenty.setOnClickListener(clickListener)
        textTwentyFive.setOnClickListener(clickListener)
        textThirty.setOnClickListener(clickListener)

        sundayText.setOnClickListener(clickListener)
        mondayText.setOnClickListener(clickListener)
        tuesdayText.setOnClickListener(clickListener)
        wednesdayText.setOnClickListener(clickListener)
        thursdayText.setOnClickListener(clickListener)
        fridayText.setOnClickListener(clickListener)
        saturdayText.setOnClickListener(clickListener)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Extract data from the API response
        val alarmTime = scheduleTiming?.medicineTime
        val alarmInterval =  scheduleTiming?.alarmInterval?.toInt()
        val repeatDays = scheduleTiming?.repeat

        // Set the time in the TimePicker
        if (alarmTime != null) {
            setTimeInTimePicker(alarmTime)
        }

        // Set the selected alarm interval
        if (alarmInterval != null) {
            setSelectedInterval(alarmInterval)
        }

        // Set the selected repeat days
        if (repeatDays != null) {
            setSelectedDays(repeatDays)
        }


        binding.imgSave.setOnClickListener {
            val selectedTime = getTimeFromTimePicker()
            val selectedDays = getSelectedDays()

            val sTime = getTimeFromTimePickerAMPM()

            val today = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("MM/dd/yyyy")
            val todayDate = dateFormat.format(today.time)

            if (selectedDays.isEmpty()) {
                commonDialog.showDialog(getString(R.string.please_select_at_least_one_day),R.drawable.ic_alret)
                return@setOnClickListener
            }

            // Get the selected schedule from the parent fragment (AddMedicationsFragment)
            val selectedSchedule = selectedSchedule

            // Check if the selected time is valid for the selected schedule
            val isValidTime = isTimeValidForSchedule(sTime, selectedSchedule)

            // If the time is not valid for the selected schedule, show an error message
            if (!isValidTime) {
                Toast.makeText(requireContext(), "Selected time does not match the selected schedule", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }



            val alarm = loginResponse?.loginDetails?.let { it1 ->
                val alarmId = scheduleTiming?.alarmId ?: 0
                val flag = if (scheduleTiming != null) "U" else "I"

                AlarmUpdateRequestInternal(
                    alarmId = alarmId,
                    alarmDate = todayDate,
                    repeat = selectedDays,
                    alarmInterval = getSelectedIntervals().firstOrNull() ?: 10,
                    flag = flag,
                    isEnabled = null,
                    medicineTime = selectedTime,
                    plId = it1.patientLocationID,
                    medicationId = medicationId,
                    scheduleType = selectedSchedule
                )
            }
            // Pass the alarm object to the listener
            //onAlarmSelectedListener?.onAlarmSelected(alarm)
            if (alarm != null) {
                Log.d("Update in Bottom ","$alarm")
                onAlarmSelectedListener?.onAlarmSelected(alarm)
            }

            Log.d("Updated Alarm","$alarm")
            //Toast.makeText(requireContext(),"$alarm",Toast.LENGTH_LONG).show()

            dismiss()
        }



    }

    private fun toggleSelection(textView: TextView) {
        val clickedText = textView.text.toString()
        Log.d("ClickedText", "Clicked: $clickedText")
        Log.d("ToggleSelection", "Clicked TextView ID: ${textView.id}, Text: ${textView.text}")

        if (textView.id in listOf(R.id.text_five, R.id.text_ten, R.id.text_fifteen, R.id.text_twenty, R.id.text_twentyfive, R.id.text_thirty)) {
            // If a time interval TextView is clicked
            if (selectedTimeInterval == textView) {
                // If the clicked TextView is already selected, deselect it
                textView.setBackgroundResource(R.drawable.circle_background)
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_100))
                selectedTimeInterval = null
                textView.isSelected = false // Explicitly set isSelected to false
                textView.elevation = 0f // Remove elevation
            } else {
                // Deselect the previously selected time interval
                selectedTimeInterval?.setBackgroundResource(R.drawable.circle_background)
                selectedTimeInterval?.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_100))
                // Select the clicked TextView
                textView.setBackgroundResource(R.drawable.selected_circle_background)
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                selectedTimeInterval = textView
                textView.isSelected = true // Explicitly set isSelected to true
                textView.elevation = resources.getDimension(com.intuit.sdp.R.dimen._4sdp) // Apply elevation when selected
            }
        } else {
            // If a day TextView is clicked
            if (selectedTextViews.contains(textView)) {
                // If the TextView is already selected, deselect it
                textView.setBackgroundResource(R.drawable.circle_background)
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_100))
                selectedTextViews.remove(textView)
                textView.isSelected = false // Explicitly set isSelected to false
                textView.elevation = 0f // Remove elevation
            } else {
                // If the TextView is not selected, select it
                textView.setBackgroundResource(R.drawable.selected_circle_background)
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                selectedTextViews.add(textView)
                textView.isSelected = true // Explicitly set isSelected to true
                textView.elevation = resources.getDimension(com.intuit.sdp.R.dimen._4sdp) // Apply elevation when selected
            }
        }
    }

    private fun getSelectedIntervals(): List<Int> {
        val selectedIntervals = mutableListOf<Int>()

        // Update selected intervals based on current selections
        if (textFive.isSelected) {
            selectedIntervals.clear()
            selectedIntervals.add(5)
        }
        if (textTen.isSelected) {
            selectedIntervals.clear()
            selectedIntervals.add(10)
        }
        if (textFifteen.isSelected) {
            selectedIntervals.clear()
            selectedIntervals.add(15)
        }
        if (textTwenty.isSelected) {
            selectedIntervals.clear()
            selectedIntervals.add(20)
        }
        if (textTwentyFive.isSelected) {
            selectedIntervals.clear()
            selectedIntervals.add(25)
        }
        if (textThirty.isSelected) {
            selectedIntervals.clear()
            selectedIntervals.add(30)
        }

        return selectedIntervals
    }


    private fun getSelectedDays(): List<String> {
        val selectedDays = mutableListOf<String>()
        if (sundayText.isSelected) selectedDays.add("Sun")
        if (mondayText.isSelected) selectedDays.add("Mon")
        if (tuesdayText.isSelected) selectedDays.add("Tue")
        if (wednesdayText.isSelected) selectedDays.add("Wed")
        if (thursdayText.isSelected) selectedDays.add("Thu")
        if (fridayText.isSelected) selectedDays.add("Fri")
        if (saturdayText.isSelected) selectedDays.add("Sat")
        // Add more days as needed

        Log.d("GetSelectedDays", "Is Sunday selected: ${sundayText.isSelected}")

        return selectedDays
    }


    /*private fun getTimeFromTimePicker(): String {
        val hour = binding.timeTimePicker.hour
        val minute = binding.timeTimePicker.minute
        return String.format("%02d:%02d", hour, minute)
    }*/

    private fun getTimeFromTimePicker(): String {
        val hour = binding.timeTimePicker.hour
        val minute = binding.timeTimePicker.minute
        val second = 0
        return String.format("%02d:%02d:%02d", hour, minute, second)
    }





    private fun getTimeFromTimePickerAMPM(): String {
        val hour = binding.timeTimePicker.hour
        val minute = binding.timeTimePicker.minute
        val amPm = if (hour < 12) "AM" else "PM"
        val hour12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        return String.format("%02d:%02d %s", hour12, minute, amPm)
    }


    private fun addMinutesToTime(time: String, minutes: Int): String {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.time = formatter.parse(time)!!
        calendar.add(Calendar.MINUTE, minutes)
        return formatter.format(calendar.time)
    }

    fun setOnAlarmSelectedListener(listener: OnAlarmSelectedListenerUpdate) {
        onAlarmSelectedListener = listener
    }

    private fun isTimeValidForSchedule(selectedTime: String, selectedSchedule: String?): Boolean {
        // Determine the time range associated with the selected schedule
        val timeRange = when (selectedSchedule) {
            getString(R.string.morning) -> Pair("06:00 AM", "12:00 PM") // Example time range for morning
            getString(R.string.afternoon) -> Pair("12:00 PM", "06:00 PM") // Example time range for afternoon
            getString(R.string.evening) -> Pair("06:00 PM", "11:59 PM") // Example time range for evening
            else -> null
        }

        // Check if the selected time falls within the time range associated with the selected schedule
        return timeRange?.let { (startTime, endTime) ->
            val formatter = SimpleDateFormat("hh:mm a", Locale.US)
            val selectedTimeFormatted = formatter.parse(selectedTime)
            val startFormatted = formatter.parse(startTime)
            val endFormatted = formatter.parse(endTime)

            selectedTimeFormatted?.let { selected ->
                selected >= startFormatted && selected <= endFormatted
            } ?: false
        } ?: false
    }

    private fun setTimeInTimePicker(time: String) {
        // Extract hour and minute from the time string
        val (hour, minute) = time.split(":").map { it.toInt() }
        // Set the hour and minute in the TimePicker
        binding.timeTimePicker.hour = hour
        binding.timeTimePicker.minute = minute
    }

    private fun setSelectedInterval(interval: Int) {
        // Check the TextView corresponding to the selected interval and highlight it
        when (interval) {
            5 -> toggleSelection(binding.textFive)
            10 -> toggleSelection(binding.textTen)
            15 -> toggleSelection(binding.textFifteen)
            20 -> toggleSelection(binding.textTwenty)
            25 -> toggleSelection(binding.textTwentyfive)
            30 -> toggleSelection(binding.textThirty)
            // Add more cases for other intervals if needed
        }
    }

    private fun setSelectedDays(days: List<String>) {
        // Iterate through the list of days and select the corresponding TextViews
        for (day in days) {
            // Convert both the input day and the days in the list to lowercase for case-insensitive comparison
            when (day.lowercase(Locale.ROOT)) {
                "sun" -> toggleSelection(binding.textSunday)
                "mon" -> toggleSelection(binding.textMonday)
                "tue" -> toggleSelection(binding.textTuesday)
                "wed" -> toggleSelection(binding.textWednesday)
                "thu" -> toggleSelection(binding.textThuresady)
                "fri" -> toggleSelection(binding.textFriday)
                "sat" -> toggleSelection(binding.textSaturday)
                // Add more cases for other days if needed
            }
        }
    }


}

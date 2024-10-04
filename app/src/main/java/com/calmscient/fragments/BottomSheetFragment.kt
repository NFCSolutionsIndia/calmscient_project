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
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import com.calmscient.Interface.OnAlarmSelectedListener
import com.calmscient.R
import com.calmscient.databinding.FragmentAddMedicationsBinding
import com.calmscient.databinding.FragmentBottomSheetBinding
import com.calmscient.di.remote.request.Alarm
import com.calmscient.di.remote.request.AlarmInternal
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.common.CommonClass
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BottomSheetFragment(private val selectedSchedule: String?): BottomSheetDialogFragment() {

    private var onAlarmSelectedListener: OnAlarmSelectedListener? = null

    lateinit var save: ImageView
    private lateinit var binding: FragmentBottomSheetBinding
    private val selectedTextViews: MutableList<TextView> = mutableListOf()
    private var selectedTimeInterval: TextView? = null
    private lateinit var commonDialog: CommonAPICallDialog

    lateinit var textFive: TextView
    lateinit var textTen: TextView
    lateinit var textFifteen: TextView
    lateinit var textTwenty: TextView
    lateinit var textTwentyFive: TextView
    lateinit var textThirty: TextView

    lateinit var sundayText: TextView
    lateinit var mondayText: TextView
    lateinit var tuesdayText: TextView
    lateinit var wednesdayText: TextView
    lateinit var thursdayText: TextView
    lateinit var fridayText: TextView
    lateinit var saturdayText: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        commonDialog = CommonAPICallDialog(requireContext())

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBottomSheetBinding.inflate(inflater, container, false)


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



        binding.imgSave.setOnClickListener {
            val selectedTime = getTimeFromTimePicker()
            val selectedDays = getSelectedDays()

            val sTime = getTimeFromTimePickerAMPM()

            val today = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("MM/dd/yyyy")
            val todayDate = dateFormat.format(today.time)

            if (selectedDays.isEmpty()) {
                commonDialog.showDialog(getString(R.string.please_select_at_least_one_day))
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



            val alarm = AlarmInternal(
                alarmId = 0,
                alarmDate = todayDate,
                repeat = selectedDays,
                alarmInterval = getSelectedIntervals().firstOrNull() ?: 10,
                flag = "I",
                isEnabled = null,
                medicineTime = selectedTime,
                scheduleType = selectedSchedule
            )
            // Pass the alarm object to the listener
            if (selectedSchedule != null) {
                onAlarmSelectedListener?.onAlarmSelected(alarm)
            }


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
        if (binding.textFive.isSelected) {
            selectedIntervals.clear()
            selectedIntervals.add(5)
        }
        if (binding.textTen.isSelected) {
            selectedIntervals.clear()
            selectedIntervals.add(10)
        }
        if (binding.textFifteen.isSelected) {
            selectedIntervals.clear()
            selectedIntervals.add(15)
        }
        if (binding.textTwenty.isSelected) {
            selectedIntervals.clear()
            selectedIntervals.add(20)
        }
        if (binding.textTwentyfive.isSelected) {
            selectedIntervals.clear()
            selectedIntervals.add(25)
        }
        if (binding.textThirty.isSelected) {
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

    fun setOnAlarmSelectedListener(listener: OnAlarmSelectedListener) {
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


}

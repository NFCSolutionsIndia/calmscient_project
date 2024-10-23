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

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.calmscient.Interface.OnSelectionDateChangeListener
import com.calmscient.R
import com.calmscient.databinding.FragmentTakingControlMakeAPlanScreenFourBinding
import com.calmscient.di.remote.response.GetAlcoholFreeDayResponse
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomCalendarView
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.GetAlcoholFreeDaysViewModel
import com.calmscient.viewmodels.SaveAlcoholFreeDaysViewModel
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TakingControlMakeAPlanScreenFourFragment : Fragment() , OnSelectionDateChangeListener
{

    private lateinit var binding: FragmentTakingControlMakeAPlanScreenFourBinding
    private lateinit var customCalendarView: CustomCalendarView
    private val selectedMonths = mutableSetOf<TextView>()

    private val saveAlcoholFreeDaysViewModel : SaveAlcoholFreeDaysViewModel by activityViewModels()
    private val getAlcoholFreeDaysViewModel : GetAlcoholFreeDaysViewModel by activityViewModels()

    private var getAlcoholFreeDayResponse: GetAlcoholFreeDayResponse? = null

    private var loginResponse : LoginResponse? = null
    private  lateinit var accessToken : String
    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonDialog: CommonAPICallDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            loadFragment(TakingControlMakeAPlanScreenTwoFragment())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTakingControlMakeAPlanScreenFourBinding.inflate(inflater, container, false)

        val months = listOf(
            binding.jan, binding.feb, binding.march, binding.april, binding.may, binding.jun,
            binding.july, binding.aug, binding.sep, binding.oct, binding.nov, binding.dec
        )

        customProgressDialog = CustomProgressDialog(requireContext())
        commonDialog = CommonAPICallDialog(requireContext())

        val jsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString<LoginResponse>(jsonString)

        accessToken = SharedPreferencesUtil.getData(requireContext(), "accessToken", "")


        customCalendarView = binding.customCalendarView
        customCalendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_MULTIPLE)
        customCalendarView.setOnSelectionChangeListener(this)
        binding.tvFreeDaysCount.text = customCalendarView.getSelectedDatesSize().toString()
        // Lock the calendar to the current month
        customCalendarView.lockToCurrentMonth()

        binding.backIcon.setOnClickListener {
            loadFragment(TakingControlMakeAPlanScreenTwoFragment())
        }

        binding.previousQuestion.setOnClickListener{
           loadFragment(TakingControlMakeAPlanScreenTwoFragment())
        }

        binding.nextQuestion.setOnClickListener{
            loadFragment(TakingControlMakeAPlanScreenFiveFragment())
        }

        setupMonthSelection(months)

        if(CommonClass.isNetworkAvailable(requireContext()))
        {
            getAlcoholFreeDaysApiCall()
        }
        else{
            CommonClass.showInternetDialogue(requireContext())
        }

        binding.btnSet.setOnClickListener {
            val selectedDates = customCalendarView.getFormattedSelectedDates()
            val selectedMonthsList = getSelectedMonths()


            if(CommonClass.isNetworkAvailable(requireContext()))
            {

                saveAlcoholFreeDaysApiCall()
            }
            else{
                CommonClass.showInternetDialogue(requireContext())
            }
        }


        return binding.root
    }

    private fun setupMonthSelection(months: List<TextView>) {


        months.forEach { monthTextView ->
            monthTextView.setOnClickListener {
                toggleMonthSelection(monthTextView)
            }
        }

        binding.all.setOnClickListener {
            if (selectedMonths.size == months.size) {
                clearAllSelections(months)
            } else {
                selectAllMonths(months)
            }
        }
    }

    private fun toggleMonthSelection(textView: TextView) {
        if (selectedMonths.contains(textView)) {
            deselectMonth(textView)
        } else {
            selectMonth(textView)
        }
    }

    private fun selectMonth(textView: TextView) {
        selectedMonths.add(textView)
        textView.setBackgroundResource(R.drawable.card_selected_background)
        textView.setTextColor(Color.WHITE)
    }

    private fun deselectMonth(textView: TextView) {
        selectedMonths.remove(textView)
        textView.setBackgroundResource(R.drawable.card_default_background)
        textView.setTextColor(Color.parseColor("#424242"))
    }

    private fun clearAllSelections(months: List<TextView>) {
        selectedMonths.clear()
        months.forEach { monthTextView ->
            deselectMonth(monthTextView)
        }
    }

    private fun selectAllMonths(months: List<TextView>) {
        months.forEach { monthTextView ->
            selectMonth(monthTextView)
        }
    }

    private fun getSelectedMonths(): List<String> {
        return selectedMonths.map { textView ->
            textView.text.toString()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onSelectionChanged(count: Int) {
        binding.tvFreeDaysCount.text = count.toString()
    }

    private fun getAlcoholFreeDaysApiCall()
    {

        getAlcoholFreeDaysViewModel.clear()
        loginResponse?.loginDetails?.let { getAlcoholFreeDaysViewModel.getAlcoholFreeDays(it.patientID,accessToken) }
        observeGetAlcoholFreeDays()
    }

    private fun observeGetAlcoholFreeDays()
    {

        getAlcoholFreeDaysViewModel.loadingLiveData.observe(viewLifecycleOwner, Observer { isLoading->
            if(isLoading)
            {
                customProgressDialog.show(getString(R.string.loading))
            }
            else{
                customProgressDialog.dialogDismiss()
            }
        })

        getAlcoholFreeDaysViewModel.successLiveData.observe(viewLifecycleOwner, Observer { isSuccess->
            if(isSuccess){
                getAlcoholFreeDaysViewModel.saveResponseLiveData.observe(viewLifecycleOwner, Observer { successData->
                        if(successData != null)
                        {
                            getAlcoholFreeDayResponse = successData
                            //bind the data to the UI like selected months and dates
                            selectedMonths.clear()
                            bindDataToUI()
                        }
                    }
                )
            }
        })
    }

    private fun bindDataToUI() {
        // Pre-select the dates in the CustomCalendarView
        val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        val dates = getAlcoholFreeDayResponse?.dates?.map { dateStr ->
            dateFormatter.parse(dateStr)
        }
        if (dates != null) {
            customCalendarView.setSelectedDates(dates)
        }

        binding.tvFreeDaysCount.text = getAlcoholFreeDayResponse?.dates?.count {
            val date = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).parse(it)
            val calendar = Calendar.getInstance().apply {
                if (date != null) {
                    time = date
                }
            }
            calendar.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) &&
                    calendar.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)
        }.toString()


        // Pre-select the months in the UI
        val monthMap = mapOf(
            getString(R.string.jan) to binding.jan,
            getString(R.string.feb) to binding.feb,
            getString(R.string.mar) to binding.march,
            getString(R.string.apr) to binding.april,
            getString(R.string.may) to binding.may,
            getString(R.string.jun) to binding.jun,
            getString(R.string.jul) to binding.july,
            getString(R.string.aug) to binding.aug,
            getString(R.string.sep) to binding.sep,
            getString(R.string.oct) to binding.oct,
            getString(R.string.nov) to binding.nov,
            getString(R.string.dec) to binding.dec
        )

        getAlcoholFreeDayResponse?.months?.forEach { month ->
            monthMap[month]?.let { selectMonth(it) }
        }
    }

    private fun saveAlcoholFreeDaysApiCall()
    {
        saveAlcoholFreeDaysViewModel.clear()
        val loginDetails = loginResponse?.loginDetails
        val selectedDates = customCalendarView.getFormattedSelectedDates()
        val selectedMonthsList = getSelectedMonths()
        var pvcFlag = ""
           if(getAlcoholFreeDayResponse != null){
               pvcFlag = if(getAlcoholFreeDayResponse!!.dates.isEmpty() || getAlcoholFreeDayResponse!!.months.isEmpty()){
                   "I"
               } else {
                   "U"
               }
           }
        val updatedSelectedDates = updateDatesForSelectedMonths(selectedDates, selectedMonthsList)

        Log.d("Final Dates :","$updatedSelectedDates")
        if (loginDetails != null) {
            if(updatedSelectedDates.isNotEmpty()) {
                saveAlcoholFreeDaysViewModel.saveAlcoholFreeDays(
                    loginDetails.clientID,
                    updatedSelectedDates,
                    0,
                    selectedMonthsList,
                    loginDetails.patientID,
                    loginDetails.patientLocationID,
                    pvcFlag,
                    accessToken
                )
            }
            else{
                commonDialog.showDialog(getString(R.string.please_select_atleast_one_day_to_set_alcohol_free_days),R.drawable.ic_alret)
            }
        }
        observeViewModel()

    }

    /*private fun updateDatesForSelectedMonths(
        selectedDates: List<String>,
        selectedMonthsList: List<String>
    ): List<String> {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        val updatedDates = mutableListOf<String>()
        val calendar = Calendar.getInstance()

        // Extract existing months from selectedDates
        val existingMonths = selectedDates.mapNotNull {
            val date = dateFormat.parse(it)
            if (date != null) {
                calendar.time = date
                calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US)
            } else null
        }.toSet()

        // Remove dates for months not in selectedMonthsList
        selectedDates.forEach { dateString ->
            val date = dateFormat.parse(dateString)
            if (date != null) {
                calendar.time = date
                val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US)
                if (selectedMonthsList.contains(monthName)) {
                    updatedDates.add(dateString) // Keep the date if the month is in selectedMonthsList
                }
            }
        }

        // Add missing dates for months in selectedMonthsList
        for (month in selectedMonthsList) {
            if (!existingMonths.contains(month)) {
                // Get the index of the month
                val monthIndex = getMonthIndex(month)
                calendar.set(Calendar.MONTH, monthIndex)

                for (dateString in selectedDates) {
                    val originalDate = dateFormat.parse(dateString)
                    if (originalDate != null) {
                        calendar.set(Calendar.YEAR, originalDate.year + 1900)
                        calendar.set(Calendar.DAY_OF_MONTH, originalDate.date)
                        val newDateForMonth = dateFormat.format(calendar.time)
                        if (!updatedDates.contains(newDateForMonth)) {
                            updatedDates.add(newDateForMonth)
                        }
                    }
                }
            }
        }

        return updatedDates
    }*/

    private fun updateDatesForSelectedMonths(
        selectedDates: List<String>,
        selectedMonthsList: List<String>
    ): List<String> {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        val updatedDates = selectedDates.toMutableList() // Start with the original selected dates
        val calendar = Calendar.getInstance()

        // Extract existing months from selectedDates
        val existingMonths = selectedDates.mapNotNull {
            val date = dateFormat.parse(it)
            if (date != null) {
                calendar.time = date
                calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US)
            } else null
        }.toSet()

        // Loop through the selectedMonthsList to add missing dates
        for (month in selectedMonthsList) {
            if (!existingMonths.contains(month)) {
                // Get the index of the month
                val monthIndex = getMonthIndex(month)
                calendar.set(Calendar.MONTH, monthIndex)

                for (dateString in selectedDates) {
                    val originalDate = dateFormat.parse(dateString)
                    if (originalDate != null) {
                        // Retain the day and year from the original date
                        calendar.set(Calendar.YEAR, originalDate.year + 1900)
                        calendar.set(Calendar.DAY_OF_MONTH, originalDate.date)
                        val newDateForMonth = dateFormat.format(calendar.time)

                        // Add new dates for the month if not already present
                        if (!updatedDates.contains(newDateForMonth)) {
                            updatedDates.add(newDateForMonth)
                        }
                    }
                }
            }
        }

        return updatedDates
    }


    private fun getMonthIndex(month: String): Int {
        val monthMap = mapOf(
            getString(R.string.jan) to Calendar.JANUARY,
            getString(R.string.feb) to Calendar.FEBRUARY,
            getString(R.string.mar) to Calendar.MARCH,
            getString(R.string.apr) to Calendar.APRIL,
            getString(R.string.may) to Calendar.MAY,
            getString(R.string.jun) to Calendar.JUNE,
            getString(R.string.jul) to Calendar.JULY,
            getString(R.string.aug) to Calendar.AUGUST,
            getString(R.string.sep) to Calendar.SEPTEMBER,
            getString(R.string.oct) to Calendar.OCTOBER,
            getString(R.string.nov) to Calendar.NOVEMBER,
            getString(R.string.dec) to Calendar.DECEMBER
        )
        return monthMap[month] ?: Calendar.JANUARY
    }




    private fun observeViewModel()
    {
        saveAlcoholFreeDaysViewModel.loadingLiveData.observe(viewLifecycleOwner, Observer { isLoading->
            if(isLoading)
            {
                customProgressDialog.show(getString(R.string.loading))
            }
            else{
                customProgressDialog.dialogDismiss()
            }
        })

        saveAlcoholFreeDaysViewModel.successLiveData.observe(viewLifecycleOwner, Observer { isSuccess->
            if(isSuccess)
            {
                saveAlcoholFreeDaysViewModel.saveResponseLiveData.observe(viewLifecycleOwner,
                    Observer { successData->
                        if(successData != null)
                        {
                            commonDialog.showDialog(successData.responseMessage,R.drawable.ic_success_dialog)
                        }
                    }
                )
            }
        })
    }
}

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
import java.util.Locale

class TakingControlMakeAPlanScreenFourFragment : Fragment() , OnSelectionDateChangeListener
{

    private lateinit var binding: FragmentTakingControlMakeAPlanScreenFourBinding
    private lateinit var customCalendarView: CustomCalendarView
    private val selectedMonths = mutableSetOf<TextView>()

    private val saveAlcoholFreeDaysViewModel : SaveAlcoholFreeDaysViewModel by activityViewModels()
    private val getAlcoholFreeDaysViewModel : GetAlcoholFreeDaysViewModel by activityViewModels()

    private lateinit var getAlcoholFreeDayResponse: GetAlcoholFreeDayResponse

    private var loginResponse : LoginResponse? = null
    private  lateinit var accessToken : String
    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonDialog: CommonAPICallDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().supportFragmentManager.popBackStack()
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

        binding.backIcon.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.previousQuestion.setOnClickListener{
           loadFragment(TakingControlMakeAPlanScreenTwoFragment())
        }

        binding.nextQuestion.setOnClickListener{
            loadFragment(TakingControlMakeAPlanScreenFiveFragment())
        }

        setupMonthSelection(months)

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

        if(CommonClass.isNetworkAvailable(requireContext()))
        {
           getAlcoholFreeDaysApiCall()
        }
        else{
            CommonClass.showInternetDialogue(requireContext())
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
        val dates = getAlcoholFreeDayResponse.dates.map { dateStr ->
            dateFormatter.parse(dateStr)
        }
        customCalendarView.setSelectedDates(dates)

        binding.tvFreeDaysCount.text = getAlcoholFreeDayResponse.dates.size.toString()

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

        getAlcoholFreeDayResponse.months.forEach { month ->
            monthMap[month]?.let { selectMonth(it) }
        }
    }

    private fun saveAlcoholFreeDaysApiCall()
    {

        val loginDetails = loginResponse?.loginDetails
        val selectedDates = customCalendarView.getFormattedSelectedDates()
        val selectedMonthsList = getSelectedMonths()
        var pvcFlag = ""
        pvcFlag = if(getAlcoholFreeDayResponse.dates.isEmpty() || getAlcoholFreeDayResponse.months.isEmpty()){
            "I"
        } else {
            "U"
        }

        if (loginDetails != null) {
            saveAlcoholFreeDaysViewModel.saveAlcoholFreeDays(loginDetails.clientID,selectedDates,0,selectedMonthsList,loginDetails.patientID,loginDetails.patientLocationID,pvcFlag,accessToken)
        }
        observeViewModel()

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
                            commonDialog.showDialog(successData.responseMessage)
                        }
                    }
                )
            }
        })
    }
}

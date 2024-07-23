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
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.R
import com.calmscient.adapters.TakingControlSummaryCardAdapter
import com.calmscient.databinding.LayoutSummaryTakingControlBinding
import com.calmscient.di.remote.response.GetTakingControlSummaryResponse
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.di.remote.response.Summary
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomCalendarDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.GetTakingControlSummaryViewModel
import com.calmscient.viewmodels.UpdateTakingControlIndexViewModel
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class SummaryDataClass(
    val logoUrl: String?,
    val title: String?,
    val goalCount: String?,
    val countOrTimes: String?,
    val process: Int
)

class SummaryTakingControlFragment : Fragment(), CustomCalendarDialog.OnDateSelectedListener {
    private lateinit var binding: LayoutSummaryTakingControlBinding
    private lateinit var calenderView: ImageView
    private lateinit var monthText: TextView
    private lateinit var takingControlSummaryCardAdapter: TakingControlSummaryCardAdapter
    private val summaryDataClass = mutableListOf<SummaryDataClass>()

    private val getTakingControlSummaryViewModel : GetTakingControlSummaryViewModel by activityViewModels()
    private val updateTakingControlIndexViewModel: UpdateTakingControlIndexViewModel by activityViewModels()

    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonDialog: CommonAPICallDialog
    private  lateinit var accessToken : String
    private lateinit var getTakingControlSummaryResponse: GetTakingControlSummaryResponse
    private lateinit var loginResponse: LoginResponse

    private lateinit var recyclerView: RecyclerView
    private lateinit var selectedDate : String
    private var courseTempId = 0


    companion object {
        fun newInstance(
            courseId: Int
        ): SummaryTakingControlFragment {
            val fragment = SummaryTakingControlFragment()
            val args = Bundle()
            args.putInt("courseId",courseId)

            fragment.arguments = args
            return fragment
        }
    }

    override fun onDateSelected(date: CalendarDay) {

        val formattedDate = formatDate(date)

        selectedDate = formattedDate

        binding.tvDate.text = formattedDate
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this){
            loadFragment(TakingControlFragment())
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = LayoutSummaryTakingControlBinding.inflate(inflater, container, false)


        accessToken = SharedPreferencesUtil.getData(requireContext(), "accessToken", "")
        customProgressDialog = CustomProgressDialog(requireContext())
        commonDialog = CommonAPICallDialog(requireContext())

        courseTempId = arguments?.getInt("courseId")!!


        val loginJsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString<LoginResponse>(loginJsonString)

        recyclerView = binding.recyclerSummary
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        binding.backIcon.setOnClickListener {
            loadFragment(TakingControlFragment())
        }

        calenderView = binding.calenderView

        val currentDate = CalendarDay()
        val formattedDate = formatDate(currentDate)

        binding.tvDate.text = formattedDate

        selectedDate = formattedDate


        if(CommonClass.isNetworkAvailable(requireContext()))
        {
            getSummaryAPICall(formattedDate)
        }
        else{
            CommonClass.showInternetDialogue(requireContext())
        }

        binding.calenderView.setOnClickListener{
            val dialog = CustomCalendarDialog()
            dialog.setOnDateSelectedListener(this)
            dialog.show(parentFragmentManager, "CustomCalendarDialog")

             dialog.setOnOkClickListener {
                 getSummaryAPICall(selectedDate)
             }
        }

        binding.tvDate.setOnClickListener{
            val dialog = CustomCalendarDialog()
            dialog.setOnDateSelectedListener(this)
            dialog.show(parentFragmentManager, "CustomCalendarDialog")

            dialog.setOnOkClickListener {
                getSummaryAPICall(selectedDate)
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        takingControlSummaryCardAdapter = TakingControlSummaryCardAdapter(summaryDataClass)
        binding.recyclerSummary.adapter = takingControlSummaryCardAdapter



    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun getSummaryAPICall(formattedDate: String)
    {
        val resultDate = convertDateFormat(formattedDate)
        getTakingControlSummaryViewModel.getSummaryData(resultDate,loginResponse.loginDetails.patientID,accessToken)
        observeSummaryViewModel()
    }

    private fun observeSummaryViewModel()
    {
        getTakingControlSummaryViewModel.loadingLiveData.observe(viewLifecycleOwner, Observer { isLoading->
            if(isLoading)
            {
                customProgressDialog.show(getString(R.string.loading))
            }
            else{
                customProgressDialog.dialogDismiss()
            }
        })

        getTakingControlSummaryViewModel.successLiveData.observe(viewLifecycleOwner, Observer { isSuccess->
            if(isSuccess)
            {
                getTakingControlSummaryViewModel.saveResponseLiveData.observe(viewLifecycleOwner,
                    Observer { successData->
                        if(successData != null)
                        {
                            getTakingControlSummaryResponse = successData
                            updateSummaryData(getTakingControlSummaryResponse.summaryList)
                            updateIndexApiCall()
                        }
                    })
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateSummaryData(summaryList: List<Summary>) {
        summaryDataClass.clear()
        summaryList.forEach {
            val process = if (it.target != 0) (it.count * 100) / it.target else 0
            summaryDataClass.add(SummaryDataClass(
                logoUrl = it.url,
                title = it.iconName,
                goalCount = it.target.toString(),
                countOrTimes = it.count.toString(),
                process = process
            ))
        }
        takingControlSummaryCardAdapter.notifyDataSetChanged()
    }

    private fun formatDate(date: CalendarDay): String {
        val calendar = Calendar.getInstance()
        calendar.set(date.year, date.month , date.day)
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun convertDateFormat(inputDate: String): String {
        val inputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

        val date = inputFormat.parse(inputDate)

        return outputFormat.format(date!!)
    }

    private fun updateIndexApiCall() {
        val isCompleted = 1
        loginResponse.loginDetails.let {
            updateTakingControlIndexViewModel.updateTakingControlIndexData(
                it.clientID,
                courseTempId,
                isCompleted,
                it.patientID,
                it.patientLocationID,
                accessToken
            )
        }
    }

}
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.calmscient.R
import com.calmscient.adapters.JournalEntryDailyJournalAdapter
import com.calmscient.databinding.TabDailyJournalsFragmentBinding
import com.calmscient.di.remote.request.GetPatientJournalByPatientIdRequest
import com.calmscient.di.remote.response.DailyJournal
import com.calmscient.di.remote.response.GetPatientJournalByPatientIdResponse
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomCalendarDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.GetPatientJournalByPatientIdViewModel
import com.prolificinteractive.materialcalendarview.CalendarDay
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class DailyJournalTabFragment : Fragment(){

    private lateinit var binding: TabDailyJournalsFragmentBinding

    private val getPatientJournalByPatientIdViewModel: GetPatientJournalByPatientIdViewModel by viewModels()
    private lateinit var getPatientJournalByPatientIdResponse: GetPatientJournalByPatientIdResponse
    private lateinit var accessToken: String
    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonDialog: CommonAPICallDialog
    private lateinit var loginResponse: LoginResponse
    private lateinit var journalEntryDailyJournalAdapter: JournalEntryDailyJournalAdapter
    private val dailyJournalItems = mutableListOf<DailyJournal>()
    private var selectedDate: LocalDate? = null

    companion object {
        private const val ARG_SELECTED_DATE = "selected_date"
    }

    fun newInstance(date: LocalDate): DailyJournalTabFragment {
        val fragment = DailyJournalTabFragment()
        val bundle = Bundle()
        bundle.putSerializable(ARG_SELECTED_DATE, date)
        fragment.arguments = bundle
        return fragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedDate = it.getSerializable(ARG_SELECTED_DATE) as? LocalDate
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TabDailyJournalsFragmentBinding.inflate(inflater, container, false)

        customProgressDialog = CustomProgressDialog(requireContext())
        commonDialog = CommonAPICallDialog(requireContext())

        accessToken = SharedPreferencesUtil.getData(requireContext(), "accessToken", "")
        val jsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString<LoginResponse>(jsonString)

        binding.tabDailyJournalRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        journalEntryDailyJournalAdapter = JournalEntryDailyJournalAdapter(dailyJournalItems)
        binding.tabDailyJournalRecyclerView.adapter = journalEntryDailyJournalAdapter

        if (CommonClass.isNetworkAvailable(requireContext())) {
            getPatientJournalDataAPICall(null)
        } else {
            CommonClass.showInternetDialogue(requireContext())
        }
        return binding.root
    }

    fun getPatientJournalDataAPICall(selectedDate: LocalDate?) {
        getPatientJournalByPatientIdViewModel.clear()

        var formattedDate = selectedDate?.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))

        if(formattedDate == null){
            formattedDate = ""
        }
        val request = GetPatientJournalByPatientIdRequest(
            loginResponse.loginDetails.clientID,
            "",
            "$formattedDate",
            loginResponse.loginDetails.patientID,
            loginResponse.loginDetails.patientLocationID
        )


        getPatientJournalByPatientIdViewModel.getPatientJournalByPatientId(request, accessToken)
        observeViewModel()

    }

    private fun observeViewModel() {

        getPatientJournalByPatientIdViewModel.loadingLiveData.observe(
            requireActivity(),
            Observer { isLoading ->
                if (isLoading) {
                    customProgressDialog.show(getString(R.string.loading))
                } else {
                    customProgressDialog.dialogDismiss()
                }
            })

        getPatientJournalByPatientIdViewModel.successLiveData.observe(
            requireActivity(),
            Observer { isSuccess ->
                if (isSuccess) {
                    getPatientJournalByPatientIdViewModel.saveResponseLiveData.observe(
                        requireActivity(),
                        Observer { successData ->
                            if (successData != null) {
                                // getPatientJournalByPatientIdResponse = successData
                                updateUI(successData.dailyJournal)

                                if(successData.dailyJournal.isEmpty()){
                                    binding.tvNoRecords.visibility = View.VISIBLE
                                }else{
                                    binding.tvNoRecords.visibility = View.GONE
                                }
                            }
                        }
                    )
                }
            })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateUI(dailyJournalItem: List<DailyJournal>) {
        // Clear previous data
        dailyJournalItems.clear()

        val sortedItems = dailyJournalItem.sortedByDescending { it.createdAt }
        dailyJournalItems.addAll(sortedItems)
        journalEntryDailyJournalAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        getPatientJournalDataAPICall(null)
    }

}
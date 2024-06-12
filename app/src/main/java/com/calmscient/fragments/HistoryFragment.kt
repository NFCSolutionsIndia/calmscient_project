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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.calmscient.R
import com.calmscient.adapters.HistoryCardAdapter
import com.calmscient.databinding.LayoutHistoryBinding
import com.calmscient.di.remote.response.ScreeningHistoryResponse
import com.calmscient.di.remote.response.ScreeningItem
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.HistoryViewModel
import java.text.SimpleDateFormat

data class HistoryDataClass(
    val date: String?,
    val time: String?,
    val progressBarValue: Int?,
    val score: Int?,
    val total: Int?
)

class HistoryFragment(private val screeningItem: ScreeningItem) : Fragment() {
    lateinit var binding: LayoutHistoryBinding
    lateinit var historyViewAdapter: HistoryCardAdapter
    private val historyItems = mutableListOf<HistoryDataClass>()

    private val historyViewModel: HistoryViewModel by activityViewModels()
    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonDialog: CommonAPICallDialog
    private var historyResponse: List<ScreeningHistoryResponse> = emptyList()
    private lateinit var accessToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            loadFragment(ScreeningsFragment())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LayoutHistoryBinding.inflate(inflater, container, false)
        binding.historyBackIcon.setOnClickListener {
            loadFragment(ScreeningsFragment())
        }

        customProgressDialog = CustomProgressDialog(requireContext())
        commonDialog = CommonAPICallDialog(requireContext())

        accessToken = SharedPreferencesUtil.getData(requireContext(), "accessToken", "")
        Log.d("History Fragment :", "{${screeningItem.screeningID}}")

        fetchScreeningHistory()
        observeViewModel()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerHistory.layoutManager = LinearLayoutManager(requireContext())
        historyViewAdapter = HistoryCardAdapter(historyItems)
        binding.recyclerHistory.adapter = historyViewAdapter

        binding.titleScreenings.text = screeningItem.screeningType
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun fetchScreeningHistory() {
        // Make API call to fetch screening history data
        historyViewModel.getScreeningHistoryDetails(screeningItem.plid,screeningItem.patientID,screeningItem.clientID,screeningItem.screeningID,accessToken)
    }
    private fun observeViewModel()
    {
        historyViewModel.loadingLiveData.observe(viewLifecycleOwner, Observer { isLoading ->
            if(isLoading) {
                customProgressDialog.show("Loading...")
            } else {
                customProgressDialog.dialogDismiss()
            }
        })

        historyViewModel.successLiveData.observe(viewLifecycleOwner, Observer { isSuccess ->
            if (isSuccess){
                historyViewModel.saveResponseLiveData.observe(viewLifecycleOwner, Observer { successData ->
                    if (successData != null) {
                        historyResponse = successData.screeningHistory

                        updateUI(historyResponse)
                    }
                })
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateUI(screeningHistory: List<ScreeningHistoryResponse>) {
        // Clear previous data
        historyItems.clear()

        // Sort the data by date in descending order
        val sortedHistory = screeningHistory.sortedByDescending { it.completionDateTime }

        // Add sorted data to historyItems list
        sortedHistory.forEach { history ->
            val score = history.score ?: 0
            val total = history.totalScore ?: 0

            val progressBarValue = calculateProgressBarValue(score, total)
            val (date, time) = separateDateAndTime(history.completionDateTime)
            historyItems.add(
                HistoryDataClass(
                    date = date,
                    time = time,
                    progressBarValue = progressBarValue,
                    score = history.score,
                    total = history.totalScore
                )
            )
        }

        historyViewAdapter.notifyDataSetChanged()
    }

    private fun calculateProgressBarValue(score: Int, total: Int): Int {
        // Calculate the percentage
        val percentage = if (total != 0) {
            (score.toDouble() / total.toDouble()) * 100
        } else {
            0.0
        }
        return percentage.toInt()
    }

    private fun separateDateAndTime(completionDateTime: String): Pair<String, String> {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val dateTime = formatter.parse(completionDateTime)
        val dateFormatter = SimpleDateFormat("MM/dd/yyyy")
        val timeFormatter = SimpleDateFormat("HH:mm:ss")

        val date = dateFormatter.format(dateTime)
        val time = timeFormatter.format(dateTime)

        return Pair(date, time)
    }
}
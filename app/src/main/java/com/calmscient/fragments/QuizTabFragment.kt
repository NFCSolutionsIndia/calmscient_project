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
import com.calmscient.adapters.HistoryCardAdapter
import com.calmscient.adapters.JournalEntryQuizAdapter
import com.calmscient.databinding.TabQuizFragmentBinding
import com.calmscient.di.remote.request.GetPatientJournalByPatientIdRequest
import com.calmscient.di.remote.response.GetPatientJournalByPatientIdResponse
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.di.remote.response.Quiz
import com.calmscient.di.remote.response.QuizDataForAdapter
import com.calmscient.di.remote.response.ScreeningHistoryResponse
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.DateTimeUtils
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.GetPatientJournalByPatientIdViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat

@AndroidEntryPoint
class QuizTabFragment : Fragment() {

    private lateinit var binding: TabQuizFragmentBinding
    private val getPatientJournalByPatientIdViewModel: GetPatientJournalByPatientIdViewModel by viewModels()
    private lateinit var getPatientJournalByPatientIdResponse: GetPatientJournalByPatientIdResponse
    private lateinit var accessToken: String
    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonDialog: CommonAPICallDialog
    private lateinit var loginResponse : LoginResponse
    private lateinit var journalEntryQuizAdapter: JournalEntryQuizAdapter
    private val quizItems = mutableListOf<QuizDataForAdapter>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TabQuizFragmentBinding.inflate(inflater,container,false)

        customProgressDialog = CustomProgressDialog(requireContext())
        commonDialog = CommonAPICallDialog(requireContext())

        accessToken = SharedPreferencesUtil.getData(requireContext(), "accessToken", "")
        val jsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString<LoginResponse>(jsonString)

        binding.tabQuizRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        journalEntryQuizAdapter = JournalEntryQuizAdapter(quizItems)
        binding.tabQuizRecyclerView.adapter = journalEntryQuizAdapter


        if(CommonClass.isNetworkAvailable(requireContext())){
            getPatientJournalDataAPICall()
        }else{
            CommonClass.showInternetDialogue(requireContext())
        }
        return  binding.root
    }

    private fun getPatientJournalDataAPICall(){
        getPatientJournalByPatientIdViewModel.clear()


        var request = GetPatientJournalByPatientIdRequest(loginResponse.loginDetails.clientID,"","",loginResponse.loginDetails.patientID,loginResponse.loginDetails.patientLocationID)


        getPatientJournalByPatientIdViewModel.getPatientJournalByPatientId(request,accessToken)
        observeViewModel()

    }

    private fun observeViewModel(){

        getPatientJournalByPatientIdViewModel.loadingLiveData.observe(requireActivity(), Observer { isLoading->
            if(isLoading){
                customProgressDialog.show(getString(R.string.loading))
            }else{
                customProgressDialog.dialogDismiss()
            }
        })

        getPatientJournalByPatientIdViewModel.successLiveData.observe(requireActivity(), Observer { isSuccess->
            if(isSuccess){
                getPatientJournalByPatientIdViewModel.saveResponseLiveData.observe(requireActivity(),
                    Observer { successData->
                        if(successData != null){
                           // getPatientJournalByPatientIdResponse = successData
                            updateUI(successData.quiz)
                        }
                    }
                )
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateUI(screeningHistory: List<Quiz>) {
        // Clear previous data
        quizItems.clear()

        val sortedHistory = screeningHistory.sortedByDescending { it.completionDateTime }

        sortedHistory.forEach { quiz ->
            val score = quiz.score ?: 0
            val total = quiz.totalScore ?: 0

            val progressBarValue = calculateProgressBarValue(score, total)
            val (date, time) = DateTimeUtils.formatDateTime(quiz.completionDateTime)
            quizItems.add(
                QuizDataForAdapter(
                    date = date,
                    time = time,
                    progressBarValue = progressBarValue,
                    score = quiz.score,
                    total = quiz.totalScore,
                    title = quiz.title
                )
            )
        }

        journalEntryQuizAdapter.notifyDataSetChanged()
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
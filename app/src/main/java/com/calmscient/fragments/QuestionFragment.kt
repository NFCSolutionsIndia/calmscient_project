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
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.R
import com.calmscient.activities.CommonDialog
import com.calmscient.adapters.QuestionAdapter
import com.calmscient.databinding.FragmentQuestionBinding
import com.calmscient.di.remote.response.QuestionnaireItem
import com.calmscient.di.remote.response.ScreeningItem
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.network.ServerTimeoutHandler
import com.calmscient.viewmodels.ScreeningQuestionnaireViewModel
import java.text.SimpleDateFormat
import java.util.Calendar

data class Question(
    val questionText: String,
    val options: List<String>,
    var selectedOption: Int = -1
)

class QuestionFragment(private val screeningItem: ScreeningItem) : Fragment() {

    private lateinit var binding: FragmentQuestionBinding
    private lateinit var questionAdapter: QuestionAdapter
    private val screeningQuestionsViewModel: ScreeningQuestionnaireViewModel by activityViewModels()
    private var screeningQuestionResponse : List<QuestionnaireItem> = emptyList()
    private lateinit var screeningResponseList: List<ScreeningItem>
    private var currentQuestionIndex = 0
    private var isPreviousButtonVisible = false
    private var isNextButtonVisible = true
    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonDialog: CommonAPICallDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this){
            if (CommonClass.isNetworkAvailable(requireContext()))
            {
                loadFragment(ScreeningsFragment())
            }
            else{
                CommonClass.showInternetDialogue(requireContext())
            }
        }

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQuestionBinding.inflate(inflater, container, false)

        // Retrieve data from arguments
        val screeningResponseJson = arguments?.getString("screeningResponse")
        Log.d("Question Fragment ", "$screeningResponseJson")

        screeningResponseList = listOf(screeningItem)

        Log.d("Questionnnnnnnnnnn Fragmentttttttttt ","$screeningResponseList")

        customProgressDialog = CustomProgressDialog(requireContext())

        commonDialog = CommonAPICallDialog(requireContext())

        binding.backIcon.setOnClickListener{
            if (CommonClass.isNetworkAvailable(requireContext()))
            {
                loadFragment(ScreeningsFragment())
            }
            else{
                CommonClass.showInternetDialogue(requireContext())
            }
        }

        binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pagerSnapHelper = PagerSnapHelper()
        pagerSnapHelper.attachToRecyclerView(binding.questionsRecyclerView)

        val commonDialog = CommonDialog(requireContext())

        // Show a dialog when the fragment is loaded
        commonDialog.showDialog(screeningResponseList[0].screeningReminder)

        // Get today's date
        val today = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MM/dd/yyyy")
        val todayDate = dateFormat.format(today.time)

        // Calculate 7 days before today
        val sevenDaysBefore = Calendar.getInstance()
        sevenDaysBefore.add(Calendar.DAY_OF_YEAR, -7)
        val sevenDaysBeforeDate = dateFormat.format(sevenDaysBefore.time)
        if (CommonClass.isNetworkAvailable(requireContext()))
        {
            observeViewModel()
            setupRecyclerView()
        }
        else{
            CommonClass.showInternetDialogue(requireContext())
        }

        screeningQuestionsViewModel.getScreeningQuestionsList(screeningResponseList[0].patientID,screeningResponseList[0].clientID,screeningResponseList[0].plid,sevenDaysBeforeDate,todayDate,screeningResponseList[0].assessmentID,screeningResponseList[0].screeningID)

        binding.nextQuestion.setOnClickListener {
            if (CommonClass.isNetworkAvailable(requireContext()))
            {
                moveToNextQuestion()
            }
            else
            {
                CommonClass.showInternetDialogue(requireContext())
            }
        }

        // Handle click on previous question button
        binding.previousQuestion.setOnClickListener {
            if (CommonClass.isNetworkAvailable(requireContext()))
            {
                moveToPreviousQuestion()
            }
            else
            {
                CommonClass.showInternetDialogue(requireContext())
            }
        }
//        // Listen to RecyclerView scroll events to update currentQuestionIndex
//        binding.questionsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//                // Update currentQuestionIndex based on the visible item position
//                currentQuestionIndex = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
//            }
//        })

    }

    private fun moveToNextQuestion() {
        if (currentQuestionIndex < (screeningQuestionsViewModel.screeningQuestionListLiveData.value?.size ?: (0 - 1))) {
            currentQuestionIndex++
            binding.questionsRecyclerView.smoothScrollToPosition(currentQuestionIndex)
        }
    }

    private fun moveToPreviousQuestion() {
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--
            binding.questionsRecyclerView.smoothScrollToPosition(currentQuestionIndex)
        }
    }


    private fun setupRecyclerView() {
        // Assuming you have already initialized questionnaireItems in your ViewModel
        questionAdapter = QuestionAdapter(requireContext(), emptyList())
        binding.questionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = questionAdapter
        }
    }


    private fun observeViewModel() {

        screeningQuestionsViewModel.screeningsQuestionResultLiveData.observe(viewLifecycleOwner,Observer{isSuccess ->

            if(isSuccess)
            {
                screeningQuestionResponse = screeningQuestionsViewModel.screeningQuestionListLiveData.value!!
                screeningQuestionResponse?.let {

                    val res = screeningQuestionsViewModel.screeningQuestionListLiveData.value!!
                    Log.d("QuestionFragment ","$res")
                    displayQuestions(it)
                }
            }
            else{
                screeningQuestionsViewModel.errorLiveData.value?.let { failureMessage ->
                    failureMessage.let{
                        ServerTimeoutHandler.handleTimeoutException(requireContext()) {
                            // Retry logic when the retry button is clicked
                            screeningQuestionsViewModel.retryScreeningsFetchMenuItems()
                        }
                    }
                }

                screeningQuestionsViewModel.failureLiveData.value?.let { failureMessage ->
                    failureMessage.let {
                        commonDialog.showDialog(
                            it
                        )
                    }
                }
            }
        })
        screeningQuestionsViewModel.screeningQuestionListLiveData.observe(viewLifecycleOwner, Observer { questionnaireItems ->
            questionnaireItems?.let {

                val res = screeningQuestionsViewModel.screeningQuestionListLiveData.value!!
                Log.d("QuestionFragment ","$res")
                displayQuestions(it)
            }
        })
        screeningQuestionsViewModel.failureLiveData.observe(viewLifecycleOwner, Observer { errorMessage ->
            // Handle failure
        })
        screeningQuestionsViewModel.loadingLiveData.observe(viewLifecycleOwner, Observer { isLoading ->
            if (isLoading) {
                customProgressDialog.show("Loading...")
            } else {

                customProgressDialog.dialogDismiss()
            }
        })
    }

    private fun displayQuestions(questionnaireItems: List<QuestionnaireItem>) {
        if (::questionAdapter.isInitialized) {
            questionAdapter.updateQuestionnaireItems(questionnaireItems)
        } else {
            // Log an error or handle the case where questionAdapter is not initialized
        }
    }
    private fun loadFragment(fragment: Fragment) {

        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

}

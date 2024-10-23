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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.calmscient.R
import com.calmscient.activities.CommonDialog
import com.calmscient.adapters.QuestionAdapter
import com.calmscient.databinding.FragmentGadQuestionsBinding
import com.calmscient.di.remote.request.PatientAnswerSaveRequest
import com.calmscient.di.remote.request.PatientAnswersWrapper
import com.calmscient.di.remote.response.QuestionnaireItem
import com.calmscient.di.remote.response.ScreeningItem
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.utils.network.ServerTimeoutHandler
import com.calmscient.viewmodels.SaveScreeningAnswersViewModel
import com.calmscient.viewmodels.ScreeningQuestionnaireViewModel
import java.text.SimpleDateFormat
import java.util.Calendar



class GADQuestionFragment(private val screeningItem: ScreeningItem) : Fragment() {

    private lateinit var binding: FragmentGadQuestionsBinding
    private lateinit var questionAdapter: QuestionAdapter
    private val screeningQuestionsViewModel: ScreeningQuestionnaireViewModel by activityViewModels()
    private val saveScreeningAnswersViewModel: SaveScreeningAnswersViewModel by activityViewModels()
    private var screeningQuestionResponse: List<QuestionnaireItem> = emptyList()
    private var result: List<QuestionnaireItem> = emptyList()
    private lateinit var screeningResponseList: List<ScreeningItem>
    private var currentQuestionIndex = 0
    private var isPreviousButtonVisible = false
    private var isNextButtonVisible = true
    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonDialog: CommonAPICallDialog
    private  lateinit var accessToken : String


    private val selectedOptionsMap = mutableMapOf<Int, String?>()
    // Define a variable to store the last saved state of selected options
    private var lastSavedSelectedOptions: Map<Int, String?> = emptyMap()

    // Function to check if there are any changes in selected options
    private fun areSelectedOptionsChanged(): Boolean {
        return selectedOptionsMap != lastSavedSelectedOptions
    }

    // Function to update the last saved state of selected options
    private fun updateLastSavedSelectedOptions() {
        lastSavedSelectedOptions = selectedOptionsMap.toMap()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (CommonClass.isNetworkAvailable(requireContext())) {
                loadFragment(ScreeningsFragment())
            } else {
                CommonClass.showInternetDialogue(requireContext())
            }
        }
        commonDialog = CommonAPICallDialog(requireContext())

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGadQuestionsBinding.inflate(inflater, container, false)

        // Retrieve data from arguments
//        val screeningResponseJson = arguments?.getString("screeningResponse")
//        Log.d("Question Fragment ", screeningResponseJson.toString())

        screeningResponseList = listOf(screeningItem)

        accessToken = SharedPreferencesUtil.getData(requireContext(), "accessToken", "")

        Log.d("GAD Fragmentttttttttt ", "$screeningResponseList")

        customProgressDialog = CustomProgressDialog(requireContext())

        commonDialog = CommonAPICallDialog(requireContext())

        binding.backIcon.setOnClickListener {
            if (CommonClass.isNetworkAvailable(requireContext())) {
                loadFragment(ScreeningsFragment())
            } else {
                CommonClass.showInternetDialogue(requireContext())
            }
        }

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
        if (CommonClass.isNetworkAvailable(requireContext())) {
            observeViewModel()
            setupRecyclerView()
        } else {
            CommonClass.showInternetDialogue(requireContext())
        }

        screeningQuestionsViewModel.getScreeningQuestionsList(
            screeningResponseList[0].patientID,
            screeningResponseList[0].clientID,
            screeningResponseList[0].plid,
            "",
            "",
            screeningResponseList[0].assessmentID,
            screeningResponseList[0].screeningID,
            accessToken
        )

        binding.nextQuestion.setOnClickListener {
            if (CommonClass.isNetworkAvailable(requireContext())) {
                moveToNextQuestion()
                if(currentQuestionIndex>0){
                    binding.previousQuestion.visibility = View.VISIBLE
                }else{
                    binding.previousQuestion.visibility = View.GONE
                }
            } else {
                CommonClass.showInternetDialogue(requireContext())
            }
        }

        // Handle click on previous question button
        binding.previousQuestion.setOnClickListener {
            if (CommonClass.isNetworkAvailable(requireContext())) {
                moveToPreviousQuestion()
                if(currentQuestionIndex <= 0){
                    binding.previousQuestion.visibility = View.GONE
                }else{
                    binding.previousQuestion.visibility = View.VISIBLE
                }
            } else {
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


    private fun setupRecyclerView() {
        // Assuming you have already initialized questionnaireItems in your ViewModel
        questionAdapter = QuestionAdapter(requireContext(), emptyList(),screeningResponseList[0].screeningReminder)
        binding.questionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = questionAdapter
        }
    }


    private fun observeViewModel() {

       /* screeningQuestionsViewModel.screeningsQuestionResultLiveData.observe(
            viewLifecycleOwner,
            Observer { isSuccess ->
                if (isSuccess) {
                    screeningQuestionsViewModel.screeningQuestionListLiveData.observe(
                        viewLifecycleOwner,
                        Observer { questionnaireItems ->
                            questionnaireItems?.let {

                                val res =
                                    screeningQuestionsViewModel.screeningQuestionListLiveData.value!!
                                screeningQuestionResponse = res
                                Log.d("GAD Fragment ", "$res")

                                result = it
                                displayQuestions(it)
                            }
                        })

                } else {
                    screeningQuestionsViewModel.errorLiveData.value?.let { failureMessage ->
                        failureMessage.let {
                            if (CommonClass.isNetworkAvailable(requireContext())) {
                                ServerTimeoutHandler.handleTimeoutException(requireContext()) {
                                    // Retry logic when the retry button is clicked
                                    screeningQuestionsViewModel.retryScreeningsFetchMenuItems()
                                }
                            } else {
                                CommonClass.showInternetDialogue(requireContext())
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
            })*/
        screeningQuestionsViewModel.screeningQuestionListLiveData.observe(
            viewLifecycleOwner,
            Observer { questionnaireItems ->
                questionnaireItems?.let {

                    val res = screeningQuestionsViewModel.screeningQuestionListLiveData.value!!
                    screeningQuestionResponse = res
                    Log.d("GADFragment ", "$res")
                    result = it
                    displayQuestions(it)
                }
            })
        screeningQuestionsViewModel.failureLiveData.observe(
            viewLifecycleOwner,
            Observer { errorMessage ->
                // Handle failure
            })
        screeningQuestionsViewModel.loadingLiveData.observe(
            viewLifecycleOwner,
            Observer { isLoading ->
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

        Log.d("LoadFragment in DF","$screeningResponseList")
        val args = Bundle().apply {
            putString("screeningResponse", JsonUtil.toJsonString(screeningResponseList))
        }
        fragment.arguments = args

        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun navigateToScreeningsFragment() {
        commonDialog.dismiss()
        customProgressDialog.dialogDismiss()
        if (CommonClass.isNetworkAvailable(requireContext())) {
            loadFragment(ScreeningsFragment())
        } else {
            CommonClass.showInternetDialogue(requireContext())
        }
    }

    private fun navigateToResultsFragment() {
        commonDialog.dismiss()
        if (CommonClass.isNetworkAvailable(requireContext())) {
            loadFragment(ResultsFragment())
        } else {
            CommonClass.showInternetDialogue(requireContext())
        }
    }

    private fun moveToNextQuestion() {
        if (screeningQuestionResponse.isNotEmpty() && currentQuestionIndex < screeningQuestionResponse.size) {
            val currentQuestion = screeningQuestionResponse[currentQuestionIndex]
            val selectedOptionId = questionAdapter.getSelectedOptionId(currentQuestionIndex)
            storeSelectedOption(currentQuestion.questionId, selectedOptionId)

            currentQuestionIndex++

            if (currentQuestionIndex == screeningQuestionResponse.size) {
                saveScreeningAnswersViewModel.successNotAnsweredData = MutableLiveData(false)
                saveScreeningAnswersViewModel.successNotAnsweredDataMessage = MutableLiveData(null)
                saveScreeningAnswersViewModel.saveResponseLiveData = MutableLiveData(null)
                if (areSelectedOptionsChanged()) {
                    updateLastSavedSelectedOptions()
                    val patientAnswers = constructPatientAnswers()

                    if (patientAnswers.patientAnswers.isNotEmpty()) {
                        saveScreeningAnswersViewModel.savePatientAnswers(patientAnswers, accessToken)
                        saveScreeningAnswersViewModel.loadingLiveData.observe(viewLifecycleOwner) { isLoading ->
                            if (isLoading) customProgressDialog.show("Loading...") else customProgressDialog.dialogDismiss()
                        }

                        saveScreeningAnswersViewModel.successLiveData.observe(viewLifecycleOwner) { isSuccess ->
                            if (isSuccess) {
                                saveScreeningAnswersViewModel.saveResponseLiveData.observe(viewLifecycleOwner) { successData ->
                                    if (successData != null) {
                                        commonDialog.showDialog(successData.statusResponse.responseMessage,R.drawable.ic_success_dialog)
                                        commonDialog.setOnDismissListener {
                                            navigateToResultsFragment()
                                        }
                                    }
                                }
                            }
                        }

                        saveScreeningAnswersViewModel.successNotAnsweredDataMessage.observe(viewLifecycleOwner) { message ->
                            if (message != null) {
                                commonDialog.showDialog(message,R.drawable.ic_info)
                            }
                            commonDialog.setOnDismissListener {
                                navigateToScreeningsFragment()
                            }
                        }

                        saveScreeningAnswersViewModel.errorLiveData.observe(viewLifecycleOwner) { errorMessage ->
                            commonDialog.showDialog(errorMessage,R.drawable.ic_failure)
                        }
                    } else {
                        commonDialog.showDialog(getString(R.string.please_answer_the_questions),R.drawable.ic_alret)
                    }
                }
            } else {
                binding.questionsRecyclerView.smoothScrollToPosition(currentQuestionIndex)
            }
        }
    }


    private fun moveToPreviousQuestion() {
        if (currentQuestionIndex == screeningQuestionResponse.size) {
            currentQuestionIndex--
        }
        if (currentQuestionIndex > 0 && currentQuestionIndex <= screeningQuestionResponse.size) {

            val currentQuestion = screeningQuestionResponse[currentQuestionIndex]
            val selectedOptionId = questionAdapter.getSelectedOptionId(currentQuestionIndex)
            storeSelectedOption(currentQuestion.questionId, selectedOptionId)

            Log.d("GAD Question ID", "${currentQuestion.questionId}")
            Log.d("GAD Selected Option ID", "$selectedOptionId")

            currentQuestionIndex--
            if (currentQuestionIndex >= 0) {
                binding.questionsRecyclerView.smoothScrollToPosition(currentQuestionIndex)
            } else {
                // Handle the case where we are already at the beginning of the list
                Log.e("GADFragment", "Reached the beginning of the questions list")
            }
        } else {
            Log.e("GADFragment", "currentQuestionIndex is out of bounds")
        }
    }


    // Function to store selected option for a question
    private fun storeSelectedOption(questionId: Int, selectedOptionId: String?) {
        selectedOptionsMap[questionId] = selectedOptionId
    }

    @SuppressLint("SuspiciousIndentation")
    private fun constructPatientAnswers(): PatientAnswersWrapper {
        val patientAnswers = mutableListOf<PatientAnswerSaveRequest>()

        var answerId: String? = null


        // Iterate through all questions in screeningQuestionResponse
        screeningQuestionResponse.forEach { questionnaireItem ->

            // Find the selected option for the current question
            val selectedOptionId = selectedOptionsMap[questionnaireItem.questionId]

            questionnaireItem.answerResponse.forEach { response ->
                if (response.answerId != null) {
                    answerId = response.answerId
                    return@forEach // Exit the loop once an answerId is found
                }
            }

            // If an option is selected for the question
            if (selectedOptionId != null) {
                // Find the answer response corresponding to the selectedOptionId
                val answerResponse =
                    questionnaireItem.answerResponse.find { it.optionLabelId == selectedOptionId }

                answerResponse?.let { response ->

                    //val flag =  if (response.selected == "true" && response.answerId != null) "U" else "I"


                    //val answerId = if (response.selected == "true") response.answerId else null

                    // Construct the PatientAnswer object
                    val patientAnswer = PatientAnswerSaveRequest(
                        flag = "",
                        answerId = answerId,
                        screeningId = screeningItem.screeningID,
                        patientLocationId = screeningItem.plid,
                        questionnaireId = questionnaireItem.questionId,
                        optionId = response.optionLabelId.toInt(),
                        score = response.optionScore.toInt(),
                        clientId = screeningItem.clientID,
                        patientId = screeningItem.patientID,
                        assessmentId = screeningItem.assessmentID
                    )


                    // Add the constructed PatientAnswer to the list
                    patientAnswers.add(patientAnswer)

                    answerId = null
                }
            } /*else {
                // If the question is not answered, construct the object with null for optionId
                val patientAnswer = PatientAnswerSaveRequest(
                    flag = "I",
                    answerId = null,
                    screeningId = screeningItem.screeningID,
                    patientLocationId = screeningItem.plid,
                    questionnaireId = questionnaireItem.questionId,
                    optionId = null, // OptionId is null for skipped questions
                    score = 0, // Set score to 0 for skipped questions
                    clientId = screeningItem.clientID,
                    patientId = screeningItem.patientID,
                    assessmentId = screeningItem.assessmentID
                )

                // Add the constructed PatientAnswer to the list
                patientAnswers.add(patientAnswer)
            }*/
        }

        return PatientAnswersWrapper(patientAnswers)

    }
}

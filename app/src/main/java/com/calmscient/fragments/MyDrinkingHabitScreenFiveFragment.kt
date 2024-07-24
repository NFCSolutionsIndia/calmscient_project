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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.calmscient.R
import com.calmscient.databinding.FragmentMyDrinkingHabitScreenFiveBinding
import com.calmscient.di.remote.BasicKnowledgeItem
import com.calmscient.di.remote.request.SaveMyDrinkingHabitAnswerRequest
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.di.remote.response.MyDrinkingHabitResponse
import com.calmscient.di.remote.response.Option
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.BasicKnowledgeSharedViewModel
import com.calmscient.viewmodels.GetPatientBasicKnowledgeCourseViewModel
import com.calmscient.viewmodels.SaveMyDrinkHabitAnswerViewModel
import com.calmscient.viewmodels.UpdateBasicKnowledgeIndexDataViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MyDrinkingHabitScreenFiveFragment : Fragment() {

    private lateinit var binding: FragmentMyDrinkingHabitScreenFiveBinding
    private val updateBasicKnowledgeIndexDataViewModel: UpdateBasicKnowledgeIndexDataViewModel by viewModels()

    private val getPatientBasicKnowledgeCourseViewModel: GetPatientBasicKnowledgeCourseViewModel by activityViewModels()
    private lateinit var myDrinkingHabitResponse: MyDrinkingHabitResponse

    private val saveMyDrinkHabitAnswerViewModel: SaveMyDrinkHabitAnswerViewModel by activityViewModels()

    private val sharedViewModel: BasicKnowledgeSharedViewModel by activityViewModels()
    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonDialog: CommonAPICallDialog
    private lateinit var accessToken: String
    private lateinit var loginResponse: LoginResponse
    private lateinit var itemTemp: BasicKnowledgeItem


    private var selectedOptionIndex: Int = -1
    private var questionFiveOptionIndex: Int = -1
    private var questionSixOptionIndex: Int = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().supportFragmentManager.popBackStack()
            requireActivity().supportFragmentManager.popBackStack()
            requireActivity().supportFragmentManager.popBackStack()
            requireActivity().supportFragmentManager.popBackStack()
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMyDrinkingHabitScreenFiveBinding.inflate(inflater, container, false)

        accessToken = SharedPreferencesUtil.getData(requireContext(), "accessToken", "")
        customProgressDialog = CustomProgressDialog(requireContext())
        commonDialog = CommonAPICallDialog(requireContext())

        val loginJsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString<LoginResponse>(loginJsonString)

        // Observe the selected item
        sharedViewModel.selectedItem.observe(viewLifecycleOwner, Observer { item ->
            if (item != null) {
                // Use item data as needed
                Log.d(
                    "MyDrinkingHabitScreenFiveFragment",
                    "Name: ${item.name}, SectionId: ${item.sectionId}"
                )
                itemTemp = item

                updateBasicKnowledgeAPICall()

            }
        })

        if (CommonClass.isNetworkAvailable(requireContext())) {
            myDrinkingHabitQuestionsAPICall()
        } else {
            CommonClass.showInternetDialogue(requireContext())
        }

        binding.backIcon.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
            requireActivity().supportFragmentManager.popBackStack()
            requireActivity().supportFragmentManager.popBackStack()
            requireActivity().supportFragmentManager.popBackStack()
            requireActivity().supportFragmentManager.popBackStack()
        }
        binding.previousQuestion.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.yesButton.setOnClickListener {
            val requests = createSaveMyDrinkingHabitAnswerRequest(myDrinkingHabitResponse)

            requests?.forEach { request ->
                saveAnswerAPICall(request)
            }
        }

        binding.nextQuestion.visibility = View.GONE
        initializeOptions()

        return binding.root
    }

    private fun updateBasicKnowledgeAPICall() {
        updateBasicKnowledgeIndexDataViewModel.updateBasicKnowledgeIndexData(
            1,
            loginResponse.loginDetails.patientID,
            itemTemp.sectionId,
            accessToken
        )
        observeBasicKnowledgeViewModel()
    }

    private fun observeBasicKnowledgeViewModel() {
        updateBasicKnowledgeIndexDataViewModel.loadingLiveData.observe(
            viewLifecycleOwner,
            Observer { isLoading ->
                if (isLoading) {
                    customProgressDialog.show(getString(R.string.loading))
                } else {
                    customProgressDialog.dialogDismiss()
                }
            })
    }

    private fun myDrinkingHabitQuestionsAPICall() {
        getPatientBasicKnowledgeCourseViewModel.myDrinkingHabitQuestions(
            1,
            loginResponse.loginDetails.clientID,
            loginResponse.loginDetails.patientID,
            loginResponse.loginDetails.patientLocationID,
            accessToken
        )

        myDrinkingHabitObserveViewModel()
    }

    private fun myDrinkingHabitObserveViewModel() {
        getPatientBasicKnowledgeCourseViewModel.loadingLiveData.observe(
            viewLifecycleOwner,
            Observer { isLoading ->
                if (isLoading) {
                    customProgressDialog.show(getString(R.string.loading))
                } else {
                    customProgressDialog.dialogDismiss()
                }
            })

        getPatientBasicKnowledgeCourseViewModel.successLiveData.observe(
            viewLifecycleOwner,
            Observer { isSuccess ->
                if (isSuccess) {
                    getPatientBasicKnowledgeCourseViewModel.saveResponseLiveData.observe(
                        viewLifecycleOwner,
                        Observer { successData ->
                            if (successData != null) {
                                myDrinkingHabitResponse = successData
                                bindUIData(myDrinkingHabitResponse)

                            }
                        }
                    )
                }
            })
    }

    private fun bindUIData(myDrinkingHabitResponse: MyDrinkingHabitResponse) {

        if (myDrinkingHabitResponse.answersList.isNotEmpty()) {

            val answer = myDrinkingHabitResponse.answersList[4]
            val questionnaireId = answer.questionnaireId ?: ""
            val question = answer.question ?: ""
            binding.questionOne.text = "$questionnaireId. $question"
            questionFiveOptionIndex = answer.options[0].optionId
            binding.etQuestionFive.setText(answer.answerText)


            val answer5 = myDrinkingHabitResponse.answersList[5]
            val questionnaireId5 = answer5.questionnaireId ?: ""
            val question5 = answer5.question ?: ""
            binding.questionTwo.text = "$questionnaireId5. $question5"
            questionSixOptionIndex = answer5.options[0].optionId
            binding.etQuestionSix.setText(answer5.answerText)


            val answer6 = myDrinkingHabitResponse.answersList[6]
            val questionnaireId6 = answer6.questionnaireId ?: ""
            val question6 = answer6.question ?: ""
            binding.questionThree.text = "$questionnaireId6. $question6"

            if (myDrinkingHabitResponse.answersList[6].options.size >= 9) {
                binding.optionOne.text = answer6.options[0].optionValue
                binding.optionTwo.text = answer6.options[1].optionValue
                binding.optionThree.text = answer6.options[2].optionValue
                binding.optionFour.text = answer6.options[3].optionValue
                binding.optionFive.text = answer6.options[4].optionValue
                binding.optionSix.text = answer6.options[5].optionValue
                binding.optionSeven.text = answer6.options[6].optionValue
                binding.optionEight.text = answer6.options[7].optionValue
                binding.optionNine.text = answer6.options[8].optionValue
            }

            // Check if there is a patientAnswer and set the selected option
            answer6.patientAnswer?.let { patientAnswer ->
                answer6.options.forEachIndexed { index, option ->
                    if (option.optionId == patientAnswer.toInt()) {
                        selectedOptionIndex = index
                        selectOption(index)
                    }
                }
            }
        }

    }

    private fun initializeOptions() {
        val options = listOf(
            binding.optionOne,
            binding.optionTwo,
            binding.optionThree,
            binding.optionFour,
            binding.optionFive,
            binding.optionSix,
            binding.optionSeven,
            binding.optionEight,
            binding.optionNine,
        )

        options.forEachIndexed { index, textView ->
            textView.setOnClickListener { onOptionClicked(index) }
        }
    }

    private fun onOptionClicked(selectedIndex: Int) {
        if (selectedIndex != selectedOptionIndex) {
            // Clear previous selection
            clearSelection(selectedOptionIndex)

            // Set new selection
            selectedOptionIndex = selectedIndex
            selectOption(selectedIndex)
        }
    }

    private fun clearSelection(index: Int) {
        val options = listOf(
            binding.optionOne,
            binding.optionTwo,
            binding.optionThree,
            binding.optionFour,
            binding.optionFive,
            binding.optionSix,
            binding.optionSeven,
            binding.optionEight,
            binding.optionNine,
        )

        if (index in options.indices) {
            val textView = options[index]
            textView.setBackgroundResource(R.drawable.card_default_background)
            textView.setTextColor(Color.parseColor("#424242"))
        }
    }

    private fun selectOption(index: Int) {
        val options = listOf(
            binding.optionOne,
            binding.optionTwo,
            binding.optionThree,
            binding.optionFour,
            binding.optionFive,
            binding.optionSix,
            binding.optionSeven,
            binding.optionEight,
            binding.optionNine,
        )

        if (index in options.indices) {
            val textView = options[index]
            textView.setBackgroundResource(R.drawable.card_selected_background)
            textView.setTextColor(Color.parseColor("#FFFFFF"))
        }
    }

    private fun getSelectedOption(): Option? {
        val options = myDrinkingHabitResponse.answersList[6].options
        return if (selectedOptionIndex in options.indices) {
            options[selectedOptionIndex]
        } else {
            null
        }
    }

    private fun createSaveMyDrinkingHabitAnswerRequest(response: MyDrinkingHabitResponse): List<SaveMyDrinkingHabitAnswerRequest>? {
        val selectedOption = getSelectedOption()
        val questionFiveAnswer = binding.etQuestionFive.text.toString()
        val questionSixAnswer = binding.etQuestionSix.text.toString()

        val questionFiveRequest = SaveMyDrinkingHabitAnswerRequest(
            answerId = response.answersList[4].answerId,
            assessmentId = 0,
            clientId = loginResponse.loginDetails.clientID,
            optionId = questionFiveOptionIndex,
            optionValue = questionFiveAnswer,
            patientId = loginResponse.loginDetails.patientID,
            plId = loginResponse.loginDetails.patientLocationID,
            quantity = 0,
            questionnaireId = response.answersList[4].questionnaireId,
        )

        val questionSixRequest = SaveMyDrinkingHabitAnswerRequest(
            answerId = response.answersList[5].answerId,
            assessmentId = 0,
            clientId = loginResponse.loginDetails.clientID,
            optionId = questionSixOptionIndex,
            optionValue = questionSixAnswer,
            patientId = loginResponse.loginDetails.patientID,
            plId = loginResponse.loginDetails.patientLocationID,
            quantity = 0,
            questionnaireId = response.answersList[5].questionnaireId,
        )

        val questionSevenRequest = if (selectedOption != null) {
            SaveMyDrinkingHabitAnswerRequest(
                answerId = response.answersList[6].answerId,
                assessmentId = 0,
                clientId = loginResponse.loginDetails.clientID,
                optionId = selectedOption.optionId,
                optionValue = selectedOption.optionValue,
                patientId = loginResponse.loginDetails.patientID,
                plId = loginResponse.loginDetails.patientLocationID,
                quantity = 0,
                questionnaireId = response.answersList[6].questionnaireId,
            )
        } else {
            null
        }

        return listOfNotNull(questionFiveRequest, questionSixRequest, questionSevenRequest)
    }

    private fun saveAnswerAPICall(request: SaveMyDrinkingHabitAnswerRequest) {
        saveMyDrinkHabitAnswerViewModel.saveMyDrinkHabitAnswer(request, accessToken)
        observeViewModel()
    }

    private fun observeViewModel() {
        saveMyDrinkHabitAnswerViewModel.loadingLiveData.observe(
            viewLifecycleOwner,
            Observer { isLoding ->
                if (isLoding) {
                    customProgressDialog.show(getString(R.string.loading))
                } else {
                    customProgressDialog.dialogDismiss()
                }
            })

        saveMyDrinkHabitAnswerViewModel.successLiveData.observe(
            viewLifecycleOwner,
            Observer { isSuccess ->
                if (isSuccess) {
                    saveMyDrinkHabitAnswerViewModel.saveResponseLiveData.observe(viewLifecycleOwner,
                        Observer { successData ->
                            if (successData != null) {
                                commonDialog.showDialog(successData.statusResponse.responseMessage)
                            }
                        })
                }
            })
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

}
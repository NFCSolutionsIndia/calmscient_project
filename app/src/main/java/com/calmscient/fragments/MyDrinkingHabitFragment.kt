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
import com.calmscient.databinding.FragmentMyDrinkingHabitBinding
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
import com.calmscient.viewmodels.SaveCourseJournalEntryMakeAPlanViewModel
import com.calmscient.viewmodels.SaveMyDrinkHabitAnswerViewModel
import com.calmscient.viewmodels.UpdateBasicKnowledgeIndexDataViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MyDrinkingHabitFragment : Fragment() {

    private lateinit var binding: FragmentMyDrinkingHabitBinding

    private val getPatientBasicKnowledgeCourseViewModel: GetPatientBasicKnowledgeCourseViewModel by activityViewModels()
    private lateinit var myDrinkingHabitResponse: MyDrinkingHabitResponse

    private val saveMyDrinkHabitAnswerViewModel : SaveMyDrinkHabitAnswerViewModel by activityViewModels()

    private val updateBasicKnowledgeIndexDataViewModel: UpdateBasicKnowledgeIndexDataViewModel by viewModels()
    private val sharedViewModel: BasicKnowledgeSharedViewModel by activityViewModels()
    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonDialog: CommonAPICallDialog
    private lateinit var accessToken: String
    private lateinit var loginResponse: LoginResponse
    private lateinit var itemTemp: BasicKnowledgeItem


    private var selectedOptionIndex: Int = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (CommonClass.isNetworkAvailable(requireContext())) {
                val res = SharedPreferencesUtil.getData(requireContext(), "courseIdBasicKnowledge", "")
                val basicKnowledgeFragment = BasicKnowledgeFragment.newInstanceBasicKnowledge(res.toInt())
                loadFragment(basicKnowledgeFragment)
            } else {
                CommonClass.showInternetDialogue(requireContext())
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMyDrinkingHabitBinding.inflate(inflater, container, false)

        accessToken = SharedPreferencesUtil.getData(requireContext(), "accessToken", "")
        customProgressDialog = CustomProgressDialog(requireContext())
        commonDialog = CommonAPICallDialog(requireContext())

        val loginJsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString<LoginResponse>(loginJsonString)


        binding.backIcon.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        /* // Observe the selected item
         sharedViewModel.selectedItem.observe(viewLifecycleOwner, Observer { item ->
             if (item != null) {
                 // Use item data as needed
                 Log.d("MyDrinkingHabitFragment", "Name: ${item.name}, SectionId: ${item.sectionId}")
                 itemTemp = item

             }
         })*/

        if(CommonClass.isNetworkAvailable(requireContext()))
        {
            myDrinkingHabitQuestionsAPICall()
        }
        else{
            CommonClass.showInternetDialogue(requireContext())
        }

        binding.previousQuestion.visibility = View.GONE

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.backIcon.setOnClickListener {
            val res = SharedPreferencesUtil.getData(requireContext(), "courseIdBasicKnowledge", "")
            val basicKnowledgeFragment = BasicKnowledgeFragment.newInstanceBasicKnowledge(res.toInt())
            loadFragment(basicKnowledgeFragment)
        }

        binding.nextQuestion.setOnClickListener {

            val request = createSaveMyDrinkingHabitAnswerRequest(myDrinkingHabitResponse)

            if(request != null)
            {
                saveAnswerAPICall(request)
            }
            loadFragment(MyDrinkingHabitScreenTwoFragment())
        }
        initializeOptions()

    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
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

            binding.description.text = myDrinkingHabitResponse.answersList[0].description
            val answer = myDrinkingHabitResponse.answersList[0]
            val questionnaireId = answer.questionnaireId ?: ""
            val question = answer.question ?: ""
            binding.tvQuestionOne.text = "$questionnaireId. $question"

           if(myDrinkingHabitResponse.answersList[0].options.size >= 4)
           {
               binding.none.text = myDrinkingHabitResponse.answersList[0].options[0].optionValue
               binding.lessThanTwo.text = myDrinkingHabitResponse.answersList[0].options[1].optionValue
               binding.threeToFive.text = myDrinkingHabitResponse.answersList[0].options[2].optionValue
               binding.almostEveryday.text = myDrinkingHabitResponse.answersList[0].options[3].optionValue
               binding.everyday.text = myDrinkingHabitResponse.answersList[0].options[4].optionValue
           }

            // Check if there is a patientAnswer and set the selected option
            answer.patientAnswer?.let { patientAnswer ->
                answer.options.forEachIndexed { index, option ->
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
            binding.none,
            binding.lessThanTwo,
            binding.threeToFive,
            binding.almostEveryday,
            binding.everyday
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
            binding.none,
            binding.lessThanTwo,
            binding.threeToFive,
            binding.almostEveryday,
            binding.everyday
        )

        if (index in options.indices) {
            val textView = options[index]
            textView.setBackgroundResource(R.drawable.card_default_background)
            textView.setTextColor(Color.parseColor("#424242"))
        }
    }

    private fun selectOption(index: Int) {
        val options = listOf(
            binding.none,
            binding.lessThanTwo,
            binding.threeToFive,
            binding.almostEveryday,
            binding.everyday
        )

        if (index in options.indices) {
            val textView = options[index]
            textView.setBackgroundResource(R.drawable.card_selected_background)
            textView.setTextColor(Color.parseColor("#FFFFFF"))
        }
    }

    private fun getSelectedOption(): Option? {
        val options = myDrinkingHabitResponse.answersList[0].options
        return if (selectedOptionIndex in options.indices) {
            options[selectedOptionIndex]
        } else {
            null
        }
    }

    private fun createSaveMyDrinkingHabitAnswerRequest(response: MyDrinkingHabitResponse): SaveMyDrinkingHabitAnswerRequest? {
        val selectedOption = getSelectedOption()
        return if (selectedOption != null) {
            SaveMyDrinkingHabitAnswerRequest(
                answerId = response.answersList[0].answerId,
                assessmentId = 0,
                clientId = loginResponse.loginDetails.clientID,
                optionId = selectedOption.optionId,
                optionValue = selectedOption.optionValue,
                patientId = loginResponse.loginDetails.patientID,
                plId = loginResponse.loginDetails.patientLocationID,
                quantity = 0,
                questionnaireId = response.answersList[0].questionnaireId
            )
        } else {
            null
        }
    }

    private fun saveAnswerAPICall(request: SaveMyDrinkingHabitAnswerRequest)
    {

        saveMyDrinkHabitAnswerViewModel.saveMyDrinkHabitAnswer(request,accessToken)

    }

    private fun saveAnswerObserveIewModel()
    {

    }

}
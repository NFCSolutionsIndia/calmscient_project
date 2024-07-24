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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.calmscient.R
import com.calmscient.databinding.FragmentMyDrinkingHabitScreenThreeBinding
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
class MyDrinkingHabitScreenThreeFragment : Fragment() {

    private lateinit var binding: FragmentMyDrinkingHabitScreenThreeBinding
    private var selectedOptionIndex = -1

    private val getPatientBasicKnowledgeCourseViewModel: GetPatientBasicKnowledgeCourseViewModel by activityViewModels()
    private lateinit var myDrinkingHabitResponse: MyDrinkingHabitResponse

    private val saveMyDrinkHabitAnswerViewModel : SaveMyDrinkHabitAnswerViewModel by activityViewModels()


    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonDialog: CommonAPICallDialog
    private lateinit var accessToken: String
    private lateinit var loginResponse: LoginResponse

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().supportFragmentManager.popBackStack()
            requireActivity().supportFragmentManager.popBackStack()
            requireActivity().supportFragmentManager.popBackStack()


        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyDrinkingHabitScreenThreeBinding.inflate(inflater, container, false)

        accessToken = SharedPreferencesUtil.getData(requireContext(), "accessToken", "")
        customProgressDialog = CustomProgressDialog(requireContext())
        commonDialog = CommonAPICallDialog(requireContext())

        val loginJsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString<LoginResponse>(loginJsonString)


        binding.backIcon.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
            requireActivity().supportFragmentManager.popBackStack()
            requireActivity().supportFragmentManager.popBackStack()
        }

        if (CommonClass.isNetworkAvailable(requireContext())) {
            myDrinkingHabitQuestionsAPICall()
        } else {
            CommonClass.showInternetDialogue(requireContext())
        }

        // Initialize card views and set click listeners
        initializeCardViews()

        binding.nextQuestion.setOnClickListener {
            val request = createSaveMyDrinkingHabitAnswerRequest(myDrinkingHabitResponse)

            if(request != null)
            {
                saveAnswerAPICall(request)
            }
            loadFragment(MyDrinkingHabitScreenFourFragment())
        }
        binding.previousQuestion.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        return binding.root
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun initializeCardViews() {
        val cardViewOne = binding.cardViewOne
        val cardViewTwo = binding.cardViewTwo
        val cardViewThree = binding.cardViewThree
        val cardViewFour = binding.cardViewFour

        cardViewOne.setOnClickListener { onOptionClicked(0) }
        cardViewTwo.setOnClickListener { onOptionClicked(1) }
        cardViewThree.setOnClickListener { onOptionClicked(2) }
        cardViewFour.setOnClickListener { onOptionClicked(3) }
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
        val cardViews = listOf(
            Pair(binding.cardViewOne, Pair(R.id.headingOne, R.id.descOne)),
            Pair(binding.cardViewTwo, Pair(R.id.headingTwo, R.id.descTwo)),
            Pair(binding.cardViewThree, Pair(R.id.headingThree, R.id.descThree)),
            Pair(binding.cardViewFour, Pair(R.id.headingFour, R.id.descFour))
        )
        if (index in cardViews.indices) {
            val (layout, textViewIds) = cardViews[index]
            layout.setBackgroundResource(R.drawable.card_default_background)
            val textViewOne = layout.findViewById<TextView>(textViewIds.first)
            val textViewTwo = layout.findViewById<TextView>(textViewIds.second)
            textViewOne?.setTextColor(Color.parseColor("#424242"))
            textViewTwo?.setTextColor(Color.parseColor("#424242"))
        }
    }

    private fun selectOption(index: Int) {
        val cardViews = listOf(
            Pair(binding.cardViewOne, Pair(R.id.headingOne, R.id.descOne)),
            Pair(binding.cardViewTwo, Pair(R.id.headingTwo, R.id.descTwo)),
            Pair(binding.cardViewThree, Pair(R.id.headingThree, R.id.descThree)),
            Pair(binding.cardViewFour, Pair(R.id.headingFour, R.id.descFour))
        )
        if (index in cardViews.indices) {
            val (layout, textViewIds) = cardViews[index]
            layout.setBackgroundResource(R.drawable.card_selected_background)
            val textViewOne = layout.findViewById<TextView>(textViewIds.first)
            val textViewTwo = layout.findViewById<TextView>(textViewIds.second)
            textViewOne?.setTextColor(Color.parseColor("#FFFFFF"))
            textViewTwo?.setTextColor(Color.parseColor("#FFFFFF"))
        }
    }

    private fun myDrinkingHabitQuestionsAPICall() {
        getPatientBasicKnowledgeCourseViewModel.clear()
        getPatientBasicKnowledgeCourseViewModel.saveResponseLiveData = MutableLiveData(null)
        getPatientBasicKnowledgeCourseViewModel.successLiveData = MutableLiveData(false)
        getPatientBasicKnowledgeCourseViewModel.loadingLiveData = MutableLiveData(false)
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

        if (myDrinkingHabitResponse.answersList.isNotEmpty() && myDrinkingHabitResponse.answersList.size >=2) {

            binding.description.text = myDrinkingHabitResponse.answersList[2].description
            val answer = myDrinkingHabitResponse.answersList[2]
            val questionnaireId = answer.questionnaireId ?: ""
            val question = answer.question ?: ""
            binding.tvQuestionThree.text = "$questionnaireId. $question"

            binding.scrollView.visibility = View.VISIBLE

            // Check if there is a patientAnswer and set the selected option
            answer.patientAnswer?.let { patientAnswer ->
                answer.options.forEachIndexed { index, option ->
                    if (option.optionId ==  patientAnswer.toInt()) {
                        selectedOptionIndex = index
                        selectOption(index)
                    }
                }
            }

        }

    }

    private fun getSelectedOption(): Option? {
        val options = myDrinkingHabitResponse.answersList[2].options
        return if (selectedOptionIndex in options.indices) {
            options[selectedOptionIndex]
        } else {
            null
        }
    }

    private fun createSaveMyDrinkingHabitAnswerRequest(response: MyDrinkingHabitResponse): SaveMyDrinkingHabitAnswerRequest? {
        val selectedOption = getSelectedOption()
        return if (selectedOption != null && response.answersList.size >=2 ) {
            SaveMyDrinkingHabitAnswerRequest(
                answerId = response.answersList[2].answerId,
                assessmentId = 0,
                clientId = loginResponse.loginDetails.clientID,
                optionId = selectedOption.optionId,
                optionValue = selectedOption.optionValue,
                patientId = loginResponse.loginDetails.patientID,
                plId = loginResponse.loginDetails.patientLocationID,
                quantity = 0,
                questionnaireId = response.answersList[2].questionnaireId
            )
        } else {
            null
        }
    }

    private fun saveAnswerAPICall(request: SaveMyDrinkingHabitAnswerRequest)
    {

        saveMyDrinkHabitAnswerViewModel.saveMyDrinkHabitAnswer(request,accessToken)

    }
}

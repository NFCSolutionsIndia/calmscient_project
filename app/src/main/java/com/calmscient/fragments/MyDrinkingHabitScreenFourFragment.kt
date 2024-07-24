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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.calmscient.R
import com.calmscient.databinding.FragmentMyDrinkingHabitScreenFourBinding
import com.calmscient.di.remote.request.SaveMyDrinkingHabitAnswerRequest
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.di.remote.response.MyDrinkingHabitResponse
import com.calmscient.di.remote.response.Option
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.GetPatientBasicKnowledgeCourseViewModel
import com.calmscient.viewmodels.SaveMyDrinkHabitAnswerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyDrinkingHabitScreenFourFragment : Fragment() {

    private lateinit var binding: FragmentMyDrinkingHabitScreenFourBinding
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
            val res = SharedPreferencesUtil.getData(requireContext(), "courseIdBasicKnowledge", "")
            val basicKnowledgeFragment = BasicKnowledgeFragment.newInstanceBasicKnowledge(res.toInt())
            loadFragment(basicKnowledgeFragment)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMyDrinkingHabitScreenFourBinding.inflate(inflater,container,false)

        accessToken = SharedPreferencesUtil.getData(requireContext(), "accessToken", "")
        customProgressDialog = CustomProgressDialog(requireContext())
        commonDialog = CommonAPICallDialog(requireContext())

        val loginJsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString<LoginResponse>(loginJsonString)

        binding.backIcon.setOnClickListener {
            val res = SharedPreferencesUtil.getData(requireContext(), "courseIdBasicKnowledge", "")
            val basicKnowledgeFragment = BasicKnowledgeFragment.newInstanceBasicKnowledge(res.toInt())
            loadFragment(basicKnowledgeFragment)
        }

        if (CommonClass.isNetworkAvailable(requireContext())) {
            myDrinkingHabitQuestionsAPICall()
        } else {
            CommonClass.showInternetDialogue(requireContext())
        }

        binding.previousQuestion.setOnClickListener{
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.nextQuestion.setOnClickListener{
            val request = createSaveMyDrinkingHabitAnswerRequest(myDrinkingHabitResponse)
            if(request != null)
            {
                saveAnswerAPICall(request)
            }
            loadFragment(MyDrinkingHabitScreenFiveFragment())
        }

        binding.yesButton.setOnClickListener{
            val request = createSaveMyDrinkingHabitAnswerRequest(myDrinkingHabitResponse)
            if(request != null)
            {
                saveAnswerAPICall(request)
            }
            loadFragment(MyDrinkingHabitScreenFiveFragment())
        }

        return binding.root

    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
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

        if (myDrinkingHabitResponse.answersList.isNotEmpty() && myDrinkingHabitResponse.answersList.size >=3) {

            binding.description.text = myDrinkingHabitResponse.answersList[3].description
            val answer = myDrinkingHabitResponse.answersList[3]
            val questionnaireId = answer.questionnaireId ?: ""
            val question = answer.question ?: ""
            binding.tvQuestionFour.text = "$questionnaireId. $question"

            binding.etReson.setText(answer.answerText)
            selectedOptionIndex = answer.options[0].optionId

        }

    }

    private fun getSelectedOption():String
    {
        return  binding.etReson.text.toString()
    }

    private fun createSaveMyDrinkingHabitAnswerRequest(response: MyDrinkingHabitResponse): SaveMyDrinkingHabitAnswerRequest? {
        val selectedOption = getSelectedOption()
        return if (selectedOption != null && response.answersList.size >=3 ) {
            SaveMyDrinkingHabitAnswerRequest(
                answerId = response.answersList[3].answerId,
                assessmentId = 0,
                clientId = loginResponse.loginDetails.clientID,
                optionId = selectedOptionIndex,
                optionValue = selectedOption,
                patientId = loginResponse.loginDetails.patientID,
                plId = loginResponse.loginDetails.patientLocationID,
                quantity = 0,
                questionnaireId = response.answersList[3].questionnaireId
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
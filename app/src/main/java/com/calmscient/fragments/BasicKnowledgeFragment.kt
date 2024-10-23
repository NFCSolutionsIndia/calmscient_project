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
import androidx.recyclerview.widget.LinearLayoutManager
import com.calmscient.R
import com.calmscient.adapters.BasicKnowledgeAdapter
import com.calmscient.databinding.FragmentBasicKnowledgeBinding
import com.calmscient.di.remote.BasicKnowledgeItem
import com.calmscient.utils.NonSwipeRecyclerView

import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.calmscient.di.remote.response.BasicKnowledgeIndex
import com.calmscient.di.remote.response.GetBasicKnowledgeIndexResponse
import com.calmscient.di.remote.response.GetTakingControlIndexResponse
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.BasicKnowledgeSharedViewModel
import com.calmscient.viewmodels.GetBasicKnowledgeIndexDataViewModel
import com.calmscient.viewmodels.UpdateTakingControlIndexViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BasicKnowledgeFragment : Fragment() {

    private lateinit var binding: FragmentBasicKnowledgeBinding
    private lateinit var recyclerView: NonSwipeRecyclerView

    private val getBasicKnowledgeIndexDataViewModel: GetBasicKnowledgeIndexDataViewModel by viewModels()
    private val sharedViewModel: BasicKnowledgeSharedViewModel by activityViewModels()
    private val updateTakingControlIndexViewModel: UpdateTakingControlIndexViewModel by activityViewModels()


    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonDialog: CommonAPICallDialog
    private  lateinit var accessToken : String
    private lateinit var getBasicKnowledgeIndexResponse: GetBasicKnowledgeIndexResponse
    private lateinit var loginResponse: LoginResponse
    private lateinit var basicKnowledgeAdapter: BasicKnowledgeAdapter
    private var completedItemCount = 0

    private var courseTempId = 0
    companion object {
        fun newInstanceBasicKnowledge(
            courseId: Int
        ): BasicKnowledgeFragment {
            val fragment = BasicKnowledgeFragment()
            val args = Bundle()
            args.putInt("courseId",courseId)

            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this){
            loadFragment(TakingControlFragment())
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBasicKnowledgeBinding.inflate(inflater, container, false)

        accessToken = SharedPreferencesUtil.getData(requireContext(), "accessToken", "")
        customProgressDialog = CustomProgressDialog(requireContext())
        commonDialog = CommonAPICallDialog(requireContext())

        courseTempId = arguments?.getInt("courseId")!!

        val loginJsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString<LoginResponse>(loginJsonString)

        recyclerView = binding.basicKnowledgeRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        binding.backIcon.setOnClickListener{
            loadFragment(TakingControlFragment())
        }
        binding.completeButton.setOnClickListener{
            loadFragment(TakingControlFragment())
        }
        getIndexDataAPICall()

        return binding.root
    }

    private fun getIndexDataAPICall()
    {

        getBasicKnowledgeIndexDataViewModel.getBasicKnowledgeIndexData(loginResponse.loginDetails.clientID,loginResponse.loginDetails.patientID,accessToken)
        observeIndexViewModel()

    }

    private fun observeIndexViewModel()
    {
        getBasicKnowledgeIndexDataViewModel.loadingLiveData.observe(viewLifecycleOwner, Observer { isLodaing->
            if(isLodaing)
            {
                customProgressDialog.show(getString(R.string.loading))
            }
            else
            {
                customProgressDialog.dialogDismiss()
            }
        })

        getBasicKnowledgeIndexDataViewModel.successLiveData.observe(viewLifecycleOwner, Observer { isSuccess->
            if(isSuccess)
            {
                getBasicKnowledgeIndexDataViewModel.saveResponseLiveData.observe(viewLifecycleOwner,Observer { successData->
                        if(successData != null)
                        {
                            getBasicKnowledgeIndexResponse = successData
                            bindDataToRecyclerView(successData.index)
                            checkAllLessonsCompletedOrNot(successData.index)
                        }
                    }
                )
            }
        })

    }

    private fun checkAllLessonsCompletedOrNot(indexItems: List<BasicKnowledgeIndex>) {
        completedItemCount = indexItems.count { it.isCompleted == 1 }
        if (completedItemCount == indexItems.size) {
            updateIndexApiCall()
        }
        else{
            Log.d("Total Completed ","$completedItemCount")
        }
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

    private fun bindDataToRecyclerView(indexItems: List<BasicKnowledgeIndex>) {
        val items = indexItems.map {
            BasicKnowledgeItem(
                name = it.sectionName,
                tickImg = if (it.isCompleted == 1) R.drawable.basic_knowledge_complete_icon else null,
                isCompleted = it.isCompleted == 1,
                sectionId = it.sectionId
            )
        }
        basicKnowledgeAdapter = BasicKnowledgeAdapter(
            fragmentManager = requireActivity().supportFragmentManager,
            context = requireContext(),
            items = items
        ) { item ->
            handleItemClicked(item)
        }
        recyclerView.adapter = basicKnowledgeAdapter
    }

    private fun handleItemClicked(item: BasicKnowledgeItem) {
        Log.d("BasicKnowledgeFragment", "Name: ${item.name}, SectionId: ${item.sectionId}")
        sharedViewModel.selectItem(item)
        //val currentName = item.name.trim()

        val currentName = item.name.trim().replace("\"", "")
        Log.d("BasicKnowledgeFragment", "Name: ${getString(R.string.what_s_a_standard_drink)}")
        val fragment = when {
            currentName.equals(getString(R.string.what_s_a_standard_drink), ignoreCase = true) -> StandardDrinkFragment()
            currentName.equals(getString(R.string.what_are_the_u_s_guidelines_for_drink), ignoreCase = true) -> GuidelinesForDrinkingFragment()
            currentName.equals(getString(R.string.when_is_drink_in_moderation_too_much), ignoreCase = true) -> ModerationDrinkingFragment()
            currentName.equals(getString(R.string.what_happens_to_your_brain_when_you_drink), ignoreCase = true) -> WhatHappensToYourBrainFragment("Basicknowledge")
            currentName.equals(getString(R.string.what_to_expect_when_you_quit_drinking), ignoreCase = true) -> WhatExpectsWhenYouQuitDrinkingFragment("Basicknowledge")
            currentName.equals(getString(R.string.what_are_the_consequences), ignoreCase = true) -> ConsequencesFragment()
            currentName.equals(getString(R.string.my_drinking_habit), ignoreCase = true) -> MyDrinkingHabitFragment()
            else -> null
        }


        fragment?.let {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.flFragment, it)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}

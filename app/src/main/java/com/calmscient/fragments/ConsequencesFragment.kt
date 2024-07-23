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

// ConsequencesFragment.kt
package com.calmscient.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.R
import com.calmscient.adapters.ConsequencesAdapter
import com.calmscient.databinding.FragmentConsequencesBinding
import com.calmscient.di.remote.BasicKnowledgeItem
import com.calmscient.di.remote.ConsequencesDataClass
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.BasicKnowledgeSharedViewModel
import com.calmscient.viewmodels.UpdateBasicKnowledgeIndexDataViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConsequencesFragment : Fragment() {
    private lateinit var binding: FragmentConsequencesBinding
    private lateinit var consequencesAdapter: ConsequencesAdapter
    private val consequencesData = mutableListOf<ConsequencesDataClass>()
    private var currentQuestionIndex = 0
    private var previousQuestionIndex = -1
    private lateinit var stepIndicators: List<ImageView>
    private val maxProgress = 99
    private lateinit var progressBar: ProgressBar

    private val updateBasicKnowledgeIndexDataViewModel: UpdateBasicKnowledgeIndexDataViewModel by viewModels()
    private val sharedViewModel: BasicKnowledgeSharedViewModel by activityViewModels()
    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonDialog: CommonAPICallDialog
    private  lateinit var accessToken : String
    private lateinit var loginResponse: LoginResponse
    private lateinit var itemTemp: BasicKnowledgeItem


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentConsequencesBinding.inflate(inflater, container, false)
        val view = binding.root

        accessToken = SharedPreferencesUtil.getData(requireContext(), "accessToken", "")
        customProgressDialog = CustomProgressDialog(requireContext())
        commonDialog = CommonAPICallDialog(requireContext())

        val loginJsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString<LoginResponse>(loginJsonString)

        val pagerSnapHelper = PagerSnapHelper()
        pagerSnapHelper.attachToRecyclerView(binding.consequencesRecyclerView)

        binding.previousQuestion.visibility = View.GONE

        // Find your ProgressBar by its ID
        progressBar = binding.anxietyAndDietProgressBar

        // Set the initial progress
        progressBar.progress = currentQuestionIndex * (maxProgress / (consequencesData.size - 1))


        binding.backIcon.setOnClickListener{
            requireActivity().supportFragmentManager.popBackStack()
        }

        setupNavigation()
        initializeAdapter()

        displayConsequencesData()

        stepIndicators = listOf(
            binding.step1Indicator,
            binding.step2Indicator,
            binding.step3Indicator,
            binding.step4Indicator,
            binding.step5Indicator,
            binding.step6Indicator
        )

        // Observe the selected item
        sharedViewModel.selectedItem.observe(viewLifecycleOwner, Observer { item ->
            if (item != null) {
                // Use item data as needed
                Log.d("GuidelinesForDrinkingFragment", "Name: ${item.name}, SectionId: ${item.sectionId}")
                itemTemp = item
                updateBasicKnowledgeAPICall()
            }
        })

        return view
    }

    private fun initializeAdapter() {
        binding.consequencesRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        consequencesAdapter = ConsequencesAdapter(requireContext(),consequencesData)
        binding.consequencesRecyclerView.adapter = consequencesAdapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun displayConsequencesData() {
        // Add duplicate data to the list
        consequencesData.add(
            ConsequencesDataClass(
                headingText = null,
                dialogText = null,
                descriptionText = getString(R.string.consequences_card_1_description),
                pointsText = null
            )
        )
        consequencesData.add(
            ConsequencesDataClass(
                headingText = getString(R.string.consequences_card_2_heading),
                dialogText = getString(R.string.consequences_card_2_dilaog),
                descriptionText = getString(R.string.consequences_card_2_description),
                pointsText = getString(R.string.consequences_card_2_points)
            )
        )

        consequencesData.add(
            ConsequencesDataClass(
                headingText = getString(R.string.consequences_card_3_heading),
                dialogText = null,
                descriptionText = getString(R.string.consequences_card_3_description),
                pointsText = getString(R.string.consequences_card_3_points)
            )
        )

        consequencesData.add(
            ConsequencesDataClass(
                headingText = getString(R.string.consequences_card_4_heading),
                dialogText = null,
                descriptionText = getString(R.string.consequences_card_4_description),
                pointsText = getString(R.string.consequences_card_4_points)
            )
        )

        consequencesData.add(
            ConsequencesDataClass(
                headingText = getString(R.string.consequences_card_5_heading),
                dialogText = null,
                descriptionText = getString(R.string.consequences_card_5_description),
                pointsText = null
            )
        )

        consequencesData.add(
            ConsequencesDataClass(
                headingText = getString(R.string.consequences_card_6_heading),
                dialogText = null,
                descriptionText = getString(R.string.consequences_card_6_description),
                pointsText = null
            )
        )

        consequencesAdapter.notifyDataSetChanged()
    }

    private fun setupNavigation() {
        binding.nextQuestion.setOnClickListener {
            navigateToQuestion(currentQuestionIndex + 1, true)
        }

        binding.previousQuestion.setOnClickListener {
            navigateToQuestion(currentQuestionIndex - 1, false)
        }

       /* binding.consequencesRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Check if the user is scrolling horizontally
                if (Math.abs(dx) > Math.abs(dy)) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    if (firstVisibleItemPosition != currentQuestionIndex) {
                        previousQuestionIndex = currentQuestionIndex
                        currentQuestionIndex = firstVisibleItemPosition

                        // Calculate and set the progress based on the current question index
                        progressBar.progress = currentQuestionIndex * (maxProgress / (consequencesData.size - 1))
                        // Update the step indicators (ImageViews) as active or inactive
                        updateStepIndicators()
                    }
                }
            }
        })*/
    }

    private fun updateStepIndicators() {

        // Update the current step indicator to active
        if (currentQuestionIndex >= 0 && currentQuestionIndex < stepIndicators.size) {
            stepIndicators[currentQuestionIndex].setImageResource(R.drawable.ic_activetickmark)
        }

        if (currentQuestionIndex == 0) {
            binding.previousQuestion.visibility = View.GONE
        } else {
            binding.previousQuestion.visibility = View.VISIBLE
        }

        if (currentQuestionIndex == stepIndicators.size - 1) {
            binding.nextQuestion.visibility = View.GONE
        } else {
            binding.nextQuestion.visibility = View.VISIBLE
        }
    }

    private fun navigateToQuestion(index: Int, isNext: Boolean) {
        if (index in 0 until consequencesData.size) {
            if (isNext) {
                previousQuestionIndex = currentQuestionIndex
            } else {
                // Update the current step indicator to inactive when going to the previous question
                if (currentQuestionIndex >= 0 && currentQuestionIndex < stepIndicators.size) {
                    stepIndicators[currentQuestionIndex].setImageResource(R.drawable.ic_inactivetickmark)
                }
            }
            currentQuestionIndex = index
            binding.consequencesRecyclerView.smoothScrollToPosition(currentQuestionIndex)
            // Calculate and set the progress based on the current question index
            progressBar.progress = currentQuestionIndex * (maxProgress / (consequencesData.size - 1))
            // Update the step indicators (ImageViews) as active or inactive
            updateStepIndicators()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun updateBasicKnowledgeAPICall()
    {
        updateBasicKnowledgeIndexDataViewModel.updateBasicKnowledgeIndexData(1,loginResponse.loginDetails.patientID,itemTemp.sectionId,accessToken)
        observeBasicKnowledgeViewModel()
    }

    private fun observeBasicKnowledgeViewModel()
    {
        updateBasicKnowledgeIndexDataViewModel.loadingLiveData.observe(viewLifecycleOwner, Observer { isLoading->
            if(isLoading)
            {
                customProgressDialog.show(getString(R.string.loading))
            }
            else
            {
                customProgressDialog.dialogDismiss()
            }
        })
    }
}

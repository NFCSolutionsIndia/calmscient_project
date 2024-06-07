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

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.runtime.currentRecomposeScope
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.calmscient.R
import com.calmscient.adapters.TakingControlScreeningAdapter
import com.calmscient.databinding.FragmentTakingControlIntroductionBinding
import com.calmscient.di.remote.TakingControlScreeningItem
import com.calmscient.di.remote.response.GetTakingControlIntroductionResponse
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.di.remote.response.ScreeningItem
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.NonSwipeRecyclerView
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.FlagsViewModel
import com.calmscient.viewmodels.GetTakingControlIntroductionViewModel
import com.calmscient.viewmodels.ScreeningViewModel

class TakingControlIntroductionFragment : Fragment() {

    private lateinit var binding: FragmentTakingControlIntroductionBinding
    private lateinit var recyclerView: NonSwipeRecyclerView
    private val screeningsMenuViewModel: ScreeningViewModel by activityViewModels()
    private var screeningResponse: List<ScreeningItem> = emptyList()
    private var loginResponse: LoginResponse? = null
    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonDialog: CommonAPICallDialog
    private lateinit var accessToken: String
    private var currentScreenIndex = -1

    private val getTakingControlIntroductionViewModel: GetTakingControlIntroductionViewModel by activityViewModels()
    private var getTakingControlIntroductionResponse: GetTakingControlIntroductionResponse? = null

    private val flagsViewModel: FlagsViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Listen for the fragment result
        parentFragmentManager.setFragmentResultListener("currentScreenIndex", this) { _, bundle ->
            val result = bundle.getInt("currentScreenIndex")

            // Handle the result data here
            if(result == 3) {
                currentScreenIndex = 3
                Log.d("Result : ","$result")
                //Toast.makeText(requireContext(), "Received result: $result", Toast.LENGTH_LONG).show()
                // Handle the logic to show the appropriate screen or perform any other action
                binding.screenOne.visibility = View.GONE
                binding.screenTwo.visibility = View.GONE
                binding.screenThree.visibility = View.VISIBLE
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentTakingControlIntroductionBinding.inflate(inflater, container, false)
        customProgressDialog = CustomProgressDialog(requireContext())
        commonDialog = CommonAPICallDialog(requireContext())
        val loginJsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString(loginJsonString)
        accessToken = SharedPreferencesUtil.getData(requireContext(), "accessToken", "")

        if (CommonClass.isNetworkAvailable(requireContext())) {
            observeViewModel()
            introductionApiCall()
        } else {
            CommonClass.showInternetDialogue(requireContext())
        }

        binding.firstScreenNextButton.setOnClickListener {
            currentScreenIndex = 2
            binding.screenOne.visibility = View.GONE
            binding.screenThree.visibility = View.GONE
            binding.screenFour.visibility = View.GONE
            binding.screenTwo.visibility = View.VISIBLE
        }

        recyclerView = binding.takingControlScreeningRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        binding.thirdScreenNextButton.setOnClickListener {
            currentScreenIndex = 4
            binding.screenOne.visibility = View.GONE
            binding.screenTwo.visibility = View.GONE
            binding.screenThree.visibility = View.GONE
            binding.screenFour.visibility = View.VISIBLE
        }

        binding.takingControlScreeningBulbIcon.setOnClickListener {
            showCustomDialog(requireContext())
        }
        binding.backIcon.setOnClickListener{
            if(currentScreenIndex == 4)
            {

                //Toast.makeText(requireContext(), "Received result: APICALL", Toast.LENGTH_LONG).show()
                saveIntroductionRequestApiCall()
            }
            loadFragment(TakingControlFragment())
        }

        binding.dontShowCheckBox.setOnCheckedChangeListener { _, isChecked ->
            // Update the ViewModel with the checkbox state
            flagsViewModel.updateTutorialFlag(isChecked)
        }

        return binding.root
    }

    private fun getScreeningItems(): List<TakingControlScreeningItem> {
        val introduction = getTakingControlIntroductionResponse?.takingControlIntroduction ?: return emptyList()
        return listOf(
            TakingControlScreeningItem("AUDIT", "Doesn't apply for me", introduction.auditFlag != 1),
            TakingControlScreeningItem("DAST-10", "Doesn't apply for me", introduction.dastFlag != 1),
            TakingControlScreeningItem("CAGE", "Doesn't apply for me", introduction.cageFlag != 1)
        )
    }

    private fun observeViewModel() {
        loginResponse?.loginDetails?.let {
            screeningsMenuViewModel.getScreeningList(it.patientID, it.clientID, it.patientLocationID, accessToken)
        }

        screeningsMenuViewModel.loadingLiveData.observe(requireActivity()) { isLoading ->
            if (isLoading) {
                customProgressDialog.show("Loading...")
            } else {
                customProgressDialog.dialogDismiss()
            }
        }

        screeningsMenuViewModel.screeningListLiveData.observe(viewLifecycleOwner, Observer { successData ->
            if (successData != null) {
                Log.d("TCF:", "$successData")
                screeningResponse = successData
                updateAdapter()
            }
        })
    }

    private fun updateAdapter() {
        if (getTakingControlIntroductionResponse != null) {
            recyclerView.adapter = TakingControlScreeningAdapter(
                requireActivity().supportFragmentManager,
                getScreeningItems(),
                requireContext(),
                screeningResponse
            )
            binding.takingControlScreeningRecyclerView.adapter = recyclerView.adapter
        }
    }

    private fun showCustomDialog(context: Context) {
        val dialogView = LayoutInflater.from(context)
            .inflate(R.layout.taking_control_introduction_dialog_item, null)
        val dialog = android.app.AlertDialog.Builder(context, R.style.CustomDialog)
            .setView(dialogView)
            .create()
        val closeIcon = dialogView.findViewById<ImageView>(R.id.dialogClose)
        dialog.show()
        closeIcon.setOnClickListener { dialog.dismiss() }
    }

    private fun introductionApiCall() {
        loginResponse?.loginDetails?.let {
            getTakingControlIntroductionViewModel.getTakingControlIntroductionData(
                it.clientID,it.patientID,it.patientLocationID, accessToken)
        }
        observeIntroductionData()
    }

    private fun observeIntroductionData() {
        getTakingControlIntroductionViewModel.loadingLiveData.observe(viewLifecycleOwner, Observer { isLoading ->
            if (isLoading) {
                customProgressDialog.show("Loading")
            } else {
                customProgressDialog.dialogDismiss()
            }
        })

        getTakingControlIntroductionViewModel.successLiveData.observe(viewLifecycleOwner, Observer { isSuccess ->
            if (isSuccess) {
                getTakingControlIntroductionViewModel.saveResponseLiveData.observe(viewLifecycleOwner, Observer { successData ->
                    if (successData != null) {
                        getTakingControlIntroductionResponse = successData
                        Log.d("Intro Fragment : ", "$getTakingControlIntroductionResponse")
                        updateAdapter()

                        binding.dontShowCheckBox.isChecked = getTakingControlIntroductionResponse?.takingControlIntroduction?.tutorialFlag  == 1
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

    private fun saveIntroductionRequestApiCall() {


    }


}

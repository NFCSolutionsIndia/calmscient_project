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
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.calmscient.R
import com.calmscient.adapters.PayloadCallback
import com.calmscient.adapters.TakingControlScreeningAdapter
import com.calmscient.databinding.FragmentTakingControlIntroductionBinding
import com.calmscient.di.remote.TakingControlScreeningItem
import com.calmscient.di.remote.request.SaveTakingControlIntroductionRequest
import com.calmscient.di.remote.response.GetTakingControlIndexResponse
import com.calmscient.di.remote.response.GetTakingControlIntroductionResponse
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.di.remote.response.SaveTakingControlIntroductionResponse
import com.calmscient.di.remote.response.ScreeningItem
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.NonSwipeRecyclerView
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.GetTakingControlIntroductionViewModel
import com.calmscient.viewmodels.SaveTakingControlIntroductionDataViewModel
import com.calmscient.viewmodels.ScreeningViewModel
import com.calmscient.viewmodels.UpdateTakingControlIndexViewModel
import com.google.gson.Gson

class TakingControlIntroductionFragment : Fragment(), PayloadCallback {

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

    private val saveTakingControlIntroductionDataViewModel: SaveTakingControlIntroductionDataViewModel by activityViewModels()
    private var saveTakingControlIntroductionResponse: SaveTakingControlIntroductionResponse? = null


    private val updateTakingControlIndexViewModel: UpdateTakingControlIndexViewModel by activityViewModels()
    private lateinit var getTakingControlIndexResponse: GetTakingControlIndexResponse

    private var courseTempId = 0

    companion object {
        fun newInstance(
            takingControlIndexResponse: GetTakingControlIndexResponse,
            courseId: Int
        ): TakingControlIntroductionFragment {
            val fragment = TakingControlIntroductionFragment()
            val args = Bundle()
            val gson = Gson()
            val appointmentDetailsJson = gson.toJson(takingControlIndexResponse)
            args.putString("takingControlIndexResponse", appointmentDetailsJson)
            args.putInt("courseId",courseId)

            fragment.arguments = args
            return fragment
        }
    }


    override fun onPayloadConstructed(item: TakingControlScreeningItem) {
        constructAndSendApiRequest(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Listen for the fragment result
        parentFragmentManager.setFragmentResultListener("currentScreenIndex", this) { _, bundle ->
            val result = bundle.getInt("currentScreenIndex")

            // Handle the result data here
            if (result == 3) {
                currentScreenIndex = 3
                Log.d("Result : ", "$result")
                //Toast.makeText(requireContext(), "Received result: $result", Toast.LENGTH_LONG).show()
                // Handle the logic to show the appropriate screen or perform any other action
                binding.screenOne.visibility = View.GONE
                binding.screenTwo.visibility = View.GONE
                binding.screenThree.visibility = View.VISIBLE
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            loadFragment(TakingControlFragment())
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


        val takingControlIndexJson = arguments?.getString("takingControlIndexResponse")
        courseTempId = arguments?.getInt("courseId")!!

        Log.d("CourseTempId : ","$courseTempId")

        val gson = Gson()
        getTakingControlIndexResponse =
            gson.fromJson(takingControlIndexJson, GetTakingControlIndexResponse::class.java)

        Log.d("getTakingControlIndexResponse", "$getTakingControlIndexResponse")

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

        binding.secondScreenNextButton.setOnClickListener{
            currentScreenIndex = 3
            binding.screenOne.visibility = View.GONE
            binding.screenTwo.visibility = View.GONE
            binding.screenThree.visibility = View.VISIBLE
            binding.screenFour.visibility = View.GONE
        }

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
        binding.backIcon.setOnClickListener {
            if (currentScreenIndex == 4) {
                updateIndexApiCall()
            }
            loadFragment(TakingControlFragment())
        }

        binding.dontShowCheckBox.setOnCheckedChangeListener { _, isChecked ->

            val request = loginResponse?.loginDetails?.let {
                SaveTakingControlIntroductionRequest(
                    auditFlag = null,
                    cageFlag = null,
                    clientId = it.clientID,
                    dastFlag = null,
                    introductionFlag = 1,
                    patientId = it.patientID,
                    plId = it.patientLocationID,
                    tutorialFlag = if (isChecked) 1 else 0
                )
            }

            Log.d("dontShowCheckBox", "Payload constructed: $request")

            if (request != null) {
                saveTakingControlIntroductionDataViewModel.saveTakingControlIntroductionData(
                    request,
                    accessToken
                )
            }

            observeSaveViewModel()

        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()

       if(currentScreenIndex == 4)
       {
           updateIndexApiCall()
       }
    }

    private fun getScreeningItems(): List<TakingControlScreeningItem> {
        val introduction =
            getTakingControlIntroductionResponse?.takingControlIntroduction ?: return emptyList()
        return listOf(
            TakingControlScreeningItem(
                "AUDIT",
                "Doesn't apply for me",
                introduction.auditFlag != 1
            ),
            TakingControlScreeningItem(
                "DAST-10",
                "Doesn't apply for me",
                introduction.dastFlag != 1
            ),
            TakingControlScreeningItem("CAGE", "Doesn't apply for me", introduction.cageFlag != 1)
        )
    }

    private fun observeViewModel() {
        loginResponse?.loginDetails?.let {
            screeningsMenuViewModel.getScreeningList(
                it.patientID,
                it.clientID,
                it.patientLocationID,
                accessToken
            )
        }

        screeningsMenuViewModel.loadingLiveData.observe(requireActivity()) { isLoading ->
            if (isLoading) {
                customProgressDialog.show("Loading...")
            } else {
                customProgressDialog.dialogDismiss()
            }
        }

        screeningsMenuViewModel.screeningListLiveData.observe(
            viewLifecycleOwner,
            Observer { successData ->
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
                screeningResponse,
                this
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
                it.clientID, it.patientID, it.patientLocationID, accessToken
            )
        }
        observeIntroductionData()
    }

    private fun observeIntroductionData() {
        getTakingControlIntroductionViewModel.loadingLiveData.observe(
            viewLifecycleOwner,
            Observer { isLoading ->
                if (isLoading) {
                    customProgressDialog.show("Loading")
                } else {
                    customProgressDialog.dialogDismiss()
                }
            })

        getTakingControlIntroductionViewModel.successLiveData.observe(
            viewLifecycleOwner,
            Observer { isSuccess ->
                if (isSuccess) {
                    getTakingControlIntroductionViewModel.saveResponseLiveData.observe(
                        viewLifecycleOwner,
                        Observer { successData ->
                            if (successData != null) {
                                getTakingControlIntroductionResponse = successData
                                Log.d("Intro Fragment : ", "$getTakingControlIntroductionResponse")
                                updateAdapter()

                                binding.dontShowCheckBox.isChecked =
                                    getTakingControlIntroductionResponse?.takingControlIntroduction?.tutorialFlag == 1
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

    private fun constructAndSendApiRequest(item: TakingControlScreeningItem) {

        val introductionFlag = 0

        // Initialize all flags as null
        var auditFlag: Int? = null
        var dastFlag: Int? = null
        var cageFlag: Int? = null

        // Set the flag based on the item name
        when (item.name) {
            "AUDIT" -> auditFlag = if (item.isApplied) 0 else 1
            "DAST-10" -> dastFlag = if (item.isApplied) 0 else 1
            "CAGE" -> cageFlag = if (item.isApplied) 0 else 1
        }

        val request = loginResponse?.loginDetails?.let {
            SaveTakingControlIntroductionRequest(
                auditFlag = auditFlag,
                cageFlag = cageFlag,
                clientId = it.clientID,
                dastFlag = dastFlag,
                introductionFlag = introductionFlag,
                patientId = it.patientID,
                plId = it.patientLocationID,
                tutorialFlag = 0
            )
        }

        Log.d("TakingControlFragment", "Payload constructed: $request")

        if (request != null) {
            saveTakingControlIntroductionDataViewModel.saveTakingControlIntroductionData(
                request,
                accessToken
            )
        }

        observeSaveViewModel()

    }

    private fun observeSaveViewModel() {
        saveTakingControlIntroductionDataViewModel.loadingLiveData.observe(viewLifecycleOwner,
            Observer { isLoading ->
                if (isLoading) {
                    customProgressDialog.show("Loading...")
                } else {
                    customProgressDialog.dialogDismiss()
                }
            })
    }

    private fun updateIndexApiCall() {
        val isCompleted = 1
        loginResponse?.loginDetails?.let {
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
}

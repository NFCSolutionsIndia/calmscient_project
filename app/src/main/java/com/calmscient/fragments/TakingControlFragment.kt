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
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.calmscient.R
import com.calmscient.databinding.FragmentTakingControlBinding
import com.calmscient.di.remote.response.CourseLists
import com.calmscient.di.remote.response.GetTakingControlIndexResponse
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.di.remote.response.SummaryOfSleepResponse
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.GetSummaryOfSleepViewModel
import com.calmscient.viewmodels.GetTakingControlIndexViewModel
import com.github.mikephil.charting.charts.LineChart

class TakingControlFragment : Fragment() {

    private lateinit var binding: FragmentTakingControlBinding
    private var alcoholLayoutVisible = true

    private val getTakingControlIndexViewModel : GetTakingControlIndexViewModel by activityViewModels()
    private lateinit var commonAPICallDialog: CommonAPICallDialog
    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var getTakingControlIndexResponse: GetTakingControlIndexResponse
    private var loginResponse : LoginResponse? = null
    private  lateinit var accessToken : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this){
            loadFragment(DiscoveryFragment())
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTakingControlBinding.inflate(inflater, container, false)

        accessToken = SharedPreferencesUtil.getData(requireContext(), "accessToken", "")
        customProgressDialog = CustomProgressDialog(requireContext())
        commonAPICallDialog = CommonAPICallDialog(requireContext())
        val loginJsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString<LoginResponse>(loginJsonString)

        if(CommonClass.isNetworkAvailable(requireContext()))
        {
            apiCall()
            observeViewModel()
        }
        else
        {
            CommonClass.showInternetDialogue(requireContext())
        }


        binding.btnBasicKnowledge.setOnClickListener{
            loadFragment(BasicKnowledgeFragment())
        }
        binding.btnMakeAPlan.setOnClickListener{
            if(CommonClass.isNetworkAvailable(requireContext())){
                loadFragment(TakingControlMakeAPlanScreenOneFragment())
            }
            else{
                CommonClass.showInternetDialogue(requireContext())
            }
        }
        binding.btnSummary.setOnClickListener {
            loadFragment(SummaryTakingControlFragment())
        }
        binding.btnDrinkTracker.setOnClickListener {
            loadFragment(DrinkTrackerFragment())
        }
        binding.btnEventTracker.setOnClickListener {
            loadFragment(EventsTrackerFragment())
        }
        binding.alcoholtext.setOnClickListener {
            binding.alcohollayout.visibility = View.VISIBLE
            binding.substancelayouttext.visibility = View.GONE
            updateViewBackgroundColor(binding.viewalcohol, R.color.example_7_button)
            updateViewBackgroundColor(binding.viewsubstance, R.color.viewbackgroundcolor)
        }
        binding.substancetext.setOnClickListener {
            binding.alcohollayout.visibility = View.GONE
            alcoholLayoutVisible = false
            binding.substancelayouttext.visibility = View.VISIBLE
            updateViewBackgroundColor(binding.viewalcohol, R.color.viewbackgroundcolor)
            updateViewBackgroundColor(binding.viewsubstance, R.color.example_7_button)

        }
        binding.needToTalkWithSomeOne.setOnClickListener {
            loadFragment(EmergencyResourceFragment())
        }
        binding.backIcon.setOnClickListener{
            loadFragment(DiscoveryFragment())
        }

        binding.btnSeeTheInformation.setOnClickListener{
            loadFragment(TakingControlIntroductionFragment())
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //loadFragmentWithDelay(TakingControlIntroductionFragment())
    }


    private fun updateViewBackgroundColor(view: View, colorResId: Int) {
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), colorResId))
    }
    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun loadFragmentWithDelay(fragment: Fragment) {
        Handler(Looper.getMainLooper()).postDelayed({
            loadFragment(fragment)
        }, 2000)
    }

    private fun apiCall()
    {
        //loginResponse?.loginDetails?.let { getTakingControlIndexViewModel.getTakingControlIndex(it.clientID,it.patientID,it.patientLocationID,accessToken) }

        getTakingControlIndexViewModel.getTakingControlIndex(1,1,1,accessToken)
    }

    private fun observeViewModel()
    {

        getTakingControlIndexViewModel.loadingLiveData.observe(viewLifecycleOwner, Observer { isLoading->
            if(isLoading)
            {
                customProgressDialog.show("Loading...")
            }
            else
            {
                customProgressDialog.dialogDismiss()
            }
        })

        getTakingControlIndexViewModel.successLiveData.observe(viewLifecycleOwner, Observer { isSuccess->
            if(isSuccess)
            {
                customProgressDialog.dialogDismiss()

                getTakingControlIndexViewModel.saveResponseLiveData.observe(viewLifecycleOwner,
                    Observer { successData->
                        if(successData != null)
                        {
                            getTakingControlIndexResponse = successData

                            Log.d("TCI","$getTakingControlIndexResponse")
                            bindUIData(getTakingControlIndexResponse)
                        }
                    })
            }
        })
    }

    private fun bindUIData(getTakingControlIndexResponse: GetTakingControlIndexResponse) {
        // Check if index list is not null and has at least 2 elements

        if(getTakingControlIndexResponse.index.size == 1)
        {
            getTakingControlIndexResponse.index[0]?.let {
                binding.tvDrinkCount.text = it.goalType ?: "N/A"
                binding.tvDrinkCountGoalDays.text = it.goal?.toString() ?: "0"
                binding.tvDrinkCountNowDays.text = it.now?.toString() ?: "0"
            }
        }
        if (getTakingControlIndexResponse.index.size > 1) {


            getTakingControlIndexResponse.index[1]?.let {
                binding.tvAlcoholFreeDays.text = it.goalType ?: "N/A"
                binding.tvAlcoholGoalDays.text = it.goal?.toString() ?: "0"
                binding.tvAlcoholNowDays.text = it.now?.toString() ?: "0"
            }
        }

        // Check if courseLists is not null and has at least 7 elements
        if (getTakingControlIndexResponse.courseLists.size > 6) {
            binding.btnSeeTheInformation.text = getTakingControlIndexResponse.courseLists[0].courseName
            binding.btnHowToUse.text = getTakingControlIndexResponse.courseLists[1].courseName
            binding.btnBasicKnowledge.text = getTakingControlIndexResponse.courseLists[2].courseName
            binding.btnMakeAPlan.text = getTakingControlIndexResponse.courseLists[3].courseName
            binding.btnSummary.text = getTakingControlIndexResponse.courseLists[4].courseName
            binding.btnDrinkTracker.text = getTakingControlIndexResponse.courseLists[5].courseName
            binding.btnEventTracker.text = getTakingControlIndexResponse.courseLists[6].courseName
        }

        updateButtons(getTakingControlIndexResponse.courseLists)
    }

   /* private fun updateButtons(courseLists: List<CourseLists>) {
        for (course in courseLists) {
            when (course.courseName) {
                "Basic knowledge" -> binding.btnBasicKnowledge.isEnabled = course.isEnable == 1
                "Make a plan" -> binding.btnMakeAPlan.isEnabled = course.isEnable == 1
                "Summary" -> binding.btnSummary.isEnabled = course.isEnable == 1
                "Drink tracker" -> binding.btnDrinkTracker.isEnabled = course.isEnable == 1
                "Event tracker" -> binding.btnEventTracker.isEnabled = course.isEnable == 1
                "See the introduction" -> binding.btnSeeTheInformation.isEnabled = course.isEnable == 1
                "How to use" -> binding.btnHowToUse.isEnabled = course.isEnable == 1
            }
        }
    }*/

    private fun updateButtons(courseLists: List<CourseLists>) {
        for (course in courseLists) {
            when (course.courseName) {
                "Basic knowledge" -> {
                    binding.btnBasicKnowledge.isEnabled = course.isEnable == 1
                    binding.btnBasicKnowledge.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_background_enabled)
                }
                "Make a plan" -> {
                    binding.btnMakeAPlan.isEnabled = course.isEnable == 1
                    binding.btnMakeAPlan.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_background_enabled)
                }
                "Summary" -> {
                    binding.btnSummary.isEnabled = course.isEnable == 1
                    binding.btnSummary.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_background_enabled)
                }
                "Drink tracker" -> {
                    binding.btnDrinkTracker.isEnabled = course.isEnable == 1
                    binding.btnDrinkTracker.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_background_enabled)
                }
                "Event tracker" -> {
                    binding.btnEventTracker.isEnabled = course.isEnable == 1
                    binding.btnEventTracker.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_background_enabled)
                }
                "See the introduction" -> {
                    binding.btnSeeTheInformation.isEnabled = course.isEnable == 1
                    binding.btnSeeTheInformation.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_background_enabled)
                }
                "How to use" -> {
                    binding.btnHowToUse.isEnabled = course.isEnable == 1
                    binding.btnHowToUse.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_background_enabled)
                }
            }
        }
    }


}
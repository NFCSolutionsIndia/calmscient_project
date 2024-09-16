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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.calmscient.R
import com.calmscient.databinding.WeeklysummaryBinding
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.di.remote.response.MenuItem
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.utils.network.ServerTimeoutHandler
import com.calmscient.viewmodels.MenuItemViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WeeklySummaryFragment : Fragment() {
    private lateinit var binding:WeeklysummaryBinding

    @Inject
    lateinit var menuViewModel: MenuItemViewModel
    private lateinit var myMedicalMenuResponseDate: List<MenuItem>
    private lateinit var loginResponse: LoginResponse
    private lateinit var menuResponse: List<MenuItem>
    private lateinit var commonAPICallDialog: CommonAPICallDialog
    private lateinit var customProgressDialog: CustomProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this){
            if(CommonClass.isNetworkAvailable(requireContext()))
            {
                loadFragment(HomeFragment())
            }
            else
            {
                CommonClass.showInternetDialogue(requireContext())
            }

        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = WeeklysummaryBinding.inflate(inflater, container, false)

        customProgressDialog = CustomProgressDialog(requireContext())
        commonAPICallDialog = CommonAPICallDialog(requireContext())

        val jsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
         loginResponse = JsonUtil.fromJsonString<LoginResponse>(jsonString)

        Log.d("Login Response in WSF","$loginResponse")

        val menuJsonString = SharedPreferencesUtil.getData(requireContext(), "menuResponse", "")
         menuResponse = JsonUtil.fromJsonString<List<MenuItem>>(menuJsonString)



        if(CommonClass.isNetworkAvailable(requireContext()))
        {
            apiCall()
            observeViewModel()
        }
        else
        {
            CommonClass.showInternetDialogue(requireContext())
        }

        binding.moodCard.setOnClickListener {
            if(CommonClass.isNetworkAvailable(requireContext()))
            {
                loadFragment(SummaryofMoodFragment())
            }
            else
            {
                CommonClass.showInternetDialogue(requireContext())
            }
        }
        binding.summarySleepCard.setOnClickListener {
            if(CommonClass.isNetworkAvailable(requireContext()))
            {
                loadFragment(SummaryofSleepFragment())
            }
            else
            {
                CommonClass.showInternetDialogue(requireContext())
            }
        }
        binding.summaryPHQCard.setOnClickListener {
            if(CommonClass.isNetworkAvailable(requireContext()))
            {
                loadFragment(SummaryofPHQ9Fragment())
            }
            else
            {
                CommonClass.showInternetDialogue(requireContext())
            }
        }
        binding.summaryOfGADCard.setOnClickListener {
            if(CommonClass.isNetworkAvailable(requireContext()))
            {
                loadFragment(SummaryofGAD7Fragment())
            }
            else
            {
                CommonClass.showInternetDialogue(requireContext())
            }
        }
        binding.summaryOfAuditCard.setOnClickListener {
            if(CommonClass.isNetworkAvailable(requireContext()))
            {
                loadFragment(SummaryOfAUDITFragment())
            }
            else
            {
                CommonClass.showInternetDialogue(requireContext())
            }
        }
        binding.summaryOfDASTCard.setOnClickListener {
            if(CommonClass.isNetworkAvailable(requireContext()))
            {
                loadFragment(SummaryOfDASTFragment())
            }
            else
            {
                CommonClass.showInternetDialogue(requireContext())
            }
        }
        binding.summaryOfProgressWorkCard.setOnClickListener {
            if(CommonClass.isNetworkAvailable(requireContext()))
            {
                loadFragment(ProgressOnCourseWorkFragment())
            }
            else
            {
                CommonClass.showInternetDialogue(requireContext())
            }
        }
        binding.backIcon.setOnClickListener {
            if(CommonClass.isNetworkAvailable(requireContext()))
            {
                loadFragment(HomeFragment())
            }
            else
            {
                CommonClass.showInternetDialogue(requireContext())
            }
        }
        binding.journalEntryCard.setOnClickListener{
            if(CommonClass.isNetworkAvailable(requireContext()))
            {
                loadFragment(JournalEntryFragmentNew())
            }
            else
            {
                CommonClass.showInternetDialogue(requireContext())
            }

        }
        return binding.root
    }
    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
    fun snackBar(){
        view?.let {
            Snackbar.make(it, getString(R.string.coming_soon), Snackbar.LENGTH_SHORT)
                .setAction("OK") {
                    // Code to execute when the action button is clicked
                }
                .show()
        }
    }

    private fun apiCall()
    {
        menuViewModel.fetchMenuItems(loginResponse.loginDetails.patientLocationID,menuResponse[1].menuId,loginResponse.loginDetails.patientID,loginResponse.loginDetails.clientID,loginResponse.token.access_token)
    }

    private fun observeViewModel()
    {
        menuViewModel.loadingLiveData.observe(viewLifecycleOwner, Observer { isLoading->
            if(isLoading)
            {
                customProgressDialog.dialogDismiss()
                customProgressDialog.show("Loading...")
            }
            else
            {
                customProgressDialog.dialogDismiss()
            }
        })

        menuViewModel.errorLiveData.value?.let { failureMessage ->
            failureMessage.let {
                if (CommonClass.isNetworkAvailable(requireContext())) {
                    ServerTimeoutHandler.handleTimeoutException(requireContext()) {
                        // Retry logic when the retry button is clicked

                        menuViewModel.retryFetchMenuItems()
                    }
                } else {
                    CommonClass.showInternetDialogue(requireContext())
                }
            }

        }

        menuViewModel.menuItemsLiveData.observe(viewLifecycleOwner, Observer { successDate->
            if(successDate != null && successDate.size == 8)
            {
                binding.summaryOfMood.text = successDate[0].menuName
                binding.summaryOfSleep.text = successDate[1].menuName
                binding.summaryOfPHQ.text = successDate[2].menuName
                binding.summaryOfGAD.text = successDate[3].menuName
                binding.summaryOfAUDIT.text = successDate[4].menuName
                binding.summaryOfDAST.text = successDate[5].menuName
                binding.progressOnCourceWork.text = successDate[6].menuName
                binding.journalEntry.text = successDate[7].menuName

            }
        })
    }
}
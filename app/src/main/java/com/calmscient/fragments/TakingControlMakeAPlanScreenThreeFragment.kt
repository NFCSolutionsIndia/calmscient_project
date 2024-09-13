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

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.calmscient.R
import com.calmscient.activities.LearnMoreWebviewActivity
import com.calmscient.databinding.FragmentTakingControlMakeAPlanScreenThreeBinding
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.SendNotificationToDoctorMakeAPlanViewModel

class TakingControlMakeAPlanScreenThreeFragment : Fragment() {

    private lateinit var binding: FragmentTakingControlMakeAPlanScreenThreeBinding

    private val sendNotificationToDoctorMakeAPlanViewModel: SendNotificationToDoctorMakeAPlanViewModel by activityViewModels()
    private lateinit var commonAPICallDialog: CommonAPICallDialog
    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var loginResponse: LoginResponse
    private lateinit var accessToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            loadFragment(TakingControlMakeAPlanScreenTwoFragment())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTakingControlMakeAPlanScreenThreeBinding.inflate(inflater, container, false)

        accessToken = SharedPreferencesUtil.getData(requireContext(), "accessToken", "")
        customProgressDialog = CustomProgressDialog(requireContext())
        commonAPICallDialog = CommonAPICallDialog(requireContext())
        val loginJsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString<LoginResponse>(loginJsonString)

        binding.notifyCompleteDoctor.visibility = View.GONE
        binding.notifyCompleteMedicane.visibility = View.GONE

        binding.notifyDoctor.setOnClickListener {
            apiCall(1, 0)
            showLoader(binding.loaderDoctor, binding.notifyCompleteDoctor)
        }

        binding.notifyMedicane.setOnClickListener {
            apiCall(0, 1)
            showLoader(binding.loaderMedication, binding.notifyCompleteMedicane)
        }

        binding.link.setOnClickListener {
            val intent = Intent(activity, LearnMoreWebviewActivity::class.java)
            intent.putExtra("988_url", "https://alcoholtreatment.niaaa.nih.gov/")
            startActivity(intent)
        }

        binding.thirdScreenBackButton.setOnClickListener {
            loadFragment(TakingControlMakeAPlanScreenTwoFragment())
        }

        binding.backIcon.setOnClickListener {
            loadFragment(TakingControlMakeAPlanScreenTwoFragment())
        }

        binding.completeButton.setOnClickListener{
            loadFragment(TakingControlFragment())
        }



        return binding.root
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun apiCall(notifyToPCPDoctor: Int, notifyToPCPMedicine: Int) {
        sendNotificationToDoctorMakeAPlanViewModel.sendNotificationToDoctor(
            loginResponse.loginDetails.patientID,
            loginResponse.loginDetails.clientID,
            loginResponse.loginDetails.patientLocationID,
            notifyToPCPDoctor,
            notifyToPCPMedicine,
            accessToken
        )
        observeViewModel(notifyToPCPDoctor,notifyToPCPMedicine)
    }

    private fun observeViewModel(notifyToPCPDoctor: Int, notifyToPCPMedicine: Int) {

        sendNotificationToDoctorMakeAPlanViewModel.successLiveData.observe(viewLifecycleOwner, Observer { success ->
            if (success && notifyToPCPDoctor == 1) {
                binding.loaderDoctor.visibility = View.GONE
                binding.notifyCompleteDoctor.visibility = View.VISIBLE

            }
            if(success && notifyToPCPMedicine == 1)
            {
                binding.loaderMedication.visibility = View.GONE
                binding.notifyCompleteMedicane.visibility = View.VISIBLE
            }
        })

        sendNotificationToDoctorMakeAPlanViewModel.failureLiveData.observe(viewLifecycleOwner, Observer { errorMessage ->
            // Handle failure
        })
    }

    private fun showLoader(loader: View, completeImage: View) {
        loader.visibility = View.VISIBLE
        completeImage.visibility = View.GONE
    }
}

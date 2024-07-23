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
import android.text.Html
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.calmscient.R
import com.calmscient.databinding.FragmentGuidelinesForDrinkingBinding
import com.calmscient.databinding.FragmentModerationDrinkingBinding
import com.calmscient.di.remote.BasicKnowledgeItem
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.BasicKnowledgeSharedViewModel
import com.calmscient.viewmodels.BasicKnowledgeViewModel
import com.calmscient.viewmodels.UpdateBasicKnowledgeIndexDataViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ModerationDrinkingFragment : Fragment() {

    private lateinit var binding: FragmentModerationDrinkingBinding

    private val updateBasicKnowledgeIndexDataViewModel: UpdateBasicKnowledgeIndexDataViewModel by viewModels()
    private val sharedViewModel: BasicKnowledgeSharedViewModel by activityViewModels()
    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonDialog: CommonAPICallDialog
    private  lateinit var accessToken : String
    private lateinit var loginResponse: LoginResponse
    private lateinit var itemTemp: BasicKnowledgeItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this){
            requireActivity().supportFragmentManager.popBackStack()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding = FragmentModerationDrinkingBinding.inflate(inflater,container,false)


        accessToken = SharedPreferencesUtil.getData(requireContext(), "accessToken", "")
        customProgressDialog = CustomProgressDialog(requireContext())
        commonDialog = CommonAPICallDialog(requireContext())

        val loginJsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString<LoginResponse>(loginJsonString)


        binding.backIcon.setOnClickListener{
            requireActivity().supportFragmentManager.popBackStack()
        }

        val textView: TextView =  binding.tvThree
        textView.text = Html.fromHtml(getString(R.string.drinking_in_moderation_still_desc_3))

        // Observe the selected item
        sharedViewModel.selectedItem.observe(viewLifecycleOwner, Observer { item ->
            if (item != null) {
                // Use item data as needed
                Log.d("ModerationDrinkingFragment", "Name: ${item.name}, SectionId: ${item.sectionId}")
                itemTemp = item
                updateBasicKnowledgeAPICall()
            }
        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
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
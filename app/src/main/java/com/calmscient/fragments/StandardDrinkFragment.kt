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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.calmscient.R
import com.calmscient.adapters.StandardDrinkImagePagerAdapter
import com.calmscient.databinding.FragmentStandardDrinkBinding
import com.calmscient.di.remote.BasicKnowledgeItem
import com.calmscient.di.remote.StandardDrinkDataClass
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.BasicKnowledgeSharedViewModel
import com.calmscient.viewmodels.UpdateBasicKnowledgeIndexDataViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StandardDrinkFragment() : Fragment() {

    private lateinit var binding : FragmentStandardDrinkBinding

    private lateinit var viewPager: ViewPager2
    private lateinit var buttonPrevious: View
    private lateinit var buttonNext: View

    private lateinit var imagePagerAdapter: StandardDrinkImagePagerAdapter
    private val imageItems = mutableListOf<StandardDrinkDataClass>()

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
    ): View? {

        binding = FragmentStandardDrinkBinding.inflate(inflater,container,false)

        accessToken = SharedPreferencesUtil.getData(requireContext(), "accessToken", "")
        customProgressDialog = CustomProgressDialog(requireContext())
        commonDialog = CommonAPICallDialog(requireContext())

        val loginJsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString<LoginResponse>(loginJsonString)

        binding.backIcon.setOnClickListener{
            requireActivity().supportFragmentManager.popBackStack()
        }

        viewPager = binding.viewPager
        buttonPrevious = binding.buttonPrevious
        buttonNext = binding.buttonNext

        imageItems.add(StandardDrinkDataClass(R.drawable.ic_zeroalcohol, getString(R.string.drink_tracker_text1)))
        imageItems.add(StandardDrinkDataClass(R.drawable.ic_regularbeer, getString(R.string.drink_tracker_text2)))
        imageItems.add(StandardDrinkDataClass(R.drawable.ic_liquorbeer, getString(R.string.drink_tracker_text3)))
        imageItems.add(StandardDrinkDataClass(R.drawable.ic_tablewine, getString(R.string.drink_tracker_text4)))
        imageItems.add(StandardDrinkDataClass(R.drawable.ic_fortifiedwine, getString(R.string.drink_tracker_text5)))
        imageItems.add(StandardDrinkDataClass(R.drawable.ic_cardialwine, getString(R.string.drink_tracker_text6)))
        imageItems.add(StandardDrinkDataClass(R.drawable.ic_brandy, getString(R.string.drink_tracker_text7)))
        imageItems.add(StandardDrinkDataClass(R.drawable.ic_spirits, getString(R.string.drink_tracker_text8)))


        imagePagerAdapter = StandardDrinkImagePagerAdapter(imageItems)
        viewPager.adapter = imagePagerAdapter

        // Set the initial state of the buttons
        updateButtonVisibility(0)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateButtonVisibility(position)
            }
        })

        buttonPrevious.setOnClickListener {
            val currentItem = viewPager.currentItem
            if (currentItem > 0) {
                viewPager.setCurrentItem(currentItem - 1, true)
            }
        }

        buttonNext.setOnClickListener {
            val currentItem = viewPager.currentItem
            if (currentItem < imageItems.size - 1) {
                viewPager.setCurrentItem(currentItem + 1, true)
            }
        }

        // Observe the selected item
        sharedViewModel.selectedItem.observe(viewLifecycleOwner, Observer { item ->
            if (item != null) {
                // Use item data as needed
                Log.d("StandardDrinkFragment", "Name: ${item.name}, SectionId: ${item.sectionId}")
                itemTemp = item

                updateBasicKnowledgeAPICall()
            }
        })


        binding.completeButton.setOnClickListener{
            requireActivity().supportFragmentManager.popBackStack()
        }

        return binding.root
    }

    private fun updateButtonVisibility(position: Int) {
        // Hide "Previous" button if on the first page, else show it
        if (position == 0) {
            buttonPrevious.visibility = View.GONE
        } else {
            buttonPrevious.visibility = View.VISIBLE
        }

        // Hide "Next" button if on the last page, else show it
        if (position == imageItems.size - 1) {
            buttonNext.visibility = View.GONE
        } else {
            buttonNext.visibility = View.VISIBLE
        }
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
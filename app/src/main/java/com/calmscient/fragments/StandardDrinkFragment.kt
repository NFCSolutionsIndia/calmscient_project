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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.calmscient.R
import com.calmscient.adapters.StandardDrinkImagePagerAdapter
import com.calmscient.databinding.FragmentStandardDrinkBinding
import com.calmscient.di.remote.StandardDrinkDataClass
import com.calmscient.viewmodels.BasicKnowledgeViewModel

class StandardDrinkFragment : Fragment() {

    private lateinit var binding : FragmentStandardDrinkBinding
    private val viewModel: BasicKnowledgeViewModel by activityViewModels()

    private lateinit var viewPager: ViewPager2
    private lateinit var buttonPrevious: View
    private lateinit var buttonNext: View

    private lateinit var imagePagerAdapter: StandardDrinkImagePagerAdapter
    private val imageItems = mutableListOf<StandardDrinkDataClass>()


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

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.markItemCompleted("What’s a “standard drink”?")
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }


}
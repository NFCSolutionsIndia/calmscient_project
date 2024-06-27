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
import android.widget.ImageView
import android.widget.NumberPicker
import androidx.activity.addCallback
import com.calmscient.R
import com.calmscient.databinding.FragmentTakingControlMakeAPlanScreenFourBinding
import com.google.android.material.bottomsheet.BottomSheetDialog


class TakingControlMakeAPlanScreenFourFragment : Fragment() {

    private lateinit var binding: FragmentTakingControlMakeAPlanScreenFourBinding
    private var selectedNumber = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        binding =  FragmentTakingControlMakeAPlanScreenFourBinding.inflate(inflater, container, false)

        binding.goalsTextView.setOnClickListener{
            showBottomSheet()
        }

        binding.backIcon.setOnClickListener{
            requireActivity().supportFragmentManager.popBackStack()
        }
        return binding.root
    }

    private fun showBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.layout_drink_goals_bottomsheet, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        val numberPicker = bottomSheetView.findViewById<NumberPicker>(R.id.number_picker)
        numberPicker.minValue = 1
        numberPicker.maxValue = 40
        numberPicker.value = selectedNumber

        val saveButton = bottomSheetView.findViewById<ImageView>(R.id.saveButton)
        saveButton.setOnClickListener {
            selectedNumber = numberPicker.value
            binding.goalsTextView.text = selectedNumber.toString()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }


}
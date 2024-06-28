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

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.activity.addCallback
import com.calmscient.R
import com.calmscient.databinding.FragmentTakingControlMakeAPlanScreenFourBinding
import com.calmscient.utils.CustomCalendarView
import com.prolificinteractive.materialcalendarview.MaterialCalendarView

class TakingControlMakeAPlanScreenFourFragment : Fragment() {

    private lateinit var binding: FragmentTakingControlMakeAPlanScreenFourBinding
    private lateinit var customCalendarView: CustomCalendarView
    private val selectedMonths = mutableSetOf<TextView>()

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
        binding = FragmentTakingControlMakeAPlanScreenFourBinding.inflate(inflater, container, false)
        customCalendarView = binding.customCalendarView
        customCalendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_MULTIPLE)

        binding.backIcon.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.previousQuestion.setOnClickListener{
           loadFragment(TakingControlMakeAPlanScreenTwoFragment())
        }

        binding.nextQuestion.setOnClickListener{
            loadFragment(TakingControlMakeAPlanScreenFiveFragment())
        }

        setupMonthSelection()
        return binding.root
    }

    private fun setupMonthSelection() {
        val months = listOf(
            binding.jan, binding.feb, binding.march, binding.april, binding.may, binding.jun,
            binding.july, binding.aug, binding.sep, binding.oct, binding.nov, binding.dec
        )

        months.forEach { monthTextView ->
            monthTextView.setOnClickListener {
                toggleMonthSelection(monthTextView)
            }
        }

        binding.all.setOnClickListener {
            if (selectedMonths.size == months.size) {
                clearAllSelections(months)
            } else {
                selectAllMonths(months)
            }
        }
    }

    private fun toggleMonthSelection(textView: TextView) {
        if (selectedMonths.contains(textView)) {
            deselectMonth(textView)
        } else {
            selectMonth(textView)
        }
    }

    private fun selectMonth(textView: TextView) {
        selectedMonths.add(textView)
        textView.setBackgroundResource(R.drawable.card_selected_background)
        textView.setTextColor(Color.WHITE)
    }

    private fun deselectMonth(textView: TextView) {
        selectedMonths.remove(textView)
        textView.setBackgroundResource(R.drawable.card_default_background)
        textView.setTextColor(Color.parseColor("#424242"))
    }

    private fun clearAllSelections(months: List<TextView>) {
        selectedMonths.clear()
        months.forEach { monthTextView ->
            deselectMonth(monthTextView)
        }
    }

    private fun selectAllMonths(months: List<TextView>) {
        months.forEach { monthTextView ->
            selectMonth(monthTextView)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}

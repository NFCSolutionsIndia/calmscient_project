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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.calmscient.Interface.BottomSheetListener
import com.calmscient.R
import com.calmscient.databinding.FragmentJournalEntryBinding
import com.calmscient.databinding.FragmentJournalEntryNewBinding
import com.calmscient.utils.CustomCalendarDialog
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.time.LocalDate

class JournalEntryFragmentNew : Fragment(), BottomSheetAddFragment.BottomSheetListener,CustomCalendarDialog.OnDateSelectedListener  {



    private lateinit var binding: FragmentJournalEntryNewBinding
    private var selectedDate = LocalDate.now()
    private var currentlySelectedButton: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            loadFragment(WeeklySummaryFragment())
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentJournalEntryNewBinding.inflate(inflater, container, false)
        binding.backIcon.setOnClickListener {
            loadFragment(WeeklySummaryFragment())
        }

        binding.btnAddNewEntry.setOnClickListener {
            val bottomSheet = BottomSheetAddFragment()
            bottomSheet.bottomSheetListener = this
            bottomSheet.show(parentFragmentManager, bottomSheet.tag)
        }

        binding.calendarIcon.setOnClickListener {
            val dialog = CustomCalendarDialog()
            dialog.setOnDateSelectedListener(this)
            dialog.show(parentFragmentManager, "CustomCalendarDialog")

            dialog.setOnOkClickListener {
                Toast.makeText(requireContext(),"Selected Date: $selectedDate",Toast.LENGTH_LONG).show()
            }
        }

        binding.btnQuiz.setOnClickListener {
            selectOption(0)
        }

        binding.btnDailyJournals.setOnClickListener {
            selectOption(1)
        }

        binding.btnDiscoverExercise.setOnClickListener {
            selectOption(2)
        }


        return binding.root
    }

    override fun onTextEntered(text: String) {
       Toast.makeText(requireContext(),text,Toast.LENGTH_LONG).show()

        Log.e("BottomText","$text")
    }

    override fun onDateSelected(date: CalendarDay) {
        selectedDate = date.toLocalDate()
    }

    private fun CalendarDay.toLocalDate(): LocalDate {
        return LocalDate.of(this.year, this.month + 1, this.day)
    }

    private fun clearSelection() {
        currentlySelectedButton?.let { selectedIndex ->
            val button = when (selectedIndex) {
                0 -> binding.btnQuiz
                1 -> binding.btnDailyJournals
                2 -> binding.btnDiscoverExercise
                else -> null
            }
            button?.setBackgroundResource(R.drawable.journal_entry_buttons_border)
            button?.setTextColor(Color.parseColor("#424242"))
        }
    }

    private fun selectOption(selectedIndex: Int) {
        clearSelection()

        currentlySelectedButton = selectedIndex

        val button = when (selectedIndex) {
            0 -> binding.btnQuiz
            1 -> binding.btnDailyJournals
            2 -> binding.btnDiscoverExercise
            else -> null
        }

        button?.setBackgroundResource(R.drawable.journal_entry_buttons_selected_background)
        button?.setTextColor(Color.parseColor("#FFFFFF"))
    }


}
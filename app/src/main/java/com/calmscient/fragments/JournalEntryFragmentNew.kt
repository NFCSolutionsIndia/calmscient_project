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
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.calmscient.Interface.BottomSheetListener
import com.calmscient.R
import com.calmscient.databinding.FragmentJournalEntryBinding
import com.calmscient.databinding.FragmentJournalEntryNewBinding
import com.calmscient.utils.CustomCalendarDialog
import com.calmscient.utils.ViewPager2Utils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.time.LocalDate

class JournalEntryFragmentNew : Fragment(), BottomSheetAddFragment.BottomSheetListener,CustomCalendarDialog.OnDateSelectedListener  {



    private lateinit var binding: FragmentJournalEntryNewBinding
    private var selectedDate = LocalDate.now()
    private var currentlySelectedButton: Int? = null
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var tabAdapter: TabFragmentAdapter

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

        viewPager = binding.viewPager
        tabLayout = binding.tabLayout

        tabAdapter = TabFragmentAdapter(requireActivity())
        viewPager.adapter = tabAdapter


        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val tabView = tab.customView
                tabView?.findViewById<TextView>(R.id.tabTextView)?.apply {
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    //setPadding(8, 0, 8, 0)
                }
                //tabView?.setBackgroundResource(R.drawable.selected_tab_background)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val tabView = tab.customView
                tabView?.findViewById<TextView>(R.id.tabTextView)?.apply {
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                    //setPadding(8, 0, 8, 0)
                }
               //tabView?.setBackgroundResource(R.drawable.default_tab_background)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        binding.btnAddNewEntry.setOnClickListener {
            val existingBottomSheet = parentFragmentManager.findFragmentByTag(BottomSheetAddFragment::class.java.simpleName)

            if (existingBottomSheet == null) {
                val bottomSheet = BottomSheetAddFragment()
                bottomSheet.bottomSheetListener = this
                bottomSheet.show(parentFragmentManager, BottomSheetAddFragment::class.java.simpleName)
            }
        }

        binding.calendarIcon.setOnClickListener {
            val dialog = CustomCalendarDialog()
            dialog.setOnDateSelectedListener(this)
            dialog.show(parentFragmentManager, "CustomCalendarDialog")

            dialog.setOnOkClickListener {
                Toast.makeText(requireContext(),"Selected Date: $selectedDate",Toast.LENGTH_LONG).show()
            }
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.customView = getCustomTabView(position)
        }.attach()

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
    private fun getCustomTabView(position: Int): View {
        val tabView = LayoutInflater.from(context).inflate(R.layout.custom_tab, null)

        val tabTextView = tabView.findViewById<TextView>(R.id.tabTextView)
        when (position) {
            0 -> tabTextView.text = getString(R.string.quiz)
            1 -> tabTextView.text = getString(R.string.daily_journals)
            2 -> tabTextView.text = getString(R.string.discovery_exercise)
        }
        return tabView
    }

}
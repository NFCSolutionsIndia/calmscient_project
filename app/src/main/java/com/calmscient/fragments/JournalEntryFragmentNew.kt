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

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.calmscient.R
import com.calmscient.databinding.FragmentJournalEntryNewBinding
import com.calmscient.di.remote.request.AddPatientJournalEntryRequest
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomCalendarDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.AddPatientJournalEntryViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.prolificinteractive.materialcalendarview.CalendarDay
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class JournalEntryFragmentNew : Fragment(), BottomSheetAddFragment.BottomSheetListener,CustomCalendarDialog.OnDateSelectedListener  {



    private lateinit var binding: FragmentJournalEntryNewBinding
    private var selectedDate = LocalDate.now()
    private var currentlySelectedButton: Int? = null
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var tabAdapter: TabFragmentAdapter

    private lateinit var accessToken: String
    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonDialog: CommonAPICallDialog
    private lateinit var loginResponse : LoginResponse

    private val addPatientJournalEntryViewModel: AddPatientJournalEntryViewModel by viewModels()
    var dailyJournalTabFragment: DailyJournalTabFragment? = null
    var quizTabFragment: QuizTabFragment? = null
    var discoveryExerciseTabFragment: DiscoveryExerciseTabFragment? = null
    private var tabPosition : Int = -1

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

        customProgressDialog = CustomProgressDialog(requireContext())
        commonDialog = CommonAPICallDialog(requireContext())

        accessToken = SharedPreferencesUtil.getData(requireContext(), "accessToken", "")
        val jsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString<LoginResponse>(jsonString)


        viewPager = binding.viewPager
        tabLayout = binding.tabLayout

        tabAdapter = TabFragmentAdapter(requireActivity(), this,selectedDate)
        disableViewPagerSwipe(viewPager)// disabling swipe
        viewPager.adapter = tabAdapter

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {

                tabPosition = tab.position
                val tabView = tab.customView
                tabView?.findViewById<TextView>(R.id.tabTextView)?.apply {
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    //setPadding(8, 0, 8, 0)
                }
                //tabView?.setBackgroundResource(R.drawable.selected_tab_background)

                if (tab.position == 1) {
                    binding.btnAddNewEntry.visibility = View.VISIBLE
                } else {
                    binding.btnAddNewEntry.visibility = View.GONE
                }
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
                when (tabPosition) {
                    0 -> quizTabFragment?.getPatientJournalDataAPICall(selectedDate)
                    1 -> dailyJournalTabFragment?.getPatientJournalDataAPICall(selectedDate)
                    2 -> discoveryExerciseTabFragment?.getPatientJournalDataAPICall(selectedDate)
                    else -> {
                        // Optional: Handle any other tabPosition if necessary
                    }
                }
            }
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.customView = getCustomTabView(position)
        }.attach()


        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed here
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                performSearch(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                // No action needed here
            }
        })


        return binding.root
    }

    override fun onTextEntered(text: String) {
        //Need to make an API call for the new Journal entry text
        addPatientJournalEntryViewModel.clear()
        val request =  AddPatientJournalEntryRequest(loginResponse.loginDetails.clientID,text,loginResponse.loginDetails.patientID,loginResponse.loginDetails.patientLocationID)
        addPatientJournalEntryViewModel.addPatientJournalEntry(request,accessToken)

        observeAddPatientJournalEntry()

    }

    private fun observeAddPatientJournalEntry(){
        addPatientJournalEntryViewModel.loadingLiveData.observe(this, Observer { isLoading->
            if(isLoading){
                customProgressDialog.show(getString(R.string.loading))
            }else{
                customProgressDialog.dialogDismiss()
            }
        })

        addPatientJournalEntryViewModel.successLiveData.observe(this, Observer { isSuccess->
            if(isSuccess){
                addPatientJournalEntryViewModel.saveResponseLiveData.observe(this, Observer { successData->
                    if(successData != null && successData.responseCode == 200){
                        commonDialog.showDialog(successData.responseMessage,R.drawable.ic_success_dialog)
                       commonDialog.setOnDismissListener {
                           // I want to update the daily journal adapter here...
                           dailyJournalTabFragment?.getPatientJournalDataAPICall(null)
                       }
                    }
                })
            }
        })
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

    private fun performSearch(query: String?) {
        when (tabPosition) {
            0 -> quizTabFragment?.performSearch(query) // Quiz Tab
            1 -> dailyJournalTabFragment?.performSearch(query) // Daily Journal Tab
            2 -> discoveryExerciseTabFragment?.performSearch(query) // Discovery Exercise Tab
        }
    }
    private fun disableViewPagerSwipe(viewPager: ViewPager2) {
        viewPager.isUserInputEnabled = false
    }

}
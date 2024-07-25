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
import android.os.Bundle
import android.text.Html
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.R
import com.calmscient.adapters.ProsAndConsAdapter
import com.calmscient.databinding.FragmentTakingControlMakeAPlanScreenOneBinding
import com.calmscient.di.remote.ConsItem
import com.calmscient.di.remote.ProsItem
import com.calmscient.di.remote.request.JournalEntry
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.di.remote.response.SaveCourseJournalEntryMakeAPlanResponse
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.NonSwipeRecyclerView
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.SaveCourseJournalEntryMakeAPlanViewModel


class TakingControlMakeAPlanScreenOneFragment : Fragment() {

    private lateinit var binding :FragmentTakingControlMakeAPlanScreenOneBinding
    private lateinit var prosRecyclerView: RecyclerView
    private lateinit var consRecyclerView: RecyclerView

    private lateinit var prosAdapter: ProsAndConsAdapter
    private lateinit var consAdapter: ProsAndConsAdapter

    private val saveCourseJournalEntryMakeAPlanViewModel : SaveCourseJournalEntryMakeAPlanViewModel by activityViewModels()
    private lateinit var saveCourseJournalEntryMakeAPlanResponse : SaveCourseJournalEntryMakeAPlanResponse

    private var journalEntryList: List<JournalEntry> = emptyList()

    private var loginResponse : LoginResponse? = null
    private  lateinit var accessToken : String
    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonDialog: CommonAPICallDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this){
            loadFragment(TakingControlFragment())
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTakingControlMakeAPlanScreenOneBinding.inflate(inflater,container,false)

        customProgressDialog = CustomProgressDialog(requireContext())
        commonDialog = CommonAPICallDialog(requireContext())

        val jsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString<LoginResponse>(jsonString)
        accessToken = SharedPreferencesUtil.getData(requireContext(), "accessToken", "")

        prosRecyclerView = binding.prosRecyclerview
        consRecyclerView = binding.consRecyclerview

        // Load the string from resources
        val descriptionHtml = getString(R.string.make_a_paln_screen1_description)

        // Set the spanned text to the TextView
        binding.tvDescription.text = Html.fromHtml(descriptionHtml, Html.FROM_HTML_MODE_LEGACY)

        val prosItems = getProsItems()
        val consItems = getConsItems()

        prosAdapter = ProsAndConsAdapter(prosItems)
        consAdapter = ProsAndConsAdapter(consItems)

        binding.prosRecyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = prosAdapter
        }

        binding.consRecyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = consAdapter
        }

        /*binding.btnTrackAlcohol.setOnClickListener{
            loadFragment(TakingControlMakeAPlanScreenTwoFragment())
        }*/
        binding.backIcon.setOnClickListener{
            loadFragment(TakingControlFragment())
        }

        binding.btnQuitCut.setOnClickListener{
            loadFragment(TakingControlMakeAPlanScreenTwoFragment())
        }

        binding.yesButton.setOnClickListener{
            saveJournalEntryAPICall()
            saveJournalEntryObserveViewModel()
        }


        binding.etPros.setOnClickListener {
            prosAdapter.clearSelection()
            binding.prosRecyclerview.adapter?.notifyDataSetChanged()
        }

        binding.etCons.setOnClickListener {
            consAdapter.clearSelection()
            binding.consRecyclerview.adapter?.notifyDataSetChanged()
        }

        return binding.root
    }


    private fun getProsItems(): List<ProsItem> {
        return listOf(
            ProsItem("To improve my health"),
            ProsItem("To improve my relationships"),
            ProsItem("To avoid hangovers"),
            ProsItem("To do better at work or in school"),
            ProsItem("To save money"),
            ProsItem("To lose weight or get fit"),
            ProsItem("To avoid more serious problems"),
            ProsItem("To meet my own personal standards")
        )
    }

    private fun getConsItems(): List<ConsItem> {
        return listOf(
            ConsItem("I'd need another way to unwind"),
            ConsItem("It helps me feel more at ease socially"),
            ConsItem("I wouldn't fit in with some of my friends"),
            ConsItem("Change can be hard"),
        )
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun saveJournalEntryAPICall() {
        saveCourseJournalEntryMakeAPlanViewModel.clear()

        val selectedProsPosition = prosAdapter.getSelectedPosition()
        val selectedConsPosition = consAdapter.getSelectedPosition()

        val enteredProsText = binding.etPros.text.toString().trim()
        val enteredConsText = binding.etCons.text.toString().trim()

        if (selectedProsPosition != RecyclerView.NO_POSITION || selectedConsPosition != RecyclerView.NO_POSITION || enteredProsText.isNotEmpty() || enteredConsText.isNotEmpty()) {
            val selectedProsItem = (prosAdapter.getItem(selectedProsPosition) as? ProsItem)?.name
            val selectedConsItem = (consAdapter.getItem(selectedConsPosition) as? ConsItem)?.name

            journalEntryList = listOf(
                JournalEntry(selectedProsItem ?: enteredProsText),
                JournalEntry(selectedConsItem ?: enteredConsText)
            )

            Log.d("journalEntryList","$journalEntryList")

            if(journalEntryList.isNotEmpty())
            {
                loginResponse?.loginDetails?.let {
                    saveCourseJournalEntryMakeAPlanViewModel.saveCourseJournalEntryMakeAPlan(
                        it.clientID, it.patientLocationID, journalEntryList, accessToken
                    )
                }
            }
        } else {
            Toast.makeText(requireContext(), "Please answer the questions", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveJournalEntryObserveViewModel() {
        saveCourseJournalEntryMakeAPlanViewModel.loadingLiveData.observe(viewLifecycleOwner,
            Observer { isLoading ->
                if (isLoading) {
                    customProgressDialog.show(getString(R.string.loading))
                } else {
                    customProgressDialog.dialogDismiss()
                }
            }
        )

        saveCourseJournalEntryMakeAPlanViewModel.successLiveData.observe(viewLifecycleOwner,
            Observer { isSuccess ->
                if (isSuccess) {
                    saveCourseJournalEntryMakeAPlanViewModel.saveResponseLiveData.observe(viewLifecycleOwner,
                        Observer { successData ->
                            if (successData != null) {
                                commonDialog.showDialog(successData.response.responseMessage)
                            }
                        }
                    )
                }
            }
        )
    }


}
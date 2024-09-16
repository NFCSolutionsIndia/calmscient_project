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

class JournalEntryFragmentNew : Fragment(), BottomSheetAddFragment.BottomSheetListener  {



    private lateinit var binding: FragmentJournalEntryNewBinding
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

        return binding.root
    }

    override fun onTextEntered(text: String) {
       Toast.makeText(requireContext(),text,Toast.LENGTH_LONG).show()

        Log.e("BottomText","$text")
    }


}
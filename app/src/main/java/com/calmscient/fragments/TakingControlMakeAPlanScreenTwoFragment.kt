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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import com.calmscient.R
import com.calmscient.databinding.FragmentTakingControlMakeAPlanScreenTwoBinding

class TakingControlMakeAPlanScreenTwoFragment : Fragment() {

    private lateinit var binding : FragmentTakingControlMakeAPlanScreenTwoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this){
            loadFragment(TakingControlMakeAPlanScreenOneFragment())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTakingControlMakeAPlanScreenTwoBinding.inflate(inflater,container,false)


        binding.secondScreenBackButton.setOnClickListener{
            loadFragment(TakingControlMakeAPlanScreenOneFragment())
        }
        binding.btnQuit.setOnClickListener{
            loadFragment(TakingControlMakeAPlanScreenThreeFragment())
        }

        binding.btnCutDown.setOnClickListener{
            loadFragment(TakingControlMakeAPlanScreenFourFragment())
        }

        binding.backIcon.setOnClickListener{
            loadFragment(TakingControlMakeAPlanScreenOneFragment())
        }

        // Load the string from resources
        val descriptionHtml = getString(R.string.make_a_plan_screen_two_description)

        // Set the spanned text to the TextView
        binding.tvDescription.text = Html.fromHtml(descriptionHtml, Html.FROM_HTML_MODE_LEGACY)


        return binding.root
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

}
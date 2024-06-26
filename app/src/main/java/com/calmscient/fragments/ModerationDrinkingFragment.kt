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
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import com.calmscient.R
import com.calmscient.databinding.FragmentGuidelinesForDrinkingBinding
import com.calmscient.databinding.FragmentModerationDrinkingBinding
import com.calmscient.viewmodels.BasicKnowledgeViewModel


class ModerationDrinkingFragment : Fragment() {

    private lateinit var binding: FragmentModerationDrinkingBinding
    private val viewModel: BasicKnowledgeViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this){
            requireActivity().supportFragmentManager.popBackStack()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding = FragmentModerationDrinkingBinding.inflate(inflater,container,false)

        binding.backIcon.setOnClickListener{
            requireActivity().supportFragmentManager.popBackStack()
        }

        val textView: TextView =  binding.tvThree
        textView.text = Html.fromHtml(getString(R.string.drinking_in_moderation_still_desc_3))

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.markItemCompleted("When is drink in moderation too much ?")
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }


}
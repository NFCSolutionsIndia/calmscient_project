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
import androidx.recyclerview.widget.LinearLayoutManager
import com.calmscient.R
import com.calmscient.adapters.ProsAndConsAdapter
import com.calmscient.databinding.FragmentTakingControlMakeAPlanScreenOneBinding
import com.calmscient.di.remote.ConsItem
import com.calmscient.di.remote.ProsItem
import com.calmscient.utils.NonSwipeRecyclerView


class TakingControlMakeAPlanScreenOneFragment : Fragment() {

    private lateinit var binding :FragmentTakingControlMakeAPlanScreenOneBinding
    private lateinit var prosRecyclerView: NonSwipeRecyclerView
    private lateinit var consRecyclerView: NonSwipeRecyclerView

    private lateinit var prosAdapter: ProsAndConsAdapter
    private lateinit var consAdapter: ProsAndConsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this){
            loadFragment(TakingControlFragment())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTakingControlMakeAPlanScreenOneBinding.inflate(inflater,container,false)

        prosRecyclerView = binding.prosRecyclerview
        consRecyclerView = binding.consRecyclerview


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


}
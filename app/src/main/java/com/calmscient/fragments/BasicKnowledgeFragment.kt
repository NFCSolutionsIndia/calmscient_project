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
import com.calmscient.adapters.BasicKnowledgeAdapter
import com.calmscient.adapters.TakingControlScreeningAdapter
import com.calmscient.databinding.FragmentBasicKnowledgeBinding
import com.calmscient.di.remote.BasicKnowledgeItem
import com.calmscient.di.remote.TakingControlScreeningItem
import com.calmscient.utils.NonSwipeRecyclerView

import androidx.fragment.app.viewModels
import com.calmscient.viewmodels.BasicKnowledgeViewModel

class BasicKnowledgeFragment : Fragment() {

    private lateinit var binding: FragmentBasicKnowledgeBinding
    private lateinit var recyclerView: NonSwipeRecyclerView
    private val viewModel: BasicKnowledgeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this){
            loadFragment(TakingControlFragment())
        }

        // Initialize the list if it's not already set
        if (viewModel.items.value.isNullOrEmpty()) {
            viewModel.setItems(getScreeningItems())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBasicKnowledgeBinding.inflate(inflater, container, false)

        recyclerView = binding.basicKnowledgeRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.items.observe(viewLifecycleOwner) { items ->
            recyclerView.adapter = BasicKnowledgeAdapter(requireActivity().supportFragmentManager, requireContext(), items, viewModel)
        }


        binding.backIcon.setOnClickListener{
            loadFragment(TakingControlFragment())
        }

        return binding.root
    }

    private fun getScreeningItems(): List<BasicKnowledgeItem> {
        return listOf(
            BasicKnowledgeItem("What’s a “standard drink”?", R.drawable.basic_knowledge_complete_icon),
            BasicKnowledgeItem("What are the U.S. guidelines for drink?", R.drawable.basic_knowledge_complete_icon),
            BasicKnowledgeItem("When is drink in moderation too much ?", R.drawable.basic_knowledge_complete_icon),
            BasicKnowledgeItem("What happens to your brain when you drink?", R.drawable.basic_knowledge_complete_icon),
            BasicKnowledgeItem("What are the consequences?", R.drawable.basic_knowledge_complete_icon),
            BasicKnowledgeItem("My drinking habit", R.drawable.basic_knowledge_complete_icon),
            BasicKnowledgeItem("What to expect when you quit drinking?", R.drawable.basic_knowledge_complete_icon),
        )
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}

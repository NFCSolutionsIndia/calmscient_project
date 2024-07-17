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
import com.calmscient.R
import com.calmscient.databinding.FragmentMindfulBreathingExerciseBinding


class MindfulBreathingExerciseFragment : Fragment() {

    private lateinit var binding : FragmentMindfulBreathingExerciseBinding
    private var isFavorite = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this){
            loadFragment(DeepBreathingExerciseFragment())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMindfulBreathingExerciseBinding.inflate(inflater,container,false)

        val favoritesIcon = binding.favoritesIcon
        favoritesIcon.setOnClickListener {
            isFavorite = !isFavorite
            if (isFavorite) {
                favoritesIcon.setImageResource(R.drawable.ic_favorites_icon)
            } else {
                favoritesIcon.setImageResource(R.drawable.ic_favorites_red)
            }
        }

        binding.backIcon.setOnClickListener{
            loadFragment(DeepBreathingExerciseFragment())
        }

        return binding.root
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }


}
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

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.time.LocalDate

class TabFragmentAdapter(fragmentActivity: FragmentActivity,private val journalEntryFragment: JournalEntryFragmentNew,private var selectedDate: LocalDate) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 ->{
                val fragment = QuizTabFragment().newInstance(selectedDate)
                journalEntryFragment.quizTabFragment = fragment
                fragment
            }
            1 -> {
                val fragment = DailyJournalTabFragment().newInstance(selectedDate)
                journalEntryFragment.dailyJournalTabFragment = fragment
                fragment
            }
            else -> {
                val fragment = DiscoveryExerciseTabFragment().newInstance(selectedDate)
                journalEntryFragment.discoveryExerciseTabFragment = fragment
                fragment
            }
        }
    }
}

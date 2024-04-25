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

package com.calmscient.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.R
import com.calmscient.di.remote.response.ScreeningItem
import com.calmscient.fragments.AUDITQuestionFragment
import com.calmscient.fragments.DASTQuestionFragment
import com.calmscient.fragments.GADQuestionFragment
import com.calmscient.fragments.HistoryFragment
import com.calmscient.fragments.QuestionFragment
import com.calmscient.fragments.ScreeningsCardItem
import com.calmscient.utils.common.CommonClass

class ScreeningsCardAdapter(private val fragmentManager: FragmentManager, private val items: List<ScreeningsCardItem>,private val screeningResponse: List<ScreeningItem>) :
    RecyclerView.Adapter<ScreeningsCardAdapter.CardViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ScreeningsCardAdapter.CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.screenings_item_card_view, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val item = items[position]
        holder.titleTextView.text = item.title
        item.historyImageResource?.let { holder.imageHistory.setImageResource(it) }
        item.nextOrKeyImageResource?.let { holder.screeningsImageNext.setImageResource(it) }
        // item.keyImageResource?.let { holder.imageViewMoon.setImageResource(it) }
        /*holder.cardViewLayout.setOnClickListener { v ->
            val activity = v!!.context as AppCompatActivity
            val fragment = QuestionFragment()
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.flFragment, fragment).addToBackStack(null).commit()
        }*/
        holder.imageHistory.setOnClickListener {
            val fragment = HistoryFragment()
            fragment.let {
                fragmentManager.beginTransaction()
                    .replace(R.id.flFragment, it)
                    .addToBackStack(null)
                    .commit()
            }
        }


        holder.cardViewLayout.setOnClickListener {
            // Check internet connection
            if (CommonClass.isNetworkAvailable(holder.itemView.context)) {
                // Load corresponding fragment based on position
                val fragment = when (position) {
                    0 -> QuestionFragment(screeningResponse[position])
                    1 -> GADQuestionFragment()
                    2 -> AUDITQuestionFragment()
                    3 -> DASTQuestionFragment()
                    // Add more cases for other positions if needed
                    else -> null
                }

                fragment?.let {
                    fragmentManager.beginTransaction()
                        .replace(R.id.flFragment, it)
                        .addToBackStack(null)
                        .commit()
                }
            } else {
                // Show internet connection dialog
                CommonClass.showInternetDialogue(holder.itemView.context)
            }
        }

    }

    override fun getItemCount(): Int = items.size

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleText)
        val cardViewLayout: CardView = itemView.findViewById(R.id.screenings_item_card_view)
        val imageHistory: ImageView = itemView.findViewById(R.id.history_icon)
        val screeningsImageNext: ImageView = itemView.findViewById(R.id.screenings_next_icon)



    }
}
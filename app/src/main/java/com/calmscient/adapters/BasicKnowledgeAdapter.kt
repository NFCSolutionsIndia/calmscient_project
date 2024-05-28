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

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.R
import com.calmscient.di.remote.BasicKnowledgeItem
import com.calmscient.fragments.AUDITQuestionFragment
import com.calmscient.fragments.ConsequencesFragment
import com.calmscient.fragments.DASTQuestionFragment
import com.calmscient.fragments.GADQuestionFragment
import com.calmscient.fragments.GuidelinesForDrinkingFragment
import com.calmscient.fragments.ModerationDrinkingFragment
import com.calmscient.fragments.QuestionFragment
import com.calmscient.fragments.StandardDrinkFragment
import com.calmscient.fragments.WhatExpectsWhenYouQuitDrinkingFragment
import com.calmscient.fragments.WhatHappensToYourBrainFragment
import com.calmscient.utils.common.CommonClass
import com.calmscient.viewmodels.BasicKnowledgeViewModel

class BasicKnowledgeAdapter(private val fragmentManager: FragmentManager,private val context: Context,private val items: List<BasicKnowledgeItem>, private val viewModel: BasicKnowledgeViewModel) :
    RecyclerView.Adapter<BasicKnowledgeAdapter.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.basic_knowledge_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        if (item.isCompleted) {
            holder.completedImage.setImageResource(item.tickImg ?: 0)
            holder.completedImage.visibility = View.VISIBLE
        } else {
            holder.completedImage.visibility = View.GONE
        }

        holder.cardViewLayout.setOnClickListener {
            if (CommonClass.isNetworkAvailable(holder.itemView.context)) {
                viewModel.markItemCompleted(item.name)
                notifyItemChanged(position)

                val fragment = when (item.name) {
                    "What’s a “standard drink”?" -> StandardDrinkFragment()
                    "What are the U.S. guidelines for drink?" -> GuidelinesForDrinkingFragment()
                    "When is drink in moderation too much ?" -> ModerationDrinkingFragment()
                    "What happens to your brain when you drink?" -> WhatHappensToYourBrainFragment()
                    "What to expect when you quit drinking?" -> WhatExpectsWhenYouQuitDrinkingFragment()
                    "What are the consequences?" -> ConsequencesFragment()
                    else -> null
                }

                fragment?.let {
                    fragmentManager.beginTransaction()
                        .replace(R.id.flFragment, it)
                        .addToBackStack(null)
                        .commit()
                }
            } else {
                CommonClass.showInternetDialogue(holder.itemView.context)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.text)
        val completedImage: ImageView = view.findViewById(R.id.completeIcon)
        val cardViewLayout: CardView = itemView.findViewById(R.id.basicKnowledgeCardView)
    }
}
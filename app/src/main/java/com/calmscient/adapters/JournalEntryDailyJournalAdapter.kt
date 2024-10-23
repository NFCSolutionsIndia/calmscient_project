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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.R
import com.calmscient.di.remote.response.DailyJournal
import com.calmscient.utils.AnimationUtils

class JournalEntryDailyJournalAdapter(private val items: List<DailyJournal>) :
    RecyclerView.Adapter<JournalEntryDailyJournalAdapter.CardViewHolder>() {

    private var expandedCardPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.journal_entry_daily_journal_item, parent, false)
        return CardViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val item = items[holder.adapterPosition] // Use the dynamic adapter position
        holder.bind(item)

        holder.titleCardView.setOnClickListener {
            val currentPosition = holder.adapterPosition
            if (expandedCardPosition == currentPosition) {
                // Collapse the currently expanded card
                holder.collapse()
                expandedCardPosition = -1
            } else {
                // Collapse the previously expanded card (if any)
                val previouslyExpandedCardPosition = expandedCardPosition
                if (previouslyExpandedCardPosition != -1) {
                    notifyItemChanged(previouslyExpandedCardPosition)
                }
                // Expand the clicked card
                holder.expand()
                expandedCardPosition = currentPosition
            }
        }


        holder.dropDownImage.setOnClickListener {
            val currentPosition = holder.adapterPosition
            if (expandedCardPosition == currentPosition) {
                // Collapse the currently expanded card
                holder.collapse()
                expandedCardPosition = -1
            } else {
                // Collapse the previously expanded card (if any)
                val previouslyExpandedCardPosition = expandedCardPosition
                if (previouslyExpandedCardPosition != -1) {
                    notifyItemChanged(previouslyExpandedCardPosition)
                }
                // Expand the clicked card
                holder.expand()
                expandedCardPosition = currentPosition
            }
        }
        // Dynamically expand/collapse the view based on the adapter position
        if (expandedCardPosition == holder.adapterPosition) {
            holder.expand()
        } else {
            holder.collapse()
        }
    }

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textviewDate: TextView = itemView.findViewById(R.id.tv_date)
        val title: TextView = itemView.findViewById(R.id.tv_daily_journal_title)
        val singleLineDescription: TextView = itemView.findViewById(R.id.tv_singleLine)
        val multiLine: TextView = itemView.findViewById(R.id.tv_Description)
        val titleCardView: ConstraintLayout = itemView.findViewById(R.id.taskTitleLayout)
         val dropDownImage: ImageView = itemView.findViewById(R.id.dropdownButton)
        private var isExpanded = false

        // Bind data to the views
        fun bind(dailyJournal: DailyJournal) {
            textviewDate.text = dailyJournal.createdAt
            title.text = dailyJournal.title
            singleLineDescription.text = dailyJournal.entry
            multiLine.text = dailyJournal.entry
            collapse() // All tasks should be collapsed by default
        }

        // Expand the journal entry and update the UI
        fun expand() {
            AnimationUtils.expand(multiLine)
            dropDownImage.setImageResource(R.drawable.minus) // Change the dropdown icon to a minus symbol
            isExpanded = true
            singleLineDescription.visibility = View.GONE // Hide single-line description
        }

        // Collapse the journal entry and update the UI
        fun collapse() {
            AnimationUtils.collapse(multiLine)
            dropDownImage.setImageResource(R.drawable.ic_expand) // Change the dropdown icon to an expand symbol
            isExpanded = false
            singleLineDescription.visibility = View.VISIBLE // Show single-line description
        }
    }
}

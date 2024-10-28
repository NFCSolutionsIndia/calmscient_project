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
class JournalEntryDailyJournalAdapter(private val items: List<DailyJournal>,  private val onUrlClick: ((url: String, title: String) -> Unit)? = null ) :
    RecyclerView.Adapter<JournalEntryDailyJournalAdapter.CardViewHolder>() {

    private var expandedCardPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.journal_entry_daily_journal_item, parent, false)
        return CardViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)

        // Set the initial view state (expanded or collapsed)
        if (expandedCardPosition == holder.adapterPosition) {
            holder.expand()
        } else {
            holder.collapse()
        }

        // Set the onClickListener for expanding/collapsing
        holder.itemView.setOnClickListener {
            if (expandedCardPosition == holder.adapterPosition) {
                holder.collapse()
                expandedCardPosition = -1
            } else {
                val previousExpandedPosition = expandedCardPosition
                expandedCardPosition = holder.adapterPosition
                notifyItemChanged(previousExpandedPosition)
                notifyItemChanged(expandedCardPosition)
            }
        }
        holder.dropDownImage.setOnClickListener {
            if (expandedCardPosition == holder.adapterPosition) {
                holder.collapse()
                expandedCardPosition = -1
            } else {
                val previousExpandedPosition = expandedCardPosition
                expandedCardPosition = holder.adapterPosition
                notifyItemChanged(previousExpandedPosition)
                notifyItemChanged(expandedCardPosition)
            }
        }
    }

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.tv_singleLineOrFullDescription)
         val dropDownImage: ImageView = itemView.findViewById(R.id.dropdownButton)

        fun bind(dailyJournal: DailyJournal) {
            textView.text = dailyJournal.entry

            if (dailyJournal.url != null && dailyJournal.title != null && dailyJournal.entryType == "DiscoveryExercises" && onUrlClick != null) {
                textView.setOnClickListener {
                    onUrlClick.invoke(dailyJournal.url, dailyJournal.title)
                }
            }

            collapse() // Start collapsed
        }

        fun expand() {
            textView.maxLines = Integer.MAX_VALUE // Show full text
            dropDownImage.setImageResource(R.drawable.minus) // Change icon to minus
        }

        fun collapse() {
            textView.maxLines = 1 // Show single line
            dropDownImage.setImageResource(R.drawable.ic_expand) // Change icon to expand
        }
    }
}


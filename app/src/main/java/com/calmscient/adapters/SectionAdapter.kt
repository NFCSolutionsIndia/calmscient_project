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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.R
import com.calmscient.di.remote.response.Sections
import com.calmscient.utils.AnimationUtils

class SectionAdapter(private val data: List<Sections>) : RecyclerView.Adapter<SectionAdapter.ViewHolder>() {

    // Variable to keep track of the currently expanded card position
    private var expandedCardPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.progressonwork_itemcard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)

        // Check if the current card should be expanded or collapsed based on its position
        if (expandedCardPosition == position) {
            holder.expand()
        } else {
            holder.collapse()
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleView: TextView = itemView.findViewById(R.id.titleView)
        val dropDownImage: ImageView = itemView.findViewById(R.id.dropdownButton)
        val titlePercentage: TextView = itemView.findViewById(R.id.titlePercentage)
        val subSectionRecyclerView: RecyclerView = itemView.findViewById(R.id.subSectionRecyclerView)

        init {
            // Set an OnClickListener to handle expanding/collapsing
            dropDownImage.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    if (position == expandedCardPosition) {
                        // Clicked on an already expanded card, so collapse it
                        collapse()
                        expandedCardPosition = -1
                    } else {
                        // Collapse the previously expanded card (if any)
                        if (expandedCardPosition != -1) {
                            notifyItemChanged(expandedCardPosition)
                        }
                        // Expand the clicked card
                        expand()
                        expandedCardPosition = position
                    }
                }
            }
        }

        fun bind(item: Sections) {
            // Bind data to views
            titleView.text = item.sectionName
            titlePercentage.text = "${item.completions}%"

            // Set up nested RecyclerView for subsections
            subSectionRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
            subSectionRecyclerView.adapter = SubsectionAdapter(item.subSectionList)

            // Ensure that the RecyclerView is initially collapsed
            if (expandedCardPosition == adapterPosition) {
                expand()
            } else {
                collapse()
            }
        }

        fun expand() {
            // Expand the RecyclerView and update the icon
            subSectionRecyclerView.visibility = View.VISIBLE
            dropDownImage.setImageResource(R.drawable.minus)
            AnimationUtils.expand(subSectionRecyclerView) // Optional if you have expand animation
        }

        fun collapse() {
            // Collapse the RecyclerView and update the icon
            subSectionRecyclerView.visibility = View.GONE
            dropDownImage.setImageResource(R.drawable.ic_expand)
            AnimationUtils.collapse(subSectionRecyclerView) // Optional if you have collapse animation
        }
    }
}

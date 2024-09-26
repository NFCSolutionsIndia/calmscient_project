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

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.R
import com.calmscient.di.remote.Task
import com.calmscient.utils.AnimationUtils

class GlossaryAdapter(private val allTasks: MutableList<Task>) :
    RecyclerView.Adapter<GlossaryAdapter.TaskViewHolder>() {
    // Store a reference to the currently expanded card position
    private var expandedCardPosition: Int = -1
    fun updateTasks(newTasks: List<Task>) {
        allTasks.clear()
        allTasks.addAll(newTasks)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_item_glossary, parent, false)
        return TaskViewHolder(itemView)

    }

    override fun onBindViewHolder(
        holder: TaskViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        holder.bind(allTasks[position])
        // Set an OnClickListener to handle expanding/collapsing
        holder.titleCardView.setOnClickListener {
            if (expandedCardPosition == position) {
                // Clicked on an already expanded card, so collapse it
                holder.collapse()
                expandedCardPosition = -1
            } else {
                // Clicked on a different card, collapse the previously expanded card (if any)
                val previouslyExpandedCardPosition = expandedCardPosition
                if (previouslyExpandedCardPosition != -1) {
                    notifyItemChanged(previouslyExpandedCardPosition)
                }
                // Expand the clicked card
                holder.expand()
                expandedCardPosition = position
            }
        }

        holder.dropDownImage.setOnClickListener {
            if (expandedCardPosition == position) {
                // Clicked on an already expanded card, so collapse it
                holder.collapse()
                expandedCardPosition = -1
            } else {
                // Clicked on a different card, collapse the previously expanded card (if any)
                val previouslyExpandedCardPosition = expandedCardPosition
                if (previouslyExpandedCardPosition != -1) {
                    notifyItemChanged(previouslyExpandedCardPosition)
                }
                // Expand the clicked card
                holder.expand()
                expandedCardPosition = position
            }
        }

        // Check if the current card should be expanded or collapsed based on its position
        if (expandedCardPosition == position) {
            holder.expand()
        } else {
            holder.collapse()
        }
    }

    override fun getItemCount(): Int {
        return allTasks.size
    }

    inner class TaskViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private var isExpanded = false
        val alphabetTextView: TextView = itemView.findViewById(R.id.tv_alphabet)
        val titleCardView: ConstraintLayout = itemView.findViewById(R.id.taskTitleLayout)
        val titleTextView: TextView = itemView.findViewById(R.id.tv_title)
        val descriptionTextView: TextView = itemView.findViewById(R.id.tv_Description)
        val dropDownImage: ImageView = itemView.findViewById(R.id.dropdownButton)
        fun bind(task: Task) {
            alphabetTextView.text = task.alphabet
            titleTextView.text = task.taskName
            descriptionTextView.text = task.taskDescription
            titleCardView.setOnClickListener {
                if (isExpanded) collapse() else expand()
            }
            dropDownImage.setOnClickListener {
                if (isExpanded) collapse() else expand()
            }
            //All tasks should be collapsed by default
            collapse()
        }

        fun expand() {
            AnimationUtils.expand(descriptionTextView)
            dropDownImage.setImageResource(R.drawable.minus)
            isExpanded = true
        }

        fun collapse() {
            AnimationUtils.collapse(descriptionTextView)
            dropDownImage.setImageResource(R.drawable.ic_expand)
            isExpanded = false
        }
    }
}
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
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.R
import com.calmscient.di.remote.response.SubSection

class SubsectionAdapter(private val subsectionList: List<SubSection>) : RecyclerView.Adapter<SubsectionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.progress_on_course_work_expandable_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val subsection = subsectionList[position]
        holder.bind(subsection)
    }

    override fun getItemCount(): Int {
        return subsectionList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val subSectionNameTextView: TextView = itemView.findViewById(R.id.subSectionName)
        private val completionTextView: TextView = itemView.findViewById(R.id.subSectionPercentage)

        fun bind(subsection: SubSection) {
            subSectionNameTextView.text = subsection.subSectionName
            completionTextView.text = "${subsection.completion}%"
        }
    }
}

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
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.R
import com.calmscient.di.remote.BasicKnowledgeItem

class BasicKnowledgeAdapter(
    private val fragmentManager: FragmentManager,
    private val context: Context,
    private val items: List<BasicKnowledgeItem>,
    private val onItemClicked: (BasicKnowledgeItem) -> Unit
) : RecyclerView.Adapter<BasicKnowledgeAdapter.ViewHolder>() {

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
            onItemClicked(item)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.text)
        val completedImage: ImageView = view.findViewById(R.id.completeIcon)
        val cardViewLayout: CardView = itemView.findViewById(R.id.basicKnowledgeCardView)
    }
}


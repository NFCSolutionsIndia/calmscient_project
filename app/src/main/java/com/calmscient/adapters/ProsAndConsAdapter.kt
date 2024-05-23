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
import com.calmscient.di.remote.ConsItem
import com.calmscient.di.remote.ProdAndConsItem
import com.calmscient.di.remote.ProsItem

class ProsAndConsAdapter(private val items: List<ProdAndConsItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION
    override fun getItemViewType(position: Int): Int {
        return items[position].type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.pros_cons_item, parent, false)
        return when (viewType) {
            1 -> ProsViewHolder(view)
            2 -> ConsViewHolder(view)
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ProsItem -> (holder as ProsViewHolder).bind(item,position)
            is ConsItem -> (holder as ConsViewHolder).bind(item,position)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ProsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.tv_name)
       // private val cardView: TextView = itemView.findViewById(R.id.prod_consCard)

        init {
            itemView.setOnClickListener {
                val previousSelectedPosition = selectedPosition
                selectedPosition = adapterPosition

                // Notify the old item to change its background
                if (previousSelectedPosition != RecyclerView.NO_POSITION) {
                    notifyItemChanged(previousSelectedPosition)
                }
                // Notify the new item to change its background
                notifyItemChanged(selectedPosition)
            }
        }

        fun bind(data: ProsItem, position: Int) {
            textView.text = data.name

            if (position == selectedPosition) {
                textView.setBackgroundResource(R.drawable.card_selected_background)
                textView.setTextColor(itemView.resources.getColor(R.color.white, null))
            } else {
                textView.setBackgroundResource(R.drawable.card_default_background)
                textView.setTextColor(itemView.resources.getColor(R.color.purple_100, null))
            }
        }
    }

    inner class ConsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.tv_name)

        init {
            itemView.setOnClickListener {
                val previousSelectedPosition = selectedPosition
                selectedPosition = adapterPosition

                // Notify the old item to change its background
                if (previousSelectedPosition != RecyclerView.NO_POSITION) {
                    notifyItemChanged(previousSelectedPosition)
                }
                // Notify the new item to change its background
                notifyItemChanged(selectedPosition)
            }
        }

        fun bind(data: ConsItem, position: Int) {
            textView.text = data.name

            if (position == selectedPosition) {
                textView.setBackgroundResource(R.drawable.card_selected_background)
                textView.setTextColor(itemView.resources.getColor(R.color.white, null))
            } else {
                textView.setBackgroundResource(R.drawable.card_default_background)
                textView.setTextColor(itemView.resources.getColor(R.color.purple_100, null))
            }
        }
    }
}


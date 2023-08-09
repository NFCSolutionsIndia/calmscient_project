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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.R
class OptionsAdapter(
    private val options: List<String>,
    private val selectedOptionIndex: Int,
    private val optionClickListener: (Int) -> Unit
) : RecyclerView.Adapter<OptionsAdapter.OptionsViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionsViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.option_item_card_view, parent, false)
        return OptionsViewHolder(itemView)
    }

    inner class OptionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val optionTextView: TextView = itemView.findViewById(R.id.option)

        init {
            itemView.setOnClickListener {
                val clickedPosition = adapterPosition
                optionClickListener.invoke(clickedPosition)
            }
        }
    }


    override fun onBindViewHolder(holder: OptionsViewHolder, position: Int) {
        val option = options[position]
        holder.optionTextView.text = option

        if (position == selectedOptionIndex) {
            // Change the color of the selected card and text
            holder.itemView.setBackgroundResource(R.drawable.card_selected_background)
            holder.optionTextView.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.white
                )
            )
        } else {
            // Reset the color for non-selected options
            holder.itemView.setBackgroundResource(R.drawable.card_default_background)
            holder.optionTextView.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.black
                )
            )
        }
    }


    override fun getItemCount(): Int {
        return options.size
    }
}

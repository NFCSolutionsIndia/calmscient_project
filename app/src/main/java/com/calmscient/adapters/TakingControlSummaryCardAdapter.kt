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
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.calmscient.R
import com.calmscient.fragments.SummaryDataClass
class TakingControlSummaryCardAdapter (private val items: List<SummaryDataClass>) :
    RecyclerView.Adapter<TakingControlSummaryCardAdapter.CardViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.summary_takingconotrol_items, parent, false)
        return CardViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val item = items[position]
        Glide.with(holder.logo.context).load(item.logoUrl).into(holder.logo)
        holder.progressValue.progress = item.process
        holder.title.text = item.title
        holder.goalCount.text = item.goalCount
        // Conditional text setting for countOrTimes
        when (position) {
            0 -> holder.countOrTimes.text = "days" // For the first item
            1 -> holder.countOrTimes.text = "counts" // For the second item
            else -> holder.countOrTimes.text = "times" // For all other items
        }
    }
    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val logo: ImageView = itemView.findViewById(R.id.iv_logo)
        val title: TextView = itemView.findViewById(R.id.tv_title)
        val progressValue: ProgressBar = itemView.findViewById(R.id.progressbar)
        val goalCount: TextView = itemView.findViewById(R.id.tv_goalCount)
        val countOrTimes: TextView = itemView.findViewById(R.id.tv_countOrTimes)
    }
}
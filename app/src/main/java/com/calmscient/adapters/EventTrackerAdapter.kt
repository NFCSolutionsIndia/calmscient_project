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
import android.widget.ToggleButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.calmscient.R
import com.calmscient.databinding.EventTrackerItemBinding
import com.calmscient.di.remote.EventTrackerDataClass

class EventTrackerAdapter(private val events: List<EventTrackerDataClass>) :
    RecyclerView.Adapter<EventTrackerAdapter.EventViewHolder>() {

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = EventTrackerItemBinding.bind(itemView)

        fun bind(event: EventTrackerDataClass) {
            binding.apply {
                eventTrackerName.text = event.name
                Glide.with(itemView.context).load(event.image).into(eventTrackerImage)
                eventTrackerToggleButton.isOn = event.toggleButton

                eventTrackerToggleButton.labelOn = "Yes"
                eventTrackerToggleButton.labelOff = "No"


            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_tracker_item, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.bind(event)

    }

    override fun getItemCount(): Int {
        return events.size
    }


}

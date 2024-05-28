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
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.R
import com.calmscient.databinding.ConsequencesCardItemBinding
import com.calmscient.di.remote.ConsequencesDataClass

class ConsequencesAdapter(private val data: List<ConsequencesDataClass>) : RecyclerView.Adapter<ConsequencesAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ConsequencesCardItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ConsequencesDataClass) {
            binding.headingTextView.text = item.headingText
           // binding.dialogText.text = item.dialogText
            binding.descriptionText.text = item.descriptionText
            binding.pointsText.text = item.pointsText
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ConsequencesCardItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size
}

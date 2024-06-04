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
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.calmscient.databinding.DrinkTrackerItemCardBinding
import com.calmscient.di.remote.DrinkTrackerItem

class DrinkTrackerAdapter(private val itemList: List<DrinkTrackerItem>) : RecyclerView.Adapter<DrinkTrackerAdapter.ItemViewHolder>() {

    val totalItemCount: MutableLiveData<Int> = MutableLiveData()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = DrinkTrackerItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class ItemViewHolder(private val binding: DrinkTrackerItemCardBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DrinkTrackerItem) {
            binding.itemDescription.text = item.itemDescription
            binding.itemCount.text = item.itemCount.toString()


            Glide.with(itemView.context)
                .load(item.itemImage)
                .into(binding.itemImage)

            binding.itemPlusIcon.setOnClickListener {
                item.itemCount++
                binding.itemCount.text = item.itemCount.toString()
                totalItemCount.value = calculateTotalItemCount()
            }

            binding.itemMinusIcon.setOnClickListener {
                if (item.itemCount > 0) {
                    item.itemCount--
                    binding.itemCount.text = item.itemCount.toString()
                    totalItemCount.value = calculateTotalItemCount()
                }
            }
        }
    }

    internal fun calculateTotalItemCount(): Int {
        var totalCount = 0
        itemList.forEach { totalCount += it.itemCount }
        return totalCount
    }

    fun getCurrentQuantities(): List<Int> {
        return itemList.map { it.itemCount }
    }
}

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
import android.widget.TextView
import android.widget.ToggleButton
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.R
import com.calmscient.di.remote.PrivacyItemDataClass

class PrivacyAdapter (private val data: List<PrivacyItemDataClass>, private val context: Context) : RecyclerView.Adapter<PrivacyAdapter.ViewHolder>() {

    // Define the ViewHolder class
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val textView: TextView = itemView.findViewById(R.id.tv_privacyItem)
        val toggleButton: com.github.angads25.toggle.widget.LabeledSwitch = itemView.findViewById(R.id.privacyToggleButtonItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate your item layout here and create a new ViewHolder
        val view = LayoutInflater.from(parent.context).inflate(R.layout.privacy_item_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Bind data to your ViewHolder components here
        val item = data[position]
        holder.textView.text = item.text

        holder.toggleButton.labelOn = context.getString(R.string.yes)
        holder.toggleButton.labelOff = context.getString(R.string.no)

        // holder.switch.isChecked = item.isChecked
        // holder.toggleButton.setHasTransientState(item.isChecked)

    }

    override fun getItemCount(): Int {
        // Return the number of items in your data list
        return data.size
    }
}
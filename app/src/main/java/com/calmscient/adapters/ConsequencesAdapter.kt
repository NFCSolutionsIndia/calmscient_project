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
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.R
import com.calmscient.databinding.ConsequencesCardItemBinding
import com.calmscient.di.remote.ConsequencesDataClass

class ConsequencesAdapter(private val context: Context, private val data: List<ConsequencesDataClass>) : RecyclerView.Adapter<ConsequencesAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ConsequencesCardItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ConsequencesDataClass) {
            binding.headingTextView.text = item.headingText
            binding.descriptionText.text = item.descriptionText
            binding.pointsText.text = item.pointsText

            // Set the visibility of the bulb icon
            if (item.dialogText.isNullOrEmpty()) {
                binding.bulbIcon.visibility = View.GONE
            } else {
                binding.bulbIcon.visibility = View.VISIBLE
                binding.bulbIcon.setOnClickListener {

                    showInformationDialog(item.dialogText)
                }
            }
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

    internal fun showInformationDialog(dialogText: String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.recognize_dialog, null)
        val infoTextView = dialogView.findViewById<TextView>(R.id.dialogTextView)
        val closeButton = dialogView.findViewById<ImageView>(R.id.closeButton)
        val titleTextView = dialogView.findViewById<TextView>(R.id.titleTextView)

        titleTextView.text = context.getString(R.string.did_you_know)
        infoTextView.text = dialogText
        val dialogBuilder = AlertDialog.Builder(context, R.style.CustomDialog)
            .setView(dialogView)

        val dialog = dialogBuilder.create()
        dialog.show()

        closeButton.setOnClickListener {
            dialog.dismiss()
        }
    }
}


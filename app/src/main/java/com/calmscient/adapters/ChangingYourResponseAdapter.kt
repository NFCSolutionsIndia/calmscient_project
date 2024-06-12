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

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.calmscient.R
import com.calmscient.di.remote.ChapterDataClass

class ChangingYourResponseAdapter(
    private val chapters: List<ChapterDataClass>,
    private val itemClickListener: (ChapterDataClass) -> Unit
) : RecyclerView.Adapter<ChangingYourResponseAdapter.ChapterViewHolder>() {

    inner class ChapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chapterName: TextView = itemView.findViewById(R.id.titleTextView)
        val chapterImage: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.change_response_item_card, parent, false)
        return ChapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) {
        val chapter = chapters[position]
        holder.chapterName.text = chapter.chapterName

        Log.d("Image URL :","${chapter.imageUrl}")
        // Use Glide to load the image
        Glide.with(holder.itemView.context)
            .load(chapter.imageUrl)
            .placeholder(R.drawable.placeholder)
            .into(holder.chapterImage)

        holder.itemView.setOnClickListener {
            itemClickListener(chapter)
        }
    }

    override fun getItemCount(): Int {
        return chapters.size
    }


}


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
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.R
import com.calmscient.di.remote.MindfulnessExercisesTextDataClass
class MindfulnessScreensAdapter (private val items: List<MindfulnessExercisesTextDataClass>,private val favourite: Int, private val onFavoriteClicked: (Boolean) -> Unit) :
    RecyclerView.Adapter<MindfulnessScreensAdapter.MindfullnessQuestionsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MindfullnessQuestionsViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.exercises_items, parent, false)
        return MindfullnessQuestionsViewHolder(itemView)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: MindfullnessQuestionsViewHolder, position: Int) {
        val item = items[position]
        var isFavorite = favourite == 1
        // Check and set text1
        if (item.text1.isNullOrBlank()) {
            holder.text1.visibility = View.GONE
        } else {
            holder.text1.text = item.text1
            holder.text1.visibility = View.VISIBLE
        }

        // Check and set text2
        if (item.text2.isNullOrBlank()) {
            holder.text2.visibility = View.GONE
        } else {

            holder.text2.text = Html.fromHtml(item.text2, Html.FROM_HTML_MODE_LEGACY)
            holder.text2.visibility = View.VISIBLE
        }
        /*holder.text1.text = item.text1
        holder.text2.text = item.text2*/
        item.exercisesBellIcon?.let { holder.exercisesBellIcon.setImageResource(it) }
        item.exercisesHeartImage?.let { holder.exercisesHeartImage.setImageResource(it) }
        item.imageAnxiety?.let { holder.imageAnxiety.setImageResource(it) }

        item.exercisesHeartImage?.let {
            holder.exercisesHeartImage.visibility = View.VISIBLE
            if (favourite == 1) {
                isFavorite = false
                holder.exercisesHeartImage.setImageResource(R.drawable.ic_mind_exercise_red_fill)
            } else {
                isFavorite = true
                holder.exercisesHeartImage.setImageResource(R.drawable.mindfullexercise_heart__image)
            }
        }

        // Handle click event for exercisesHeartImage
        holder.exercisesHeartImage.setOnClickListener {
            isFavorite = !isFavorite

            // Update the image based on the isFavorite flag
            if (isFavorite) {
                holder.exercisesHeartImage.setImageResource(R.drawable.mindfullexercise_heart__image)
                // Perform any other actions you need when it's set as favorite
                onFavoriteClicked(false)
            } else {
                holder.exercisesHeartImage.setImageResource(R.drawable.ic_mind_exercise_red_fill)
                // Perform any other actions you need when it's not set as favorite
                onFavoriteClicked(true)
            }
        }


    }

    inner class MindfullnessQuestionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text1: TextView = itemView.findViewById(R.id.text1)
        val text2: TextView = itemView.findViewById(R.id.text2)
        val exercisesBellIcon: ImageView = itemView.findViewById(R.id.exercisesBellIcon)
        val exercisesHeartImage: ImageView = itemView.findViewById(R.id.exercisesHeartImage)
        val imageAnxiety: ImageView = itemView.findViewById(R.id.imageAnxiety)

    }
}

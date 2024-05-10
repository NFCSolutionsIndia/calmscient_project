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
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.R
import com.calmscient.fragments.HistoryDataClass

class HistoryCardAdapter(private val items: List<HistoryDataClass>) :
    RecyclerView.Adapter<HistoryCardAdapter.CardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return CardViewHolder(view)
    }

    override fun getItemCount(): Int = items.size


    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val item = items[position]
        holder.textviewDate.text = item.date
        holder.textviewTime.text = item.time
        holder.score.text = item.score.toString()
        holder.total.text = item.total.toString()
        //holder.textviewQuestionValue.text = item.questionCount
        holder.progressValue.progress = item.progressBarValue!!
    }
   /* private fun resultPercent() {

        val progressBar: ProgressBar
        val textView: TextView
        var progressStatus = 0
        val handler = Handler()

        Thread {
            while (progressStatus < 60) {
                progressStatus += 1
                // Update the progress bar and display the
                //current value in the text view
                handler.post(Runnable {
                    //binding.progressbarResult.progress = progressStatus
                })
                try {
                    // Sleep for 200 milliseconds.
                    Thread.sleep(25)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }.start()
    }*/
    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textviewDate: TextView = itemView.findViewById(R.id.tv_date)
        val textviewTime: TextView = itemView.findViewById(R.id.tv_time)
        val progressValue: ProgressBar = itemView.findViewById(R.id.progressbar_history)
        val score: TextView = itemView.findViewById(R.id.tv_score)
        val total: TextView = itemView.findViewById(R.id.tv_total)
    }
}
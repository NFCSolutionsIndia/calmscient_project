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
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.Interface.CellClickListener
import com.calmscient.R
import com.calmscient.fragments.CardViewItem
import com.calmscient.fragments.MedicationDetailFragment

class MedicationsCardAdapter(private val items: List<CardViewItem>) :
    RecyclerView.Adapter<MedicationsCardAdapter.CardViewHolder>() {
    lateinit var mListener: CellClickListener
    /*interface onItemClickListener {
        fun onItemClick(postion: Int)
    }

    fun setOnItemClickListener(listener: CellClickListener) {
        mListener = listener
    }*/


    fun setCellClickListener(listener: CellClickListener) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.medications_item_card_view, parent, false)
        return CardViewHolder(view)
    }

    /*override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val item = items[position]
        holder.titleTextView.text = item.title
        holder.dosageTextView.text = item.dosage
        holder.timeMorningTextView.text = item.timeMorning
        holder.timeEveningTextView.text = item.timeEvening
        item.sunImageResource?.let { holder.imageViewSun.setImageResource(it) }
        item.moonImageResource?.let { holder.imageViewMoon.setImageResource(it) }
       *//* holder.cardViewLayout.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val activity = v!!.context as AppCompatActivity
                val fragment = MedicationDetailFragment()
                activity.supportFragmentManager.beginTransaction()
                    .replace(R.id.flFragment, fragment).addToBackStack(null).commit()
            }
        })*//*

        holder.cardViewLayout.setOnClickListener {
            // Retrieve the corresponding CardViewItem object
            val clickedItem = items[position]
            // Pass the clicked item to the onCellClickListener interface
            mListener.onCellClickListener(position, clickedItem)
        }
    }*/


    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val item = items[position]
        holder.titleTextView.text = item.title
        holder.dosageTextView.text = item.dosage
        holder.timeMorningTextView.text = item.timeMorning
        holder.timeEveningTextView.text = item.timeEvening

        // Check if sunImageResource is not null before setting the image resource
        if (item.sunImageResource != null) {
            holder.imageViewSun.setImageResource(item.sunImageResource)
            holder.imageViewSun.visibility = View.VISIBLE
        } else {
            holder.imageViewSun.visibility = View.GONE
        }

        // Check if moonImageResource is not null before setting the image resource
        if (item.moonImageResource != null) {
            holder.imageViewMoon.setImageResource(item.moonImageResource)
            holder.imageViewMoon.visibility = View.VISIBLE
        } else {
            holder.imageViewMoon.visibility = View.GONE
        }

        holder.cardViewLayout.setOnClickListener {
            // Retrieve the corresponding CardViewItem object
            val clickedItem = items[position]
            // Pass the clicked item to the onCellClickListener interface
            mListener.onCellClickListener(position, clickedItem)
        }
    }


    /*private fun loadFragment(fragment: Fragment) {
        val transaction = context.supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.commit()
    }*/
    override fun getItemCount(): Int = items.size

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val cardViewLayout: CardView = itemView.findViewById(R.id.item_card_view)
        val dosageTextView: TextView = itemView.findViewById(R.id.dosageTextView)
        val timeMorningTextView: TextView = itemView.findViewById(R.id.timeMorningTextView)
        val timeEveningTextView: TextView = itemView.findViewById(R.id.timeEveningTextView)
        val imageViewSun: ImageView = itemView.findViewById(R.id.imageViewSun)
        val imageViewMoon: ImageView = itemView.findViewById(R.id.imageViewMoon)

    }

}

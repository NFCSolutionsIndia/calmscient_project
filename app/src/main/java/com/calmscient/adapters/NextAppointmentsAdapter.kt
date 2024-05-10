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
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.Interface.CellClickListener
import com.calmscient.Interface.CellClickListenerAppointments
import com.calmscient.R
import com.calmscient.fragments.AppointmentdetailsFragment

import com.calmscient.fragments.CardViewItems
import com.calmscient.fragments.NextAppointmentsFragment
class NextAppointmentsAdapter (val context: Context, private val items: List<CardViewItems>) :
    RecyclerView.Adapter<NextAppointmentsAdapter.CardViewHolder>() {
    lateinit var mListener: CellClickListenerAppointments
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NextAppointmentsAdapter.CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.nextappointemts_item, parent, false)
        return CardViewHolder(view)
    }
    override fun onBindViewHolder(
        holder: NextAppointmentsAdapter.CardViewHolder, position: Int
    ) {
        val item = items[position]
        if (item.noAppointmentsTextView.isNullOrEmpty()) {
            holder.noAppointmentsTextView.visibility = View.GONE
        } else {
            holder.noAppointmentsTextView.visibility = View.VISIBLE
            holder.noAppointmentsTextView.text = item.noAppointmentsTextView
        }
        holder.dateview1.text = item.dateview1
        item.doctor_logo?.let { holder.doctor_logo.setImageResource(it) }
        holder.DoctorNameTextView.text = item.DoctorNameTextView

        // holder.noAppointmentsTextView.text = item.noAppointmentsTextView
        holder.HosptailNameTextView.text = item.HosptailNameTextView
        item.img_arrow1?.let {
            holder.img_arrow1.setImageResource(it)
        }

        holder.nextAppointmentsCard.setOnClickListener {
            // Retrieve the corresponding CardViewItem object
            val clickedItem = items[position]
            // Pass the clicked item to the onCellClickListener interface
            mListener.onCellClickListener(position, clickedItem)
        }

       /* holder.nextAppointmentsCard.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if(item.DoctorNameTextView == context.getString(R.string.doctor_name1)) {
                    val activity = v!!.context as AppCompatActivity
                    val fragment = AppointmentdetailsFragment()
                    activity.supportFragmentManager.beginTransaction()
                        .replace(R.id.flFragment, fragment).addToBackStack(null).commit()
                }else{
                    Toast.makeText(context, context.getString(R.string.no_appointments), Toast.LENGTH_SHORT).show()

                }
            }
        })*/
    }

    override fun getItemCount(): Int = items.size
    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateview1: TextView = itemView.findViewById(R.id.dateview1)
        val nextAppointmentsCard: CardView = itemView.findViewById(R.id.mainCard)
        val doctor_logo: ImageView = itemView.findViewById(R.id.doctor_logo)
        val DoctorNameTextView: TextView = itemView.findViewById(R.id.DoctorNameTextView)
        val noAppointmentsTextView: TextView = itemView.findViewById(R.id.noAppointmentsTextView)
        val HosptailNameTextView: TextView = itemView.findViewById(R.id.HosptailNameTextView)
        val img_arrow1: ImageView = itemView.findViewById(R.id.img_arrow1)

    }

    fun setCellClickListener(listener: CellClickListenerAppointments) {
        mListener = listener
    }
}
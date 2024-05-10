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

package com.calmscient.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.calmscient.R
import com.calmscient.adapters.TakingControlSummaryCardAdapter
import com.calmscient.databinding.LayoutSummaryTakingControlBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class SummaryDataClasss(
    val imageView1: Int?,
    val text1: String?,
    val text3:String?,
    val text4:String?,
    val process: Int)
class SummaryTakingControlFragment : Fragment() {
    private lateinit var binding: LayoutSummaryTakingControlBinding
    private lateinit var calenderView: ImageView
    private lateinit var monthText: TextView
    lateinit var takingControlSummaryCardAdapter: TakingControlSummaryCardAdapter
    private val SummaryDataClasss = mutableListOf<SummaryDataClasss>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this){
            loadFragment(TakingControlFragment())
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = LayoutSummaryTakingControlBinding.inflate(inflater, container, false)
        binding.backIcon.setOnClickListener {
            loadFragment(TakingControlFragment())
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerSummary.layoutManager = LinearLayoutManager(requireContext())
        takingControlSummaryCardAdapter = TakingControlSummaryCardAdapter(SummaryDataClasss)
        binding.recyclerSummary.adapter = takingControlSummaryCardAdapter
        displayCardViews()
        calenderView = view.findViewById(R.id.calenderview)
        monthText = view.findViewById(R.id.monthtext)

        //create a date string.
        val date_n: String = SimpleDateFormat(
            "dd MM yyyy",
            Locale.getDefault()
        ).format(Date()) //get hold of textview.
        monthText.text = date_n

        val myCalender = Calendar.getInstance()

        val datePickerListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            myCalender.set(Calendar.YEAR, year)
            myCalender.set(Calendar.MONTH, month)
            myCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabel(myCalender)
        }

        calenderView.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                datePickerListener,
                myCalender.get(Calendar.YEAR),
                myCalender.get(Calendar.MONTH),
                myCalender.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun displayCardViews() {
        SummaryDataClasss.clear()
        SummaryDataClasss.addAll(
            listOf(
                SummaryDataClasss(R.drawable.ic_alcoholfree, getString(R.string.alchol_free), "10",getString(R.string.summary_takingcontrol_days),30),
                SummaryDataClasss(R.drawable.ic_drinks, getString(R.string.drinks), "60",getString(R.string.summary_takingcontrol_counts),80),
                SummaryDataClasss(R.drawable.ic_argument1, getString(R.string.argument),"04",getString(R.string.summary_takingcontrol_times),4),
                SummaryDataClasss(R.drawable.ic_accident1, getString(R.string.events_tracker_text3),  "02",getString(R.string.summary_takingcontrol_times),2),
            )
        )
        takingControlSummaryCardAdapter.notifyDataSetChanged()
    }
    private fun updateLabel(myCalender: Calendar) {
        val monthNames = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )

        val currentTime = Calendar.getInstance().time
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.UK)
        val formattedDate = sdf.format(myCalender.time)


        monthText.text = " $formattedDate"
    }
    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null) // This ensures that the previous fragment is added to the back stack
        transaction.commit()
    }
}
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
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.databinding.ProgressOnCourseWorkItemCardBinding
import com.calmscient.di.remote.response.PatientcourseWork

class SummaryOfProgressWorksAdapter(
    private val context: Context,
    private var courseWorkList: List<PatientcourseWork>,
    private val onItemClick: (selectedCourseWork: PatientcourseWork, position: Int) -> Unit // Add position parameter
) : RecyclerView.Adapter<SummaryOfProgressWorksAdapter.CourseWorkViewHolder>() {

    inner class CourseWorkViewHolder(private val binding: ProgressOnCourseWorkItemCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(courseWork: PatientcourseWork, position: Int) {
            binding.courseName.text = courseWork.courseName
            binding.completedPercentage.text = "${courseWork.completedPer}%"
            binding.itemNextIcon.setOnClickListener {
                onItemClick(courseWork, position)
            }
            binding.root.setOnClickListener {
                onItemClick(courseWork, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseWorkViewHolder {
        val binding = ProgressOnCourseWorkItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CourseWorkViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseWorkViewHolder, position: Int) {
        val courseWork = courseWorkList[position]
        holder.bind(courseWork, position) // Pass position to bind function
    }

    override fun getItemCount(): Int {
        return courseWorkList.size
    }

    fun updateCourseWorkList(newCourseWorkList: List<PatientcourseWork>) {
        courseWorkList = newCourseWorkList
        notifyDataSetChanged()
    }
}




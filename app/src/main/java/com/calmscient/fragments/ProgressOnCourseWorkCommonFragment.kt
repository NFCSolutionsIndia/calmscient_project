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

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.calmscient.R
import com.calmscient.adapters.SectionAdapter
import com.calmscient.databinding.FragmentProgressOnCourseWorkCommonBinding
import com.calmscient.di.remote.response.PatientcourseWork
import com.calmscient.utils.common.JsonUtil


class ProgressOnCourseWorkCommonFragment : Fragment() {

    private lateinit var binding : FragmentProgressOnCourseWorkCommonBinding
    private var selectedCourseWork: PatientcourseWork? = null
    private var selectedPosition: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            val res = it.getString("selectedCourseWork")

            selectedCourseWork = res?.let { it1 -> JsonUtil.fromJsonString(it1) }

           selectedPosition = arguments?.getInt("selectedPosition")





            Log.d("Selected Course Work","$selectedCourseWork")

            Log.d("Selected Position","$selectedPosition")
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentProgressOnCourseWorkCommonBinding.inflate(inflater,container,false)

        binding.itemHeading.text = selectedCourseWork?.courseName
        binding.itemPercentageText.text = selectedCourseWork?.completedPer.toString()


        binding.backIcon.setOnClickListener{
            loadFragment(ProgressOnCourseWorkFragment())
        }

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectedCourseWork = arguments?.getSerializable("selectedCourseWork") as? PatientcourseWork
        selectedCourseWork?.let {
            binding.itemHeading.text = it.courseName
            binding.itemPercentageText.text = "${it.completedPer}%"
            binding.itemProgress.progress = it.completedPer.toInt()

            Log.d("Selected Course Work", "$it")


        }

        binding.expandableRecyclerView.layoutManager = LinearLayoutManager(context)

        // Set adapter
        val adapter = selectedCourseWork?.let { SectionAdapter(it.sectionsList) }
        binding.expandableRecyclerView.adapter = adapter

        selectedPosition = arguments?.getInt("selectedPosition") as? Int

        Log.d("Selected Position", "$selectedPosition")
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }






}
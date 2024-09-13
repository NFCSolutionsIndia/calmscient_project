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

package com.calmscient.activities

import com.calmscient.fragments.HomeFragment
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.R
import com.calmscient.databinding.ActivityMedicationsBinding

class MedicationsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMedicationsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backIcon.setOnClickListener {
            // Call the loadFragment function with HomeFragment instance
            loadFragment(HomeFragment())
        }
        setupMonthDaysRecyclerView()
    }

    private fun loadFragment(fragment: Fragment) {
        /*val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.homeFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()*/
    }

    private fun setupMonthDaysRecyclerView() {
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)

        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        //recyclerView.adapter = MonthDaysAdapter()
    }
}
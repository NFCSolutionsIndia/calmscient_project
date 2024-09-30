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

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.calmscient.R
import com.calmscient.fragments.DiscoveryFragment
import com.calmscient.fragments.ExerciseFragment
import com.calmscient.fragments.ExerciseInteractionListener
import com.calmscient.fragments.HomeFragment
import com.calmscient.utils.common.SavePreferences
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardActivity : AppCompat(), ExerciseInteractionListener {
    lateinit var bottomNav: BottomNavigationView
    lateinit var savePrefData: SavePreferences

    override fun onExerciseSelected() {
        bottomNav.menu.findItem(R.id.home).setIcon(R.drawable.ic_home)
        bottomNav.menu.findItem(R.id.discovery).setIcon(R.drawable.ic_discovery_un)
        bottomNav.menu.findItem(R.id.exercises).setIcon(R.drawable.ic_exercises_selected)
        bottomNav.menu.findItem(R.id.rewards).setIcon(R.drawable.ic_rewards_un)

        bottomNav.menu.findItem(R.id.title)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        savePrefData = SavePreferences(this)
        bottomNav = findViewById(R.id.bottomNavigationView) as BottomNavigationView
        bottomNav.labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_LABELED
        bottomNav.itemIconTintList = null
        // Set the HomeFragment icon to the new icon as default
        bottomNav.menu.findItem(R.id.home).setIcon(R.drawable.ic_home_selected)
        loadFragment(HomeFragment())
        //Navigation graph
        /*val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.flFragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setupWithNavController(navController)*/



        bottomNav.setOnItemSelectedListener {
            bottomNav.menu.findItem(R.id.home).setIcon(R.drawable.ic_home)
            bottomNav.menu.findItem(R.id.discovery).setIcon(R.drawable.ic_discovery_un)
            bottomNav.menu.findItem(R.id.exercises).setIcon(R.drawable.ic_exercises_un)
            bottomNav.menu.findItem(R.id.rewards).setIcon(R.drawable.ic_rewards_un)
            when (it.itemId) {
                R.id.home -> {
                    it.setIcon(R.drawable.ic_home_selected)
                    loadFragment(HomeFragment())
                    true
                }

                R.id.discovery -> {
                    loadFragment(DiscoveryFragment())
                    it.setIcon(R.drawable.ic_discovery_selected)
                    true
                }

                R.id.exercises -> {
                    loadFragment(ExerciseFragment())
                    it.setIcon(R.drawable.ic_exercises_selected)
                    true
                }

                R.id.rewards -> {
                    //loadFragment(RewardsFragment())
                    Toast.makeText(applicationContext, "Coming Soon", Toast.LENGTH_SHORT).show()
                    it.setIcon(R.drawable.ic_rewards_selected)
                    true
                }

                else -> {
                    false
                }
            }
        }
    }
    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.flFragment)
        if (fragment is HomeFragment) {
            fragment.onBackPressed()
        } else if (fragment is ExerciseFragment) {
            fragment.onBackPressed()
        } else if (fragment is DiscoveryFragment) {
            fragment.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }

}
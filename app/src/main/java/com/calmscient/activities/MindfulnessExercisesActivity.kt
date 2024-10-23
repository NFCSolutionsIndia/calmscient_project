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
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.R
import com.calmscient.adapters.MindfulnessScreensAdapter
import com.calmscient.di.remote.MindfulnessExercisesTextDataClass
import com.calmscient.databinding.MindfulnesscreensBinding
import com.calmscient.fragments.DiscoveryFragment
class MindfulnessExercisesActivity : AppCompat() {
    private lateinit var binding: MindfulnesscreensBinding
    private lateinit var anxietyadapter: MindfulnessScreensAdapter
    private val anxietyText = mutableListOf<MindfulnessExercisesTextDataClass>()
    private var currentQuestionIndex = 0
    private var previousQuestionIndex = -1
    private lateinit var stepIndicators: List<ImageView>
    private val maxProgress = 99
    private lateinit var progressBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MindfulnesscreensBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val pagerSnapHelper = PagerSnapHelper()
        pagerSnapHelper.attachToRecyclerView(binding.optionsRecyclerView1)
        binding.previousQuestion.visibility = View.GONE
        // Find your ProgressBar by its ID
        progressBar = findViewById<ProgressBar>(R.id.progressBar2)

        // Set the initial progress
        progressBar.progress = currentQuestionIndex * (maxProgress / (anxietyText.size - 1))

        setupNavigation()
        //initializeAdapter()
        displayCardViews()

        stepIndicators = listOf(
            findViewById(R.id.step1Indicator),
            findViewById(R.id.step2Indicator),
            findViewById(R.id.step3Indicator),
            findViewById(R.id.step4Indicator),
            findViewById(R.id.step5Indicator),
            findViewById(R.id.step6Indicator)
        )

        binding.menuIcon.setOnClickListener {
            onBackPressed()
        }
    }
    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentByTag(DiscoveryFragment::class.java.simpleName)
        if (fragment != null && fragment.isVisible) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
    private fun initializeAdapter() {
        binding.optionsRecyclerView1.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        //anxietyadapter = MindfulnessScreensAdapter(anxietyText)
        binding.optionsRecyclerView1.adapter = anxietyadapter
    }

    private fun displayCardViews() {
        anxietyText.add(
            MindfulnessExercisesTextDataClass(
                null,
                getString(R.string.screen1),
                null,
                null,
                R.drawable.ic_mind_1

            )
        );
        anxietyText.add(
            MindfulnessExercisesTextDataClass(
                null,
                getString(R.string.screen2),
                null,
                null,
                R.drawable.ic_mind_2
            )
        );
        anxietyText.add(
            MindfulnessExercisesTextDataClass(
                getString(R.string.screen3_heading),
                getString(R.string.screen3_belowheading),
                null,
                null,
                R.drawable.ic_mind_3

            )
        );
        anxietyText.add(
            MindfulnessExercisesTextDataClass(
                null,
                getString(R.string.screen4),
                null,
                null,
                R.drawable.ic_mind_4
            )
        );
        anxietyText.add(
            MindfulnessExercisesTextDataClass(
                null,
                getString(R.string.screen5),
                null,
                null,
                R.drawable.ic_mind_5
            )
        );
        anxietyText.add(
            MindfulnessExercisesTextDataClass(
                null,
                getString(R.string.screen6),
                R.drawable.exercises_bellicon,
                R.drawable.mindfullexercise_heart__image,
                R.drawable.ic_mind_6
            )
        );
        anxietyadapter.notifyDataSetChanged()
    }

    private fun setupNavigation() {
        binding.nextQuestion.setOnClickListener {
            navigateToQuestion(currentQuestionIndex + 1, true)
        }

        binding.previousQuestion.setOnClickListener {
            navigateToQuestion(currentQuestionIndex - 1, false)
        }

        binding.optionsRecyclerView1.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Check if the user is scrolling horizontally
                if (Math.abs(dx) > Math.abs(dy)) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    if (firstVisibleItemPosition != currentQuestionIndex) {
                        previousQuestionIndex = currentQuestionIndex
                        currentQuestionIndex = firstVisibleItemPosition

                        // Calculate and set the progress based on the current question index
                        progressBar.progress =
                            currentQuestionIndex * (maxProgress / (anxietyText.size - 1))
                        // Update the step indicators (ImageViews) as active or inactive
                        updateStepIndicators()
                    }
                }
            }
        })
    }

    private fun updateStepIndicators() {
        // Update the previous step indicator to inactive
        /*if (previousQuestionIndex >= 0 && previousQuestionIndex < stepIndicators.size) {
            stepIndicators[previousQuestionIndex].setImageResource(R.drawable.ic_inactivetickmark)
        }*/
        // Update the current step indicator to active
        if (currentQuestionIndex >= 0 && currentQuestionIndex < stepIndicators.size) {
            stepIndicators[currentQuestionIndex].setImageResource(R.drawable.ic_activetickmark)
        }
        if(currentQuestionIndex == 0)
        {
            binding.previousQuestion.visibility = View.GONE
        }
        else
        {
            binding.previousQuestion.visibility = View.VISIBLE
        }
        if(currentQuestionIndex == stepIndicators.size-1)
        {
            binding.nextQuestion.visibility = View.GONE
        }
        else
        {
            binding.nextQuestion.visibility = View.VISIBLE
        }
    }

    private fun navigateToQuestion(index: Int, isNext: Boolean) {
        if (index in 0 until anxietyText.size) {
            if (isNext) {
                previousQuestionIndex = currentQuestionIndex
            } else {
                // Update the current step indicator to inactive when going to the previous question
                if (currentQuestionIndex >= 0 && currentQuestionIndex < stepIndicators.size) {
                    stepIndicators[currentQuestionIndex].setImageResource(R.drawable.ic_inactivetickmark)
                }
            }
            currentQuestionIndex = index
            binding.optionsRecyclerView1.smoothScrollToPosition(currentQuestionIndex)

            // Calculate and set the progress based on the current question index
            progressBar.progress = currentQuestionIndex * (maxProgress / (anxietyText.size - 1))

            // Update the step indicators (ImageViews) as active or inactive
            updateStepIndicators()
        }
    }

}

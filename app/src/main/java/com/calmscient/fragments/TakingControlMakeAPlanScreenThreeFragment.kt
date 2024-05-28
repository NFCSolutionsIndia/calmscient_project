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

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import com.calmscient.R
import com.calmscient.activities.LearnMoreWebviewActivity
import com.calmscient.databinding.FragmentTakingControlMakeAPlanScreenThreeBinding


class TakingControlMakeAPlanScreenThreeFragment : Fragment() {

    private lateinit var binding : FragmentTakingControlMakeAPlanScreenThreeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this){
            loadFragment(TakingControlMakeAPlanScreenTwoFragment())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding = FragmentTakingControlMakeAPlanScreenThreeBinding.inflate(inflater,container,false)

        binding.notifyComplete.visibility = View.GONE
        binding.notifyCompleteTwo.visibility = View.GONE

        // Add click listener to the TextView
      /*  binding.textNotify.setOnClickListener {

            binding.loaderOne.visibility = View.VISIBLE

            binding.notifyComplete.visibility = View.GONE

            // Use Handler to delay execution and make complete image visible after 1 second
            Handler().postDelayed({
                // Hide loader
                binding.loaderOne.visibility = View.GONE


                binding.notifyComplete.visibility = View.VISIBLE
            }, 1000)
        }

        binding.textNotifyTwo.setOnClickListener {

            binding.loaderTwo.visibility = View.VISIBLE

            binding.notifyCompleteTwo.visibility = View.GONE

            // Use Handler to delay execution and make complete image visible after 1 second
            Handler().postDelayed({
                // Hide loader
                binding.loaderTwo.visibility = View.GONE


                binding.notifyCompleteTwo.visibility = View.VISIBLE
            }, 1000)
        }*/
        binding.textNotify.setOnClickListener {
            showLoaderAndHideCompleteImage(
                binding.loaderOne,
                binding.notifyComplete,
                binding.textNotify
            )
            // Perform other actions if needed
        }

        binding.textNotifyTwo.setOnClickListener {
            showLoaderAndHideCompleteImage(
                binding.loaderTwo,
                binding.notifyCompleteTwo,
                binding.textNotifyTwo
            )
            // Perform other actions if needed
        }

        binding.link.setOnClickListener{
            val intent = Intent(activity, LearnMoreWebviewActivity::class.java)
            intent.putExtra("988_url", "https://alcoholtreatment.niaaa.nih.gov/")
            startActivity(intent)
        }

        binding.thirdScreenBackButton.setOnClickListener{
            loadFragment(TakingControlMakeAPlanScreenTwoFragment())
        }
        binding.backIcon.setOnClickListener{
            loadFragment(TakingControlMakeAPlanScreenTwoFragment())
        }

        binding.backIcon.setOnClickListener{
            loadFragment(TakingControlMakeAPlanScreenTwoFragment())
        }
        binding.thirdScreenBackButton.setOnClickListener{
            loadFragment(TakingControlMakeAPlanScreenTwoFragment())
        }



        return binding.root
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }


    private fun showLoaderAndHideCompleteImage(loader: View, completeImage: View, notifyText: View) {
        // Show loader
        loader.visibility = View.VISIBLE

        // Hide complete image
        completeImage.visibility = View.GONE

        Handler().postDelayed({

            loader.visibility = View.GONE

            completeImage.visibility = View.VISIBLE
        }, 1000)
    }

}
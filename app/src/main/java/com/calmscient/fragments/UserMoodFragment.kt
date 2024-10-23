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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.calmscient.R
import com.calmscient.databinding.FragmentUserMoodBinding
import com.calmscient.utils.getColorCompat
import java.util.Calendar
import java.util.Date

class UserMoodFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentUserMoodBinding
    private var isImage1Visible = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserMoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        greeting()

        // Set click listeners for morning images
        binding.imgMBad.setOnClickListener(this)
        binding.imgMBetter.setOnClickListener(this)
        binding.imgMGood.setOnClickListener(this)
        binding.imgMFair.setOnClickListener(this)
        binding.imgMExcellent.setOnClickListener(this)

        // Set click listeners for morning sleep images
        binding.sleepLess.setOnClickListener(this)
        binding.sleep4.setOnClickListener(this)
        binding.sleep5.setOnClickListener(this)
        binding.sleep6.setOnClickListener(this)
        binding.sleep7.setOnClickListener(this)
        binding.sleep8.setOnClickListener(this)
        binding.sleep9.setOnClickListener(this)
        binding.sleep10.setOnClickListener(this)
        binding.sleepMore.setOnClickListener(this)

        // Set click listeners for afternoon images
        binding.imgEveBad.setOnClickListener(this)
        binding.imgEveBetter.setOnClickListener(this)
        binding.imgEveGood.setOnClickListener(this)
        binding.imgEveFair.setOnClickListener(this)
        binding.imgEveExcellent.setOnClickListener(this)

        // Set click listeners for evening images
        binding.imgNigBad.setOnClickListener(this)
        binding.imgNigBetter.setOnClickListener(this)
        binding.imgNigFair.setOnClickListener(this)
        binding.imgNigGood.setOnClickListener(this)
        binding.imgNigExcellent.setOnClickListener(this)
        binding.imgFamily.setOnClickListener(this)
        binding.imgFriends.setOnClickListener(this)
        binding.imgWorkmates.setOnClickListener(this)
        binding.imgOthers.setOnClickListener(this)
        binding.imgAlone.setOnClickListener(this)

        // Set click listeners for buttons
        binding.btnSave.setOnClickListener(this)
        binding.btnSkip.setOnClickListener(this)
    }

    private fun greeting() {
        val date = Date()
        val cal = Calendar.getInstance()
        cal.time = date
        val hour: Int = cal.get(Calendar.HOUR_OF_DAY)
        var greeting: String? = null
        when (hour) {
            in 6..11 -> greeting = getString(R.string.good_morning)
            in 12..17 -> greeting = getString(R.string.good_afternoon)
            in 18..23 -> greeting = getString(R.string.good_evening)
        }
        binding.idWishes.text = greeting
        if (greeting == getString(R.string.good_morning)) {
            binding.cardMorniMood.visibility = View.VISIBLE
            binding.mornHoursSleepCard.visibility = View.VISIBLE
            binding.idMornMeds.visibility = View.VISIBLE
            binding.idSwitch.labelOn = getString(R.string.yes)
            binding.idSwitch.labelOff = getString(R.string.no)
            binding.idSwitch.colorOn = requireActivity().getColorCompat(R.color.example_7_button)
            //binding.idSwitch.colorOff =requireActivity().getColorCompat(R.color.hospital_name)

        } else if (greeting == getString(R.string.good_afternoon)) {
            binding.cardAfternoon.visibility = View.VISIBLE
        } else if (greeting == getString(R.string.good_evening)) {
            binding.cardEveDay.visibility = View.VISIBLE
            binding.spendTimeCard.visibility = View.VISIBLE
            binding.idMornMeds.visibility = View.VISIBLE
            binding.cardDailyJournel.visibility = View.VISIBLE
            binding.idSwitch.labelOn = getString(R.string.yes)
            binding.idSwitch.labelOff = getString(R.string.no)
            binding.idSwitch.colorOn = requireActivity().getColorCompat(R.color.example_7_button)
            //binding.idSwitch.colorOff =requireActivity().getColorCompat(R.color.hospital_name)
        }
    }

    override fun onClick(v: View?) {
        binding.imgMBad.setImageResource(R.drawable.icon_bad)
        binding.imgMBetter.setImageResource(R.drawable.icon_better)
        binding.imgMFair.setImageResource(R.drawable.icon_fair)
        binding.imgMGood.setImageResource(R.drawable.icon_good)
        binding.imgMExcellent.setImageResource(R.drawable.icon_excellent)

        when (v?.id) {
            // Handle morning images click events
            R.id.img_mBad -> {
                // Handle image click
                binding.imgMBad.elevation = 20.0F
                binding.imgMBetter.setElevation(0.0F)
                binding.imgMFair.setElevation(0.0F)
                binding.imgMGood.setElevation(0.0F)
                binding.imgMExcellent.setElevation(0.0F)
                binding.tvBad.setTextColor(ContextCompat.getColor(requireActivity(),R.color.bad_selected))
                binding.tvBetter.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvFair.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvGood.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvXcellent.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
            }
            // Handle other morning images click events similarly

            R.id.img_mBetter -> {
                //binding.imgMBetter.setImageResource(R.drawable.icon_better)
                binding.imgMBetter.setElevation(120.0F)
                binding.imgMBad.setElevation(0.0F)
                binding.imgMFair.setElevation(0.0F)
                binding.imgMGood.setElevation(0.0F)
                binding.imgMExcellent.setElevation(0.0F)
                binding.tvBetter.setTextColor(ContextCompat.getColor(requireActivity(),R.color.better_selected))
                binding.tvBad.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvFair.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvGood.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvXcellent.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
            }

            R.id.img_mFair -> {
                //binding.imgMFair.setImageResource(R.drawable.icon_excellent)
                binding.imgMFair.setElevation(20.0F)
                binding.imgMBetter.setElevation(0.0F)
                binding.imgMBad.setElevation(0.0F)
                binding.imgMGood.setElevation(0.0F)
                binding.imgMExcellent.setElevation(0.0F)
                binding.tvFair.setTextColor(ContextCompat.getColor(requireActivity(),R.color.fair_selected))
                binding.tvBad.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvBetter.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvGood.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvXcellent.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
            }

            R.id.img_mGood -> {
                //binding.imgMGood.setImageResource(R.drawable.icon_excellent)
                binding.imgMGood.setElevation(20.0F)
                binding.imgMBetter.setElevation(0.0F)
                binding.imgMBad.setElevation(0.0F)
                binding.imgMFair.setElevation(0.0F)
                binding.imgMExcellent.setElevation(0.0F)
                binding.tvGood.setTextColor(ContextCompat.getColor(requireActivity(),R.color.good_selected))
                binding.tvBad.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvBetter.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvFair.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvXcellent.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
            }

            R.id.img_mExcellent -> {
                //binding.imgMExcellent.setImageResource(R.drawable.icon_excellent)
                binding.imgMExcellent.setElevation(20.0F)
                binding.imgMGood.setElevation(0.0F)
                binding.imgMBetter.setElevation(0.0F)
                binding.imgMBad.setElevation(0.0F)
                binding.imgMFair.setElevation(0.0F)
                binding.tvXcellent.setTextColor(ContextCompat.getColor(requireActivity(),R.color.excellent_selected))
                binding.tvBad.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvBetter.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvFair.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvGood.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
            }

            //afternoon click
            R.id.img_eve_bad -> {
                //binding.imgMBad.setImageResource(R.drawable.icon_excellent)
                //binding.imgMBad.setImageResource(R.drawable.icon_excellent)
                //binding.imgMBad.setBackgroundResource(R.drawable.drawable_circular_border)
                binding.imgEveBad.setElevation(20.0F)
                binding.imgEveBetter.setElevation(0.0F)
                binding.imgEveFair.setElevation(0.0F)
                binding.imgEveExcellent.setElevation(0.0F)
                binding.imgEveGood.setElevation(0.0F)
                binding.tvAfterBad.setTextColor(ContextCompat.getColor(requireActivity(),R.color.bad_selected))
                binding.tvAfterBetter.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvAfterFair.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvAfterGood.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvAfterXcellent.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
            }

            R.id.img_eve_better -> {
                //binding.imgMBetter.setImageResource(R.drawable.icon_better)
                binding.imgEveBetter.setElevation(20.0F)
                binding.imgEveFair.setElevation(0.0F)
                binding.imgEveExcellent.setElevation(0.0F)
                binding.imgEveGood.setElevation(0.0F)
                binding.imgEveBad.setElevation(0.0F)
                binding.tvAfterBetter.setTextColor(ContextCompat.getColor(requireActivity(),R.color.better_selected))
                binding.tvAfterBad.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvAfterFair.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvAfterGood.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvAfterXcellent.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
            }

            R.id.img_eve_fair -> {
                //binding.imgMFair.setImageResource(R.drawable.icon_excellent)
                binding.imgEveFair.setElevation(20.0F)
                binding.imgEveExcellent.setElevation(0.0F)
                binding.imgEveGood.setElevation(0.0F)
                binding.imgEveBetter.setElevation(0.0F)
                binding.imgEveBad.setElevation(0.0F)
                binding.tvAfterFair.setTextColor(ContextCompat.getColor(requireActivity(),R.color.fair_selected))
                binding.tvAfterBetter.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvAfterBad.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvAfterGood.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvAfterXcellent.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
            }

            R.id.img_eve_good -> {
                //binding.imgMGood.setImageResource(R.drawable.icon_excellent)
                binding.imgEveGood.setElevation(20.0F)
                binding.imgEveExcellent.setElevation(0.0F)
                binding.imgEveBetter.setElevation(0.0F)
                binding.imgEveBad.setElevation(0.0F)
                binding.imgEveFair.setElevation(0.0F)
                binding.tvAfterGood.setTextColor(ContextCompat.getColor(requireActivity(),R.color.good_selected))
                binding.tvAfterBetter.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvAfterBad.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvAfterFair.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvAfterXcellent.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
            }

            R.id.img_eve_excellent -> {
                //binding.imgMExcellent.setImageResource(R.drawable.icon_excellent)
                binding.imgEveExcellent.setElevation(20.0F)
                binding.imgEveGood.setElevation(0.0F)
                binding.imgEveBetter.setElevation(0.0F)
                binding.imgEveBad.setElevation(0.0F)
                binding.imgEveFair.setElevation(0.0F)
                binding.tvAfterXcellent.setTextColor(ContextCompat.getColor(requireActivity(),R.color.excellent_selected))
                binding.tvAfterGood.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvAfterBetter.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvAfterBad.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvAfterFair.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
            }
            //morning sleep
            R.id.sleep_less -> {
                binding.sleepLess.setImageResource(R.drawable.less_icon)
                binding.sleep4.setImageResource(R.drawable.sleep_4)
                binding.sleep5.setImageResource(R.drawable.sleep_5)
                binding.sleep6.setImageResource(R.drawable.sleep_6)
                binding.sleep7.setImageResource(R.drawable.sleep_7)
                binding.sleep8.setImageResource(R.drawable.sleep_8)
                binding.sleep9.setImageResource(R.drawable.sleep_9)
                binding.sleep10.setImageResource(R.drawable.sleep_10)
                binding.sleepMore.setImageResource(R.drawable.sleep_more)
                binding.tvHrsSlept.text = "Less than 4 Hours"

            }

            R.id.sleep_4 -> {
                binding.sleep4.setImageResource(R.drawable.selected_4)
                binding.sleepLess.setImageResource(R.drawable.less)
                binding.sleep5.setImageResource(R.drawable.sleep_5)
                binding.sleep6.setImageResource(R.drawable.sleep_6)
                binding.sleep7.setImageResource(R.drawable.sleep_7)
                binding.sleep8.setImageResource(R.drawable.sleep_8)
                binding.sleep9.setImageResource(R.drawable.sleep_9)
                binding.sleep10.setImageResource(R.drawable.sleep_10)
                binding.sleepMore.setImageResource(R.drawable.sleep_more)
                binding.tvHrsSlept.text = "4 Hours"

            }

            R.id.sleep_5 -> {
                binding.sleep5.setImageResource(R.drawable.selected_5)
                binding.sleepLess.setImageResource(R.drawable.less)
                binding.sleep4.setImageResource(R.drawable.sleep_4)
                binding.sleep6.setImageResource(R.drawable.sleep_6)
                binding.sleep7.setImageResource(R.drawable.sleep_7)
                binding.sleep8.setImageResource(R.drawable.sleep_8)
                binding.sleep9.setImageResource(R.drawable.sleep_9)
                binding.sleep10.setImageResource(R.drawable.sleep_10)
                binding.sleepMore.setImageResource(R.drawable.sleep_more)
                binding.tvHrsSlept.text = "5 Hours"
            }

            R.id.sleep_6 -> {
                binding.sleep6.setImageResource(R.drawable.selected_6)
                binding.sleepLess.setImageResource(R.drawable.less)
                binding.sleep4.setImageResource(R.drawable.sleep_4)
                binding.sleep5.setImageResource(R.drawable.sleep_5)
                binding.sleep7.setImageResource(R.drawable.sleep_7)
                binding.sleep8.setImageResource(R.drawable.sleep_8)
                binding.sleep9.setImageResource(R.drawable.sleep_9)
                binding.sleep10.setImageResource(R.drawable.sleep_10)
                binding.sleepMore.setImageResource(R.drawable.sleep_more)
                binding.tvHrsSlept.text = "6 Hours"
            }

            R.id.sleep_7 -> {
                binding.sleep7.setImageResource(R.drawable.selected_7)
                binding.sleepLess.setImageResource(R.drawable.less)
                binding.sleep4.setImageResource(R.drawable.sleep_4)
                binding.sleep5.setImageResource(R.drawable.sleep_5)
                binding.sleep6.setImageResource(R.drawable.sleep_6)
                binding.sleep8.setImageResource(R.drawable.sleep_8)
                binding.sleep9.setImageResource(R.drawable.sleep_9)
                binding.sleep10.setImageResource(R.drawable.sleep_10)
                binding.sleepMore.setImageResource(R.drawable.sleep_more)
                binding.tvHrsSlept.text = "7 Hours"
            }

            R.id.sleep_8 -> {
                binding.sleep8.setImageResource(R.drawable.selected_8)
                binding.sleepLess.setImageResource(R.drawable.less)
                binding.sleep4.setImageResource(R.drawable.sleep_4)
                binding.sleep5.setImageResource(R.drawable.sleep_5)
                binding.sleep6.setImageResource(R.drawable.sleep_6)
                binding.sleep7.setImageResource(R.drawable.sleep_7)
                binding.sleep9.setImageResource(R.drawable.sleep_9)
                binding.sleep10.setImageResource(R.drawable.sleep_10)
                binding.sleepMore.setImageResource(R.drawable.sleep_more)
                binding.tvHrsSlept.text = "8 Hours"
            }

            R.id.sleep_9 -> {
                binding.sleep9.setImageResource(R.drawable.selected_9)
                binding.sleepLess.setImageResource(R.drawable.less)
                binding.sleep4.setImageResource(R.drawable.sleep_4)
                binding.sleep5.setImageResource(R.drawable.sleep_5)
                binding.sleep6.setImageResource(R.drawable.sleep_6)
                binding.sleep7.setImageResource(R.drawable.sleep_7)
                binding.sleep8.setImageResource(R.drawable.sleep_8)
                binding.sleep10.setImageResource(R.drawable.sleep_10)
                binding.sleepMore.setImageResource(R.drawable.sleep_more)
                binding.tvHrsSlept.text = "9 Hours"
            }

            R.id.sleep_10 -> {
                binding.sleep10.setImageResource(R.drawable.selected_10)
                binding.sleepLess.setImageResource(R.drawable.less)
                binding.sleep4.setImageResource(R.drawable.sleep_4)
                binding.sleep5.setImageResource(R.drawable.sleep_5)
                binding.sleep6.setImageResource(R.drawable.sleep_6)
                binding.sleep7.setImageResource(R.drawable.sleep_7)
                binding.sleep8.setImageResource(R.drawable.sleep_8)
                binding.sleep9.setImageResource(R.drawable.sleep_9)
                binding.sleepMore.setImageResource(R.drawable.sleep_more)
                binding.tvHrsSlept.text = "10 Hours"
            }

            R.id.sleep_more -> {
                binding.sleepMore.setImageResource(R.drawable.more_selected)
                binding.sleepLess.setImageResource(R.drawable.less)
                binding.sleep4.setImageResource(R.drawable.sleep_4)
                binding.sleep5.setImageResource(R.drawable.sleep_5)
                binding.sleep6.setImageResource(R.drawable.sleep_6)
                binding.sleep7.setImageResource(R.drawable.sleep_7)
                binding.sleep8.setImageResource(R.drawable.sleep_8)
                binding.sleep9.setImageResource(R.drawable.sleep_9)
                binding.sleep10.setImageResource(R.drawable.sleep_10)
                binding.tvHrsSlept.text = "More than 10 Hours"
            }
            //evening images
            R.id.img_nig_bad -> {
                //binding.imgMBad.setImageResource(R.drawable.icon_excellent)
                //binding.imgMBad.setImageResource(R.drawable.icon_excellent)
                //binding.imgMBad.setBackgroundResource(R.drawable.drawable_circular_border)
                binding.imgNigBad.setElevation(20.0F)
                binding.imgNigBetter.setElevation(0.0F)
                binding.imgNigFair.setElevation(0.0F)
                binding.imgNigGood.setElevation(0.0F)
                binding.imgNigExcellent.setElevation(0.0F)
                binding.tvEveBad.setTextColor(ContextCompat.getColor(requireActivity(),R.color.bad_selected))
                binding.tvEveBetter.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvEveFair.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvEveGood.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvEveXcellent.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
            }

            R.id.img_nig_better -> {
                //binding.imgMBetter.setImageResource(R.drawable.icon_better)
                binding.imgNigBetter.setElevation(20.0F)
                binding.imgNigFair.setElevation(0.0F)
                binding.imgNigGood.setElevation(0.0F)
                binding.imgNigExcellent.setElevation(0.0F)
                binding.imgNigBad.setElevation(0.0F)
                binding.tvEveBetter.setTextColor(ContextCompat.getColor(requireActivity(),R.color.better_selected))
                binding.tvEveBad.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvEveFair.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvEveGood.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvEveXcellent.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
            }

            R.id.img_nig_fair -> {
                //binding.imgMFair.setImageResource(R.drawable.icon_excellent)
                binding.imgNigFair.setElevation(20.0F)
                binding.imgNigGood.setElevation(0.0F)
                binding.imgNigExcellent.setElevation(0.0F)
                binding.imgNigBad.setElevation(0.0F)
                binding.imgNigBetter.setElevation(0.0F)
                binding.tvEveFair.setTextColor(ContextCompat.getColor(requireActivity(),R.color.fair_selected))
                binding.tvEveBad.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvEveBetter.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvEveGood.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvEveXcellent.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
            }

            R.id.img_nig_good -> {
                //binding.imgMGood.setImageResource(R.drawable.icon_excellent)
                binding.imgNigGood.setElevation(20.0F)
                binding.imgNigExcellent.setElevation(0.0F)
                binding.imgNigBad.setElevation(0.0F)
                binding.imgNigBetter.setElevation(0.0F)
                binding.imgNigFair.setElevation(0.0F)
                binding.tvEveGood.setTextColor(ContextCompat.getColor(requireActivity(),R.color.good_selected))
                binding.tvEveBad.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvEveBetter.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvEveFair.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvEveXcellent.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
            }

            R.id.img_nig_excellent -> {
                //binding.imgMExcellent.setImageResource(R.drawable.icon_excellent)
                binding.imgNigExcellent.setElevation(20.0F)
                binding.imgNigBad.setElevation(0.0F)
                binding.imgNigBetter.setElevation(0.0F)
                binding.imgNigFair.setElevation(0.0F)
                binding.imgNigGood.setElevation(0.0F)
                binding.tvEveXcellent.setTextColor(ContextCompat.getColor(requireActivity(),R.color.excellent_selected))
                binding.tvEveBad.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvEveBetter.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvEveFair.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
                binding.tvEveGood.setTextColor(ContextCompat.getColor(requireActivity(),R.color.grey_light))
            }

            R.id.img_family -> {
                binding.imgFamily.setImageResource(R.drawable.family_selected)
                binding.imgFriends.setImageResource(R.drawable.friends)
                binding.imgWorkmates.setImageResource(R.drawable.workmates)
                binding.imgOthers.setImageResource(R.drawable.others)
                binding.imgAlone.setImageResource(R.drawable.alone)
            }

            R.id.img_friends -> {
                binding.imgFriends.setImageResource(R.drawable.friends_selected)
                binding.imgFamily.setImageResource(R.drawable.family)
                binding.imgWorkmates.setImageResource(R.drawable.workmates)
                binding.imgOthers.setImageResource(R.drawable.others)
                binding.imgAlone.setImageResource(R.drawable.alone)
            }

            R.id.img_workmates -> {
                binding.imgWorkmates.setImageResource(R.drawable.workmates_selected)
                binding.imgFamily.setImageResource(R.drawable.family)
                binding.imgFriends.setImageResource(R.drawable.friends)
                binding.imgOthers.setImageResource(R.drawable.others)
                binding.imgAlone.setImageResource(R.drawable.alone)
            }

            R.id.img_others -> {
                binding.imgOthers.setImageResource(R.drawable.others_selected)
                binding.imgWorkmates.setImageResource(R.drawable.workmates)
                binding.imgFamily.setImageResource(R.drawable.family)
                binding.imgFriends.setImageResource(R.drawable.friends)
                binding.imgAlone.setImageResource(R.drawable.alone)
            }

            R.id.img_alone -> {
                binding.imgAlone.setImageResource(R.drawable.alone_selected)
                binding.imgWorkmates.setImageResource(R.drawable.workmates)
                binding.imgFamily.setImageResource(R.drawable.family)
                binding.imgFriends.setImageResource(R.drawable.friends)
                binding.imgOthers.setImageResource(R.drawable.others)
            }


            R.id.btn_save -> {
                loadFragment(HomeFragment())
            }

            R.id.btn_skip -> {
                loadFragment(HomeFragment())
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val homeFragment = HomeFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.flFragment, homeFragment)
            .commit()
    }
}

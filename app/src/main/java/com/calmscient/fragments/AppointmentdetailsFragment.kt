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
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import com.calmscient.R
import com.calmscient.databinding.FragmentAppointmentdetailsBinding
import com.calmscient.di.remote.response.AppointmentDetails
import com.calmscient.di.remote.response.MedicalDetails
import com.google.gson.Gson


class AppointmentdetailsFragment:Fragment() {
    private lateinit var binding:FragmentAppointmentdetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this){
            loadFragment(NextAppointmentsFragment())
        }
    }

    companion object {
        fun newInstance(appointmentDetails: AppointmentDetails): AppointmentdetailsFragment {
            val fragment = AppointmentdetailsFragment()
            val args = Bundle()
            val gson = Gson()
            val appointmentDetailsJson = gson.toJson(appointmentDetails)
            args.putString("appointmentDetailsJson", appointmentDetailsJson)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAppointmentdetailsBinding.inflate(inflater, container, false)
        binding.backIcon.setOnClickListener {
            loadFragment(NextAppointmentsFragment())
        }
        val appointmentDetailsJson = arguments?.getString("appointmentDetailsJson")

        // Parse the JSON string back to MedicalDetails object
        val gson = Gson()
        val appointmentDetails = gson.fromJson(appointmentDetailsJson, AppointmentDetails::class.java)

        Log.d("Appointment Details","$appointmentDetails")

        binding.doctorName.text = appointmentDetails.providerName
        binding.phoneNumber.text = appointmentDetails.contact
        binding.tvAddress.text = appointmentDetails.address
        binding.tvDate.text = appointmentDetails.dateAndTime
        binding.tvAppointmentDetails.text = appointmentDetails.appointmentDetails

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the TextViews
        val hospitalNumber = view.findViewById<TextView>(R.id.phoneNumber)


        // Apply the click behavior for each TextView
        makePhoneNumberClickable(hospitalNumber)

    }
    private fun makePhoneNumberClickable(textView: TextView) {
        // Get the SpannableString from the TextView's text
        val originalText = SpannableString.valueOf(textView.text)

        // Find all phone number links in the text
        val phonePattern = "(?:\\(\\d{3}\\)\\s?|\\d{1,}-)?\\d{3}\\s?\\d{3}\\s?\\d{4}".toRegex()
        val phoneMatcher = phonePattern.findAll(originalText)
        val spannableText = SpannableString(originalText)
        for (match in phoneMatcher) {
            val start = match.range.first
            val end = match.range.last +1

            // Apply the clickable span to the phone number link
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val phoneNumber = match.value
                    val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                    startActivity(callIntent)
                }

            }

            val clickableTextColor = ContextCompat.getColor(requireContext(), R.color.purple_100)
            // Apply the clickable span with color to the clickable portion of the string
            originalText.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            originalText.setSpan(ForegroundColorSpan(clickableTextColor), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        // Set the ClickableSpan to the TextView
        textView.text = originalText
        textView.movementMethod = LinkMovementMethod.getInstance()
    }
    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
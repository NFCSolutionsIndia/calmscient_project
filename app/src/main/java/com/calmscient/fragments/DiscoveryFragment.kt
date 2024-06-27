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

import android.content.DialogInterface
import android.content.Intent
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.calmscient.R
import com.calmscient.activities.BeginManageAnxietyActivity
import com.calmscient.activities.GlossaryActivity
import com.calmscient.activities.SettingsActivity
import com.calmscient.databinding.FragmentDiscoveryBinding

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class DiscoveryFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentDiscoveryBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this){
            requireActivity().supportFragmentManager.popBackStack()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDiscoveryBinding.inflate(inflater, container, false)
        binding.icProfile.setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }
        binding.manageAnxietyCard.setOnClickListener {
            //Toast.makeText(requireActivity(), "Coming Soon", Toast.LENGTH_SHORT).show()
            loadFragment(BeginManageAnxietyFragment())
        }

        binding.changeYourResponseToStressCard.setOnClickListener {
                /*val bottomSheetFragment = BottomSheetFragment()
                bottomSheetFragment.show(requireActivity().supportFragmentManager, bottomSheetFragment.tag)*/
                //Toast.makeText(requireActivity(), "Coming Soon", Toast.LENGTH_SHORT).show()
            loadFragment(ChangingYourResponseFragment())
        }


        binding.cardTakingControl.setOnClickListener {
            //Toast.makeText(requireActivity(), "Coming Soon", Toast.LENGTH_SHORT).show()
            loadFragment(TakingControlFragment())
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    fun onBackPressed() {
        showExitConfirmationDialog()
    }

    private fun showExitConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.plz_confirm))
        builder.setMessage(getString(R.string.exit_app))
        builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
            // User clicked "Yes," so exit the app
            requireActivity().finishAffinity() // This closes the entire app
        }
        builder.setNegativeButton(getString(R.string.no)) { _, _ ->
            // User clicked "No," so dismiss the dialog and stay on the current page
        }
        builder.setOnCancelListener(DialogInterface.OnCancelListener {
            // User canceled the dialog, do nothing
        })
        builder.show()
    }

    private fun loadFragment(fragment: Fragment) {
        // Toast.makeText(requireContext(), "Back Button is calling", Toast.LENGTH_SHORT).show()
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DiscoveryFragment.
         */
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DiscoveryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
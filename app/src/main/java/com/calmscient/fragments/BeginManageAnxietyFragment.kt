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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.calmscient.activities.ManageAnxietyActivity
import com.calmscient.databinding.LayoutBeginanxietyBinding
class BeginManageAnxietyFragment : Fragment() {
    private lateinit var binding: LayoutBeginanxietyBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LayoutBeginanxietyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.beginButton.setOnClickListener {
            startActivity(Intent(requireContext(), ManageAnxietyActivity::class.java))
        }

        binding.backIcon.setOnClickListener {
            requireActivity().finish()
        }
    }
}
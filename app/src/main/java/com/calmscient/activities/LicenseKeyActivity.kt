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

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat
import com.calmscient.R
import com.calmscient.databinding.ActivityLicenseKeyBinding

class LicenseKeyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLicenseKeyBinding
    private lateinit var dialog: Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLicenseKeyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        binding.btnSubmit.setOnClickListener {
            showLicenceKeyDialog()
        }

        binding.backIcon.setOnClickListener{
            finish()
        }
    }

    private fun showLicenceKeyDialog() {
        // Inflate the dialog layout
        val dialogView = LayoutInflater.from(this).inflate(R.layout.license_key_dialog, null)
        val btnContinue = dialogView.findViewById<AppCompatButton>(R.id.btn_continue)

        // Build the dialog
        val dialogBuilder = AlertDialog.Builder(this, R.style.CustomDialog)
            .setView(dialogView)
            .setCancelable(false)

        // Create and show the dialog
        dialog = dialogBuilder.create()
        dialog.show()

        btnContinue.setOnClickListener{
            dialog.dismiss()
            startActivity(Intent(this, UserMoodActivity::class.java))
        }
    }


}
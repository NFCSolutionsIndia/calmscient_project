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

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.calmscient.R

class CommonDialog(private val context: Context) {

    @SuppressLint("MissingInflatedId")
    fun showDialog(message: String?) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.common_dialog, null)
        val infoTextView = dialogView.findViewById<TextView>(R.id.dialogInfoTextView)
        val closeButton = dialogView.findViewById<ImageView>(R.id.closeDialogButton)
        val titleTextView = dialogView.findViewById<TextView>(R.id.titleVideoDialog)
        val okButton = dialogView.findViewById<AppCompatButton>(R.id.okButton)
        infoTextView.text = message
        titleTextView.text = context.getString(R.string.information)
        val dialogBuilder =
            androidx.appcompat.app.AlertDialog.Builder(context, R.style.CustomDialog)
                .setView(dialogView)

        val dialog = dialogBuilder.create()
        dialog.show()

        // Handle the close button click
        okButton.setOnClickListener {
            dialog.dismiss()
        }
    }
}


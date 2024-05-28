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

package com.calmscient.utils

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.calmscient.R

class CommonAPICallDialog(private val context: Context) {
    lateinit var dialog: Dialog
    @SuppressLint("MissingInflatedId")
    fun showDialog(message: String) {
        dialog = Dialog(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.layout_common_dialog, null)
        val infoTextView = dialogView.findViewById<TextView>(R.id.dialogInfoTextView)
        val okButton = dialogView.findViewById<AppCompatButton>(R.id.okButton)
        infoTextView.text = message
        val dialogBuilder =
            androidx.appcompat.app.AlertDialog.Builder(context, R.style.CustomDialog)
                .setView(dialogView)

        dialog = dialogBuilder.create()
        dialog.show()

        // Handle the close button click

        okButton.setOnClickListener{
           dialog.dismiss()
        }
    }

    fun dismiss() {
        if (::dialog.isInitialized && dialog.isShowing) {
            dialog.dismiss()
        }
    }

    fun setOnDismissListener(listener: () -> Unit) {
        dialog.setOnDismissListener {
            listener.invoke()
        }
    }

    fun isShowing(): Boolean {
        return ::dialog.isInitialized && dialog.isShowing
    }

}


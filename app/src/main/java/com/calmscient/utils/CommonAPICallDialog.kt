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
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.calmscient.R

class CommonAPICallDialog(private val context: Context) {
    private var dialog: Dialog? = null
    private var onDismissListener: (() -> Unit)? = null

    @SuppressLint("MissingInflatedId")
    fun showDialog(message: String, imageResId: Int? = null) {
        dismiss()

        val dialogView = LayoutInflater.from(context).inflate(R.layout.layout_common_dialog, null)
        val infoTextView = dialogView.findViewById<TextView>(R.id.dialogInfoTextView)
        val dialogImageView = dialogView.findViewById<ImageView>(R.id.dialogImageView)
        val okButton = dialogView.findViewById<AppCompatButton>(R.id.okButton)
        infoTextView.text = message

        if (imageResId != null) {
            dialogImageView.setImageResource(imageResId)
            dialogImageView.visibility = View.VISIBLE
        } else {
            dialogImageView.visibility = View.GONE
        }

        val dialogBuilder = AlertDialog.Builder(context, R.style.CustomDialog).setView(dialogView)
        dialogBuilder.setCancelable(false)
        dialog = dialogBuilder.create()
        dialog?.show()

        okButton.setOnClickListener {
            dialog?.dismiss()
            onDismissListener?.invoke()
        }
    }

    fun dismiss() {
        dialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
        dialog = null
    }

    fun setOnDismissListener(listener: () -> Unit) {
        onDismissListener = listener
    }

    fun isShowing(): Boolean {
        return dialog?.isShowing == true
    }
}

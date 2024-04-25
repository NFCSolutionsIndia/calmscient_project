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
package com.calmscient.utils.network

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import com.calmscient.R

object ServerTimeoutHandler {

    private var retryListener: (() -> Unit)? = null
    private var dialog: AlertDialog? = null

    fun handleTimeoutException(context: Context, retryAction: () -> Unit) {
        retryListener = retryAction
        val builder = AlertDialog.Builder(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.timeout_dialog, null)
        builder.setView(dialogView)

        val titleTextView = dialogView.findViewById<TextView>(R.id.dialog_title)
        val messageTextView = dialogView.findViewById<TextView>(R.id.dialog_message)
        val retryButton = dialogView.findViewById<Button>(R.id.retryBtn)

        titleTextView.text = "Server Timeout"
        messageTextView.text = "The server took too long to respond. Please try again."

        retryButton.setOnClickListener {
            retryListener?.invoke()
        }

        builder.setCancelable(false) // Prevent dismissing dialog by tapping outside
        dialog = builder.create()
        dialog?.show()
    }

    fun clearRetryListener() {
        retryListener = null
    }

    fun dismissDialog() {
        dialog?.dismiss()
    }
}



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

import android.content.Context
import android.widget.Toast

object ToastUtil {
    private var currentToast: Toast? = null

    fun showToast(context: Context, message: String) {
        // Cancel the previous toast if it's still visible
        currentToast?.cancel()

        // Create and show a new toast
        currentToast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        currentToast?.show()
    }
}

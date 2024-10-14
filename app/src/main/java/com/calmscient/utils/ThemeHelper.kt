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
import androidx.appcompat.app.AppCompatDelegate
import com.calmscient.utils.common.SavePreferences

class ThemeHelper(private val context: Context) {

    private val savePref: SavePreferences = SavePreferences(context)

    // Function to set Dark Mode without recreating the activity
    fun setDarkMode(isDarkMode: Boolean) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        savePref.setDarkModeState(isDarkMode) // Save dark mode state
    }

    // Function to apply the saved dark mode when the app is launched
    fun applySavedDarkMode() {
        val isDarkMode = savePref.getDarkModeState()
        setDarkMode(isDarkMode)
    }

    // Return the current dark mode state
    fun getDarkModeState(): Boolean {
        return savePref.getDarkModeState()
    }
}


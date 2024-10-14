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

package com.calmscient.utils.common

import android.content.Context
import android.content.SharedPreferences
import java.util.Objects

class SavePreferences(context:Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "file", Context.MODE_PRIVATE)

    fun setEngLanguageState(engLanguageState: Boolean?) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putBoolean("english", engLanguageState!!)
        editor.apply()
    }
    fun setSpanLanguageState(spanLanguageState: Boolean?) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putBoolean("spanish", spanLanguageState!!)
        editor.apply()
    }
    fun setAslLanguageState(aslLanguageState: Boolean?) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putBoolean("asl", aslLanguageState!!)
        editor.apply()
    }
    fun getEngLanguageState():Boolean? {
        val state:Boolean = sharedPreferences.getBoolean("english",false)
        return (state)
    }
    fun getSpanLanguageState():Boolean? {
        val state:Boolean = sharedPreferences.getBoolean("spanish",false)
        return (state)
    }
    fun getAslLanguageState():Boolean? {
        val state:Boolean = sharedPreferences.getBoolean("asl",false)
        return (state)
    }
    fun setLanguageMode(languageMode: String?) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString("lang", languageMode!!)
        editor.apply()
    }
    fun getLanguageMode():String? {
        val langM:String? = sharedPreferences.getString("lang","")
        return (langM)
    }

    fun setDarkModeState(isNightMode: Boolean) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putBoolean("DARK_MODE", isNightMode)
        editor.apply()
    }

    fun getDarkModeState(): Boolean {
        return sharedPreferences.getBoolean("DARK_MODE", false)
    }

}
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

package com.calmscient.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FlagsViewModel : ViewModel() {
    val auditFlag = MutableLiveData<Boolean>()
    val dastFlag = MutableLiveData<Boolean>()
    val cageFlag = MutableLiveData<Boolean>()

    init {
        // Initialize flags with default values
        auditFlag.value = false
        dastFlag.value = false
        cageFlag.value = false
    }

    private val _tutorialFlag = MutableLiveData<Boolean>()
    val tutorialFlag: LiveData<Boolean> get() = _tutorialFlag

    fun updateTutorialFlag(newValue: Boolean) {
        _tutorialFlag.value = newValue
    }
}

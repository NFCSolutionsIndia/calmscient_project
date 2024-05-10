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

package com.calmscient.Interface

import android.view.View
import com.calmscient.di.remote.request.Alarm
import com.calmscient.di.remote.request.AlarmInternal
import com.calmscient.di.remote.request.AlarmUpdateRequest
import com.calmscient.di.remote.request.AlarmUpdateRequestInternal

interface BottomSheetListener {
    fun onShowBottomSheet()
    fun onShowEditBottomSheet(view: View, position: Int, description: String)
}


interface OnAlarmSelectedListener {
    fun onAlarmSelected(alarm: AlarmInternal)
}

interface OnAlarmSelectedListenerUpdate {
    fun onAlarmSelected(alarm: AlarmUpdateRequestInternal)
}


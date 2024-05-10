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

import com.calmscient.di.remote.response.MedicalDetails
import com.calmscient.fragments.CardViewItem
import com.calmscient.fragments.CardViewItems

interface CellClickListener {
    fun onCellClickListener(position:Int,item: CardViewItem)

}

interface CellClickListenerAppointments {
    fun onCellClickListener(position:Int,item: CardViewItems)
}

interface CellClickListenerOne {
    fun onCellClickListener(position:Int)
}
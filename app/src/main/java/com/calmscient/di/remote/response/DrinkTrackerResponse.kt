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

package com.calmscient.di.remote.response

data class DrinkTrackerResponse(
    val date: String,
    val drinksList: List<Drinks>,
    val statusResponse: StatusResponse,
    val totalCount: Int
)

data class Drinks(
    val drinkId: Int,
    val drinkName: String,
    val imageUrl: String,
    val isActive: Int,
    val quantity: Int
)

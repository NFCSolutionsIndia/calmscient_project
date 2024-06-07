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

data class GetTakingControlIntroductionResponse(
    val statusResponse: StatusResponse,
    val takingControlIntroduction: TakingControlIntroduction
)


data class TakingControlIntroduction(
    val auditFlag: Int,
    val cageFlag: Int,
    val clientId: Int,
    val createdAt: String,
    val dastFlag: Int,
    val introductionFlag: Int,
    val isEnable: Int,
    val notifyPCPDoctor: Int,
    val notifyPCPMedicine: Int,
    val patientId: Int,
    val plId: Int,
    val ptcId: Int,
    val tutorialFlag: Int,
    val updatedAt: String
)
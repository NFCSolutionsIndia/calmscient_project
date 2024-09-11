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

data class GetUserProfileResponse(
    val settings: Settings,
    val statusResponse: StatusResponse
)

data class Settings(
    val helpIcon: String,
    val helpTitle: String,
    val languageIcon: String,
    val languageTitle: String,
    val licenseDetails: LicenseDetails,
    val logoutIcon: String,
    val logoutTitle: String,
    val notificationIcon: String,
    val notificationTitle: String,
    val privacyIcon: String,
    val privacyTitle: String,
    val profileIcon: String,
    val profileImage: String,
    val profileTitle: String,
    val themeDetails: ThemeDetails
)

data class LicenseDetails(
    val licenseIcon: String,
    val licenseKey: String,
    val licenseTitle: String
)

data class ThemeDetails(
    val darkTheme: Int,
    val themeIcon: String,
    val themeTitle: String
)
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

data class MenuStatusResponse(
    val responseCode: Int,
    val responseMessage: String
)

data class MenuItem(
    val menuId: Int,
    val menuName: String,
    val menuType: String,
    val parentId: Int,
    val isChildExist: Int,
    val seqOrder: String
)

data class FavoriteItem(
    val patientId: Int,
    val favoritesId: Int,
    val lessonId: Int,
    val chapterId: Int,
    val pageNo: Int,
    val url: String,
    val isFavorite: Int,
    val thumbnailUrl: String,
    val navigateURL: String,
    val language: Int,
    val darkTheme: Int,
    val title: String,
    val isFromExercises: Int
)

data class MenuItemsResponse(
    val statusResponse: MenuStatusResponse,
    val menuItems: List<MenuItem>,
    val favorites: List<FavoriteItem>,
    val languageName: String
)


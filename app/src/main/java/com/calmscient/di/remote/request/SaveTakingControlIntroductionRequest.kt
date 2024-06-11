package com.calmscient.di.remote.request

data class SaveTakingControlIntroductionRequest(
    val auditFlag: Int?,
    val cageFlag: Int?,
    val clientId: Int,
    val dastFlag: Int?,
    val introductionFlag: Int,
    val patientId: Int,
    val plId: Int,
    val tutorialFlag: Int?
)

data class  SaveTakingControlIntroductionWrapper(
    val savePatientMood: SaveTakingControlIntroductionRequest
)
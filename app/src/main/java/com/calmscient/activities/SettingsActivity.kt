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

package com.calmscient.activities

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.calmscient.R
import com.calmscient.databinding.ActivitySettingsBinding
import com.calmscient.di.remote.response.GetBasicKnowledgeIndexResponse
import com.calmscient.di.remote.response.GetUserLanguagesResponse
import com.calmscient.di.remote.response.GetUserProfileResponse
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.di.remote.response.UpdateUserLanguageResponse
import com.calmscient.fragments.HomeFragment
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.LocaleHelper
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SavePreferences
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.utils.getColorCompat
import com.calmscient.viewmodels.GetPatientPrivacyDetailsViewModel
import com.calmscient.viewmodels.GetUserLanguagesViewModel
import com.calmscient.viewmodels.GetUserProfileDetailsViewModel
import com.calmscient.viewmodels.UpdateUserLanguageViewModel
import com.calmscient.viewmodels.UserLogoutViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale


@AndroidEntryPoint
class SettingsActivity : AppCompat(), View.OnClickListener {
    private var currentClickedLayoutId: Int =
        R.id.English // Initialize with the ID of English layout
    private var isFirstTime = true // Flag to determine if it's the first time onResume is called
    lateinit var binding: ActivitySettingsBinding
    lateinit var savePrefData: SavePreferences
    lateinit var localeLang: LocaleHelper
    lateinit var progressDialog: CustomProgressDialog

    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonDialog: CommonAPICallDialog
    private  lateinit var accessToken : String
    private lateinit var loginResponse: LoginResponse

    private val getUserProfileDetailsViewModel: GetUserProfileDetailsViewModel by viewModels()
    private lateinit var getUserProfileResponse: GetUserProfileResponse

    private val getUserLanguagesViewModel: GetUserLanguagesViewModel by viewModels()
    private lateinit var getUserLanguagesResponse: GetUserLanguagesResponse

    private val updateUserLanguageViewModel: UpdateUserLanguageViewModel by viewModels()
    private lateinit var updateUserLanguagesResponse: UpdateUserLanguageResponse
    private var englishLanguageId = -1
    private var spanishLanguageId = -1
    private var aslLanguageId = -1

    private val getPatientPrivacyDetailsViewModel: GetPatientPrivacyDetailsViewModel by viewModels()
    private val logoutViewModel: UserLogoutViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)


        accessToken = SharedPreferencesUtil.getData(this, "accessToken", "")
        customProgressDialog = CustomProgressDialog(this)
        commonDialog = CommonAPICallDialog(this)
        val loginJsonString = SharedPreferencesUtil.getData(this, "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString<LoginResponse>(loginJsonString)

        if(CommonClass.isNetworkAvailable(this)){
            getUserDateApiCall()
            languageAPICall()
            getPrivacyDataAPICall()
        }else{
            CommonClass.showInternetDialogue(this)
        }
        val view = binding.root
        setContentView(view)
        savePrefData = SavePreferences(this)
        localeLang = LocaleHelper(this)
        progressDialog = CustomProgressDialog(this)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val englishParentLayout = findViewById<View>(R.id.English)
        val spanishParentLayout = findViewById<View>(R.id.Spanish)
        val aslParentLayout = findViewById<View>(R.id.ASL)

        // Set click listeners for the parent layouts
        englishParentLayout.setOnClickListener(this)
        spanishParentLayout.setOnClickListener(this)
        aslParentLayout.setOnClickListener(this)

        binding.backIcon.setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }
        binding.privacyLayout.setOnClickListener {
            val privacyActivity = PrivacyBottomSheet()
            privacyActivity.show(this.supportFragmentManager, privacyActivity.tag)
        }
        binding.logout.setOnClickListener {
            logoutAPICall()
        }
    }

    fun loader() {
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            progressDialog.show("Loading")
        }, 3000)
        progressDialog.dialogDismiss()
    }
    override fun onClick(v: View?) {
        // Handle click events here
        when (v?.id) {
            R.id.English -> {
                currentClickedLayoutId = R.id.English
                // Code to handle click on English layout
                updateBackground(currentClickedLayoutId, v.id)
                binding.tvEnglish.setTextColor(this.getColorCompat(R.color.white))
                binding.tvSpanish.setTextColor(this.getColorCompat(R.color.black))
                binding.tvAsl.setTextColor(this.getColorCompat(R.color.black))
                savePrefData.setEngLanguageState(true)
                savePrefData.setSpanLanguageState(false)
                savePrefData.setAslLanguageState(false)
                localeLang.setLocale(this, "en")
                languageSettingAPICall(englishLanguageId)
            }

            R.id.Spanish -> {
                // Code to handle click on Spanish layout
                currentClickedLayoutId = R.id.Spanish
                updateBackground(currentClickedLayoutId, v.id)
                binding.tvSpanish.setTextColor(this.getColorCompat(R.color.white))
                binding.tvEnglish.setTextColor(this.getColorCompat(R.color.black))
                binding.tvAsl.setTextColor(this.getColorCompat(R.color.black))
                savePrefData.setSpanLanguageState(true)
                savePrefData.setEngLanguageState(false)
                savePrefData.setAslLanguageState(false)
                localeLang.setLocale(this, "es")
                languageSettingAPICall(spanishLanguageId)
            }

            R.id.ASL -> {
                // Code to handle click on ASL layout
                currentClickedLayoutId = R.id.ASL
                updateBackground(currentClickedLayoutId, v.id)
                binding.tvAsl.setTextColor(this.getColorCompat(R.color.white))
                binding.tvEnglish.setTextColor(this.getColorCompat(R.color.black))
                binding.tvSpanish.setTextColor(this.getColorCompat(R.color.black))
                savePrefData.setAslLanguageState(true)
                savePrefData.setSpanLanguageState(false)
                savePrefData.setEngLanguageState(false)
                localeLang.setLocale(this, "en")
                languageSettingAPICall(aslLanguageId)

            }
        }
    }

    private fun updateBackground(previousId: Int, currentId: Int) {
        val previousLayout = findViewById<View>(previousId)
        val previousTextView = previousLayout.findViewById<TextView>(R.id.tv_english)

        previousLayout.setBackgroundResource(R.drawable.rectangle_normal)
        //previousTextView.setTextColor(ContextCompat.getColor(this,R.color.black))

        val currentLayout = findViewById<View>(currentId)
        currentLayout.setBackgroundResource(R.drawable.background_language_settings)

        currentClickedLayoutId = currentId
    }


    private fun getUserDateApiCall(){
        getUserProfileDetailsViewModel.getUserProfileDetails(loginResponse.loginDetails.clientID,loginResponse.loginDetails.patientID,accessToken)
        observeUserDate()
    }

    private fun observeUserDate(){
        getUserProfileDetailsViewModel.loadingLiveData.observe(this, Observer { isLoading->
            if(isLoading){
                customProgressDialog.show(getString(R.string.loading))
            }else{
                customProgressDialog.dialogDismiss()
            }
        })

        getUserProfileDetailsViewModel.successLiveData.observe(this, Observer { isSuccess->
            if(isSuccess){
                getUserProfileDetailsViewModel.saveResponseLiveData.observe(this, Observer { successData->
                    if(successData != null){
                        getUserProfileResponse = successData
                        bindUserProfile(successData)
                        binding.settingsLayout.visibility = View.VISIBLE
                    }
                })
            }
        })
    }

    private fun bindUserProfile(response: GetUserProfileResponse) {
        // Bind settings to UI elements
       /* Glide.with(this)
            .load(response.settings.profileImage)
            .into(binding.profileImage)
        Glide.with(this)
            .load(response.settings.profileIcon)
            .into(binding.ivProfile)
        Glide.with(this)
            .load(response.settings.themeDetails.themeIcon)
            .into(binding.ivTheme)
        Glide.with(this)
            .load(response.settings.languageIcon)
            .into(binding.ivLanguage)
        Glide.with(this)
            .load(response.settings.privacyIcon)
            .into(binding.ivPrivacy)
        Glide.with(this)
            .load(response.settings.notificationIcon)
            .into(binding.ivNotification)
        Glide.with(this)
            .load(response.settings.licenseDetails.licenseIcon)
            .into(binding.ivLicense)
        Glide.with(this)
            .load(response.settings.helpIcon)
            .into(binding.ivHelpAndSupport)
        Glide.with(this)
            .load(response.settings.logoutIcon)
            .into(binding.ivLogout)*/



      binding.apply {
          tvProfile.text = response.settings.profileTitle
          tvTheme.text = response.settings.themeDetails.themeTitle
          tvLanguage.text = response.settings.languageTitle
          tvPrivacy.text = response.settings.privacyTitle
          tvNotification.text = response.settings.notificationTitle
          tvLincense.text = response.settings.licenseDetails.licenseTitle
          tvHelpAndSupport.text = response.settings.helpTitle
          tvLogout.text = response.settings.logoutTitle

          darkThemeToggleButton.isOn = response.settings.themeDetails.darkTheme == 1
          /*darkThemeToggleButton.labelOn = getString(R.string.yes)
          darkThemeToggleButton.labelOff = getString(R.string.no)*/
      }
    }

    private fun languageAPICall(){
        getUserLanguagesViewModel.getUserLanguages(loginResponse.loginDetails.clientID,loginResponse.loginDetails.patientID,accessToken)
        observeLanguageAPIData()
    }
    private fun observeLanguageAPIData(){
        getUserLanguagesViewModel.loadingLiveData.observe(this, Observer { isLoading->
            if(isLoading){
                customProgressDialog.show(getString(R.string.loading))
            }else{
                customProgressDialog.dialogDismiss()
            }
        })

        getUserLanguagesViewModel.successLiveData.observe(this, Observer { isSuccess->
            if(isSuccess){
                getUserLanguagesViewModel.saveResponseLiveData.observe(this, Observer { successData->
                    if(successData != null){
                        getUserLanguagesResponse = successData
                        binding.settingsLayout.visibility = View.VISIBLE
                        if(successData.patientLanguages.isNotEmpty()){
                            bindLanguageSettings(successData)
                        }
                    }
                })
            }
        })
    }

    private fun bindLanguageSettings(response: GetUserLanguagesResponse) {
        // Loop through the patientLanguages list to find the preferred language
        response.patientLanguages?.let { languages ->
            languages.forEach { language ->
                when (language.languageName) {
                    "English" -> {
                        englishLanguageId = language.languageId
                        if (language.preferred == 1) {
                            currentClickedLayoutId = R.id.English
                            updateBackground(currentClickedLayoutId, R.id.English)
                            binding.tvEnglish.setTextColor(this.getColorCompat(R.color.white))
                            binding.tvSpanish.setTextColor(this.getColorCompat(R.color.black))
                            binding.tvAsl.setTextColor(this.getColorCompat(R.color.black))
                            savePrefData.setEngLanguageState(true)
                            savePrefData.setSpanLanguageState(false)
                            savePrefData.setAslLanguageState(false)
                            savePrefData.setLanguageMode("en")
                            localeLang.setLocale(this, "en")
                        }
                    }
                    "Spanish" -> {
                        spanishLanguageId = language.languageId
                        if (language.preferred == 1) {
                            currentClickedLayoutId = R.id.Spanish
                            updateBackground(currentClickedLayoutId, R.id.Spanish)
                            binding.tvSpanish.setTextColor(this.getColorCompat(R.color.white))
                            binding.tvEnglish.setTextColor(this.getColorCompat(R.color.black))
                            binding.tvAsl.setTextColor(this.getColorCompat(R.color.black))
                            savePrefData.setSpanLanguageState(true)
                            savePrefData.setEngLanguageState(false)
                            savePrefData.setAslLanguageState(false)
                            savePrefData.setLanguageMode("es")
                            localeLang.setLocale(this, "es")

                        }
                    }
                    "ASL" -> {
                        aslLanguageId = language.languageId
                        if (language.preferred == 1) {
                            currentClickedLayoutId = R.id.ASL
                            updateBackground(currentClickedLayoutId, R.id.ASL)
                            binding.tvAsl.setTextColor(this.getColorCompat(R.color.white))
                            binding.tvEnglish.setTextColor(this.getColorCompat(R.color.black))
                            binding.tvSpanish.setTextColor(this.getColorCompat(R.color.black))
                            savePrefData.setAslLanguageState(true)
                            savePrefData.setEngLanguageState(false)
                            savePrefData.setSpanLanguageState(false)
                            savePrefData.setLanguageMode("en")
                            localeLang.setLocale(this, "en")
                        }
                    }
                }
            }
        }
    }

    private fun languageSettingAPICall(languageId: Int){

        updateUserLanguageViewModel.updateUserLanguage(loginResponse.loginDetails.clientID,loginResponse.loginDetails.patientID,languageId,1,accessToken)

        observeLanguageSettingAPICall()
    }

    private fun observeLanguageSettingAPICall(){
        updateUserLanguageViewModel.loadingLiveData.observe(this, Observer { isLoading->
            if(isLoading){
                customProgressDialog.show(getString(R.string.loading))
            }else{
                customProgressDialog.dialogDismiss()
            }
        })

        updateUserLanguageViewModel.successLiveData.observe(this, Observer { isSuccess->
            if(isSuccess){
                updateUserLanguageViewModel.saveResponseLiveData.observe(this, Observer { successData->
                    if(successData != null){
                        updateUserLanguagesResponse = successData
                        binding.settingsLayout.visibility = View.VISIBLE
                        setResult(Activity.RESULT_OK)
                        finish()
                        startActivity(intent)
                    }
                })
            }
        })
    }

    private fun getPrivacyDataAPICall(){
        getPatientPrivacyDetailsViewModel.getPatientPrivacyDetails(loginResponse.loginDetails.clientID,loginResponse.loginDetails.patientID,loginResponse.loginDetails.patientLocationID,accessToken)
    }

    private fun observePrivacyAPICall(){

    }

    private fun logoutAPICall(){
        logoutViewModel.userLogout(accessToken)
        observeLogOutAPICall()
    }

    private fun observeLogOutAPICall(){
        logoutViewModel.loadingLiveData.observe(this, Observer { isLoading->
            if(isLoading){
                customProgressDialog.show(getString(R.string.loading))
            }else
            {
                customProgressDialog.dialogDismiss()
            }
        })

        logoutViewModel.successLiveData.observe(this, Observer { isSuccess->
            if(isSuccess){
                logoutViewModel.saveResponseLiveData.observe(this, Observer { successData->
                    if(successData != null){
                        commonDialog.showDialog(successData.responseMessage)
                        commonDialog.setOnDismissListener {
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                        }
                    }
                })
            }
        })

    }
}
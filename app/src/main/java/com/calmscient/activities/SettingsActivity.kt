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

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.UiModeManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.calmscient.viewmodels.UploadProfileImageViewModel
import com.calmscient.viewmodels.UserLogoutViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
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

    private val uploadProfileImageViewModel: UploadProfileImageViewModel by viewModels()

    companion object {
        private const val REQUEST_IMAGE_PICKER = 1001
        private const val STORAGE_PERMISSION_REQUEST_CODE = 1002
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1003
    }


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
            val privacyBottomSheet = PrivacyBottomSheet()
            privacyBottomSheet.show(supportFragmentManager, privacyBottomSheet.tag)
        }
        binding.logout.setOnClickListener {
            logoutAPICall()
        }

        binding.uploadImage.setOnClickListener{
            // here i want to open two options one is Camera and external storage for the internal phone images in seperate method
            showImagePickerOptions()
        }

        binding.darkThemeToggleButton.setOnToggledListener { toggleableView, isOn ->
            val uiModeManager = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager

            if (isOn) {
                uiModeManager.nightMode = UiModeManager.MODE_NIGHT_YES
            } else {
                uiModeManager.nightMode = UiModeManager.MODE_NIGHT_NO
            }

            // Optionally: Manually refresh necessary UI elements here without recreating the entire activity.
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
        getUserProfileDetailsViewModel.clear()
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
        val iconsAndViews = listOf(
            response.settings.profileImage to binding.profileImage,
            response.settings.profileIcon to binding.ivProfile,
            response.settings.themeDetails.themeIcon to binding.ivTheme,
            response.settings.languageIcon to binding.ivLanguage,
            response.settings.privacyIcon to binding.ivPrivacy,
            response.settings.notificationIcon to binding.ivNotification,
            response.settings.licenseDetails.licenseIcon to binding.ivLicense,
            response.settings.helpIcon to binding.ivHelpAndSupport,
            response.settings.logoutIcon to binding.ivLogout
        )

        iconsAndViews.forEach { (icon, view) ->
            Glide.with(this)
                .load(icon)
                .into(view)
        }




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
        getUserLanguagesViewModel.clear()
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
        updateUserLanguageViewModel.clear()
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

    private fun logoutAPICall(){
        logoutViewModel.clear()
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
    private fun showImagePickerOptions() {
        val options = arrayOf("Camera", "Gallery")
        AlertDialog.Builder(this)
            .setTitle("Select Image Source")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> openCamera() // Open Camera
                    1 -> openGallery() // Open Gallery
                }
            }
            .show()
    }

    private fun openCamera() {
      /*  val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(cameraIntent)*/

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Open Camera directly if permission is granted
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(cameraIntent)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

   /* private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }*/

    private fun openGallery() {
        if (checkAndRequestStoragePermissions()) {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(galleryIntent)
        }
    }


    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as Bitmap
            binding.profileImage.setImageBitmap(imageBitmap) // Show the selected image in the ImageView
            // Convert Bitmap to File and upload it to the server
            uploadImageAPICall(convertBitmapToFile(imageBitmap))
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            imageUri?.let {
                Glide.with(this).load(it).into(binding.profileImage) // Show the selected image in the ImageView
                // Convert Uri to File and upload it to the server
                val imageFile = getFileFromUri(it)
                imageFile?.let { file -> uploadImageAPICall(file) }
            }
        }
    }

    // Convert Bitmap to File
    private fun convertBitmapToFile(bitmap: Bitmap): File {
        val file = File(this.cacheDir, "profile_image.png")
        file.createNewFile()

        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos)
        val bitmapData = bos.toByteArray()

        val fos = FileOutputStream(file)
        fos.write(bitmapData)
        fos.flush()
        fos.close()

        return file
    }

    // Convert Uri to File
    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(this.cacheDir, "profile_image_${System.currentTimeMillis()}.jpg") // Use cacheDir
            inputStream?.use { stream ->
                FileOutputStream(file).use { outputStream ->
                    stream.copyTo(outputStream)
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun uploadImageAPICall(imageFile: File) {
        val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        val multipartBody = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)

        updateUserLanguageViewModel.clear()
        uploadProfileImageViewModel.uploadProfileImage(accessToken,
            loginResponse.loginDetails.patientID.toString(),
            loginResponse.loginDetails.clientID.toString(),multipartBody)
        observeImageUpload()
    }

    private fun observeImageUpload() {
        uploadProfileImageViewModel.loadingLiveData.observe(this, Observer { isLoading ->
            if (isLoading) {
                customProgressDialog.show("Uploading...")
            } else {
                customProgressDialog.dialogDismiss()
            }
        })

        uploadProfileImageViewModel.successLiveData.observe(this, Observer { isSuccess ->
            if (isSuccess) {
               uploadProfileImageViewModel.saveResponseLiveData.observe(this, Observer { successData->
                   if(successData != null){
                       Toast.makeText(this,successData.responseMessage,Toast.LENGTH_SHORT).show()
                   }
               })

            }
        })
    }

    private fun checkAndRequestStoragePermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 14 (API 34) and above, check READ_MEDIA_IMAGES
            return if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
                // Request permission for reading images
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), STORAGE_PERMISSION_REQUEST_CODE)
                false
            } else {
                true // Permission already granted
            }
        } else {
            // For Android 13 and below, check READ_EXTERNAL_STORAGE
            return if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST_CODE)
                false
            } else {
                true // Permission already granted
            }
        }
    }





    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                openGallery()
                // Proceed with your action, e.g., open gallery
            } else {
                // Permission denied
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                    // "Don't ask again" is checked, guide the user to settings
                    //showStoragePermissionDeniedDialog()

                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, open the gallery
                openGallery()
            } else {
                // Permission was denied, show a message to the user
                Toast.makeText(this, "Storage permission is required to access the gallery.", Toast.LENGTH_SHORT).show()
            }
        }
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, open the camera
                openCamera()
            } else {
                // Permission was denied, show a message to the user
                Toast.makeText(this, "Camera permission is required to use the camera.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /*private fun showStoragePermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Storage Permissions Required")
            .setMessage("To upload images, storage permission is required. Please allow the permission in the app settings.")
            .setPositiveButton("Go to Settings") { dialog, _ ->
                dialog.dismiss()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // For Android 11 (API 30) and above, navigate to All Files Access screen
                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                            data = Uri.parse("package:$packageName")
                        }
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        // Handle case where the settings screen isn't available
                        Toast.makeText(this, "Settings screen not available", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // For Android 10 (API 29) and below, navigate to the specific app settings page
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                // Dismiss the dialog without further action
                dialog.dismiss()
            }
            .setNeutralButton("Don't ask again") { dialog, _ ->
                // Optional button if you want to remind the user about the "Don't ask again" selection
                dialog.dismiss()
                Toast.makeText(this, "Permission permanently denied. Please enable it in settings.", Toast.LENGTH_SHORT).show()
            }
            .show()
    }*/




}
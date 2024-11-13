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

import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.calmscient.R
import com.calmscient.databinding.ActivityProfileBinding
import com.calmscient.di.remote.response.GetPatientProfileDetailsResponse
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.GetPatientProfileDetailsViewModel
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileActivity : AppCompat() {

    private lateinit var binding: ActivityProfileBinding
    private val getPatientProfileDetailsViewModel: GetPatientProfileDetailsViewModel by viewModels()


    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonDialog: CommonAPICallDialog
    private  lateinit var accessToken : String
    private lateinit var loginResponse: LoginResponse
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)

        accessToken = SharedPreferencesUtil.getData(this, "accessToken", "")
        customProgressDialog = CustomProgressDialog(this)
        commonDialog = CommonAPICallDialog(this)
        val loginJsonString = SharedPreferencesUtil.getData(this, "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString<LoginResponse>(loginJsonString)

        getPatientProfileInformationAPICall()
        setContentView(binding.root)

        setupFocusListeners()

        binding.backIcon.setOnClickListener{
            finish()
        }
    }

    private fun setupFocusListeners() {
        // Add focus listeners to each TextInputEditText
        val editTexts = listOf(
            binding.firstName,
            binding.lastName,
            binding.oldPassword,
            binding.newPassword,
            binding.confirmNewPassword
        )

        for (editText in editTexts) {
            editText.setOnFocusChangeListener { _, hasFocus ->
                // Hide submit button when any TextInputEditText gets focus
                binding.submitBtn.visibility = if (hasFocus) View.GONE else View.VISIBLE
            }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            if (view is TextInputEditText) {
                val outRect = Rect()
                view.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    view.clearFocus()
                    hideKeyboard(view)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun  getPatientProfileInformationAPICall(){
        getPatientProfileDetailsViewModel.clear()

        getPatientProfileDetailsViewModel.getPatientProfileDetails(loginResponse.loginDetails.patientID, accessToken)

        observeGetPatientProfileInfo()
    }
    private fun observeGetPatientProfileInfo(){
        getPatientProfileDetailsViewModel.loadingLiveData.observe(this, Observer { isLoading->
            if(isLoading){
                customProgressDialog.show(getString(R.string.loading))
            }else{
                customProgressDialog.dialogDismiss()
            }
        })

        getPatientProfileDetailsViewModel.successLiveData.observe(this, Observer { isSuccess->
            if(isSuccess){
                getPatientProfileDetailsViewModel.saveResponseLiveData.observe(this, Observer { successData->
                    if(successData != null && successData.statusResponse.responseCode == 200){

                        bindUserData(successData)
                    }
                })
            }
        })
    }

    private fun bindUserData(successData: GetPatientProfileDetailsResponse) {

        binding.apply {
            firstName.setText(successData.patientProfileDetails.firstName)
            lastName.setText(successData.patientProfileDetails.lastName)
            email.setText(successData.patientProfileDetails.emailAddress)
            phoneNumber.setText(successData.patientProfileDetails.phone)

        }

    }

}
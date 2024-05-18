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

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.calmscient.R
import com.calmscient.databinding.LayoutLoginBinding
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.viewmodels.LoginViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.utils.network.ServerTimeoutHandler

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    lateinit var binding: LayoutLoginBinding
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var responseDate: LoginResponse
    private lateinit var customProgressDialog: CustomProgressDialog

    private lateinit var commonDialog: CommonAPICallDialog

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        customProgressDialog = CustomProgressDialog(this)

        commonDialog = CommonAPICallDialog(this)

        binding.btnLogin.setOnClickListener {
            //navigateToDayScreen()
        }
        binding.forgotPass.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
        binding.userName.setOnFocusChangeListener(OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                binding.userName.hint = "xxxxxxxxxxxx@gmail.com"
            }
            if (!hasFocus) {
                binding.userName.setHint(R.string.username)
            }
        })
        binding.editPassword.setOnFocusChangeListener(OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                binding.editPassword.hint = "**********"
            }
            if (!hasFocus) {
                binding.editPassword.setHint(R.string.password)
            }
        })
        binding.termsCheckBox.setOnClickListener {
            // Open a web page with the terms and conditions when clicked
            /*val termsUrl = "https://"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(termsUrl))
            startActivity(intent)*/
        }
        binding.termsCheckBox.setOnCheckedChangeListener { _, isChecked ->
            // Enable/disable further actions based on checkbox state
            if (isChecked) {
                //navigateToDayScreen()
                // Checkbox is checked, perform actions here
                binding.termsTextView.visibility = View.GONE
            } else {
                binding.termsTextView.visibility = View.VISIBLE
                // Checkbox is unchecked, handle this case if needed
            }
        }
        binding.parentLayout.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                // Hide the soft keyboard
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)

                // Clear focus from the TextInputEditText
                binding.userNameTextInputLayout.clearFocus()
                binding.TinPassword.clearFocus()
            }
            false
        }

        loginViewModel.loginResultLiveData.observe(this) { isValidLogin ->
            if (isValidLogin) {
                // Login successful, navigate to the next screen
                responseDate = loginViewModel.responseData.value!!
                //handleLoginResponse(responseDate)

                val jsonString = JsonUtil.toJsonString(responseDate)
                SharedPreferencesUtil.saveData(this, "loginResponse", jsonString)

                Log.d("LoginActivity Response", "${responseDate.loginDetails}")
                val accessToken = responseDate.token.access_token

                SharedPreferencesUtil.saveData(this, "accessToken", accessToken)

                navigateToDayScreen(responseDate)
            } else {
                loginViewModel.failureResponseData.value?.let { failureMessage ->
                    failureMessage.statusResponse.responseMessage.let {
                        commonDialog.showDialog(
                            it
                        )
                    }
                }
                loginViewModel.errorLiveData.value?.let { failureMessage ->
                    failureMessage.let{
                        ServerTimeoutHandler.handleTimeoutException(this) {
                            // Retry logic when the retry button is clicked
                            loginViewModel.retryLogin()
                        }
                    }
                }

                //Snackbar.make(binding.root, "Invalid username or password", Snackbar.LENGTH_SHORT).show()
            }
        }
        loginViewModel.loadingLiveData.observe(this) { isLoading ->
            if (isLoading) {
                customProgressDialog.show("Loading...")
            } else {

                customProgressDialog.dialogDismiss()
            }
        }
        // Adding a text change listener to the email and password fields
        binding.userName.addTextChangedListener(inputTextWatcher)
        binding.editPassword.addTextChangedListener(inputTextWatcher)

        binding.errorIconUsername.setOnClickListener { showInfoMessage("Please enter Email ") }
        binding.errorIconPassword.setOnClickListener { showInfoMessage("Please enter Password ") }


        // Set up your views and listeners as before
        binding.btnLogin.setOnClickListener {

            val username = binding.userName.text.toString()
            val password = binding.editPassword.text.toString()

            if (!username.isEmpty() && !password.isEmpty()) {
                if (CommonClass.isNetworkAvailable(this)) {
                    loginViewModel.loginUser(username, password)
                } else {
                    CommonClass.showInternetDialogue(this)
                }
            } else {
                showError(binding.TinPassword, "Enter Valid Username & Password")
            }
            /*

                        else if (password.isEmpty()) {
                            showError(binding.TinPassword, "Please enter Password")
                        } else {
                            loginViewModel.loginUser(username, password)
                        }*/
        }


        val passwordToggle = findViewById<TextInputLayout>(R.id.Tin_password)
        val passwordEditText = findViewById<TextInputEditText>(R.id.edit_password)
        passwordToggle.passwordVisibilityToggleDrawable =
            ContextCompat.getDrawable(this, R.drawable.ic_eye_close)
        passwordToggle.setEndIconOnClickListener {
            if (passwordEditText.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                // Hide the password
                passwordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                passwordToggle.passwordVisibilityToggleDrawable =
                    ContextCompat.getDrawable(this, R.drawable.ic_eye_close)
            } else {
                // Show the password
                passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                passwordToggle.passwordVisibilityToggleDrawable =
                    ContextCompat.getDrawable(this, R.drawable.ic_eye_open)
            }
        }
    }

    private val inputTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            // Clear error and remove tint when text is entered
            binding.userNameTextInputLayout.error = null
            binding.TinPassword.error = null
        }
    }

    private fun showError(textInputLayout: TextInputLayout, errorMessage: String) {
        textInputLayout.error = errorMessage
        textInputLayout.editText?.background?.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP)
    }

    private fun showInfoMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        finishAffinity();
        finish()
    }

    private fun navigateToDayScreen(response: LoginResponse) {
        //loginViewModel.setResponseDate(response)
        startActivity(Intent(this, UserMoodActivity::class.java))
    }

//    private fun handleLoginResponse(response: LoginResponse) {
//        loginViewModel.setResponseDate(response)
//    }
}
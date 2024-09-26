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

package com.calmscient.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.calmscient.R
import com.calmscient.databinding.FragmentWebViewLessonBinding
import org.json.JSONException
import org.json.JSONObject

class WebViewLessonFragment : Fragment() {

    private var _binding: FragmentWebViewLessonBinding? = null
    private val binding get() = _binding!!

    private lateinit var webView: WebView
    private var urlString: String = ""
    private var pageTitle: String = ""
    private var index: Int = 0
    companion object {
        private const val ARG_URL = "url"
        private const val ARG_CHAPTER_NAME = "chapterName"

        fun newInstance(url: String, name: String) = WebViewLessonFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_URL, url)
                putString(ARG_CHAPTER_NAME, name)
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWebViewLessonBinding.inflate(inflater, container, false)
        binding.webViewTitle.text = arguments?.getString(WebViewLessonFragment.ARG_CHAPTER_NAME)
        urlString = arguments?.getString(WebViewLessonFragment.ARG_URL).toString()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = binding.webView
        setupWebView()

        // Load URL
        if (urlString.isNotEmpty()) {
            webView.loadUrl(urlString)
        }

        // Show loading indicator (using Toast for simplicity)
        Toast.makeText(context, "Loading...", Toast.LENGTH_SHORT).show()

        // Hide the loading indicator after 5 seconds
        webView.postDelayed({
            Toast.makeText(context, "Finished Loading", Toast.LENGTH_SHORT).show()
        }, 5000)

        configureCustomBackButton()
        configureRightButton()
        disableZoom()
    }

    @SuppressLint("JavascriptInterface")
    private fun setupWebView() {
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                // Do something when page starts loading
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                // Do something when page finishes loading
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                // Handle errors
                Toast.makeText(context, "Error loading page", Toast.LENGTH_SHORT).show()
            }
        }

        // WebChromeClient to handle JavaScript dialogs, titles, progress, etc.
        webView.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                // Update the page title
                if (!pageTitle.isEmpty()) {
                    activity?.title = pageTitle
                } else {
                    activity?.title = title
                }
            }
        }

        // Adding a JavaScript interface for communication with JS
        webView.addJavascriptInterface(WebAppInterface(), "nativeDispatch")
    }

    private fun disableZoom() {
        val script = """
            var meta = document.createElement('meta');
            meta.name = 'viewport';
            meta.content = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no';
            document.getElementsByTagName('head')[0].appendChild(meta);
        """
        webView.evaluateJavascript(script, null)
    }

    private fun configureCustomBackButton() {
        // Custom back button implementation
        val backButton = activity?.actionBar?.customView?.findViewById<View>(R.id.backIcon)
        backButton?.setOnClickListener {
            handleBackButtonAction()
        }
    }

    private fun configureRightButton() {
        // Custom right button implementation (similar to iOS)
       /* val rightButton = activity?.actionBar?.customView?.findViewById<View>(R.id.rightButton)
        rightButton?.setOnClickListener {
            handleRightButtonAction()
        }*/
    }

    private fun handleBackButtonAction() {
        val javascript = "onAbortCourseGotoIndex();"
        webView.evaluateJavascript(javascript) { result ->
            val messageBody = mapOf("1001" to "turn off loading and go to index")
            for ((key, value) in messageBody) {
                if (key == "1001") {
                    if (index == 2 || index == 3) {
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }

    private fun handleRightButtonAction() {
        // Handle right button action (implement based on your needs)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // JavaScript interface for communicating between JS and Kotlin
    inner class WebAppInterface {
        @JavascriptInterface
        fun onMessageFromJs(messageBody: String) {
            // Handle the messages from JavaScript
            val message = parseJson(messageBody)
            for ((key, value) in message) {
                when (key) {
                    "1001" -> {
                        // Handle case for key "1001"
                        if (index == 2 || index == 3) {
                            findNavController().popBackStack()
                        }
                    }
                    "1100" -> {
                        // Handle loading complete
                        activity?.title = pageTitle
                        Toast.makeText(context, "Web page loaded", Toast.LENGTH_SHORT).show()
                    }
                    "401" -> {
                        // Show an alert dialog for error
                        AlertDialog.Builder(requireContext())
                            .setTitle("Error Occurred")
                            .setMessage("Error occurred. Please try again.")
                            .setPositiveButton("OK") { _, _ -> }
                            .show()
                    }
                    // Add more cases as necessary
                }
            }
        }
    }

    // Helper function to parse JSON (simplified for demonstration)
    private fun parseJson(jsonString: String): Map<String, Any> {
        return try {
            val jsonObject = JSONObject(jsonString)
            jsonObject.keys().asSequence().associateWith { jsonObject.get(it) }
        } catch (e: JSONException) {
            emptyMap()
        }
    }
}
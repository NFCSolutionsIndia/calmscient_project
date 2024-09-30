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
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.calmscient.R
import com.calmscient.utils.CustomProgressDialog
import org.json.JSONException
import org.json.JSONObject

class WebViewFragment : Fragment() {

    companion object {
        private const val ARG_URL = "url"
        private const val ARG_CHAPTER_NAME = "chapterName"

        fun newInstance(url: String, name: String) = WebViewFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_URL, url)
                putString(ARG_CHAPTER_NAME, name)
            }
        }
    }

    private var toolbar: Toolbar? = null
    private var webViewLearn: WebView? = null
    private var icBack: ImageView? = null
    private var icGlossary: ImageView? = null
    private lateinit var progressBar: ProgressBar
    private lateinit var headingText: TextView
    private var url: String? = null
    private lateinit var customProgressDialog: CustomProgressDialog
    var index : Int = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_webview, container, false)
        toolbar = rootView.findViewById(R.id.toolbar_learn_more)
        webViewLearn = rootView.findViewById(R.id.webview_learn_more)
        progressBar = rootView.findViewById(R.id.progressBar_webview)
        headingText = rootView.findViewById(R.id.webViewTitle)
        icBack = rootView.findViewById(R.id.backIcon)
        icGlossary = rootView.findViewById(R.id.ic_glossary)
        customProgressDialog = CustomProgressDialog(requireContext())
        headingText.text = arguments?.getString(ARG_CHAPTER_NAME)
        url = arguments?.getString(ARG_URL)

        if (savedInstanceState != null) {
            webViewLearn?.restoreState(savedInstanceState)
        } else if (url != null) {
            initializeWebView(url!!)
        } else {
            Log.e("WebViewFragment", "No URL provided")
        }

        return rootView
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*icBack!!.setOnClickListener {
            activity?.onBackPressed()
        }*/

        icBack!!.setOnClickListener {
            webViewLearn?.evaluateJavascript("onAbortCourseGotoIndex();") { result ->
                Log.d("WebViewFragment", "JS Result: $result")

            }
            activity?.onBackPressed()
        }

        icGlossary?.setOnClickListener{
            loadFragment(GlossaryFragment())
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initializeWebView(url: String) {
        webViewLearn?.settings?.javaScriptEnabled = true
        webViewLearn?.settings?.domStorageEnabled = true
        webViewLearn?.settings?.userAgentString =
            "Mozilla/5.0 (Linux; Android 4.1.2; C1905 Build/15.1.C.2.8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.114 Mobile Safari/537.36"
        webViewLearn?.settings?.loadWithOverviewMode = true
        webViewLearn?.settings?.useWideViewPort = true
        webViewLearn?.settings?.cacheMode = WebSettings.LOAD_NO_CACHE
        webViewLearn?.webViewClient = CustomWebViewClient()
        webViewLearn?.webChromeClient = CustomWebChromeClient(requireContext())

        // Add the JavaScript Interface
        webViewLearn?.addJavascriptInterface(JavaScriptInterface(requireContext()), "nativeDispatch")

        // Load the URL
        webViewLearn?.loadUrl(url)
    }

    override fun onPause() {
        super.onPause()
        webViewLearn?.onPause()
    }

    override fun onResume() {
        super.onResume()
        webViewLearn?.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webViewLearn?.saveState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webViewLearn?.destroy()
    }

    private inner class CustomWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            //progressBar.visibility = View.VISIBLE
            customProgressDialog.show("Loading...")
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            webViewLearn!!.visibility = View.VISIBLE
            //progressBar.visibility = View.GONE
            Handler(Looper.getMainLooper()).postDelayed({
                customProgressDialog.dialogDismiss()
            }, 4000)
        }

        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
            super.onReceivedError(view, request, error)
            progressBar.visibility = View.GONE
            Log.e("WebViewFragment", "Error loading page: ${error.description}")
        }
    }

    private inner class CustomWebChromeClient(var context: Context) : WebChromeClient() {
        override fun onShowFileChooser(
            webView: WebView,
            filePathCallback: ValueCallback<Array<Uri>>,
            fileChooserParams: FileChooserParams
        ): Boolean {
            return super.onShowFileChooser(webView, filePathCallback, fileChooserParams)
        }
    }

    private inner class JavaScriptInterface(private val context: Context) {

        @JavascriptInterface
        fun postMessage(data: String) {
            // Parse the incoming JSON string
            try {
                val jsonData = JSONObject(data)
                Log.d("WebViewFragment", "Success Data: $data")
                handleJavaScriptMessage(jsonData)
            } catch (e: JSONException) {
                Log.e("WebViewFragment", "Failed to parse JSON: $data", e)
            }
        }

        private fun handleJavaScriptMessage(jsonData: JSONObject) {
            val keys: Iterator<String> = jsonData.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = jsonData.get(key)  // Get the value associated with the key

                when (key) {
                    "1001" -> {
                        // Show the navigation bar if needed
                        requireActivity().actionBar?.show()
                    }
                    "1100" -> {
                        // Hide any loading indicators
                        customProgressDialog.dialogDismiss()
                        // Set the page title if it is available
                        requireActivity().actionBar?.show()
                        // Log the value to ensure correct handling
                        Log.d("WebViewFragment", "Success Data: $value")
                    }
                    "1003" -> {
                        // Hide the navigation bar
                        requireActivity().actionBar?.hide()
                    }
                    "401" -> {
                        // Show an error message
                        requireActivity().actionBar?.show()
                        val alertDialog = AlertDialog.Builder(context)
                            .setTitle("Error Occurred")
                            .setMessage("Error occurred. Please try again!!")
                            .setPositiveButton("OK", null)
                            .create()
                        alertDialog.show()
                    }
                    else -> {
                        // Handle other cases if necessary
                        Toast.makeText(context, "Unhandled key: $key", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }

}

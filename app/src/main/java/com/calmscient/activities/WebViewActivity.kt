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
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.calmscient.R
import com.calmscient.fragments.GlossaryFragment

class WebViewActivity : AppCompatActivity() {

    companion object {
        const val ARG_URL = "url"
        const val CHAPTER_NAME = "chapterName"
    }

    private lateinit var toolbar: Toolbar
    private lateinit var webViewLearn: WebView
    private lateinit var icBack: ImageView
    private lateinit var icGlossary: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var headingText: TextView
    private var url: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_webview)

        toolbar = findViewById(R.id.toolbar_learn_more)
        webViewLearn = findViewById(R.id.webview_learn_more)
        progressBar = findViewById(R.id.progressBar_webview)
        headingText = findViewById(R.id.webViewTitle)
        icBack = findViewById(R.id.backIcon)
        icGlossary = findViewById(R.id.ic_glossary)

        url = intent.getStringExtra(ARG_URL)
        headingText.text = intent.getStringExtra(CHAPTER_NAME)

        if (url == null) {
            Log.e("WebViewActivity", "No URL provided")
            return
        }

        initializeWebView(url!!)

        icBack.setOnClickListener {
            onBackPressed()
        }
        icGlossary.setOnClickListener {
            // Implement glossary action
            loadFragment(GlossaryFragment())
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initializeWebView(url: String) {
        webViewLearn.settings.javaScriptEnabled = true
        webViewLearn.settings.domStorageEnabled = true
        webViewLearn.settings.userAgentString =
            "Mozilla/5.0 (Linux; Android 4.1.2; C1905 Build/15.1.C.2.8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.114 Mobile Safari/537.36"
        webViewLearn.settings.loadWithOverviewMode = true
        webViewLearn.webViewClient = CustomWebViewClient()
        webViewLearn.webChromeClient = CustomWebChromeClient(this)

        webViewLearn.loadUrl(url)
    }

    private inner class CustomWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return false
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            webViewLearn.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }

        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
            super.onReceivedError(view, request, error)
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
    private fun loadFragment(fragment: Fragment) {
        val transaction = this.supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}

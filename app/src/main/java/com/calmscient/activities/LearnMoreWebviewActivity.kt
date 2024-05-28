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
import android.content.Intent
import android.graphics.Bitmap
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.calmscient.R
import java.io.File
import java.io.IOException

class LearnMoreWebviewActivity : AppCompat() {
    private var toolbar: Toolbar? = null
    private var webView_learn: WebView? = null
    private var icBack: ImageView? = null
    private lateinit var progressBar: ProgressBar
    private var url: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_webview)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        toolbar = findViewById(R.id.toolbar_learn_more) as Toolbar
        webView_learn = findViewById(R.id.webview_learn_more) as WebView
        progressBar = findViewById(R.id.progressBar_webview) as ProgressBar
        icBack = findViewById(R.id.backIcon) as ImageView
        val url1: String = intent.getStringExtra("988_url").toString()
        //initializeViews()
        webView_learn!!.settings.javaScriptEnabled = true
        webView_learn!!.settings.domStorageEnabled = true
        webView_learn!!.settings.userAgentString =
            "Mozilla/5.0 (Linux; Android 4.1.2; C1905 Build/15.1.C.2.8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.114 Mobile Safari/537.36"
        webView_learn!!.settings.loadWithOverviewMode = true
        webView_learn!!.webViewClient = WebViewClient()
        // Loading a URL
        webView_learn!!.loadUrl(url1)
        icBack!!.setOnClickListener {
            finish()
        }
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar_learn_more) as Toolbar
        webView_learn = findViewById(R.id.webview_learn_more) as WebView
        progressBar = findViewById(R.id.progressBar_webview) as ProgressBar
        icBack = findViewById(R.id.backIcon) as ImageView
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Rapid Order System"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val url1: String = intent.getStringExtra("988_url").toString()
        Log.d("989_url", "" + url1)
        url = url1
        webView_learn!!.settings.javaScriptEnabled = true
        webView_learn!!.settings.domStorageEnabled = true
        webView_learn!!.settings.userAgentString =
            "Mozilla/5.0 (Linux; Android 4.1.2; C1905 Build/15.1.C.2.8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.114 Mobile Safari/537.36"
        webView_learn!!.settings.loadWithOverviewMode = true
        //loadWebView()
        webView_learn!!.webViewClient = WebViewClient()
        webView_learn!!.loadUrl(url!!)
        icBack!!.setOnClickListener {
            finish()
        }
    }

    // Overriding WebViewClient functions
    open inner class WebViewClient : android.webkit.WebViewClient() {

        // Load the URL
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return false
        }

        // ProgressBar will disappear once page is loaded
        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            webView_learn!!.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadWebView() {
        webView_learn!!.setWebViewClient(WebViewClient())
        webView_learn!!.getSettings().setAllowFileAccess(true)
        webView_learn!!.setWebChromeClient(MyWebChromeClient(applicationContext))
        webView_learn!!.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap) {
                super.onPageStarted(view, url, favicon)
                view.loadUrl(
                    "javascript:(function() { " +
                            "document.querySelector('[role=\"toolbar\"]').remove();})()"
                )
                //com.showProgressDialogue();
            }

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                view.loadUrl(request.url.toString())
                return false
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                view.loadUrl(
                    "javascript:(function() { " +
                            "document.querySelector('[role=\"toolbar\"]').remove();})()"
                )
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                super.onReceivedError(view, request, error)
            }
        }
        webView_learn!!.setWebChromeClient(object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                return if (Build.VERSION.SDK_INT >= 21) {
                    val takePictureIntent: Intent? = null
                    val takeVideoIntent: Intent? = null
                    var includeVideo = false
                    var includePhoto = false

                    /*-- checking the accept parameter to determine which intent(s) to include --*/paramCheck@ for (acceptTypes in fileChooserParams.acceptTypes) {
                        val splitTypes =
                            acceptTypes.split(", ?+".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                        /*-- although it's an array, it still seems to be the whole value; split it out into chunks so that we can detect multiple values --*/for (acceptType in splitTypes) {
                            when (acceptType) {
                                "*/*" -> {
                                    includePhoto = true
                                    includeVideo = true
                                    break@paramCheck
                                }

                                "image/*" -> includePhoto = true
                                "video/*" -> includeVideo = true
                            }
                        }
                    }
                    if (fileChooserParams.acceptTypes.size == 0) {

                        /*-- no `accept` parameter was specified, allow both photo and video --*/
                        includePhoto = true
                        includeVideo = true
                    }

                    val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
                    contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)

                    val intentArray: Array<Intent?>
                    intentArray = if (takePictureIntent != null && takeVideoIntent != null) {
                        arrayOf(takePictureIntent, takeVideoIntent)
                    } else takePictureIntent?.let { arrayOf(it) }
                        ?: (takeVideoIntent?.let { arrayOf(it) } ?: arrayOfNulls(0))
                    val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                    chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                    chooserIntent.putExtra(Intent.EXTRA_TITLE, "File chooser")
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
                    true
                } else {
                    false
                }
            }

            // creating image files (Lollipop only)
            @Throws(IOException::class)
            private fun createImageFile(): File {
                var imageStorageDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "DirectoryNameHere"
                )
                if (!imageStorageDir.exists()) {
                    imageStorageDir.mkdirs()
                }

                // create an image file name
                imageStorageDir = File(
                    imageStorageDir.toString() + File.separator + "IMG_" + System.currentTimeMillis()
                        .toString() + ".jpg"
                )
                return imageStorageDir
            }
        })
        webView_learn!!.settings.javaScriptEnabled = true
        webView_learn!!.settings.domStorageEnabled = true
        webView_learn!!.isHorizontalScrollBarEnabled = false
    }

    private class MyWebChromeClient(var context: Context) :
        WebChromeClient()
    /*fun initToolbar(toolbarId: Int) {
        val myToolbar = findViewById<View>(toolbarId) as Toolbar
        setSupportActionBar(myToolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        assert(myToolbar != null)
        if (com.getcolorCode().equals("")) {
            myToolbar.setBackgroundColor(resources.getColor(Build.colorPrimary))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val window = window
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = resources.getColor(R.color.colorPrimary)
            }
        } else {
            myToolbar.setBackgroundColor(Color.parseColor(com.getcolorCode()))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val window = window
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = Color.parseColor(com.getcolorCode())
            }
        }
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }*/
}
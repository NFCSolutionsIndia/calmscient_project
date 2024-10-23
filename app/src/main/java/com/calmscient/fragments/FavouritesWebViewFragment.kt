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
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.calmscient.R
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.CommonClass
import org.json.JSONException
import org.json.JSONObject

class FavouritesWebViewFragment : Fragment() {

    companion object {
        private const val ARG_URL = "url"
        private const val ARG_CHAPTER_NAME = "chapterName"

        fun newInstance(url: String, name: String) = FavouritesWebViewFragment().apply {
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_favourites_web_view, container, false)
        toolbar = rootView.findViewById(R.id.toolbar_learn_more)
        webViewLearn = rootView.findViewById(R.id.webview_learn_more)
        progressBar = rootView.findViewById(R.id.progressBar_webview)
        headingText = rootView.findViewById(R.id.webViewTitle)
        icBack = rootView.findViewById(R.id.backIcon)
        //icGlossary = rootView.findViewById(R.id.ic_glossary)
        customProgressDialog = CustomProgressDialog(requireContext())
        val chapterName = arguments?.getString(ARG_CHAPTER_NAME)
        //Toast.makeText(requireContext(),"${chapterName}", Toast.LENGTH_LONG).show()
        headingText.text = chapterName


        url = arguments?.getString(ARG_URL)

        if (savedInstanceState != null) {
            webViewLearn?.restoreState(savedInstanceState)
        } else if (url != null) {
            initializeWebView(url!!)
        } else {
            Log.e("FavouritesWebViewFragment", "No URL provided")
        }

        return rootView
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(CommonClass.isNetworkAvailable(requireContext())){
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Call your JS function
                    webViewLearn?.evaluateJavascript("onAbortCourseGotoIndex();") { result ->

                        customProgressDialog.show(getString(R.string.loading))
                        /*Handler(Looper.getMainLooper()).postDelayed({
                            customProgressDialog.dialogDismiss()
                        }, 4000)*/

                    }
                }
            })

            icBack!!.setOnClickListener {
                webViewLearn?.evaluateJavascript("onAbortCourseGotoIndex();") { result ->
                    Log.d("WebViewFragment", "JS Result: $result")
                    customProgressDialog.show(getString(R.string.loading))
                   /* Handler(Looper.getMainLooper()).postDelayed({
                        customProgressDialog.dialogDismiss()
                    }, 4000)*/
                }
            }
        }else{
            CommonClass.showInternetDialogue(requireContext())
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
        webViewLearn!!.settings.javaScriptEnabled = true
        webViewLearn!!.settings.domStorageEnabled = true
        webViewLearn!!.settings.userAgentString =
            "Mozilla/5.0 (Linux; Android 4.1.2; C1905 Build/15.1.C.2.8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.114 Mobile Safari/537.36"
        webViewLearn!!.settings.loadWithOverviewMode = true
        webViewLearn!!.settings.useWideViewPort = true
        webViewLearn!!.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webViewLearn!!.webViewClient = CustomWebViewClient()
        webViewLearn!!.webChromeClient = CustomWebChromeClient(requireContext())

        // Add the JavaScript Interface
        webViewLearn?.addJavascriptInterface(JavaScriptInterface(requireContext()), "nativeDispatch")


        webViewLearn!!.loadUrl(url)
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
            Log.e("FavouritesWebViewFragment", "Error loading page: ${error.description}")

            if (error.description.toString().contains("net::ERR_INTERNET_DISCONNECTED")) {
                CommonClass.showInternetDialogue(requireContext())
            } else {
                showErrorDialog(error.description.toString())
            }
        }
    }

    private fun showErrorDialog(errorMessage: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Error Occurred")
            .setMessage("Error loading page: $errorMessage")
            .setPositiveButton("OK", null)
            .show()
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
            val toolbar = view?.findViewById<Toolbar>(R.id.toolbar_learn_more)
            val webViewTitle = view?.findViewById<TextView>(R.id.webViewTitle)

            while (keys.hasNext()) {
                val key = keys.next()
                val value = jsonData.getString(key)

                when (key) {
                    "1001" -> handleGotoIndex(value)
                    "1100" -> handleInitialLoadingOff(value)
                    "401" -> handleInvalidSession(value)
                    "1002" -> {
                        if (webViewTitle != null) {
                            handleChangedHeaderTitle(value, webViewTitle)
                        }
                    }
                    "1003" -> {
                        toolbar?.visibility = View.GONE
                        Log.d("WebViewFragment", "Action: Hiding toolbar")
                    }
                    "1004" -> {
                        toolbar?.visibility = View.VISIBLE
                        Log.d("WebViewFragment", "Action: Showing toolbar")
                    }
                    "1005" -> handleNeedToTalkWithSomeone(value)
                    "1006" -> handleReturningBackFromFavMedia()
                    else -> {
                        // Handle other or unknown cases
                        Toast.makeText(context, "Unhandled key: $key with message: $value", Toast.LENGTH_SHORT).show()
                        Log.d("WebViewFragment", "Unhandled key: $key, Message: $value")
                    }
                }
            }
        }


        private fun handleGotoIndex(value: String) {
            if (value == "turn off loading and go to index") {
                customProgressDialog.dialogDismiss()
                // Turn off loading, go to index
                requireActivity().actionBar?.show()
                Log.d("WebViewFragment", "Action: Turning off loading, going to index")
                loadFragment(ManageAnxietyFragment())
            }
        }
        private fun handleInitialLoadingOff(value: String) {
            if (value == "web page loaded with valid session") {
                // Dismiss the loading dialog
                customProgressDialog.dialogDismiss()
                requireActivity().actionBar?.show()
                Log.d("WebViewFragment", "Success Data: $value")
            }
        }
        private fun handleInvalidSession(value: String) {
            customProgressDialog.dialogDismiss()
            if (value.contains("in valid session")) {
                requireActivity().actionBar?.show()
                val alertDialog = AlertDialog.Builder(context)
                    .setTitle("Error Occurred")
                    .setMessage("Error occurred. Please try again!")
                    .setPositiveButton("OK", null)
                    .create()
                alertDialog.show()
                Log.d("WebViewFragment", "Error: $value")
            }
        }
        private fun handleChangedHeaderTitle(newTitle: String, titleView: TextView) {
            customProgressDialog.dialogDismiss()
            // Update the header title in the toolbar
            titleView.text = newTitle
            Log.d("WebViewFragment", "Updated header title to: $newTitle")
        }


        private fun handleHideHeader(toolbar: Toolbar) {
            toolbar.visibility = View.GONE
            Log.d("WebViewFragment", "Action: Hiding toolbar")
        }

        private fun handleShowHeader(toolbar: Toolbar) {
            toolbar.visibility = View.VISIBLE
            Log.d("WebViewFragment", "Action: Showing toolbar")
        }

        private fun handleNeedToTalkWithSomeone(value: String) {
            customProgressDialog.dialogDismiss()
            // Implement logic for handling the "need to talk with someone" case
            Log.d("WebViewFragment", "Action: Need to talk with someone, value: $value")
            // Add your action, e.g., open a contact dialog, navigate to a new fragment, etc.
        }

        private fun handleReturningBackFromFavMedia() {
            customProgressDialog.dialogDismiss()
            requireActivity().actionBar?.show()
            Log.d("WebViewFragment", "Action: Returning back from favorite media")
            loadFragment(HomeFragment())
        }


    }
}

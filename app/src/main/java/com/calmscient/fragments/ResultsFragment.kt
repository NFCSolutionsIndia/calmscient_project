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

import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.calmscient.R
import com.calmscient.databinding.LayoutResultsBinding
import com.calmscient.di.remote.response.ScreeningItem
import com.calmscient.di.remote.response.ScreeningResponse
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.LoginViewModel
import com.calmscient.viewmodels.ScreeningResultsViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale


data class DateTime(val date: String, val time: String)

@AndroidEntryPoint
class ResultsFragment() : Fragment() {
    lateinit var binding: LayoutResultsBinding
    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonDialog: CommonAPICallDialog
    private  var screeningResponseJson :String? = null
    private  var screeningResponse : ScreeningItem? = null
    private  lateinit var accessToken : String
    private val screeningResultsViewModel: ScreeningResultsViewModel by activityViewModels()

    companion object {
        const val SOURCE_SCREEN_KEY = "source_screen"
        const val SCREENINGS_FRAGMENT = 0
        const val YOUR_STRESS_FRAGMENT = 1
        // Add more constants if needed for other fragments
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this){
          /*  val sourceFragment = arguments?.getInt(SOURCE_SCREEN_KEY)
            when (sourceFragment) {
                SCREENINGS_FRAGMENT -> loadFragment(ScreeningsFragment())
                YOUR_STRESS_FRAGMENT -> loadFragment(YourStressTriggersQuizFragment())
                // Handle other fragments if needed
            }*/





            if (CommonClass.isNetworkAvailable(requireContext())) {
                loadFragment(ScreeningsFragment())
            } else {
                CommonClass.showInternetDialogue(requireContext())
            }
        }

        //Retrieve data from arguments

        screeningResponseJson = arguments?.getString("screeningResponse")
        Log.d("Result Fragment ", screeningResponseJson.toString())

        screeningResponseJson?.let { json ->
            val screeningItemList: List<ScreeningItem> = JsonUtil.fromJsonString(json)
            // Here you need to decide how to handle the list, whether to take the first item, iterate over it, etc.
            screeningResponse = screeningItemList.firstOrNull()

            Log.d("Result Fragment 123", "$screeningResponse")
            Log.d("ClientId", "${screeningResponse?.clientID}")
        }



    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        screeningResponse?.let { screeningItem ->
//            screeningResultsViewModel.getScreeningResultsData(
//                screeningItem.plid,
//                screeningItem.screeningID,
//                screeningItem.patientID,
//                screeningItem.clientID,
//                screeningItem.assessmentID
//            )
//        }


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LayoutResultsBinding.inflate(inflater, container, false)
        binding.resultsBackIcon.setOnClickListener {
            /*val sourceFragment = arguments?.getInt(SOURCE_SCREEN_KEY)
            when (sourceFragment) {
                SCREENINGS_FRAGMENT -> loadFragment(ScreeningsFragment())
                YOUR_STRESS_FRAGMENT -> loadFragment(YourStressTriggersQuizFragment())
                // Handle other fragments if needed
            }*/


            if (CommonClass.isNetworkAvailable(requireContext())) {
                loadFragment(ScreeningsFragment())
            } else {
                CommonClass.showInternetDialogue(requireContext())
            }


        }



        customProgressDialog = CustomProgressDialog(requireContext())

        commonDialog = CommonAPICallDialog(requireContext())


        binding.needToTalkWithSomeOneResults.setOnClickListener {

            if (CommonClass.isNetworkAvailable(requireContext())) {
                loadFragment(EmergencyResourceFragment())
            } else {
                CommonClass.showInternetDialogue(requireContext())
            }
        }
//        binding.torchResults.setOnClickListener {
//            val dialogFragment = ResultsinfoPopupFragment()
//            dialogFragment.show(requireActivity().supportFragmentManager, "ResultsInfoDialog")
//        }

        val sourceFragment = arguments?.getInt(SOURCE_SCREEN_KEY)
        if (sourceFragment == YOUR_STRESS_FRAGMENT) {
            // Display info icon
            binding.infoIcon.visibility = View.VISIBLE
            binding.torchResults.visibility = View.GONE
            binding.torchResults.setOnClickListener {
                // Add logic to handle info icon click
                // For example, show an info dialog
            }
        } else {
            // Display torch icon
            // binding.torchResults.setImageResource(R.drawable.ic_bulb_recognize)
            binding.torchResults.visibility = View.VISIBLE
            binding.infoIcon.visibility = View.GONE
            binding.torchResults.setOnClickListener {
                val dialogFragment = ResultsinfoPopupFragment()
                dialogFragment.show(requireActivity().supportFragmentManager, "ResultsInfoDialog")
            }
        }

        val description = arguments?.getString("title")
        description?.let {
            binding.titleTextView.text = it
        }
        //resultPercent()


        if (CommonClass.isNetworkAvailable(requireContext())) {
           observeViewModel()
        } else {
            CommonClass.showInternetDialogue(requireContext())
        }




        return binding.root
    }

    private fun loadFragment(fragment: Fragment) {

        val bundle = Bundle()
        bundle.putString("description", getString(R.string.work_related_stress_quiz))
        fragment.arguments = bundle

        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(com.calmscient.R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }


    private fun resultPercent(maxValue: Int, securedMarks: Int) {
       // binding.progressbarResult.max = maxValue
        val progressPercentage = (securedMarks.toFloat() / maxValue.toFloat() * 100).toInt()
        val progressAnimator = ValueAnimator.ofInt(0, progressPercentage)
        progressAnimator.addUpdateListener { valueAnimator ->
            binding.progressbarResult.progress = valueAnimator.animatedValue as Int
        }
        progressAnimator.interpolator = LinearInterpolator()
        progressAnimator.duration = 2000 // Adjust duration as needed for the desired animation speed
        progressAnimator.start()
    }


    /*private fun resultPercent() {
        binding.progressbarResult.setMax(100);
        binding.progressbarResult.setProgress(60);

        val progressBar: ProgressBar
        val textView: TextView
        var progressStatus = 0
        val handler = Handler()

        Thread {
            while (progressStatus < 60) {
                progressStatus += 1
                handler.post(Runnable {
                    binding.progressbarResult.progress = progressStatus
                })
                try {
                    Thread.sleep(25)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }.start()
    }*/

    private fun separateDateTime(dateTimeString: String): DateTime? {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = dateFormat.parse(dateTimeString)
            val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val dateString = dateFormatter.format(date) // Date in format "MM/dd/yyyy"
            val timeString = timeFormatter.format(date) // Time in format "HH:mm:ss"
            return DateTime(dateString, timeString)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun observeViewModel()
    {
        accessToken = SharedPreferencesUtil.getData(requireContext(), "accessToken", "")

        Log.d("Access Token ","$accessToken")
        screeningResultsViewModel.clear()

        screeningResponse?.let { screeningItem ->
            screeningResultsViewModel.getScreeningResultsData(
                screeningItem.plid,
                screeningItem.screeningID,
                screeningItem.patientID,
                screeningResponse!!.clientID,
                screeningItem.assessmentID,
                accessToken
            )
        }

//        screeningResponse?.let { screeningItem ->
//            screeningResultsViewModel.getScreeningResultsData(
//               1,
//               1,
//                1,
//                1,
//               1
//            )
//        }

        screeningResultsViewModel.loadingLiveData.observe(viewLifecycleOwner, Observer { isLoading->
            if(isLoading)
            {
                customProgressDialog.show("Loading...")

            }
            else
            {
                customProgressDialog.dialogDismiss()
            }
        })

        screeningResultsViewModel.successLiveData.observe(viewLifecycleOwner, Observer { isSuccess->
            if(isSuccess) {
                screeningResultsViewModel.saveResponseLiveData.observe(viewLifecycleOwner,Observer { successDate ->
                    if(successDate != null)
                    {
                        // Separate date and time
                        val screeningDateTime = successDate.screeningResults.screeningDate
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val date = dateFormat.parse(screeningDateTime)

                        // Format date and time separately
                        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

                        val dateString = dateFormatter.format(date) // Date in format "yyyy-MM-dd"
                        val timeString = timeFormatter.format(date) // Time in format "HH:mm:ss"


                        val separatedDateTime = separateDateTime(successDate.screeningResults.screeningDate)

                        binding.tvScoreValue.text = successDate.screeningResults.score.toString()
                        binding.tvTotalValue.text = successDate.screeningResults.total.toString()
                        if(separatedDateTime != null)
                        {
                            binding.resultsDate.text = separatedDateTime.date
                            binding.resultsTime.text = separatedDateTime.time
                        }

                        resultPercent(successDate.screeningResults.total,successDate.screeningResults.score)
                    }
                })
            }
        })
    }
}
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

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.R
import com.calmscient.adapters.ChangingYourResponseAdapter
import com.calmscient.databinding.FragmentChangingYourResponseBinding
import com.calmscient.di.remote.ChapterDataClass
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.di.remote.response.ManageAnxietyIndexResponse
import com.calmscient.di.remote.response.ManagingAnxiety
import com.calmscient.di.remote.response.SessionIdResponse
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.LocaleHelper
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SavePreferences
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.GetManageAnxietyIndexDataViewModel
import com.calmscient.viewmodels.GetSessionIdViewModel

class ChangingYourResponseFragment : Fragment() {
    private lateinit var savePrefData: SavePreferences
    private lateinit var binding : FragmentChangingYourResponseBinding

    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonAPICallDialog: CommonAPICallDialog
    private val getManageAnxietyIndexDataViewModel: GetManageAnxietyIndexDataViewModel by activityViewModels()
    private lateinit var manageAnxietyIndexResponse: ManageAnxietyIndexResponse
    private var loginResponse : LoginResponse? = null
    private  lateinit var accessToken : String


    private val getSessionIdViewModel: GetSessionIdViewModel by activityViewModels()
    private lateinit var sessionIdResponse: SessionIdResponse
    private  lateinit var sessionId : String
    private var languageCode = -1
    private lateinit var localeLang: LocaleHelper



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChangingYourResponseBinding.inflate(inflater, container, false)
        savePrefData = SavePreferences(requireContext())
        localeLang = LocaleHelper(requireContext())

        commonAPICallDialog = CommonAPICallDialog(requireContext())
        customProgressDialog = CustomProgressDialog(requireContext())

        accessToken = SharedPreferencesUtil.getData(requireContext(), "accessToken", "")
        val loginJsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString<LoginResponse>(loginJsonString)

        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        if (CommonClass.isNetworkAvailable(requireContext()))
        {
            manageAnxietyIndexDataAPICall()
            observeViewModel()
        }
        else
        {
            CommonClass.showInternetDialogue(requireContext())
        }

        if (CommonClass.isNetworkAvailable(requireContext()))
        {
            getSessionIdAPICall()
        }
        else
        {
            CommonClass.showInternetDialogue(requireContext())
        }

        binding.icGlossary.setOnClickListener {
            //startActivity(Intent(requireContext(), GlossaryActivity::class.java))
            loadFragment(GlossaryFragment())

        }
        binding.backIcon.setOnClickListener {
            //requireActivity().onBackPressed()
            loadFragment(DiscoveryFragment())
        }

        return binding.root
    }

    private fun manageAnxietyIndexDataAPICall()
    {
        clearRecyclerViewData()
        getManageAnxietyIndexDataViewModel.clear()

        loginResponse?.loginDetails?.let {
            getManageAnxietyIndexDataViewModel.getManageAnxietyIndexData(3,it.patientLocationID,it.patientID,it.clientID,accessToken)
        }

    }

    private fun observeViewModel()
    {
        getManageAnxietyIndexDataViewModel.clear()
        getManageAnxietyIndexDataViewModel.loadingLiveData.observe(viewLifecycleOwner, Observer { isLoading->
            if(isLoading)
            {
                customProgressDialog.show("Loading...")
            }
            else {
                Handler(Looper.getMainLooper()).postDelayed({
                    customProgressDialog.dialogDismiss()
                }, 6000) // Delay of 3 seconds
            }
        })

        getManageAnxietyIndexDataViewModel.successLiveData.observe(viewLifecycleOwner, Observer { isSuccess->
            if(isSuccess)
            {
                getManageAnxietyIndexDataViewModel.saveResponseLiveData.observe(viewLifecycleOwner,Observer { successData->
                    if(successData != null)
                    {
                        manageAnxietyIndexResponse = successData
                        clearRecyclerViewData()  // Clear existing RecyclerView data
                        getManageAnxietyIndexDataViewModel.clear()  // Clear ViewModel data

                        bindDataToRecyclerView(manageAnxietyIndexResponse.managingAnxiety)
                        bindUIData(manageAnxietyIndexResponse.managingAnxiety)
                        languageCode = successData.patientSessionDetails.languageId
                    }
                })
            }
        })
    }

    private fun bindUIData(managingAnxiety: List<ManagingAnxiety>)
    {

        if(managingAnxiety.size>=4)
        {
            binding.tvUnderstandingStressSigns.text = managingAnxiety[0].lessonName
            binding.tvCauseOfYourStress.text = managingAnxiety[1].lessonName
            binding.tvStressResponse.text = managingAnxiety[2].lessonName
            binding.tvResources.text = managingAnxiety[3].lessonName
        }

    }

    private fun bindDataToRecyclerView(managingAnxiety: List<ManagingAnxiety>) {
        managingAnxiety.forEach { lesson ->
            val chapterItems = lesson.chapters.map { chapter ->
                ChapterDataClass(
                    chapterId = chapter.chapterId,
                    chapterName = chapter.chapterName,
                    chapterUrl = chapter.chapterUrl,
                    isCourseCompleted = chapter.isCourseCompleted,
                    pageCount = chapter.pageCount,
                    imageUrl = chapter.imageUrl,
                    chapterOnlyReading = chapter.chapterOnlyReading
                )
            }
            val language = if(languageCode == 1) "en" else "sp"
            val itemClickListener: (ChapterDataClass) -> Unit = { chapter ->
                val url = "http://20.197.5.97:5000/?courseName=changingYourResponseToStress&lessonId=${lesson.lessonId}&chapterId=${chapter.chapterId}&sessionId=$sessionId&language=$language"
                Log.d("URL:","$url")
                if(CommonClass.isNetworkAvailable(requireContext())) {
                    chapter.chapterName?.let {
                        WebViewFragment.newInstance(
                            url,
                            it,
                            "ChangingResponse"
                        )
                    }
                        ?.let { loadFragment(it) }
                }else{
                    CommonClass.showInternetDialogue(requireContext())
                }
            }

            when {
                lesson.lessonName.equals("Understanding the stress signs", ignoreCase = true) ||
                        lesson.lessonName.equals("Comprender los signos de estrés", ignoreCase = true) ->
                    setupRecyclerView(binding.recyclerViewUnderstandingStressSigns, chapterItems, itemClickListener)

                lesson.lessonName.equals("Understanding the cause of your stress", ignoreCase = true) ||
                        lesson.lessonName.equals("Comprender la causa de su estrés", ignoreCase = true) ->
                    setupRecyclerView(binding.recyclerViewUnderstandingCauseOfYourStress, chapterItems, itemClickListener)

                lesson.lessonName.equals("Understanding your stress response", ignoreCase = true) ||
                        lesson.lessonName.equals("Comprender su respuesta al estrés", ignoreCase = true) ->
                    setupRecyclerView(binding.recyclerViewUnderstandingStressResponse, chapterItems, itemClickListener)

                lesson.lessonName.equals("Resources", ignoreCase = true) ||
                        lesson.lessonName.equals("Recursos", ignoreCase = true) ->
                    setupRecyclerView(binding.recyclerViewResources, chapterItems, itemClickListener)
            }

        }
    }

    private fun setupRecyclerView(
        recyclerView: RecyclerView,
        chapterItems: List<ChapterDataClass>,
        itemClickListener: (ChapterDataClass) -> Unit
    ) {
        val adapter = ChangingYourResponseAdapter(chapterItems, itemClickListener)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter
    }


    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun getSessionIdAPICall()
    {
        getSessionIdViewModel.getSessionId(accessToken)

        sessionIdObserveViewModel()

    }

    private fun sessionIdObserveViewModel()
    {
        getSessionIdViewModel.loadingLiveData.observe(viewLifecycleOwner, Observer { isLoading->
            if(isLoading)
            {
                customProgressDialog.show("Loading...")
            }
            else {
                Handler(Looper.getMainLooper()).postDelayed({
                    customProgressDialog.dialogDismiss()
                }, 6000) // Delay of 3 seconds
            }
        })

        getSessionIdViewModel.saveResponseLiveData.observe(viewLifecycleOwner, Observer { successData->
            if(successData != null)
            {
                sessionIdResponse = successData
                sessionId = sessionIdResponse.sessionId

                //Toast.makeText(requireContext(), sessionId,Toast.LENGTH_LONG).show()
            }
        })
    }


    private fun clearRecyclerViewData() {
        binding.recyclerViewUnderstandingStressSigns.adapter = null
        binding.recyclerViewUnderstandingCauseOfYourStress.adapter = null
        binding.recyclerViewUnderstandingStressResponse.adapter = null
        binding.recyclerViewResources.adapter = null
    }

    private fun updateLanguageSettings() {
        /* if (savePrefData.getEngLanguageState() == true) {
             localeLang.setLocale(requireContext(), "en")
         } else if (savePrefData.getAslLanguageState() == true) {
             localeLang.setLocale(requireContext(), "en")
         } else if (savePrefData.getSpanLanguageState() == true) {
             localeLang.setLocale(requireContext(), "es")
         }*/

        val res = savePrefData.getLanguageMode()
        if (res != null) {
            localeLang.setLocale(requireContext(), res)
            savePrefData.setLanguageMode(res)
        }

    }

    override fun onResume() {
        super.onResume()
        updateLanguageSettings()
    }

}

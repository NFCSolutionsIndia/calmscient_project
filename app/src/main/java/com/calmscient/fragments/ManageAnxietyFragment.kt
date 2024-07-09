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
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.R
import com.calmscient.adapters.AnxietyIntroductionAdapter
import com.calmscient.adapters.CardItemDiffCallback
import com.calmscient.adapters.ManageAnxietyChapterAdapter
import com.calmscient.di.remote.CardItemDataClass
import com.calmscient.di.remote.ItemType
import com.calmscient.databinding.ActivityManageAnxietyBinding
import com.calmscient.di.remote.ChapterDataClass
import com.calmscient.di.remote.response.Chapter
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.di.remote.response.ManageAnxietyIndexResponse
import com.calmscient.di.remote.response.ManagingAnxiety
import com.calmscient.di.remote.response.SessionIdResponse
import com.calmscient.di.remote.response.SummaryOfAUDITResponse
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SavePreferences
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.GetManageAnxietyIndexDataViewModel
import com.calmscient.viewmodels.GetSessionIdViewModel
import com.calmscient.viewmodels.GetSummaryOfAUDITViewModel
import com.github.mikephil.charting.charts.LineChart

class ManageAnxietyFragment : Fragment() {
    private lateinit var savePrefData: SavePreferences
    private lateinit var binding : ActivityManageAnxietyBinding

    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonAPICallDialog: CommonAPICallDialog
    private val getManageAnxietyIndexDataViewModel: GetManageAnxietyIndexDataViewModel by activityViewModels()
    private lateinit var manageAnxietyIndexResponse: ManageAnxietyIndexResponse
    private var loginResponse : LoginResponse? = null
    private  lateinit var accessToken : String


    private val getSessionIdViewModel: GetSessionIdViewModel by activityViewModels()
    private lateinit var sessionIdResponse: SessionIdResponse
    private  lateinit var sessionId : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this){
            loadFragment(BeginManageAnxietyFragment())
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityManageAnxietyBinding.inflate(inflater, container, false)
        savePrefData = SavePreferences(requireContext())


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
            loadFragment(BeginManageAnxietyFragment())
        }

        return binding.root
    }

    private fun manageAnxietyIndexDataAPICall()
    {
        clearRecyclerViewData()
        getManageAnxietyIndexDataViewModel.clear()

        loginResponse?.loginDetails?.let {
            getManageAnxietyIndexDataViewModel.getManageAnxietyIndexData(2,it.patientLocationID,it.patientID,it.clientID,accessToken)
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
                }, 7000) // Delay of 3 seconds
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
                        }
                    })
            }
        })
    }

    private fun bindUIData(managingAnxiety: List<ManagingAnxiety>)
    {

       if(managingAnxiety.size>=7)
       {
           binding.tvIntroduction.text = managingAnxiety[0].lessonName
           binding.tvLessonOne.text = managingAnxiety[1].lessonName
           binding.tvLessonTwo.text = managingAnxiety[2].lessonName
           binding.tvLessonThree.text = managingAnxiety[3].lessonName
           binding.tvLessonFour.text = managingAnxiety[4].lessonName
           binding.tvLessonFive.text = managingAnxiety[5].lessonName
           binding.tvLessonSix.text = managingAnxiety[6].lessonName
           binding.tvAdditionalResource.text = managingAnxiety[7].lessonName
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

            val itemClickListener: (ChapterDataClass) -> Unit = { chapter ->
                val url = "http://20.197.5.97:5000/?courseName=managingAnxiety&lessonId=${lesson.lessonId}&chapterId=${chapter.chapterId}&sessionId=$sessionId"
                Log.d("URL:","$url")
                chapter.chapterName?.let { WebViewFragment.newInstance(url, it) }
                    ?.let { loadFragment(it) }
            }

           /* when (lesson.lessonName) {
                "Introduction" -> setupRecyclerView(binding.recyclerViewIntroduction, chapterItems, itemClickListener)
                "Lesson 1" -> setupRecyclerView(binding.recyclerViewLesson1, chapterItems, itemClickListener)
                "Lesson 2" -> setupRecyclerView(binding.recyclerViewLesson2, chapterItems, itemClickListener)
                "Lesson 3" -> setupRecyclerView(binding.recyclerViewLesson3, chapterItems, itemClickListener)
                "Lesson 4" -> setupRecyclerView(binding.recyclerViewLesson4, chapterItems, itemClickListener)
                "Lesson 5" -> setupRecyclerView(binding.recyclerViewLesson5, chapterItems, itemClickListener)
                "Lesson 6" -> setupRecyclerView(binding.recyclerViewLesson6, chapterItems, itemClickListener)
                "Additional Resources" -> setupRecyclerView(binding.recyclerViewAdditionalResource, chapterItems, itemClickListener)
            }*/

            when {
                lesson.lessonName == "Introduction" || lesson.lessonName == "Introducción" -> setupRecyclerView(binding.recyclerViewIntroduction, chapterItems, itemClickListener)
                lesson.lessonName == "Lesson 1" || lesson.lessonName == "Lección 1" -> setupRecyclerView(binding.recyclerViewLesson1, chapterItems, itemClickListener)
                lesson.lessonName == "Lesson 2" || lesson.lessonName == "Lección 2" -> setupRecyclerView(binding.recyclerViewLesson2, chapterItems, itemClickListener)
                lesson.lessonName == "Lesson 3" || lesson.lessonName == "Lección 3" -> setupRecyclerView(binding.recyclerViewLesson3, chapterItems, itemClickListener)
                lesson.lessonName == "Lesson 4" || lesson.lessonName == "Lección 4" -> setupRecyclerView(binding.recyclerViewLesson4, chapterItems, itemClickListener)
                lesson.lessonName == "Lesson 5" || lesson.lessonName == "Lección 5" -> setupRecyclerView(binding.recyclerViewLesson5, chapterItems, itemClickListener)
                lesson.lessonName == "Lesson 6" || lesson.lessonName == "Lección 6" -> setupRecyclerView(binding.recyclerViewLesson6, chapterItems, itemClickListener)
                lesson.lessonName == "Additional Resources" || lesson.lessonName == "Recursos Adicionales" -> setupRecyclerView(binding.recyclerViewAdditionalResource, chapterItems, itemClickListener)
            }

        }
    }

    private fun setupRecyclerView(
        recyclerView: RecyclerView,
        chapterItems: List<ChapterDataClass>,
        itemClickListener: (ChapterDataClass) -> Unit
    ) {
        val adapter = ManageAnxietyChapterAdapter(chapterItems, itemClickListener)
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
                }, 7000) // Delay of 3 seconds
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
        binding.recyclerViewIntroduction.adapter = null
        binding.recyclerViewLesson1.adapter = null
        binding.recyclerViewLesson2.adapter = null
        binding.recyclerViewLesson3.adapter = null
        binding.recyclerViewLesson4.adapter = null
        binding.recyclerViewLesson5.adapter = null
        binding.recyclerViewLesson6.adapter = null
        binding.recyclerViewAdditionalResource.adapter = null
    }

}

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


import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.R
import com.calmscient.activities.SettingsActivity
import com.calmscient.adapters.AnxietyIntroductionAdapter
import com.calmscient.adapters.CardItemDiffCallback
import com.calmscient.adapters.ManageAnxietyChapterAdapter
import com.calmscient.databinding.FragmentHomeBinding
import com.calmscient.di.remote.ChapterDataClass
import com.calmscient.di.remote.response.FavoriteItem
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.di.remote.response.MenuItem
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.LocaleHelper
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SavePreferences
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.utils.network.ServerTimeoutHandler
import com.calmscient.viewmodels.LoginViewModel
import com.calmscient.viewmodels.MenuItemViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {
    lateinit var binding: FragmentHomeBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvProfileName: TextView
    private lateinit var introductionAdapter: AnxietyIntroductionAdapter
    private lateinit var profileImage: ImageView
    lateinit var savePrefData: SavePreferences
    private val loginViewModel: LoginViewModel by viewModels()
    @Inject
    lateinit var menuViewModel: MenuItemViewModel
    @Inject
    lateinit var myMedicalMenuViewModel: MenuItemViewModel
    private lateinit var myMedicalMenuResponseDate: List<MenuItem>
    private lateinit var menuResponseDate: List<MenuItem>
    private lateinit var favoriteItem: List<FavoriteItem>
    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonDialog: CommonAPICallDialog
    private lateinit var name :TextView
    private lateinit var mediaclRecords :TextView
    private lateinit var weeklySummary :TextView
    private lateinit var favorites :TextView

    private lateinit var localeLang: LocaleHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView = inflater.inflate(R.layout.fragment_home, container, false)
        binding = FragmentHomeBinding.inflate(inflater,container,false)

        recyclerView = rootView.findViewById(R.id.recyclerViewVideos)
        mediaclRecords=rootView.findViewById<TextView>(R.id.my_medical_records)
        favorites=rootView.findViewById<TextView>(R.id.favorites)
        weeklySummary=rootView.findViewById<TextView>(R.id.weekly_summary)
        name=rootView.findViewById<TextView>(R.id.name)
        customProgressDialog = CustomProgressDialog(requireContext())
        commonDialog = CommonAPICallDialog(requireContext())

        localeLang = LocaleHelper(requireContext())
        savePrefData = SavePreferences(requireContext())

        val jsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        val loginResponse = JsonUtil.fromJsonString<LoginResponse>(jsonString)

        Log.d("Login Response in HF","$loginResponse")

        if (CommonClass.isNetworkAvailable(requireContext())) {

            menuViewModel.fetchMenuItems(loginResponse.loginDetails.patientLocationID,0,loginResponse.loginDetails.patientID,loginResponse.loginDetails.clientID,loginResponse.token.access_token)
        } else {
            CommonClass.showInternetDialogue(requireContext())
        }

        menuViewModel.loadingLiveData.observe(requireActivity()) { isLoading ->
            if (isLoading) {
                commonDialog.dismiss()
                customProgressDialog.show("Loading...")
            } else {

                customProgressDialog.dialogDismiss()
            }
        }

        menuViewModel.resultLiveData.observe(requireActivity()){isSuccess ->
            if(isSuccess)
            {
                ServerTimeoutHandler.clearRetryListener()
                ServerTimeoutHandler.dismissDialog()
                menuResponseDate = menuViewModel.menuItemsLiveData.value!!
                favoriteItem = menuViewModel.favoritesLiveData.value!!

                val menuJsonString = JsonUtil.toJsonString(menuResponseDate)
                SharedPreferencesUtil.saveData(requireContext(), "menuResponse", menuJsonString)
                ServerTimeoutHandler.clearRetryListener()
                ServerTimeoutHandler.dismissDialog()

                val menuResponse = menuViewModel.menuResponse.value!!
                val languageName = menuResponse.languageName
                val languageMode = when {
                    languageName.equals("Spanish", ignoreCase = true) -> "es"
                    languageName.equals("English", ignoreCase = true) -> "en"
                    else -> ""
                }

                savePrefData.setLanguageMode(languageMode)
                localeLang.setLocale(requireContext(), languageMode)


                if (menuResponseDate.size >= 2) {

                    ServerTimeoutHandler.clearRetryListener()
                    ServerTimeoutHandler.dismissDialog()


                    name.text = loginResponse.loginDetails.firstName
                    mediaclRecords.text = menuResponseDate[0].menuName
                    weeklySummary.text = menuResponseDate[1].menuName
                    favorites.text = menuResponseDate[2].menuName
                }
                if(favoriteItem.isNotEmpty()){
                    bindDataToRecyclerView(favoriteItem)

                }
            }
            else{
                menuViewModel.failureLiveData.value?.let { failureMessage ->
                    failureMessage.let {
                        commonDialog.showDialog(
                            it,R.drawable.ic_failure
                        )
                    }
                }
                menuViewModel.errorLiveData.value?.let { failureMessage ->
                    failureMessage.let {
                        if (CommonClass.isNetworkAvailable(requireContext())) {
                            ServerTimeoutHandler.handleTimeoutException(requireContext()) {
                                // Retry logic when the retry button is clicked

                                menuViewModel.retryFetchMenuItems()
                            }
                        } else {
                            CommonClass.showInternetDialogue(requireContext())
                        }
                    }

                }
            }
        }


        tvProfileName = rootView.findViewById(R.id.tv_hello)

        // Initialize the introductionAdapter here
        introductionAdapter = AnxietyIntroductionAdapter(CardItemDiffCallback(),requireActivity().supportFragmentManager)

        // Initialize the recyclerView before using it
        recyclerView = rootView.findViewById(R.id.recyclerViewVideos)
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = introductionAdapter

        // Find the myMedicalRecordsLayout
        val myMedicalRecordsLayout = rootView.findViewById<View>(R.id.myMedicalRecordsLayout)
        profileImage = rootView.findViewById(R.id.img_profile1)
        profileImage.setOnClickListener {
            val intent = Intent(activity, SettingsActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_SETTINGS)
        }

        myMedicalRecordsLayout.setOnClickListener {

            if (CommonClass.isNetworkAvailable(requireContext())) {

                //menuItemRequest = MenuItemRequest(1,0,1,1)
                myMedicalMenuViewModel.fetchMenuItems(loginResponse.loginDetails.patientLocationID,menuResponseDate[0].menuId,loginResponse.loginDetails.patientID,loginResponse.loginDetails.clientID,loginResponse.token.access_token)
            } else {
                CommonClass.showInternetDialogue(requireContext())
            }

            myMedicalMenuViewModel.resultLiveData.observe(requireActivity()){isSuccess ->
                if(isSuccess)
                {
                    myMedicalMenuResponseDate = myMedicalMenuViewModel.menuItemsLiveData.value!!

                    Log.d("MyMedicalRecords123","$myMedicalMenuResponseDate")
                    val jsonString = JsonUtil.toJsonString(myMedicalMenuResponseDate)
                    SharedPreferencesUtil.saveData(requireContext(), "myMedicalMenuResponse", jsonString)

                    loadFragment(MedicalRecordsFragment())
                }
            }

        }
        val needToTalkWithSomeOneButton = rootView.findViewById<View>(R.id.needToTalkWithSomeOne)
        needToTalkWithSomeOneButton.setOnClickListener()
        {
            loadFragment(EmergencyResourceFragment())
        }
        val weeklySummaryLayout = rootView.findViewById<View>(R.id.weeklySummaryLayout)
        weeklySummaryLayout.setOnClickListener {
            if(CommonClass.isNetworkAvailable(requireContext()))
            {
                loadFragment(WeeklySummaryFragment())
            }
            else{
                CommonClass.showInternetDialogue(requireContext())
            }

        }
        return rootView
    }



    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun bindDataToRecyclerView(favoriteItem: List<FavoriteItem>) {
        val chapterItems = favoriteItem.map { lesson ->
            ChapterDataClass(
                chapterId = lesson.chapterId,
                chapterName = lesson.title,
                chapterUrl = lesson.navigateURL,
                isCourseCompleted = 0,
                pageCount = lesson.pageNo,
                imageUrl = lesson.thumbnailUrl,
                chapterOnlyReading = true,
                isFromExercises = lesson.isFromExercises
            )
        }

        val itemClickListener: (ChapterDataClass) -> Unit = { chapter ->
            val url = chapter.chapterUrl
            Log.d("URL:", "$url")
            Log.d("chapterName:", "${chapter.chapterName}")
            //Toast.makeText(requireContext(),"${chapter.chapterName}",Toast.LENGTH_LONG).show()
            if (url != null) {
                chapter.chapterName?.let { FavouritesWebViewFragment.newInstance(url, it) }
                    ?.let { loadFragment(it) }
            } else {
                when(chapter.chapterName){
                    getString(R.string.mindfulness_exercises) -> { loadFragment(MindfulnessExercisesFragment(1,chapter.pageCount)) }
                    getString(R.string.butterflyhug_exercises) -> { loadFragment(ButterflyHugExercisesFragment(1)) }
                    getString(R.string.handover_esxercises) -> { loadFragment(HandOverYourHeartFragment(1))}
                    getString(R.string.mindfulwalking_exercises) -> { loadFragment(MindfulWalkingExerciseFragment(1))}
                    getString(R.string.dance_exercises) -> { loadFragment(DancingExercisesFragment(1))}
                    getString(R.string.running_exercises) -> { loadFragment(RunningExerciseFragment(1))}
                    getString(R.string.body_moment_exercies) -> { loadFragment(BodyMovementExerciseFragment(1))}
                    getString(R.string.musclerelaxation_exercises) -> { loadFragment(MuscleRelaxationExerciseFragment(1))}
                    getString(R.string.breathing_technic) ,"Breathing technique"-> { loadFragment(DeepBreathingExerciseFragment(1,"Home"))}
                    getString(R.string._4_7_8_breathing_exercise), "4–7–8 Breathing exercise"-> {loadFragment(FourSevenEightBreathingExerciseFragment("Home"))}
                    getString(R.string.diaphragmatic_breathing_exercise_heading), "Diaphragmatic breathing exercise"-> {loadFragment(DiaphragmaticBreathingExerciseFragment("Home"))}
                    getString(R.string.mindful_breathing_exercise_heading), "Mindful breathing exercise"-> {loadFragment(MindfulBreathingExerciseFragment("Home"))}
                    getString(R.string.what_to_expect_when_you_quit_drinking)->{ loadFragment(WhatExpectsWhenYouQuitDrinkingFragment("Home")) }
                    getString(R.string.what_happens_to_your_brain_when_you_drink)->{ loadFragment(WhatHappensToYourBrainFragment("Home")) }
                }
            }
        }

        setupRecyclerView(recyclerView, chapterItems, itemClickListener)
    }

    companion object {
        fun newInstance() = HomeFragment()
        private const val REQUEST_CODE_SETTINGS = 1001
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

    fun onBackPressed() {
        showExitConfirmationDialog()
    }
    private fun showExitConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.plz_confirm))
        builder.setMessage(getString(R.string.exit_app))
        builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
            // User clicked "Yes," so exit the app
            requireActivity().finishAffinity() // This closes the entire app
        }
        builder.setNegativeButton(getString(R.string.no)) { _, _ ->
            // User clicked "No," so dismiss the dialog and stay on the current page
        }
        builder.setOnCancelListener(DialogInterface.OnCancelListener {
            // User canceled the dialog, do nothing
        })
        builder.show()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SETTINGS && resultCode == Activity.RESULT_OK) {
            // Reload the fragment or update the UI
            updateLanguageAndReloadFragment()
        }
    }

    private fun updateLanguageAndReloadFragment() {
        // Call the method to update the language settings
        updateLanguageSettings()
        // Reload the fragment
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, HomeFragment.newInstance())
        transaction.commit()
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

    override fun onDestroy() {
        super.onDestroy()
    }


}
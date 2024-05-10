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


import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.text.font.Typeface
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.ApiService
import com.calmscient.R
import com.calmscient.activities.SettingsActivity
import com.calmscient.activities.WeeklySummary
import com.calmscient.adapters.AnxietyIntroductionAdapter
import com.calmscient.adapters.CardItemDiffCallback
import com.calmscient.adapters.VideoAdapter
import com.calmscient.adapters.VideoItem
import com.calmscient.databinding.FragmentHomeBinding
import com.calmscient.databinding.LayoutLoginBinding
import com.calmscient.di.remote.CardItemDataClass
import com.calmscient.di.remote.ItemType
import com.calmscient.di.remote.request.MenuItemRequest
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.di.remote.response.MenuItem
import com.calmscient.di.remote.response.MenuItemsResponse
import com.calmscient.repository.LoginRepository
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SavePreferences
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.utils.network.ServerTimeoutHandler
import com.calmscient.viewmodels.LoginViewModel
import com.calmscient.viewmodels.MenuItemViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
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
    private lateinit var menuItemRequest: MenuItemRequest
    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonDialog: CommonAPICallDialog
    private lateinit var name :TextView
    private lateinit var mediaclRecords :TextView
    private lateinit var weeklySummary :TextView
    private lateinit var favorites :TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        mediaclRecords=rootView.findViewById<TextView>(R.id.my_medical_records)
        favorites=rootView.findViewById<TextView>(R.id.favorites)
        weeklySummary=rootView.findViewById<TextView>(R.id.weekly_summary)
        name=rootView.findViewById<TextView>(R.id.name)
        customProgressDialog = CustomProgressDialog(requireContext())

        commonDialog = CommonAPICallDialog(requireContext())

        val jsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        val loginResponse = JsonUtil.fromJsonString<LoginResponse>(jsonString)

        Log.d("Login Response in HF","$loginResponse")


        // menuViewModel.fetchMenuItems(1,1,1,1)
        if (CommonClass.isNetworkAvailable(requireContext())) {

            //menuItemRequest = MenuItemRequest(1,0,1,1)
            menuViewModel.fetchMenuItems(loginResponse.loginDetails.patientLocationID,0,loginResponse.loginDetails.patientID,loginResponse.loginDetails.clientID)
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

                val jsonString = JsonUtil.toJsonString(menuResponseDate)
                SharedPreferencesUtil.saveData(requireContext(), "menuResponse", jsonString)
                ServerTimeoutHandler.clearRetryListener()
                ServerTimeoutHandler.dismissDialog()


                if (menuResponseDate.size >= 2) {

                    ServerTimeoutHandler.clearRetryListener()
                    ServerTimeoutHandler.dismissDialog()


                    name.text = loginResponse.loginDetails.firstName
                    mediaclRecords.text = menuResponseDate[0].menuName
                    weeklySummary.text = menuResponseDate[1].menuName
                    favorites.text = menuResponseDate[2].menuName
                }
            }
            else{
                menuViewModel.failureLiveData.value?.let { failureMessage ->
                    failureMessage.let {
                        commonDialog.showDialog(
                            it
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


//        loginViewModel.responseData.observe(viewLifecycleOwner) { response ->
//            Log.d("HomeFragment", "Response Data received: $response")
//            response?.loginDetails?.clientID?.let { clientId ->
//                Log.d("Home Fragment Client ID", clientId.toString())
//            }
//        }



        //Log.d("HomeFragment","${loginResponse.loginDetails.patientLocationID}")
        /*   val videoItems = listOf(
               VideoItem("https://calmscient-videos.s3.ap-south-1.amazonaws.com/L1-1-Neuropsychology+of+Anxiety.mp4", R.drawable.thumbnail1),
               VideoItem("https://calmscient-videos.s3.ap-south-1.amazonaws.com/Lesson+1-2+Meet+Nora%2C+Austin+and+Melanie.wav", R.drawable.thumbnail2),
               VideoItem("https://calmscient-videos.s3.ap-south-1.amazonaws.com/Lesson+4-1+Implementing+body+calming+skills.mp4", R.drawable.thumbnail1),

               VideoItem("https://calmscient-videos.s3.ap-south-1.amazonaws.com/Lesson+6-3+North+wind+and+sun+with+music+English.wav", R.drawable.thumbnail1),
               VideoItem("https://calmscient-videos.s3.ap-south-1.amazonaws.com/Lesson+5-1+Anxiety+and+worry+with+music+English.wav", R.drawable.thumbnail2),
               VideoItem("https://calmscient-videos.s3.ap-south-1.amazonaws.com/Lesson+4-1+Implementing+body+calming+skills.mp4", R.drawable.thumbnail1),
               VideoItem("https://calmscient-videos.s3.ap-south-1.amazonaws.com/Lesson+6-3+North+wind+and+sun+with+music+English.wav",R.drawable.thumbnail1),
               VideoItem("https://calmscient-videos.s3.ap-south-1.amazonaws.com/Lesson+1-2+Meet+Nora%2C+Austin+and+Melanie.wav", R.drawable.thumbnail2),
               VideoItem("https://calmscient-videos.s3.ap-south-1.amazonaws.com/L1-1-Neuropsychology+of+Anxiety.mp4", R.drawable.thumbnail1),
               VideoItem("https://calmscient-videos.s3.ap-south-1.amazonaws.com/Lesson+6-3+North+wind+and+sun+with+music+English.wav",R.drawable.thumbnail1),
               VideoItem("https://calmscient-videos.s3.ap-south-1.amazonaws.com/Lesson+1-2+Meet+Nora%2C+Austin+and+Melanie.wav", R.drawable.thumbnail2),
               VideoItem("https://calmscient-videos.s3.ap-south-1.amazonaws.com/L1-1-Neuropsychology+of+Anxiety.mp4", R.drawable.thumbnail1),
               // Add more VideoItem objects as needed
           )
           val thumbnailResourceIds = listOf(
               R.drawable.thumbnail1,
               R.drawable.thumbnail2,
               // Add more thumbnail resource IDs as needed
           )*/
        tvProfileName = rootView.findViewById(R.id.tv_hello)
        /*val text = "Hello Kevin"
        val ss = SpannableString(text)
        val boldSpan = ResourcesCompat.getFont(requireContext(),R.font.lexendbold)
        ss.setSpan(boldSpan, 7, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvProfileName.setText(ss)*/
        // Initialize the introductionAdapter here
        introductionAdapter = AnxietyIntroductionAdapter(CardItemDiffCallback(),requireActivity().supportFragmentManager)

        // Initialize the recyclerView before using it
        recyclerView = rootView.findViewById(R.id.recyclerViewVideos)
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = introductionAdapter

        // Data for Additional Resource

        savePrefData = SavePreferences(requireContext())
        if(savePrefData.getAslLanguageState() == true) {
            val additionalResourceItems = cardItemsFavoritesASL()
            val additionalResourceRecyclerView: RecyclerView =
                rootView.findViewById(R.id.recyclerViewVideos)
            val additionalResourceAdapter = AnxietyIntroductionAdapter(CardItemDiffCallback(),requireActivity().supportFragmentManager)
            setupRecyclerView(
                additionalResourceRecyclerView, additionalResourceItems, additionalResourceAdapter
            )
        }else if(savePrefData.getSpanLanguageState() == true){
            val additionalResourceItems = cardItemsFavoritesSpanish()
            val additionalResourceRecyclerView: RecyclerView =
                rootView.findViewById(R.id.recyclerViewVideos)
            val additionalResourceAdapter = AnxietyIntroductionAdapter(CardItemDiffCallback(),requireActivity().supportFragmentManager)
            setupRecyclerView(
                additionalResourceRecyclerView, additionalResourceItems, additionalResourceAdapter
            )
        }
        else{
            val additionalResourceItems = cardItemsFavorites()
            val additionalResourceRecyclerView: RecyclerView =
                rootView.findViewById(R.id.recyclerViewVideos)
            val additionalResourceAdapter = AnxietyIntroductionAdapter(CardItemDiffCallback(),requireActivity().supportFragmentManager)
            setupRecyclerView(
                additionalResourceRecyclerView, additionalResourceItems, additionalResourceAdapter
            )
        }

        // Find the myMedicalRecordsLayout
        val myMedicalRecordsLayout = rootView.findViewById<View>(R.id.myMedicalRecordsLayout)
        profileImage = rootView.findViewById(R.id.img_profile1)
        profileImage.setOnClickListener {
            val intent = Intent(activity, SettingsActivity::class.java)
            startActivity(intent)
        }
        myMedicalRecordsLayout.setOnClickListener {

            if (CommonClass.isNetworkAvailable(requireContext())) {

                //menuItemRequest = MenuItemRequest(1,0,1,1)
                myMedicalMenuViewModel.fetchMenuItems(loginResponse.loginDetails.patientLocationID,menuResponseDate[0].menuId,loginResponse.loginDetails.patientID,loginResponse.loginDetails.clientID)
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


            /*val intent = Intent(activity, CalendarViewActivity::class.java)
            startActivity(intent)*/
        }
        val needToTalkWithSomeOneButton = rootView.findViewById<View>(R.id.needToTalkWithSomeOne)
        needToTalkWithSomeOneButton.setOnClickListener()
        {
            loadFragment(EmergencyResourceFragment())
        }
        val weeklySummaryLayout = rootView.findViewById<View>(R.id.weeklySummaryLayout)
        weeklySummaryLayout.setOnClickListener {
            //Toast.makeText(requireActivity(), "Coming Soon", Toast.LENGTH_LONG).show()
            //openWeeklySummaryActivity()
            loadFragment(WeeklySummaryFragment())
        }
        return rootView
    }



    private fun loadFragment(fragment: Fragment) {
        /* fragmentManager?.beginTransaction()?.apply {
             replace(R.id.flFragment, fragment)
                 .addToBackStack(null)
                 .commit()
         }*/
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun openWeeklySummaryActivity() {
        val intent = Intent(activity, WeeklySummary::class.java)
        startActivity(intent)
    }
    private fun cardItemsFavoritesSpanish(): List<CardItemDataClass> {
        val card1 = CardItemDataClass(
            availableContentTypes = listOf(ItemType.VIDEO),
            audioResourceId = null,
            videoResourceId = "https://calmscient-videos.s3.ap-south-1.amazonaws.com/L1-1-Neuropsychology+of+Anxiety+Spanish.mp4",
            contentIcons = listOf(R.drawable.lesson_1_1),
            description = getString(R.string.neuropsychology),
            isCompleted = false,
            heading = getString(R.string.the_neuropsychology),
            summary = getString(R.string.lesson1_video_summary),
            dialogText = getString(R.string.lesson1_video1_description)
        )

        val card2 = CardItemDataClass(
            availableContentTypes = listOf(ItemType.VIDEO),
            audioResourceId = null,
            videoResourceId = "https://calmscient-videos.s3.ap-south-1.amazonaws.com/4.1Implement+body+calming+skills+Spanish.mp4",
            contentIcons = listOf(R.drawable.lesson_4_1),
            description = getString(R.string.make_plan_card6_text2),
            isCompleted = false,
            heading = getString(R.string.make_plan_card6_text2),
            summary = null,
            dialogText = getString(R.string.lesson4_video1_description)
        )

        val card3 = CardItemDataClass(
            availableContentTypes = listOf(ItemType.AUDIO),
            audioResourceId = "https://calmscient-videos.s3.ap-south-1.amazonaws.com/Spanish+Lesson+1-2+Meet+Nora.mp3",
            videoResourceId = null,
            contentIcons = listOf(R.drawable.audio_lesson_1_2),
            description = getString(R.string.meet_nora_austin),
            isCompleted = false,
            heading = null,
            summary = getString(R.string.meet_nora_austrin_melanie_description),
            dialogText = null
        )

        val card4 = CardItemDataClass(
            availableContentTypes = listOf(ItemType.AUDIO),
            audioResourceId = "https://calmscient-videos.s3.ap-south-1.amazonaws.com/Lesson+3-3+Moral+deficiency+or+anxiety+with+music+Spanish.wav",
            videoResourceId = null,
            contentIcons = listOf(R.drawable.audio_lesson_1_2),
            description = getString(R.string.moral_deficiency),
            isCompleted = false,
            heading = null,
            summary = null,
            dialogText = null
        )

        val card5 = CardItemDataClass(
            availableContentTypes = listOf(ItemType.VIDEO),
            audioResourceId = null,
            videoResourceId = "https://calmscient-videos.s3.ap-south-1.amazonaws.com/Anxiety+and+exercise+Spanish.mp4",
            contentIcons = listOf(R.drawable.additional_1),
            description = getString(R.string.anxiety_exercise),
            isCompleted = false,
            heading = getString(R.string.fitness_tips),
            summary = getString(R.string.additional_anxiety_exercise_summary),
            dialogText = getString(R.string.additional_anxiety_exercise_dialogText)
        )

        // Add more CardItemDataClass instances as needed for lesson3
        return listOf(card1, card2, card3, card4, card5)
    }
    private fun cardItemsFavorites(): List<CardItemDataClass> {
        val card1 = CardItemDataClass(
            availableContentTypes = listOf(ItemType.VIDEO),
            audioResourceId = null,
            videoResourceId = "https://calmscient-videos.s3.ap-south-1.amazonaws.com/L1-1-Neuropsychology+of+Anxiety.mp4",
            contentIcons = listOf(R.drawable.lesson_1_1),
            description = getString(R.string.neuropsychology),
            isCompleted = false,
            heading = getString(R.string.the_neuropsychology),
            summary = getString(R.string.lesson1_video_summary),
            dialogText = getString(R.string.lesson1_video1_description)
        )

        val card2 = CardItemDataClass(
            availableContentTypes = listOf(ItemType.VIDEO),
            audioResourceId = null,
            videoResourceId = "https://calmscient-videos.s3.ap-south-1.amazonaws.com/Lesson+4-1+Implementing+body+calming+skills.mp4",
            contentIcons = listOf(R.drawable.lesson_4_1),
            description = getString(R.string.make_plan_card6_text2),
            isCompleted = false,
            heading = getString(R.string.make_plan_card6_text2),
            summary =  getString(R.string.lesson4_video_summary),
            dialogText = getString(R.string.lesson4_video1_description)
        )

        val card3 = CardItemDataClass(
            availableContentTypes = listOf(ItemType.AUDIO),
            audioResourceId = "https://calmscient-videos.s3.ap-south-1.amazonaws.com/Lesson+1-2+Meet+Nora%2C+Austin+and+Melanie.wav",
            videoResourceId = null,
            contentIcons = listOf(R.drawable.audio_lesson_1_2),
            description = getString(R.string.meet_nora_austin),
            isCompleted = false,
            heading = null,
            summary = null,
            dialogText = null
        )

        val card4 = CardItemDataClass(
            availableContentTypes = listOf(ItemType.AUDIO),
            audioResourceId = "https://calmscient-videos.s3.ap-south-1.amazonaws.com/Lesson+3+Moral+deficiency+or+anxiety+with+music+English.wav",
            videoResourceId = null,
            contentIcons = listOf(R.drawable.audio_lesson_1_2),
            description = getString(R.string.moral_deficiency),
            isCompleted = false,
            heading = null,
            summary = null,
            dialogText = null
        )

        val card5 = CardItemDataClass(
            availableContentTypes = listOf(ItemType.VIDEO),
            audioResourceId = null,
            videoResourceId = "https://calmscient-videos.s3.ap-south-1.amazonaws.com/1+Anxiety+and+exercise.mp4",
            contentIcons = listOf(R.drawable.additional_1),
            description = getString(R.string.anxiety_exercise),
            isCompleted = false,
            heading = getString(R.string.fitness_tips),
            summary = getString(R.string.additional_anxiety_exercise_summary),
            dialogText = getString(R.string.additional_anxiety_exercise_dialogText)
        )

        // Add more CardItemDataClass instances as needed for lesson3
        return listOf(card1, card2, card3, card4, card5)
    }

    private fun cardItemsFavoritesASL(): List<CardItemDataClass> {
        val card1 = CardItemDataClass(
            availableContentTypes = listOf(ItemType.VIDEO),
            audioResourceId = null,
            videoResourceId = "https://calmscient-videos.s3.ap-south-1.amazonaws.com/The+Neuropsychology+of+Anxiety(ASL).m4v",
            contentIcons = listOf(R.drawable.lesson_1_1),
            description = getString(R.string.neuropsychology),
            isCompleted = false,
            heading = getString(R.string.the_neuropsychology),
            summary = getString(R.string.lesson1_video_summary),
            dialogText = getString(R.string.lesson1_video1_description)
        )

        val card2 = CardItemDataClass(
            availableContentTypes = listOf(ItemType.VIDEO),
            audioResourceId = null,
            videoResourceId = "https://calmscient-videos.s3.ap-south-1.amazonaws.com/Lesson+4-1+Implementing+body+calming+skills.mp4",
            contentIcons = listOf(R.drawable.lesson_4_1),
            description = getString(R.string.make_plan_card6_text2),
            isCompleted = false,
            heading = getString(R.string.make_plan_card6_text2),
            summary = null,
            dialogText = getString(R.string.lesson4_video1_description)
        )

        val card3 = CardItemDataClass(
            availableContentTypes = listOf(ItemType.AUDIO),
            audioResourceId = "https://calmscient-videos.s3.ap-south-1.amazonaws.com/Lesson+1-2+Meet+Nora%2C+Austin+and+Melanie.wav",
            videoResourceId = null,
            contentIcons = listOf(R.drawable.audio_lesson_1_2),
            description = getString(R.string.meet_nora_austin),
            isCompleted = false,
            heading = null,
            summary = null,
            dialogText = null
        )

        val card4 = CardItemDataClass(
            availableContentTypes = listOf(ItemType.AUDIO),
            audioResourceId = "https://calmscient-videos.s3.ap-south-1.amazonaws.com/Lesson+3+Moral+deficiency+or+anxiety+with+music+English.wav",
            videoResourceId = null,
            contentIcons = listOf(R.drawable.audio_lesson_1_2),
            description = getString(R.string.moral_deficiency),
            isCompleted = false,
            heading = null,
            summary = null,
            dialogText = null
        )

        val card5 = CardItemDataClass(
            availableContentTypes = listOf(ItemType.VIDEO),
            audioResourceId = null,
            videoResourceId = "https://calmscient-videos.s3.ap-south-1.amazonaws.com/1+Anxiety+and+exercise.mp4",
            contentIcons = listOf(R.drawable.additional_1),
            description = getString(R.string.anxiety_exercise),
            isCompleted = false,
            heading = getString(R.string.fitness_tips),
            summary = getString(R.string.additional_anxiety_exercise_summary),
            dialogText = getString(R.string.additional_anxiety_exercise_dialogText)
        )

        // Add more CardItemDataClass instances as needed for lesson3
        return listOf(card1, card2, card3, card4, card5)
    }
    companion object {
        fun newInstance() = HomeFragment()
    }

    private fun setupRecyclerView(
        recyclerView: RecyclerView,
        cardItems: List<CardItemDataClass>,
        adapter: AnxietyIntroductionAdapter
    ) {
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter
        adapter.submitList(cardItems)
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
}
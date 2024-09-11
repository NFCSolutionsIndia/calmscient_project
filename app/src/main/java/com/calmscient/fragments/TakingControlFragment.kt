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
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.calmscient.R
import com.calmscient.databinding.FragmentTakingControlBinding
import com.calmscient.di.remote.response.CourseLists
import com.calmscient.di.remote.response.DrinkTrackerResponse
import com.calmscient.di.remote.response.GetTakingControlIndexResponse
import com.calmscient.di.remote.response.GetTakingControlIntroductionResponse
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.GetDrinkTrackerViewModel
import com.calmscient.viewmodels.GetTakingControlIndexViewModel
import com.calmscient.viewmodels.GetTakingControlIntroductionViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TakingControlFragment : Fragment() {

    private lateinit var binding: FragmentTakingControlBinding
    private var alcoholLayoutVisible = true

    private val getTakingControlIndexViewModel : GetTakingControlIndexViewModel by activityViewModels()
    private lateinit var commonAPICallDialog: CommonAPICallDialog
    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var getTakingControlIndexResponse: GetTakingControlIndexResponse
    private var loginResponse : LoginResponse? = null
    private  lateinit var accessToken : String
    private  var courseIdBasicKnowledge : Int = 0
    private  var courseIdMakeAPlan : Int = 0
    private  var courseIdSummary : Int = 0
    private  var courseIdDrinkTracker : Int = 0
    private  var courseIdEventTracker : Int = 0
    private  var courseIdSeeTheIntro : Int = 0
    private  var courseIdHowToUse : Int = 0
    private var flag : Int = -1


    private val getDrinkTrackerViewModel: GetDrinkTrackerViewModel by activityViewModels()
    private lateinit var drinkTrackerResponse: DrinkTrackerResponse

    private val getTakingControlIntroductionViewModel: GetTakingControlIntroductionViewModel by activityViewModels()
    private lateinit var getTakingControlIntroductionResponse: GetTakingControlIntroductionResponse



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this){
            loadFragment(DiscoveryFragment())
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTakingControlBinding.inflate(inflater, container, false)

        accessToken = SharedPreferencesUtil.getData(requireContext(), "accessToken", "")
        customProgressDialog = CustomProgressDialog(requireContext())
        commonAPICallDialog = CommonAPICallDialog(requireContext())
        val loginJsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString<LoginResponse>(loginJsonString)

        if (CommonClass.isNetworkAvailable(requireContext())) {
            performApiCalls()
        } else {
            CommonClass.showInternetDialogue(requireContext())
        }


        binding.btnBasicKnowledge.setOnClickListener{
            if(CommonClass.isNetworkAvailable(requireContext())) {

                val res = SharedPreferencesUtil.getData(requireContext(), "courseIdBasicKnowledge", "")
                val basicKnowledgeFragment = BasicKnowledgeFragment.newInstanceBasicKnowledge(res.toInt())
                loadFragment(basicKnowledgeFragment)
            }
            else{
                CommonClass.showInternetDialogue(requireContext())
            }
        }
        binding.btnMakeAPlan.setOnClickListener{
            if(CommonClass.isNetworkAvailable(requireContext())){

                SharedPreferencesUtil.saveData(requireContext(),"courseIdMakeAPlan",courseIdMakeAPlan.toString())
                loadFragment(TakingControlMakeAPlanScreenOneFragment())
            }
            else{
                CommonClass.showInternetDialogue(requireContext())
            }
        }
        binding.btnSummary.setOnClickListener {
            if(CommonClass.isNetworkAvailable(requireContext())) {
                val summaryTakingControlFragment = SummaryTakingControlFragment.newInstance(courseIdSummary)
                loadFragment(summaryTakingControlFragment)
            }
            else{
                CommonClass.showInternetDialogue(requireContext())
            }
        }
        binding.btnDrinkTracker.setOnClickListener {
            if(CommonClass.isNetworkAvailable(requireContext())) {
                val drinkTrackerFragment = DrinkTrackerFragment.newInstance(courseIdDrinkTracker)
                loadFragment(drinkTrackerFragment)
            }
            else{
                CommonClass.showInternetDialogue(requireContext())
            }
        }
        binding.btnEventTracker.setOnClickListener {
            if(CommonClass.isNetworkAvailable(requireContext())) {
                val eventsTrackerFragment = EventsTrackerFragment.newInstance(courseIdEventTracker)
                loadFragment(eventsTrackerFragment)
            }
            else{
                CommonClass.showInternetDialogue(requireContext())
            }
        }
        binding.alcoholtext.setOnClickListener {
            binding.alcohollayout.visibility = View.VISIBLE
            binding.substancelayouttext.visibility = View.GONE
            updateViewBackgroundColor(binding.viewalcohol, R.color.example_7_button)
            updateViewBackgroundColor(binding.viewsubstance, R.color.viewbackgroundcolor)
        }
        binding.substancetext.setOnClickListener {
            binding.alcohollayout.visibility = View.GONE
            alcoholLayoutVisible = false
            binding.substancelayouttext.visibility = View.VISIBLE
            updateViewBackgroundColor(binding.viewalcohol, R.color.viewbackgroundcolor)
            updateViewBackgroundColor(binding.viewsubstance, R.color.example_7_button)

        }
        binding.needToTalkWithSomeOne.setOnClickListener {
            loadFragment(EmergencyResourceFragment())
        }
        binding.backIcon.setOnClickListener{
            loadFragment(DiscoveryFragment())
        }

        binding.btnSeeTheInformation.setOnClickListener{
            if(CommonClass.isNetworkAvailable(requireContext())) {
                val introductionFragment = TakingControlIntroductionFragment.newInstance(
                    getTakingControlIndexResponse,
                    courseIdSeeTheIntro
                )
                loadFragment(introductionFragment)
            }
            else{
                CommonClass.showInternetDialogue(requireContext())
            }
        }
        return binding.root
    }

    private fun performApiCalls() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Launch all API calls in parallel
                val deferreds = listOf(
                    async { apiCall() },
                    async { drinkTrackerApiCall() }
                )

                // Wait for all API calls to complete
                deferreds.awaitAll()

            } catch (e: Exception) {
                // Handle exceptions
                e.printStackTrace()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


    private fun updateViewBackgroundColor(view: View, colorResId: Int) {
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), colorResId))
    }
    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun loadFragmentWithDelay(fragment: Fragment) {
        customProgressDialog.dialogDismiss()
        Handler(Looper.getMainLooper()).postDelayed({
            loadFragment(fragment)
        }, 2000)
    }

    private suspend fun apiCall()
    {
        loginResponse?.loginDetails?.let { getTakingControlIndexViewModel.getTakingControlIndex(it.clientID,it.patientID,it.patientLocationID,accessToken) }

       // getTakingControlIndexViewModel.getTakingControlIndex(1,1,1,accessToken)
        observeViewModel()

    }

    private fun getCurrentDate(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        return currentDate.format(formatter)
    }

    private suspend fun drinkTrackerApiCall() {

        getDrinkTrackerViewModel.clear()

        val date = getCurrentDate()
        loginResponse?.loginDetails?.let {
            getDrinkTrackerViewModel.getDrinkTackerData(
                date,
                it.clientID,
                it.patientID,
                it.patientLocationID,
                accessToken
            )
        }

        drinkTrackerObserveViewModel()
    }
    private fun drinkTrackerObserveViewModel() {

        getDrinkTrackerViewModel.loadingLiveData.observe(viewLifecycleOwner, Observer { isLoading->
            if(isLoading)
            {
                customProgressDialog.show(getString(R.string.loading))
            }
            else{
                customProgressDialog.dialogDismiss()
            }
        })

        getDrinkTrackerViewModel.successLiveData.observe(viewLifecycleOwner, Observer { isSuccess ->
            if (isSuccess) {
                getDrinkTrackerViewModel.saveResponseLiveData.observe(
                    viewLifecycleOwner,
                    Observer { successData ->
                        if (successData != null) {
                            drinkTrackerResponse = successData

                            val jsonString = JsonUtil.toJsonString(drinkTrackerResponse)
                            SharedPreferencesUtil.saveData(requireContext(), "drinkTrackerData", jsonString)
                        }
                    }
                )
            }
        })
    }

    private fun observeViewModel()
    {

        getTakingControlIndexViewModel.loadingLiveData.observe(viewLifecycleOwner, Observer { isLoading->
            if(isLoading)
            {
                customProgressDialog.show("Loading...")
            }
            else
            {
                customProgressDialog.dialogDismiss()
            }
        })

        getTakingControlIndexViewModel.successLiveData.observe(viewLifecycleOwner, Observer { isSuccess->
            if(isSuccess)
            {
                customProgressDialog.dialogDismiss()

                getTakingControlIndexViewModel.saveResponseLiveData.observe(viewLifecycleOwner,
                    Observer { successData->
                        if(successData != null)
                        {
                            getTakingControlIndexResponse = successData

                            Log.d("TCI","$getTakingControlIndexResponse")
                            bindUIData(getTakingControlIndexResponse)

                        }
                    })
            }
        })
    }

    private fun bindUIData(getTakingControlIndexResponse: GetTakingControlIndexResponse) {
        // Check if index list is not null and has at least 2 elements


            getTakingControlIndexResponse.index[0]?.let {
                binding.tvDrinkCount.text = it.goalType ?: "N/A"
                binding.tvDrinkCountGoalDays.text = it.goal?.toString() ?: "0"
                binding.tvDrinkCountNowDays.text = it.now?.toString() ?: "0"
            }

        if (getTakingControlIndexResponse.index.size > 1) {


            getTakingControlIndexResponse.index[1]?.let {
                binding.tvAlcoholFreeDays.text = it.goalType ?: "N/A"
                binding.tvAlcoholGoalDays.text = it.goal?.toString() ?: "0"
                binding.tvAlcoholNowDays.text = it.now?.toString() ?: "0"
            }
        }

        // Check if courseLists is not null and has at least 7 elements
       /* if (getTakingControlIndexResponse.courseLists.size > 6) {
            binding.btnSeeTheInformation.text = getTakingControlIndexResponse.courseLists[0].courseName
            binding.btnHowToUse.text = getTakingControlIndexResponse.courseLists[1].courseName
            binding.btnBasicKnowledge.text = getTakingControlIndexResponse.courseLists[2].courseName
            binding.btnMakeAPlan.text = getTakingControlIndexResponse.courseLists[3].courseName
            binding.btnSummary.text = getTakingControlIndexResponse.courseLists[4].courseName
            binding.btnDrinkTracker.text = getTakingControlIndexResponse.courseLists[5].courseName
            binding.btnEventTracker.text = getTakingControlIndexResponse.courseLists[6].courseName
        }*/

        updateButtons(getTakingControlIndexResponse.courseLists)
    }

   /* private fun updateButtons(courseLists: List<CourseLists>) {
        for (course in courseLists) {
            when (course.courseName) {
                "Basic knowledge" -> binding.btnBasicKnowledge.isEnabled = course.isEnable == 1
                "Make a plan" -> binding.btnMakeAPlan.isEnabled = course.isEnable == 1
                "Summary" -> binding.btnSummary.isEnabled = course.isEnable == 1
                "Drink tracker" -> binding.btnDrinkTracker.isEnabled = course.isEnable == 1
                "Event tracker" -> binding.btnEventTracker.isEnabled = course.isEnable == 1
                "See the introduction" -> binding.btnSeeTheInformation.isEnabled = course.isEnable == 1
                "How to use" -> binding.btnHowToUse.isEnabled = course.isEnable == 1
            }
        }
    }*/

    private fun updateButtons(courseLists: List<CourseLists>) {
        for (course in courseLists) {
            when (course.courseName.trim()) {
                getString(R.string.basic_knowledge) -> {
                    binding.btnBasicKnowledge.isEnabled = course.isEnable == 1
                    binding.btnBasicKnowledge.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_background_enabled)
                    setButtonDrawable(binding.btnBasicKnowledge, course.isCompleted == 1)
                    courseIdBasicKnowledge = course.courseId
                    SharedPreferencesUtil.clearData(requireContext(),"courseIdBasicKnowledge")
                    SharedPreferencesUtil.saveData(requireContext(),"courseIdBasicKnowledge",courseIdBasicKnowledge.toString())

                }
                getString(R.string.make_a_plan)-> {
                    binding.btnMakeAPlan.isEnabled = course.isEnable == 1
                    binding.btnMakeAPlan.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_background_enabled)
                    setButtonDrawable(binding.btnMakeAPlan, course.isCompleted == 1)
                    courseIdMakeAPlan = course.courseId
                }
                getString(R.string.summary) -> {
                    binding.btnSummary.isEnabled = course.isEnable == 1
                    binding.btnSummary.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_background_enabled)
                    setButtonDrawable(binding.btnSummary, course.isCompleted == 1)
                    courseIdSummary = course.courseId
                }
                getString(R.string.drink_tracker)-> {
                    binding.btnDrinkTracker.isEnabled = course.isEnable == 1
                    binding.btnDrinkTracker.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_background_enabled)
                    setButtonDrawable(binding.btnDrinkTracker, course.isCompleted == 1)
                    courseIdDrinkTracker = course.courseId
                }
                getString(R.string.event_tracker) -> {
                    binding.btnEventTracker.isEnabled = course.isEnable == 1
                    binding.btnEventTracker.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_background_enabled)
                    setButtonDrawable(binding.btnEventTracker, course.isCompleted == 1)
                    courseIdEventTracker = course.courseId
                }
                getString(R.string.see_the_introduction) ,getString(R.string.introduction)-> {
                    binding.btnSeeTheInformation.isEnabled = course.isEnable == 1
                    binding.btnSeeTheInformation.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_background_enabled)
                    setButtonDrawable(binding.btnSeeTheInformation, course.isCompleted == 1)
                    courseIdSeeTheIntro = course.courseId
                    flag = course.skipTutorialFlag

                    Log.d("Flag","$flag")
                    if(flag == 0)
                    {
                        val introductionFragment = TakingControlIntroductionFragment.newInstance(
                            getTakingControlIndexResponse,
                            courseIdSeeTheIntro
                        )
                        loadFragmentWithDelay(introductionFragment)
                    }
                }
                getString(R.string.how_to_use) -> {
                    binding.btnHowToUse.isEnabled = course.isEnable == 1
                    binding.btnHowToUse.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_background_enabled)
                    setButtonDrawable(binding.btnHowToUse, course.isCompleted == 1)
                    courseIdHowToUse = course.courseId
                }
            }
        }
    }

    private fun setButtonDrawable(button: AppCompatButton, isCompleted: Boolean) {
        val drawable = if (isCompleted) {
            ContextCompat.getDrawable(requireContext(), R.drawable.basic_knowledge_complete_icon)
        } else {
            null
        }
        button.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
    }
}
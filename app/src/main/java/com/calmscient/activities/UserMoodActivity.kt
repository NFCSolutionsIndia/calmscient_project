package com.calmscient.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.calmscient.R
import com.calmscient.databinding.FragmentUserMoodBinding
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.fragments.HomeFragment
import com.calmscient.repository.LoginRepository
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.viewmodels.LoginViewModel
import com.calmscient.viewmodels.MenuItemViewModel
import java.util.Calendar
import java.util.Date
import androidx.lifecycle.Observer
import com.calmscient.ApiService
import com.calmscient.AppController
import com.calmscient.databinding.FragmentQuestionBinding
import com.calmscient.di.remote.response.PatientMoodResponse
import com.calmscient.retrofit.ApplicationModule
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.utils.network.ServerTimeoutHandler
import com.calmscient.viewmodels.GetPatientMoodViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.OkHttpClient
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class UserMoodActivity : AppCompat(), View.OnClickListener {
    lateinit var binding: FragmentUserMoodBinding
    @Inject
    lateinit var menuItemsViewModel: MenuItemViewModel
    @Inject
    lateinit var apiService: ApiService
     private val loginViewModel: LoginViewModel by viewModels()
     private val getPatientMoodViewModel: GetPatientMoodViewModel by viewModels()
     private lateinit var loginResponse: LoginResponse
     private lateinit var patientMoodResponse: PatientMoodResponse

    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonDialog: CommonAPICallDialog




    private var isImage1Visible = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentUserMoodBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        customProgressDialog = CustomProgressDialog(this)
        commonDialog = CommonAPICallDialog(this)


       val res =  loginViewModel.responseData.value
        Log.d("UserMoodActivity","$res")

        val jsonString = SharedPreferencesUtil.getData(this, "loginResponse", "")
         loginResponse = JsonUtil.fromJsonString<LoginResponse>(jsonString)

        Log.d("Login Response in USERMOOD","$loginResponse")

        if(CommonClass.isNetworkAvailable(this)){
           // observeViewModel()
        }
        else
        {
            CommonClass.showInternetDialogue(this)
        }

        greeting()
        /*binding.imgMBad.setOnClickListener {
            if(isImage1Visible){
                binding.imgMBad.setImageResource(com.calmscient.R.drawable.icon_excellent)
            }
            // Update the flag to keep track of the currently visible image
            isImage1Visible = !isImage1Visible
        }*/


        //morning Clicks
        binding.imgMBad.setOnClickListener(this);
        binding.imgMBetter.setOnClickListener(this);
        binding.imgMGood.setOnClickListener(this);
        binding.imgMFair.setOnClickListener(this);
        binding.imgMExcellent.setOnClickListener(this)
        //morning sleep
        binding.sleepLess.setOnClickListener(this)
        binding.sleep4.setOnClickListener(this)
        binding.sleep5.setOnClickListener(this)
        binding.sleep6.setOnClickListener(this)
        binding.sleep7.setOnClickListener(this)
        binding.sleep8.setOnClickListener(this)
        binding.sleep9.setOnClickListener(this)
        binding.sleep10.setOnClickListener(this)
        binding.sleepMore.setOnClickListener(this)
        //afternoon clicks
        binding.imgEveBad.setOnClickListener(this);
        binding.imgEveBetter.setOnClickListener(this);
        binding.imgEveGood.setOnClickListener(this);
        binding.imgEveFair.setOnClickListener(this);
        binding.imgEveExcellent.setOnClickListener(this);
        //evening clicks
        binding.imgNigBad.setOnClickListener(this);
        binding.imgNigBetter.setOnClickListener(this);
        binding.imgNigFair.setOnClickListener(this);
        binding.imgNigGood.setOnClickListener(this);
        binding.imgNigExcellent.setOnClickListener(this)
        binding.imgFamily.setOnClickListener(this)
        binding.imgFriends.setOnClickListener(this)
        binding.imgWorkmates.setOnClickListener(this)
        binding.imgOthers.setOnClickListener(this)
        binding.imgAlone.setOnClickListener(this)
        //button click
        binding.btnSave.setOnClickListener(this)
        binding.btnSkip.setOnClickListener(this)

        //API calling
        //menuItemsViewModel.fetchMenuItems(plid, parentId, patientId, clientId)


    }
   /* override fun onStart() {
        super.onStart()
        binding.idWishes.text = patientMoodResponse.wish
        binding.tvMeds.text = patientMoodResponse.medicineData?.medicineQuestion
    }*/

    fun greeting() {
        val date = Date()
        val cal = Calendar.getInstance()
        cal.time = date
        val hour: Int = cal.get(Calendar.HOUR_OF_DAY)
        val ampm: Int = cal.get(Calendar.AM_PM)
        Log.d("ampm",""+ampm)
        var greeting: String? = null
        if (hour in 0..11) {
            greeting = getString(R.string.good_morning)
        } else if (hour in 12..16) {
            //greeting = getString(R.string.good_afternoon)
            greeting = getString(R.string.good_afternoon)
        } else if (hour in 17..23) {
            //greeting = getString(R.string.good_evening)
            greeting = getString(R.string.good_evening)
        } /*else if (hour in 21..23) {
                greeting = "Good Night";
            } else {
                greeting = getString(R.string.good_morning)
            }*/
        binding.idWishes.setText(greeting)
        if (greeting == getString(R.string.good_morning)) {
            binding.cardMorniMood.visibility = View.VISIBLE
            binding.mornHoursSleepCard.visibility = View.VISIBLE
            binding.idMornMeds.visibility = View.VISIBLE
            binding.tvMeds.text = getString(R.string.take_medic_morning)
            binding.idSwitch.labelOn = getString(R.string.yes)
            binding.idSwitch.labelOff = getString(R.string.no)
            binding.layoutButton.visibility = View.VISIBLE
            //binding.cardDailyJournel.visibility = View.VISIBLE
        } else if (greeting == getString(R.string.good_afternoon)) {
            binding.cardAfternoon.visibility = View.VISIBLE
            binding.layoutButton.visibility = View.VISIBLE
        } else if (greeting == getString(R.string.good_evening)) {
            binding.cardEveDay.visibility = View.VISIBLE
            binding.spendTimeCard.visibility = View.VISIBLE
            binding.idMornMeds.visibility = View.VISIBLE
            binding.cardDailyJournel.visibility = View.VISIBLE
            binding.tvMeds.text = getString(R.string.take_medic_evening)
            binding.idSwitch.labelOn = getString(R.string.yes)
            binding.idSwitch.labelOff = getString(R.string.no)
            binding.layoutButton.visibility = View.VISIBLE
        }
    }


    override fun onClick(v: View?) {
        binding.imgMBad.setImageResource(R.drawable.icon_bad)
        binding.imgMBetter.setImageResource(R.drawable.icon_better)
        binding.imgMFair.setImageResource(R.drawable.icon_fair)
        binding.imgMGood.setImageResource(R.drawable.icon_good)
        binding.imgMExcellent.setImageResource(R.drawable.icon_excellent)

        when (v?.id) {
            // Handle morning images click events
            R.id.img_mBad -> {
                // Handle image click
                binding.imgMBad.elevation = 20.0F
                binding.imgMBetter.setElevation(0.0F)
                binding.imgMFair.setElevation(0.0F)
                binding.imgMGood.setElevation(0.0F)
                binding.imgMExcellent.setElevation(0.0F)
                binding.tvBad.setTextColor(ContextCompat.getColor(this,R.color.bad_selected))
                binding.tvBetter.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvFair.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvGood.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvXcellent.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
            }
            // Handle other morning images click events similarly

            R.id.img_mBetter -> {
                //binding.imgMBetter.setImageResource(R.drawable.icon_better)
                binding.imgMBetter.setElevation(20.0F)
                binding.imgMBad.setElevation(0.0F)
                binding.imgMFair.setElevation(0.0F)
                binding.imgMGood.setElevation(0.0F)
                binding.imgMExcellent.setElevation(0.0F)
                binding.tvBetter.setTextColor(ContextCompat.getColor(this,R.color.better_selected))
                binding.tvBad.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvFair.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvGood.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvXcellent.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
            }

            R.id.img_mFair -> {
                //binding.imgMFair.setImageResource(R.drawable.icon_excellent)
                binding.imgMFair.setElevation(20.0F)
                binding.imgMBetter.setElevation(0.0F)
                binding.imgMBad.setElevation(0.0F)
                binding.imgMGood.setElevation(0.0F)
                binding.imgMExcellent.setElevation(0.0F)
                binding.tvFair.setTextColor(ContextCompat.getColor(this,R.color.fair_selected))
                binding.tvBad.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvBetter.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvGood.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvXcellent.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
            }

            R.id.img_mGood -> {
                //binding.imgMGood.setImageResource(R.drawable.icon_excellent)
                binding.imgMGood.setElevation(20.0F)
                binding.imgMBetter.setElevation(0.0F)
                binding.imgMBad.setElevation(0.0F)
                binding.imgMFair.setElevation(0.0F)
                binding.imgMExcellent.setElevation(0.0F)
                binding.tvGood.setTextColor(ContextCompat.getColor(this,R.color.good_selected))
                binding.tvBad.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvBetter.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvFair.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvXcellent.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
            }

            R.id.img_mExcellent -> {
                //binding.imgMExcellent.setImageResource(R.drawable.icon_excellent)
                binding.imgMExcellent.setElevation(20.0F)
                binding.imgMGood.setElevation(0.0F)
                binding.imgMBetter.setElevation(0.0F)
                binding.imgMBad.setElevation(0.0F)
                binding.imgMFair.setElevation(0.0F)
                binding.tvXcellent.setTextColor(ContextCompat.getColor(this,R.color.excellent_selected))
                binding.tvBad.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvBetter.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvFair.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvGood.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
            }

            //afternoon click
            R.id.img_eve_bad -> {
                //binding.imgMBad.setImageResource(R.drawable.icon_excellent)
                //binding.imgMBad.setImageResource(R.drawable.icon_excellent)
                //binding.imgMBad.setBackgroundResource(R.drawable.drawable_circular_border)
                binding.imgEveBad.setElevation(20.0F)
                binding.imgEveBetter.setElevation(0.0F)
                binding.imgEveFair.setElevation(0.0F)
                binding.imgEveExcellent.setElevation(0.0F)
                binding.imgEveGood.setElevation(0.0F)
                binding.tvAfterBad.setTextColor(ContextCompat.getColor(this,R.color.bad_selected))
                binding.tvAfterBetter.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvAfterFair.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvAfterGood.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvAfterXcellent.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
            }

            R.id.img_eve_better -> {
                //binding.imgMBetter.setImageResource(R.drawable.icon_better)
                binding.imgEveBetter.setElevation(20.0F)
                binding.imgEveFair.setElevation(0.0F)
                binding.imgEveExcellent.setElevation(0.0F)
                binding.imgEveGood.setElevation(0.0F)
                binding.imgEveBad.setElevation(0.0F)
                binding.tvAfterBetter.setTextColor(ContextCompat.getColor(this,R.color.better_selected))
                binding.tvAfterBad.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvAfterFair.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvAfterGood.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvAfterXcellent.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
            }

            R.id.img_eve_fair -> {
                //binding.imgMFair.setImageResource(R.drawable.icon_excellent)
                binding.imgEveFair.setElevation(20.0F)
                binding.imgEveExcellent.setElevation(0.0F)
                binding.imgEveGood.setElevation(0.0F)
                binding.imgEveBetter.setElevation(0.0F)
                binding.imgEveBad.setElevation(0.0F)
                binding.tvAfterFair.setTextColor(ContextCompat.getColor(this,R.color.fair_selected))
                binding.tvAfterBetter.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvAfterBad.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvAfterGood.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvAfterXcellent.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
            }

            R.id.img_eve_good -> {
                //binding.imgMGood.setImageResource(R.drawable.icon_excellent)
                binding.imgEveGood.setElevation(20.0F)
                binding.imgEveExcellent.setElevation(0.0F)
                binding.imgEveBetter.setElevation(0.0F)
                binding.imgEveBad.setElevation(0.0F)
                binding.imgEveFair.setElevation(0.0F)
                binding.tvAfterGood.setTextColor(ContextCompat.getColor(this,R.color.good_selected))
                binding.tvAfterBetter.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvAfterBad.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvAfterFair.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvAfterXcellent.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
            }

            R.id.img_eve_excellent -> {
                //binding.imgMExcellent.setImageResource(R.drawable.icon_excellent)
                binding.imgEveExcellent.setElevation(20.0F)
                binding.imgEveGood.setElevation(0.0F)
                binding.imgEveBetter.setElevation(0.0F)
                binding.imgEveBad.setElevation(0.0F)
                binding.imgEveFair.setElevation(0.0F)
                binding.tvAfterXcellent.setTextColor(ContextCompat.getColor(this,R.color.excellent_selected))
                binding.tvAfterGood.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvAfterBetter.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvAfterBad.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvAfterFair.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
            }
            //morning sleep
            R.id.sleep_less -> {
                binding.sleepLess.setImageResource(R.drawable.less_icon)
                binding.sleep4.setImageResource(R.drawable.sleep_4)
                binding.sleep5.setImageResource(R.drawable.sleep_5)
                binding.sleep6.setImageResource(R.drawable.sleep_6)
                binding.sleep7.setImageResource(R.drawable.sleep_7)
                binding.sleep8.setImageResource(R.drawable.sleep_8)
                binding.sleep9.setImageResource(R.drawable.sleep_9)
                binding.sleep10.setImageResource(R.drawable.sleep_10)
                binding.sleepMore.setImageResource(R.drawable.sleep_more)
                binding.tvHrsSlept.text = "Less than 4 Hours"

            }

            R.id.sleep_4 -> {
                binding.sleep4.setImageResource(R.drawable.selected_4)
                binding.sleepLess.setImageResource(R.drawable.less)
                binding.sleep5.setImageResource(R.drawable.sleep_5)
                binding.sleep6.setImageResource(R.drawable.sleep_6)
                binding.sleep7.setImageResource(R.drawable.sleep_7)
                binding.sleep8.setImageResource(R.drawable.sleep_8)
                binding.sleep9.setImageResource(R.drawable.sleep_9)
                binding.sleep10.setImageResource(R.drawable.sleep_10)
                binding.sleepMore.setImageResource(R.drawable.sleep_more)
                binding.tvHrsSlept.text = "4 Hours"

            }

            R.id.sleep_5 -> {
                binding.sleep5.setImageResource(R.drawable.selected_5)
                binding.sleepLess.setImageResource(R.drawable.less)
                binding.sleep4.setImageResource(R.drawable.sleep_4)
                binding.sleep6.setImageResource(R.drawable.sleep_6)
                binding.sleep7.setImageResource(R.drawable.sleep_7)
                binding.sleep8.setImageResource(R.drawable.sleep_8)
                binding.sleep9.setImageResource(R.drawable.sleep_9)
                binding.sleep10.setImageResource(R.drawable.sleep_10)
                binding.sleepMore.setImageResource(R.drawable.sleep_more)
                binding.tvHrsSlept.text = "5 Hours"
            }

            R.id.sleep_6 -> {
                binding.sleep6.setImageResource(R.drawable.selected_6)
                binding.sleepLess.setImageResource(R.drawable.less)
                binding.sleep4.setImageResource(R.drawable.sleep_4)
                binding.sleep5.setImageResource(R.drawable.sleep_5)
                binding.sleep7.setImageResource(R.drawable.sleep_7)
                binding.sleep8.setImageResource(R.drawable.sleep_8)
                binding.sleep9.setImageResource(R.drawable.sleep_9)
                binding.sleep10.setImageResource(R.drawable.sleep_10)
                binding.sleepMore.setImageResource(R.drawable.sleep_more)
                binding.tvHrsSlept.text = "6 Hours"
            }

            R.id.sleep_7 -> {
                binding.sleep7.setImageResource(R.drawable.selected_7)
                binding.sleepLess.setImageResource(R.drawable.less)
                binding.sleep4.setImageResource(R.drawable.sleep_4)
                binding.sleep5.setImageResource(R.drawable.sleep_5)
                binding.sleep6.setImageResource(R.drawable.sleep_6)
                binding.sleep8.setImageResource(R.drawable.sleep_8)
                binding.sleep9.setImageResource(R.drawable.sleep_9)
                binding.sleep10.setImageResource(R.drawable.sleep_10)
                binding.sleepMore.setImageResource(R.drawable.sleep_more)
                binding.tvHrsSlept.text = "7 Hours"
            }

            R.id.sleep_8 -> {
                binding.sleep8.setImageResource(R.drawable.selected_8)
                binding.sleepLess.setImageResource(R.drawable.less)
                binding.sleep4.setImageResource(R.drawable.sleep_4)
                binding.sleep5.setImageResource(R.drawable.sleep_5)
                binding.sleep6.setImageResource(R.drawable.sleep_6)
                binding.sleep7.setImageResource(R.drawable.sleep_7)
                binding.sleep9.setImageResource(R.drawable.sleep_9)
                binding.sleep10.setImageResource(R.drawable.sleep_10)
                binding.sleepMore.setImageResource(R.drawable.sleep_more)
                binding.tvHrsSlept.text = "8 Hours"
            }

            R.id.sleep_9 -> {
                binding.sleep9.setImageResource(R.drawable.selected_9)
                binding.sleepLess.setImageResource(R.drawable.less)
                binding.sleep4.setImageResource(R.drawable.sleep_4)
                binding.sleep5.setImageResource(R.drawable.sleep_5)
                binding.sleep6.setImageResource(R.drawable.sleep_6)
                binding.sleep7.setImageResource(R.drawable.sleep_7)
                binding.sleep8.setImageResource(R.drawable.sleep_8)
                binding.sleep10.setImageResource(R.drawable.sleep_10)
                binding.sleepMore.setImageResource(R.drawable.sleep_more)
                binding.tvHrsSlept.text = "9 Hours"
            }

            R.id.sleep_10 -> {
                binding.sleep10.setImageResource(R.drawable.selected_10)
                binding.sleepLess.setImageResource(R.drawable.less)
                binding.sleep4.setImageResource(R.drawable.sleep_4)
                binding.sleep5.setImageResource(R.drawable.sleep_5)
                binding.sleep6.setImageResource(R.drawable.sleep_6)
                binding.sleep7.setImageResource(R.drawable.sleep_7)
                binding.sleep8.setImageResource(R.drawable.sleep_8)
                binding.sleep9.setImageResource(R.drawable.sleep_9)
                binding.sleepMore.setImageResource(R.drawable.sleep_more)
                binding.tvHrsSlept.text = "10 Hours"
            }

            R.id.sleep_more -> {
                binding.sleepMore.setImageResource(R.drawable.more_selected)
                binding.sleepLess.setImageResource(R.drawable.less)
                binding.sleep4.setImageResource(R.drawable.sleep_4)
                binding.sleep5.setImageResource(R.drawable.sleep_5)
                binding.sleep6.setImageResource(R.drawable.sleep_6)
                binding.sleep7.setImageResource(R.drawable.sleep_7)
                binding.sleep8.setImageResource(R.drawable.sleep_8)
                binding.sleep9.setImageResource(R.drawable.sleep_9)
                binding.sleep10.setImageResource(R.drawable.sleep_10)
                binding.tvHrsSlept.text = "More than 10 Hours"
            }
            //evening images
            R.id.img_nig_bad -> {
                //binding.imgMBad.setImageResource(R.drawable.icon_excellent)
                //binding.imgMBad.setImageResource(R.drawable.icon_excellent)
                //binding.imgMBad.setBackgroundResource(R.drawable.drawable_circular_border)
                binding.imgNigBad.setElevation(20.0F)
                binding.imgNigBetter.setElevation(0.0F)
                binding.imgNigFair.setElevation(0.0F)
                binding.imgNigGood.setElevation(0.0F)
                binding.imgNigExcellent.setElevation(0.0F)
                binding.tvEveBad.setTextColor(ContextCompat.getColor(this,R.color.bad_selected))
                binding.tvEveBetter.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvEveFair.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvEveGood.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvEveXcellent.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
            }

            R.id.img_nig_better -> {
                //binding.imgMBetter.setImageResource(R.drawable.ic_better_selected)
                binding.imgNigBetter.setElevation(20.0F)
                binding.imgNigFair.setElevation(0.0F)
                binding.imgNigGood.setElevation(0.0F)
                binding.imgNigExcellent.setElevation(0.0F)
                binding.imgNigBad.setElevation(0.0F)
                binding.tvEveBetter.setTextColor(ContextCompat.getColor(this,R.color.better_selected))
                binding.tvEveBad.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvEveFair.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvEveGood.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvEveXcellent.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
            }

            R.id.img_nig_fair -> {
                //binding.imgMFair.setImageResource(R.drawable.icon_excellent)
                binding.imgNigFair.setElevation(20.0F)
                binding.imgNigGood.setElevation(0.0F)
                binding.imgNigExcellent.setElevation(0.0F)
                binding.imgNigBad.setElevation(0.0F)
                binding.imgNigBetter.setElevation(0.0F)
                binding.tvEveFair.setTextColor(ContextCompat.getColor(this,R.color.fair_selected))
                binding.tvEveBad.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvEveBetter.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvEveGood.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvEveXcellent.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
            }

            R.id.img_nig_good -> {
                //binding.imgMGood.setImageResource(R.drawable.icon_excellent)
                binding.imgNigGood.setElevation(20.0F)
                binding.imgNigExcellent.setElevation(0.0F)
                binding.imgNigBad.setElevation(0.0F)
                binding.imgNigBetter.setElevation(0.0F)
                binding.imgNigFair.setElevation(0.0F)
                binding.tvEveGood.setTextColor(ContextCompat.getColor(this,R.color.good_selected))
                binding.tvEveBad.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvEveBetter.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvEveFair.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvEveXcellent.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
            }

            R.id.img_nig_excellent -> {
                //binding.imgMExcellent.setImageResource(R.drawable.icon_excellent)
                binding.imgNigExcellent.setElevation(20.0F)
                binding.imgNigBad.setElevation(0.0F)
                binding.imgNigBetter.setElevation(0.0F)
                binding.imgNigFair.setElevation(0.0F)
                binding.imgNigGood.setElevation(0.0F)
                binding.tvEveXcellent.setTextColor(ContextCompat.getColor(this,R.color.excellent_selected))
                binding.tvEveBad.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvEveBetter.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvEveFair.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
                binding.tvEveGood.setTextColor(ContextCompat.getColor(this,R.color.grey_light))
            }

            R.id.img_family -> {
                binding.imgFamily.setElevation(20.0F)
                binding.imgFamily.setImageResource(R.drawable.family_selected)
                binding.imgFriends.setImageResource(R.drawable.friends)
                binding.imgWorkmates.setImageResource(R.drawable.workmates)
                binding.imgOthers.setImageResource(R.drawable.others)
                binding.imgAlone.setImageResource(R.drawable.alone)
            }

            R.id.img_friends -> {
                binding.imgFriends.setElevation(20.0F)
                binding.imgFriends.setImageResource(R.drawable.friends_selected)
                binding.imgFamily.setImageResource(R.drawable.family)
                binding.imgWorkmates.setImageResource(R.drawable.workmates)
                binding.imgOthers.setImageResource(R.drawable.others)
                binding.imgAlone.setImageResource(R.drawable.alone)
            }

            R.id.img_workmates -> {
                binding.imgWorkmates.setElevation(20.0F)
                binding.imgWorkmates.setImageResource(R.drawable.workmates_selected)
                binding.imgFamily.setImageResource(R.drawable.family)
                binding.imgFriends.setImageResource(R.drawable.friends)
                binding.imgOthers.setImageResource(R.drawable.others)
                binding.imgAlone.setImageResource(R.drawable.alone)
            }
            R.id.img_others -> {
                binding.imgOthers.setElevation(20.0F)
                binding.imgOthers.setImageResource(R.drawable.others_selected)
                binding.imgWorkmates.setImageResource(R.drawable.workmates)
                binding.imgFamily.setImageResource(R.drawable.family)
                binding.imgFriends.setImageResource(R.drawable.friends)
                binding.imgAlone.setImageResource(R.drawable.alone)
            }
            R.id.img_alone -> {
                binding.imgAlone.setElevation(20.0F)
                binding.imgAlone.setImageResource(R.drawable.alone_selected)
                binding.imgWorkmates.setImageResource(R.drawable.workmates)
                binding.imgFamily.setImageResource(R.drawable.family)
                binding.imgFriends.setImageResource(R.drawable.friends)
                binding.imgOthers.setImageResource(R.drawable.others)
            }
            R.id.btn_save -> {
                //loadFragment(HomeFragment())
                startActivity(Intent(this, DashboardActivity::class.java))
            }
            R.id.btn_skip -> {
                //loadFragment(HomeFragment())
                startActivity(Intent(this, DashboardActivity::class.java))
            }
        }
    }
    private fun loadFragment(fragment: Fragment) {
        val homeFragment = HomeFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.flFragment, homeFragment).addToBackStack(null)
            .commit()
    }

    private fun getCurrentDateTime(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        return currentDateTime.format(formatter)
    }

    private fun observeViewModel()
    {
        val currentDatetime = getCurrentDateTime()
        getPatientMoodViewModel.getPatientMood(loginResponse.loginDetails.patientLocationID,loginResponse.loginDetails.clientID,loginResponse.loginDetails.patientID,currentDatetime)

        getPatientMoodViewModel.loadingLiveData.observe(this, Observer { isLoading->
            if(isLoading)
            {
                customProgressDialog.dialogDismiss()
                customProgressDialog.show("Loading...")
            }
            else
            {
                customProgressDialog.dialogDismiss()
            }
        })

        getPatientMoodViewModel.successLiveData.observe(this,Observer{isSuccess->
            if(isSuccess)
            {
                ServerTimeoutHandler.clearRetryListener()
                ServerTimeoutHandler.dismissDialog()

                getPatientMoodViewModel.saveResponseLiveData.observe(this, Observer { succesData->
                    if(succesData != null)
                    {
                        patientMoodResponse = succesData

                        bindUIData(patientMoodResponse)




                   }
                })



            }
        })

        getPatientMoodViewModel.errorLiveData.observe(this, Observer { errorData->
            if(errorData!= null)
            {

                if (CommonClass.isNetworkAvailable(this)) {
                    ServerTimeoutHandler.handleTimeoutException(this) {
                        // Retry logic when the retry button is clicked

                        getPatientMoodViewModel.retryGetPatientMood()
                    }
                } else {
                    CommonClass.showInternetDialogue(this)
                }
            }
        })

    }

    private fun bindUIData(successData : PatientMoodResponse)
    {
        binding.idWishes.text = successData.wish
        binding.tvMeds.text = successData.medicineData?.medicineQuestion

        binding.idAfterMood.text = successData.moodData.moodQuestion
        binding.idEvenMood.text = successData.moodData.moodQuestion
        binding.idMornMood.text  = successData.moodData.moodQuestion

        successData.moodData.options?.let { options ->
            if (options.size >= 5) {
                binding.tvAfterBad.text = options[0].optionType
                binding.tvAfterBetter.text = options[1].optionType
                binding.tvAfterFair.text = options[2].optionType
                binding.tvAfterGood.text = options[3].optionType
                binding.tvAfterXcellent.text = options[4].optionType
            }
        }
    }
}
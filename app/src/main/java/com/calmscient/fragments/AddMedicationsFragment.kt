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
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.calmscient.Interface.OnAlarmSelectedListener
import com.calmscient.R
import com.calmscient.databinding.FragmentAddMedicationsBinding
import com.calmscient.di.remote.request.AddMedicationDetailsRequest
import com.calmscient.di.remote.request.Alarm
import com.calmscient.di.remote.request.AlarmInternal
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.di.remote.response.MedicalDetails
import com.calmscient.receivers.AlarmReceiver
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.utils.network.ServerTimeoutHandler
import com.calmscient.viewmodels.AddMedicationViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale


class AddMedicationsFragment : Fragment(), OnAlarmSelectedListener {

    private lateinit var binding: FragmentAddMedicationsBinding
    private var isKeyboardVisible = false
    private var isMorningAlarmOn = false
    private var isEveningAlarmOn = false
    private var morningTime: String = ""
    private var morningAlarm: String = ""
    private var eveningTime: String = ""
    private var eveningAlarm: String = ""
    private var selectedSchedule: String = ""

    private var medicationName: String = ""
    private var startDate: String = ""
    private var endDate: String = ""
    private var dosage: String = ""
    private var direction: String = ""
    private var withMeal: Int = 0
    private var quantity: Int = 0
    private var isActive: Int = 0
    var selectedCard : String =""
    private val alarms: MutableList<Alarm> = mutableListOf()
    private val alarmInternal: MutableList<AlarmInternal> = mutableListOf()
    private var alarm: Alarm? = null

    private var loginResponse : LoginResponse? = null
    private val addMedicationViewModel : AddMedicationViewModel by activityViewModels()
    private lateinit var medicalDetails : MedicalDetails


    private var selectedDate: LocalDate? = null
    private lateinit var formattedDate: String


    private lateinit var commonDialog: CommonAPICallDialog
    private lateinit var customProgressDialog: CustomProgressDialog


    private val onDialogDismissed: () -> Unit = {
        loadFragment(CalendarFragment())
    }


    override fun onAlarmSelected(alarm: AlarmInternal) {
        Log.d("AddMedicationsFragment", "Selected Alarm: $alarm")
        // Check if an alarm of the same type already exists
        val existingAlarmIndex = alarmInternal.indexOfFirst { it.scheduleType == alarm.scheduleType }
        if (existingAlarmIndex != -1) {
            // Update the existing alarm
            alarmInternal[existingAlarmIndex] = alarm
        } else {
            // Add the new alarm
            alarmInternal.add(alarm)

        }
        updateAlarmTimings()
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            loadFragment(CalendarFragment())
        }

        commonDialog = CommonAPICallDialog(requireContext())

        arguments?.let {
            selectedDate = it.getSerializable("selectedDate") as? LocalDate
        }

        formattedDate = selectedDate?.let { formatDate(it) }.toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddMedicationsBinding.inflate(inflater, container, false)
        //going back to Medications Fragment
        binding.addMedicationsDetailsBackIcon.setOnClickListener()
        {

            if(CommonClass.isNetworkAvailable(requireContext()))
            {
                loadFragment(CalendarFragment())
            }
            else
            {
                CommonClass.showInternetDialogue(requireContext())
            }
        }

        customProgressDialog = CustomProgressDialog(requireContext())

        val jsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
         loginResponse = JsonUtil.fromJsonString<LoginResponse>(jsonString)


        Log.d("selectedDate in AMF","$selectedDate")
        Log.d("formattedDate in AMF","$formattedDate")

        //For with meal
        binding.idSwitch.labelOn = getString(R.string.yes)
        binding.idSwitch.labelOff = getString(R.string.no)

        //For Morning Alarm
        binding.alarmToggleButtonMorning.labelOn = getString(R.string.yes)
        binding.alarmToggleButtonMorning.labelOff = getString(R.string.no)

        //For Evening Alarm
        binding.alarmToggleButtonEvening.labelOn = getString(R.string.yes)
        binding.alarmToggleButtonEvening.labelOff = getString(R.string.no)
        //afternoon alarm
        binding.alarmToggleButtonAfternoon.labelOn = getString(R.string.yes)
        binding.alarmToggleButtonAfternoon.labelOff = getString(R.string.no)

        binding.provider.text = Editable.Factory.getInstance().newEditable(loginResponse!!.loginDetails.providerName)


        /*binding.morningCalendar.setOnClickListener {
            showMorningTimeAndAlarmDialog()
        }

        binding.eveningCalendar.setOnClickListener {
            showEveningTimeAndAlarmDialog()
        }*/
        binding.btnAddCancel.setOnClickListener {
            loadFragment(CalendarFragment())
        }
        binding.morningAlarmCard.setOnClickListener {
            selectedCard = getString(R.string.morning)
            val bottomSheetFragment = BottomSheetFragment(selectedCard)
            bottomSheetFragment.setOnAlarmSelectedListener(this)
            bottomSheetFragment.show(requireActivity().supportFragmentManager, bottomSheetFragment.tag)

        }
        binding.afternoonAlarmCard.setOnClickListener {
            selectedCard = getString(R.string.afternoon)
            val bottomSheetFragment = BottomSheetFragment(selectedCard)
            bottomSheetFragment.setOnAlarmSelectedListener(this)
            bottomSheetFragment.show(requireActivity().supportFragmentManager, bottomSheetFragment.tag)

        }
        binding.eveningAlarmCard.setOnClickListener {
            selectedCard = getString(R.string.evening)
            val bottomSheetFragment = BottomSheetFragment(selectedCard)
            bottomSheetFragment.setOnAlarmSelectedListener(this)
            bottomSheetFragment.show(requireActivity().supportFragmentManager, bottomSheetFragment.tag)

        }


        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomNavView =
            requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        view.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.height
            val keypadHeight = screenHeight - rect.bottom
            isKeyboardVisible = keypadHeight > screenHeight * 0.15
            updateBottomNavVisibility(bottomNavView)
        }

        // Set a touch listener to the parent layout
        binding.parentLayout.setOnTouchListener { _, _ ->
            clearFocusAndHideKeyboard(bottomNavView)
            false
        }


        // Listening to focus change of the EditText
        setBottomNavVisibilityOnFocusChange(binding.medicationName, bottomNavView)
        setBottomNavVisibilityOnFocusChange(binding.provider, bottomNavView)
        setBottomNavVisibilityOnFocusChange(binding.dosage, bottomNavView)
        setBottomNavVisibilityOnFocusChange(binding.direction, bottomNavView)
        binding.alarmToggleButtonMorning.setOnToggledListener { _, isOn ->
            isMorningAlarmOn = isOn
            if (isOn) {
                // Toggle is on, schedule the morning alarm if time is selected
                if (!morningTime.isNullOrEmpty()) {
                    scheduleAlarm(morningTime, "Morning")
                }
            } else {
                // Toggle is off, cancel the morning alarm
                cancelAlarm("Morning")
            }
        }

        binding.buttonAddMediactionSave.setOnClickListener{
            saveData()
        }

        if(CommonClass.isNetworkAvailable(requireContext()))
        {
            observeVieModel()
        }
        else
        {
            CommonClass.showInternetDialogue(requireContext())
        }


    }

    /*override fun onKeyboardVisibilityChanged(isVisible: Boolean) {
        isKeyboardVisible = isVisible
        if (!isKeyboardVisible) {
            // Keyboard is hidden, show the bottom navigation menu
            val bottomNavView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            bottomNavView.visibility = View.VISIBLE
        }
    }*/

    private fun updateBottomNavVisibility(bottomNavView: BottomNavigationView) {
        if (!isKeyboardVisible && !isAnyEditTextFocused()) {
            bottomNavView.visibility = View.VISIBLE
        } else {
            bottomNavView.visibility = View.GONE
        }
    }

    private fun isAnyEditTextFocused(): Boolean {
        return binding.medicationName.hasFocus() || binding.provider.hasFocus() ||
                binding.dosage.hasFocus() || binding.direction.hasFocus()
    }


   /* private fun showMorningTimeAndAlarmDialog() {
        selectedSchedule = "Morning"
        val morningDialog = ScheduleTimeAndAlarmDialogFragment.newInstance("Morning", this)
        morningDialog.show(childFragmentManager, ScheduleTimeAndAlarmDialogFragment.TAG)
    }

    private fun showEveningTimeAndAlarmDialog() {
        selectedSchedule = "Evening"
        val eveningDialog = ScheduleTimeAndAlarmDialogFragment.newInstance("Evening", this)
        eveningDialog.show(childFragmentManager, ScheduleTimeAndAlarmDialogFragment.TAG)
    }
*/
    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun setBottomNavVisibilityOnFocusChange(view: View, bottomNavView: BottomNavigationView) {
        view.setOnFocusChangeListener { _, hasFocus ->
            bottomNavView.visibility = if (hasFocus) View.GONE else View.VISIBLE
        }
    }

    private fun clearFocusAndHideKeyboard(bottomNavView: BottomNavigationView) {
        // Clear focus from all EditText views
        binding.medicationName.clearFocus()
        binding.provider.clearFocus()
        binding.dosage.clearFocus()
        binding.direction.clearFocus()

        // Hide the keyboard
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.parentLayout.windowToken, 0)

        // Show the bottom navigation menu
        bottomNavView.visibility = View.VISIBLE
    }

    private fun scheduleAlarm(time: String, scheduleType: String) {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("SCHEDULE_TYPE", scheduleType)

        val requestCode =
            if (scheduleType == "Morning") 0 else 1 // Use different request codes for Morning and Evening

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE // Use FLAG_UPDATE_CURRENT to update existing PendingIntent
        )

        val timeParts = time.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()

        val calendar = Calendar.getInstance()
        calendar.apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }
        // First, cancel any existing alarms for the same schedule type
        cancelAlarm(scheduleType)
        // Schedule the new alarm
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }


    private fun cancelAlarm(scheduleType: String) {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("SCHEDULE_TYPE", scheduleType)

        val requestCode =
            if (scheduleType == "Morning") 0 else 1 // Use the same request codes as used in scheduling

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        // Cancel the scheduled alarm
        alarmManager.cancel(pendingIntent)
    }

    private fun saveData() {

        // Check if any of the text fields are empty
        val isEmptyField = binding.medicationName.text.isNullOrBlank() ||
                binding.dosage.text.isNullOrBlank() ||
                binding.direction.text.isNullOrBlank()

        if (isEmptyField) {
            // Show a common pop-up message if any of the fields are empty
            commonDialog.showDialog(getString(R.string.please_enter_all_fields))
            return
        }

        // Get today's date
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(calendar.time)

        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val currentTime = timeFormat.format(calendar.time)

        val currentDateTime = "$currentDate $currentTime"

        val formattedDateTime = "$formattedDate $currentTime"

        // Create Alarm objects from AlarmInternal objects
        val alarmsList = alarmInternal.map { alarm ->
            Alarm(
                alarm.alarmId,
                formattedDate,
                alarm.repeat,
                alarm.alarmInterval,
                alarm.flag,
                alarm.isEnabled,
                alarm.medicineTime,
                loginResponse!!.loginDetails.patientLocationID,
                0
            )
        }


        // Get all entered data
        medicationName = binding.medicationName.text.toString()
        startDate = currentDate
        endDate = currentDate
        dosage = binding.dosage.text.toString()
        direction = binding.direction.text.toString()
        withMeal = if (binding.idSwitch.isOn) 1 else 0
        alarmsList.getOrNull(0)?.isEnabled = if(binding.alarmToggleButtonMorning.isOn) 1 else 0
        alarmsList.getOrNull(1)?.isEnabled = if(binding.alarmToggleButtonAfternoon.isOn) 1 else 0
        alarmsList.getOrNull(2)?.isEnabled = if(binding.alarmToggleButtonEvening.isOn) 1 else 0
        quantity = alarmsList.size
        isActive = 1

        // Create request object
        val request = AddMedicationDetailsRequest(
            "I",
            loginResponse!!.loginDetails.patientLocationID,
            0,
            medicationName,
            formattedDate,
            formattedDate,
            currentTime,
            loginResponse!!.loginDetails.providerID,
            direction,
            dosage,
            withMeal,
            quantity,
            isActive,
            alarmsList
        )

        Log.d("AddMedicationsFragment", "Save Data Request: $request")

        // Now you can use the 'request' object to send data to the server or perform any other action
       if(CommonClass.isNetworkAvailable(requireContext()))
       {
           addMedicationViewModel.addMedicationDetails(request, loginResponse!!.token.access_token)
       }
        else{
           CommonClass.showInternetDialogue(requireContext())
       }
    }

    private fun observeVieModel()
    {

        addMedicationViewModel.clear()
        addMedicationViewModel.loadingLiveData.observe(viewLifecycleOwner, Observer { isLoading->
            if(isLoading)
            {
                customProgressDialog.show("Loading...")
            }
            else
            {
                customProgressDialog.dialogDismiss()
            }
        })

        addMedicationViewModel.saveResponseLiveData.observe(viewLifecycleOwner, Observer { sucessData ->
            if(sucessData!= null)
            {
                commonDialog.dismiss()
                commonDialog.showDialog(sucessData.response.responseMessage)
                commonDialog.setOnDismissListener {
                    onDialogDismissed.invoke()
                }
            }

        })

        addMedicationViewModel.errorLiveData.observe(viewLifecycleOwner, Observer { failureMessage ->
            failureMessage?.let {
                if (CommonClass.isNetworkAvailable(requireContext())) {
                    ServerTimeoutHandler.handleTimeoutException(requireContext()) {
                        // Retry logic when the retry button is clicked
                        addMedicationViewModel.retryAddMedicationDetails()
                    }
                } else {
                    CommonClass.showInternetDialogue(requireContext())
                }
            }
        })


    }

    private fun updateAlarmTimings() {
        val timeFormat12Hours = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val timeFormat24Hours = SimpleDateFormat("HH:mm", Locale.getDefault())

        if (alarmInternal.isNotEmpty()) {
            // Check the selected card and update the UI accordingly
            when (selectedCard) {
                getString(R.string.morning) -> {
                    val morningAlarmIndex = alarmInternal.indexOfFirst { it.scheduleType == "Morning" }
                    if (morningAlarmIndex != -1) {
                        val morningAlarm = alarmInternal[morningAlarmIndex]
                        val morningAlarmTime = morningAlarm.medicineTime ?: "00:00"
                        val morningTime12Hours = timeFormat12Hours.format(timeFormat24Hours.parse(morningAlarmTime)!!)
                        binding.morningTimeView.text = morningTime12Hours
                        binding.morningAlarmTimeView.text = calculateAlarmTime(morningAlarmTime, morningAlarm.alarmInterval ?: 10)
                        alarmInternal[morningAlarmIndex].isEnabled = if (binding.alarmToggleButtonMorning.isOn) 1 else 0
                    }
                }
                getString(R.string.afternoon) -> {
                    val afternoonAlarmIndex = alarmInternal.indexOfFirst { it.scheduleType == "Afternoon" }
                    if (afternoonAlarmIndex != -1) {
                        val afternoonAlarm = alarmInternal[afternoonAlarmIndex]
                        val afternoonAlarmTime = afternoonAlarm.medicineTime ?: "00:00"
                        val afternoonTime12Hours = timeFormat12Hours.format(timeFormat24Hours.parse(afternoonAlarmTime)!!)
                        binding.afternoonTimeView.text = afternoonTime12Hours
                        binding.afternoonAlarmTimeView.text = calculateAlarmTime(afternoonAlarmTime, afternoonAlarm.alarmInterval ?: 10)
                        alarmInternal[afternoonAlarmIndex].isEnabled = if (binding.alarmToggleButtonAfternoon.isOn) 1 else 0
                    }
                }
                getString(R.string.evening) -> {
                    val eveningAlarmIndex = alarmInternal.indexOfFirst { it.scheduleType == "Evening" }
                    if (eveningAlarmIndex != -1) {
                        val eveningAlarm = alarmInternal[eveningAlarmIndex]
                        val eveningAlarmTime = eveningAlarm.medicineTime ?: "00:00"
                        val eveningTime12Hours = timeFormat12Hours.format(timeFormat24Hours.parse(eveningAlarmTime)!!)
                        binding.eveningTimeView.text = eveningTime12Hours
                        binding.eveningAlarmTimeView.text = calculateAlarmTime(eveningAlarmTime, eveningAlarm.alarmInterval ?: 10)
                        alarmInternal[eveningAlarmIndex].isEnabled = if (binding.alarmToggleButtonEvening.isOn) 1 else 0
                    }
                }
            }
        } else {
            // Handle the case where no alarms are added
        }
    }

    private fun calculateAlarmTime(alarm: String, interval: Int): String {
        val formatter24Hours = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formatter12Hours = SimpleDateFormat("hh:mm a", Locale.getDefault())

        val calendar = Calendar.getInstance()
        calendar.time = formatter24Hours.parse(alarm)!!
        calendar.add(Calendar.MINUTE, -interval)

        return formatter12Hours.format(calendar.time)
    }

    private fun formatDate(selectedDate: LocalDate): String {
        val outputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        return outputFormat.format(Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))
    }

}

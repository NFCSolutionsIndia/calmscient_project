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
    import android.util.Log
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import androidx.activity.addCallback
    import androidx.fragment.app.Fragment
    import androidx.fragment.app.activityViewModels
    import androidx.lifecycle.Observer
    import com.calmscient.Interface.OnAlarmSelectedListenerUpdate
    import com.calmscient.R
    import com.calmscient.databinding.FragmentMedicationDetailBinding
    import com.calmscient.di.remote.request.Alarm
    import com.calmscient.di.remote.request.AlarmInternal
    import com.calmscient.di.remote.request.AlarmUpdateRequest
    import com.calmscient.di.remote.request.AlarmUpdateRequestInternal
    import com.calmscient.di.remote.request.AlarmWrapper
    import com.calmscient.di.remote.request.PatientMoodRequest
    import com.calmscient.di.remote.response.LoginResponse
    import com.calmscient.di.remote.response.MedicalDetails
    import com.calmscient.receivers.AlarmReceiver
    import com.calmscient.utils.CommonAPICallDialog
    import com.calmscient.utils.CustomProgressDialog
    import com.calmscient.utils.common.CommonClass
    import com.calmscient.utils.common.JsonUtil
    import com.calmscient.utils.common.SharedPreferencesUtil
    import com.calmscient.utils.network.ServerTimeoutHandler
    import com.calmscient.viewmodels.UpdateMedicationViewModel
    import com.google.android.material.bottomnavigation.BottomNavigationView
    import com.google.gson.Gson
    import java.text.ParseException
    import java.text.SimpleDateFormat
    import java.time.LocalDate
    import java.time.ZoneId
    import java.util.Calendar
    import java.util.Date
    import java.util.Locale
    
    
    class MedicationDetailFragment : Fragment(),
        ScheduleTimeAndAlarmDialogFragment.TimeAndAlarmSaveClickListener , OnAlarmSelectedListenerUpdate{
        private lateinit var binding: FragmentMedicationDetailBinding
        private var isMorningAlarmOn = false
        private var isEveningAlarmOn = false
        private var morningTime: String = ""
        private var morningAlarm: String = ""
        private var eveningTime: String = ""
        private var eveningAlarm: String = ""
        private var selectedSchedule: String = ""
        var selectedCard : String =""
    
        private var selectedDate: LocalDate? = null
        private lateinit var formattedDate: String
    
        private val updateAlarms: MutableList<AlarmUpdateRequest> = mutableListOf()
        private val alarmUpdateInternal: MutableList<AlarmUpdateRequestInternal> = mutableListOf()
        private lateinit var medicalDetails : MedicalDetails
    
        private var loginResponse : LoginResponse? = null
        private val updateMedicationViewModel : UpdateMedicationViewModel by activityViewModels()
    
        private lateinit var customProgressDialog: CustomProgressDialog
        private lateinit var commonDialog: CommonAPICallDialog
    
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            requireActivity().onBackPressedDispatcher.addCallback(this){
                loadFragment(CalendarFragment())
            }
        }
    
        override fun onAlarmSelected(alarm: AlarmUpdateRequestInternal) {
            Log.d("AddMedicationsFragment", "Selected Alarm: $alarm")
            // Check if an alarm of the same type already exists
            val existingAlarmIndex = alarmUpdateInternal.indexOfFirst { it.scheduleType == alarm.scheduleType }
            if (existingAlarmIndex != -1) {
                // Update the existing alarm
                alarmUpdateInternal[existingAlarmIndex] = alarm
            } else {
                // Add the new alarm
                alarmUpdateInternal.add(alarm)
    
            }
            updateAlarmTimings()
        }
    
        companion object {
            fun newInstance(medicalDetails: MedicalDetails?,selectedDate: LocalDate): MedicationDetailFragment {
                val fragment = MedicationDetailFragment()
                val args = Bundle()
                val gson = Gson()
                val medicalDetailsJson = gson.toJson(medicalDetails)
                args.putString("medicalDetailsJson", medicalDetailsJson)
                args.putSerializable("selectedDate", selectedDate)
                fragment.arguments = args
                return fragment
            }
        }
    
        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            binding = FragmentMedicationDetailBinding.inflate(inflater, container, false)
            binding.alarmToggleButtonMorning.labelOn = getString(R.string.yes)
            binding.alarmToggleButtonMorning.labelOff = getString(R.string.no)
            binding.alarmToggleButtonAfternoon.labelOn = getString(R.string.yes)
            binding.alarmToggleButtonAfternoon.labelOff = getString(R.string.no)
    
            binding.alarmToggleButtonEvening.labelOn = getString(R.string.yes)
            binding.alarmToggleButtonEvening.labelOff = getString(R.string.no)
            binding.backIcon.setOnClickListener {
                loadFragment(CalendarFragment())
            }
    
            val jsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
            loginResponse = JsonUtil.fromJsonString<LoginResponse>(jsonString)
    
    
            // Retrieve the JSON string from arguments bundle
            val medicalDetailsJson = arguments?.getString("medicalDetailsJson")
             selectedDate = arguments?.getSerializable("selectedDate") as? LocalDate
             formattedDate = selectedDate?.let { formatDate(it) }.toString()
    
            Log.d("selectedDate in MDF","$selectedDate")
            Log.d("formattedDate in MDF","$formattedDate")
    
            // Parse the JSON string back to MedicalDetails object
            val gson = Gson()
             medicalDetails = gson.fromJson(medicalDetailsJson, MedicalDetails::class.java)
    
            Log.d("Medical Details","$medicalDetails")
    
    
            customProgressDialog = CustomProgressDialog(requireContext())
            commonDialog = CommonAPICallDialog(requireContext())
    
           /* binding.morningCalendar.setOnClickListener {
                showMorningTimeAndAlarmDialog()
            }
    
            binding.eveningCalendar.setOnClickListener {
                showEveningTimeAndAlarmDialog()
            }*/
    
    
    
            // Define the time ranges for each card
            val morningTimeRange = Pair("06:00:00", "12:00:00") // Adjust as per your needs
            val afternoonTimeRange = Pair("12:00:00", "18:00:00") // Adjust as per your needs
            val eveningTimeRange = Pair("18:00:00", "23:59:59") // Adjust as per your needs
    
           // Click listener for morning card
            binding.morningAlarmCard.setOnClickListener {
                handleCardClick(R.string.morning, morningTimeRange)
            }
    
            // Click listener for afternoon card
            binding.afternoonAlarmCard.setOnClickListener {
                handleCardClick(R.string.afternoon, afternoonTimeRange)
            }
    
            // Click listener for evening card
            binding.eveningAlarmCard.setOnClickListener {
                handleCardClick(R.string.evening, eveningTimeRange)
            }
    
            binding.tvDoctorName.text = medicalDetails.providerName
            binding.tvDosageName.text = medicalDetails.medicineName
            binding.tvDosageValue.text = medicalDetails.medicineDosage
            binding.tvDirectionValue.text = medicalDetails.directions
    
         /*   binding.morningTimeView.text = medicalDetails.scheduledTimeList[0].scheduledTimes[0].medicineTime
    
            val isEnabled = medicalDetails.scheduledTimeList[0].scheduledTimes[0].alarmEnabled == "1"
            binding.alarmToggleButtonMorning.isOn = isEnabled
    */
    
            val alarmInputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val medicationInputFormat = SimpleDateFormat("HH:mm:ss")
            val outputFormat12Hour = SimpleDateFormat("hh:mm a")
    
            for (scheduledTime in medicalDetails.scheduledTimeList) {
                val alarmTime = scheduledTime.scheduledTimes[0].alarmTime
                val medicationTime = scheduledTime.scheduledTimes[0].medicineTime
                val isEnabled = scheduledTime.scheduledTimes[0].alarmEnabled == "1"
    
                // Parsing alarm time
                val alarmDate = alarmInputFormat.parse(alarmTime)
                val alarmCalendar = Calendar.getInstance().apply {
                    time = alarmDate ?: Date()
                }
    
                // Parsing medication time
                val medicationDate = medicationInputFormat.parse(medicationTime)
                val medicationCalendar = Calendar.getInstance().apply {
                    time = medicationDate ?: Date()
                }
    
                // Formatting times in 12-hour format with AM/PM indicator
                val alarmFormattedTime12Hour = outputFormat12Hour.format(alarmCalendar.time)
                val medicationFormattedTime12Hour = outputFormat12Hour.format(medicationCalendar.time)
    
                val hourOfDay = alarmCalendar.get(Calendar.HOUR_OF_DAY)
    
                when {
                    hourOfDay in 6..11 -> { // Morning (6:00 AM to 11:59 AM)
                        binding.morningTimeView.text = medicationFormattedTime12Hour
                        binding.morningAlarmTimeView.text = alarmFormattedTime12Hour
                        binding.alarmToggleButtonMorning.isOn = isEnabled
                    }
                    hourOfDay in 12..17 -> { // Afternoon (12:00 PM to 5:59 PM)
                        binding.afternoonTimeView.text = medicationFormattedTime12Hour
                        binding.afternoonAlarmTimeView.text = alarmFormattedTime12Hour
                        binding.alarmToggleButtonAfternoon.isOn = isEnabled
                    }
                    else -> { // Evening (6:00 PM to 5:59 AM)
                        binding.eveningTimeView.text = medicationFormattedTime12Hour
                        binding.eveningAlarmTimeView.text = alarmFormattedTime12Hour
                        binding.alarmToggleButtonEvening.isOn = isEnabled
                    }
                }
            }
    
    
    
    
            if (binding.morningTimeView.text.isBlank()) {
                binding.morningAlarmCard.visibility = View.VISIBLE
            }
            if (binding.afternoonTimeView.text.isBlank()) {
                binding.afternoonAlarmCard.visibility = View.VISIBLE
            }
            if (binding.eveningTimeView.text.isBlank()) {
                binding.eveningAlarmCard.visibility = View.VISIBLE
            }
    
    
            return binding.root;
            // return inflater.inflate(R.layout.fragment_medication_detail, container, false)
        }
        @SuppressLint("ClickableViewAccessibility")
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
    
    
            binding.buttonUpdateMedicationSave.setOnClickListener{
                saveData()
    
                if(CommonClass.isNetworkAvailable(requireContext()))
                {
                    observeViewModel()
                }
                else
                {
                    CommonClass.showInternetDialogue(requireContext())
                }
            }
    
    
    
    
        }
        private fun showMorningTimeAndAlarmDialog() {
            selectedSchedule = "Morning"
            val morningDialog = ScheduleTimeAndAlarmDialogFragment.newInstance("Morning", this)
            morningDialog.show(childFragmentManager, ScheduleTimeAndAlarmDialogFragment.TAG)
        }
    
        private fun showEveningTimeAndAlarmDialog() {
            selectedSchedule = "Evening"
            val eveningDialog = ScheduleTimeAndAlarmDialogFragment.newInstance("Evening", this)
            eveningDialog.show(childFragmentManager, ScheduleTimeAndAlarmDialogFragment.TAG)
        }
    
        private fun loadFragment(fragment: Fragment) {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.flFragment, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    
        override fun onSaveClicked(time: String, alarm: String) {
            val currentTime = getCurrentTime()
            val selectedTime = SimpleDateFormat("HH:mm").parse(alarm)
    
            if (currentTime.before(selectedTime)) {
                // The selected time is in the future, schedule the alarm for today
                when (selectedSchedule) {
                    "Morning" -> {
                        morningTime = time
                        morningAlarm = alarm
                        binding.morningTimeView.text = morningTime
                        binding.morningAlarmTimeView.text = morningAlarm
                        if (isMorningAlarmOn) {
                            scheduleAlarm(morningAlarm, "Morning")
                        } else {
                            cancelAlarm("Morning")
                        }
                    }
    
                    "Evening" -> {
                        eveningTime = time
                        eveningAlarm = alarm
                        binding.eveningTimeView.text = eveningTime
                        binding.eveningAlarmTimeView.text = eveningAlarm
                        if (isEveningAlarmOn) {
                            scheduleAlarm(eveningAlarm, "Evening")
                        } else {
                            cancelAlarm("Evening")
                        }
                    }
                    // ... Add other schedule types if needed
                }
            } else {
                // The selected time is in the past, schedule the alarm for the next day
                val calendar = Calendar.getInstance()
                calendar.time = selectedTime
                calendar.add(Calendar.DAY_OF_YEAR, 1)
    
                when (selectedSchedule) {
                    "Morning" -> {
                        morningTime = time
                        morningAlarm = SimpleDateFormat("HH:mm").format(calendar.time)
                        binding.morningTimeView.text = morningTime
                        binding.morningAlarmTimeView.text = morningAlarm
                        if (isMorningAlarmOn) {
                            scheduleAlarm(morningAlarm, "Morning")
                        } else {
                            cancelAlarm("Morning")
                        }
                    }
    
                    "Evening" -> {
                        eveningTime = time
                        eveningAlarm = SimpleDateFormat("HH:mm").format(calendar.time)
                        binding.eveningTimeView.text = eveningTime
                        binding.eveningAlarmTimeView.text = eveningAlarm
                        if (isEveningAlarmOn) {
                            scheduleAlarm(eveningAlarm, "Evening")
                        } else {
                            cancelAlarm("Evening")
                        }
                    }
                }
            }
        }
    
        private fun getCurrentTime(): Date {
            return Calendar.getInstance().time
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
                PendingIntent.FLAG_IMMUTABLE
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
    
        private fun updateAlarmTimings() {
            val timeFormat12Hours = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val timeFormat24Hours = SimpleDateFormat("HH:mm", Locale.getDefault())
    
            if (alarmUpdateInternal.isNotEmpty()) {
                // Check the selected card and update the UI accordingly
                when (selectedCard) {
                    getString(R.string.morning) -> {
                        val morningAlarmIndex = alarmUpdateInternal.indexOfFirst { it.scheduleType == "Morning" }
                        if (morningAlarmIndex != -1) {
                            val morningAlarm = alarmUpdateInternal[morningAlarmIndex]
                            val morningAlarmTime = morningAlarm.medicineTime ?: "00:00"
                            val morningTime12Hours = timeFormat12Hours.format(timeFormat24Hours.parse(morningAlarmTime)!!)
                            binding.morningTimeView.text = morningTime12Hours
                            binding.morningAlarmTimeView.text = calculateAlarmTime(morningAlarmTime, morningAlarm.alarmInterval ?: 10)
                            alarmUpdateInternal[morningAlarmIndex].isEnabled = if (binding.alarmToggleButtonMorning.isOn) 1 else 0
                        }
                    }
                    getString(R.string.afternoon) -> {
                        val afternoonAlarmIndex = alarmUpdateInternal.indexOfFirst { it.scheduleType == "Afternoon" }
                        if (afternoonAlarmIndex != -1) {
                            val afternoonAlarm = alarmUpdateInternal[afternoonAlarmIndex]
                            val afternoonAlarmTime = afternoonAlarm.medicineTime ?: "00:00"
                            val afternoonTime12Hours = timeFormat12Hours.format(timeFormat24Hours.parse(afternoonAlarmTime)!!)
                            binding.afternoonTimeView.text = afternoonTime12Hours
                            binding.afternoonAlarmTimeView.text = calculateAlarmTime(afternoonAlarmTime, afternoonAlarm.alarmInterval ?: 10)
                            alarmUpdateInternal[afternoonAlarmIndex].isEnabled = if (binding.alarmToggleButtonAfternoon.isOn) 1 else 0
                        }
                    }
                    getString(R.string.evening) -> {
                        val eveningAlarmIndex = alarmUpdateInternal.indexOfFirst { it.scheduleType == "Evening" }
                        if (eveningAlarmIndex != -1) {
                            val eveningAlarm = alarmUpdateInternal[eveningAlarmIndex]
                            val eveningAlarmTime = eveningAlarm.medicineTime ?: "00:00"
                            val eveningTime12Hours = timeFormat12Hours.format(timeFormat24Hours.parse(eveningAlarmTime)!!)
                            binding.eveningTimeView.text = eveningTime12Hours
                            binding.eveningAlarmTimeView.text = calculateAlarmTime(eveningAlarmTime, eveningAlarm.alarmInterval ?: 10)
                            alarmUpdateInternal[eveningAlarmIndex].isEnabled = if (binding.alarmToggleButtonEvening.isOn) 1 else 0
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
            calendar.add(Calendar.MINUTE, interval)
    
            return formatter12Hours.format(calendar.time)
        }
        // Function to handle card click
        private fun handleCardClick(selectedSchedule: Int, timeRange: Pair<String, String>) {
            selectedCard = getString(selectedSchedule)
            // Find the corresponding scheduledTime for the clicked card's time range
            val scheduledTime = medicalDetails.scheduledTimeList.flatMap { it.scheduledTimes }
                .find {
                    val time = it.medicineTime.split(":").firstOrNull()?.toIntOrNull() ?: 0
                    time in timeRange.first.split(":").first().toInt()..timeRange.second.split(":").first().toInt()
                }
    
    
                val bottomSheetFragment = UpdateMedicationDetailsBottomSheetFragment(selectedCard, scheduledTime, medicalDetails.medicationId)
                bottomSheetFragment.setOnAlarmSelectedListener(this)
                bottomSheetFragment.show(requireActivity().supportFragmentManager, bottomSheetFragment.tag)
    
        }
    
        private fun observeViewModel()
        {

            updateMedicationViewModel.loadingLiveData.observe(viewLifecycleOwner, Observer { isLoading ->
                if(isLoading)
                {
                    commonDialog.dismiss()
                    customProgressDialog.dialogDismiss()
                    customProgressDialog.show("Loading...")
    
                }
                else
                {
                    customProgressDialog.dialogDismiss()
                }
            })
    
            updateMedicationViewModel.errorLiveData.observe(viewLifecycleOwner, Observer { errorData->
                if(errorData != null)
                {
                    if (CommonClass.isNetworkAvailable(requireContext())) {
                        ServerTimeoutHandler.handleTimeoutException(requireContext()) {
                            // Retry logic when the retry button is clicked
                            updateMedicationViewModel.retryUpdateMedicationDetails()
                        }
                    } else {
                        CommonClass.showInternetDialogue(requireContext())
                    }
                }
                else
                {
                    ServerTimeoutHandler.clearRetryListener()
                    ServerTimeoutHandler.dismissDialog()
    
                }
            })
    
            updateMedicationViewModel.successLiveData.observe(viewLifecycleOwner, Observer { isSuccess->
                if(isSuccess)
                {
                    customProgressDialog.dialogDismiss()
                    updateMedicationViewModel.saveResponseLiveData.observe(viewLifecycleOwner, Observer { successDate->
                        if(successDate != null){
                            customProgressDialog.dialogDismiss()
                            commonDialog.dismiss()
                            commonDialog.showDialog(successDate.responseMessage)
                            commonDialog.setOnDismissListener {
                                onDialogDismissed.invoke()
                            }
                        }
                    })
                }
            })
        }
    
        private fun saveData()
        {
            val alarmsList = alarmUpdateInternal.map { alarm ->
                AlarmUpdateRequest(
                    loginResponse!!.loginDetails.patientLocationID,
                    alarm.alarmId,
                    medicalDetails.medicationId,
                    alarm.medicineTime,
                    formattedDate,
                    alarm.repeat,
                    alarm.alarmInterval,
                    alarm.flag,
                    null
                )
            }
    
            val morningTime = binding.morningTimeView.text.toString()
            val afternoonTime = binding.afternoonTimeView.text.toString()
            val eveningTime = binding.eveningTimeView.text.toString()
    
            updateAlarmToggle(alarmsList, morningTime, binding.alarmToggleButtonMorning.isOn)
            updateAlarmToggle(alarmsList, afternoonTime, binding.alarmToggleButtonAfternoon.isOn)
            updateAlarmToggle(alarmsList, eveningTime, binding.alarmToggleButtonEvening.isOn)
    
    
            val request = AlarmWrapper(alarmsList)
    
    
            if(CommonClass.isNetworkAvailable(requireContext()))
            {
                Log.d("Updation ", "Update Alarm Data Request: $request")
    
    
                if (request.alarms.isNotEmpty()) {
                    loginResponse?.token?.let {
                        updateMedicationViewModel.updatePatientMedicationDetails(request, it.access_token)
                    }
                }
                else{
    
                    commonDialog.showDialog("You did not change any timings")
                }
            }
            else
            {
                CommonClass.showInternetDialogue(requireContext())
            }
        }
    
        private fun formatDate(selectedDate: LocalDate): String {
            val outputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
            return outputFormat.format(Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))
        }
    
        private val onDialogDismissed: () -> Unit = {
            commonDialog.dismiss()
            loadFragment(CalendarFragment())
        }
    
        private fun updateAlarmToggle(alarmsList: List<AlarmUpdateRequest>, time: String, isEnabled: Boolean) {
            val timeFormat12Hours = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val timeFormat24Hours = SimpleDateFormat("HH:mm", Locale.getDefault())
    
            // Parse the time from the UI binding
            val parsedTime: Date? = try {
                timeFormat12Hours.parse(time)
            } catch (e: ParseException) {
                // Try parsing with 24-hour format if parsing with 12-hour format fails
                timeFormat24Hours.parse(time)
            }
    
            // Iterate through alarmsList and find the matching alarm
            parsedTime?.let { parsedTime ->
                for (alarm in alarmsList) {
                    // Convert the alarm time to a Date object
                    val alarmTime = try {
                        timeFormat12Hours.parse(alarm.medicineTime)
                    } catch (e: ParseException) {
                        // Try parsing with 24-hour format if parsing with 12-hour format fails
                        timeFormat24Hours.parse(alarm.medicineTime)
                    }
    
                    // Compare the alarm times
                    if (alarmTime != null && alarmTime == parsedTime) {
                        // Update the isEnabled property based on the state of the alarm toggle
                        alarm.isEnabled = if (isEnabled) 1 else 0
                        // Break the loop after updating the alarm
                        break
                    }
                }
            }
        }
    
    
    }

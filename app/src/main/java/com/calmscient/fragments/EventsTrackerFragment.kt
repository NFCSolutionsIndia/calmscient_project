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
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.calmscient.R
import com.calmscient.adapters.EventTrackerAdapter
import com.calmscient.databinding.FragmentEventsTrackerBinding
import com.calmscient.di.remote.EventTrackerDataClass
import com.calmscient.di.remote.request.CreateAlcohol
import com.calmscient.di.remote.request.CreateEventTrackerRequest
import com.calmscient.di.remote.response.CreateEventTrackerResponse
import com.calmscient.di.remote.response.Evevnts
import com.calmscient.di.remote.response.GetEventsListResponse
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.di.remote.response.SummaryOfAUDITResponse
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.CreateEventTrackingViewModel
import com.calmscient.viewmodels.GetEventTrackerViewModel
import com.calmscient.viewmodels.GetSummaryOfAUDITViewModel
import com.calmscient.viewmodels.UpdateTakingControlIndexViewModel
import com.github.mikephil.charting.charts.LineChart
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class EventsTrackerFragment : Fragment() {
    private  lateinit var binding: FragmentEventsTrackerBinding
    private lateinit var monthText: TextView

    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonAPICallDialog: CommonAPICallDialog
    private val getEventTrackerViewModel: GetEventTrackerViewModel by activityViewModels()
    private lateinit var getEventsListResponse: GetEventsListResponse
    private var loginResponse : LoginResponse? = null
    private  lateinit var accessToken : String

    private lateinit var eventTrackerAdapter: EventTrackerAdapter

    private val updateTakingControlIndexViewModel: UpdateTakingControlIndexViewModel by activityViewModels()

    private val createEventTrackingViewModel: CreateEventTrackingViewModel by activityViewModels()
    private lateinit var createEventTrackerResponse: CreateEventTrackerResponse

    private var courseTempId = 0

    companion object {
        fun newInstance(
            courseId: Int
        ): EventsTrackerFragment {
            val fragment = EventsTrackerFragment()
            val args = Bundle()
            args.putInt("courseId",courseId)

            fragment.arguments = args
            return fragment
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this){
            loadFragment(TakingControlFragment())
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentEventsTrackerBinding.inflate(inflater, container, false)

        accessToken = SharedPreferencesUtil.getData(requireContext(), "accessToken", "")
        val loginJsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString<LoginResponse>(loginJsonString)

        courseTempId = arguments?.getInt("courseId")!!

        commonAPICallDialog = CommonAPICallDialog(requireContext())
        customProgressDialog = CustomProgressDialog(requireContext())


        binding.backIcon.setOnClickListener {
            loadFragment(TakingControlFragment())
        }
        monthText = binding.monthtext


        if (CommonClass.isNetworkAvailable(requireContext()))
        {
           getEventTrackerApiCall()
           updateIndexApiCall()
        }
        else
        {
            CommonClass.showInternetDialogue(requireContext())
        }

        binding.eventTrackerRecycleView.layoutManager = LinearLayoutManager(requireContext())

        eventTrackerAdapter = EventTrackerAdapter(mutableListOf())

        binding.eventTrackerRecycleView.adapter = eventTrackerAdapter

        val currentDate = SimpleDateFormat("dd MMMM yyyy", Locale.UK).format(Date())
        binding.monthtext.text = currentDate



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.saveButton.setOnClickListener{
            createEventTrackAPICall()
        }
    }
    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null) // This ensures that the previous fragment is added to the back stack
        transaction.commit()
    }

    private fun getEventTrackerApiCall()
    {
        loginResponse?.loginDetails?.let {
            getEventTrackerViewModel.getEventTackerData("",
                it.clientID,it.patientID,it.patientLocationID,accessToken)
        }
        observeViewModel()
    }

    private fun observeViewModel()
    {
        getEventTrackerViewModel.loadingLiveData.observe(viewLifecycleOwner, Observer { isLoading->
            if(isLoading)
            {
                customProgressDialog.show("Loading...")
            }
            else{
                customProgressDialog.dialogDismiss()
            }
        })

        getEventTrackerViewModel.successLiveData.observe(viewLifecycleOwner, Observer { isSuccess->
            if(isSuccess)
            {
                getEventTrackerViewModel.saveResponseLiveData.observe(viewLifecycleOwner, Observer { successData->
                    if(successData != null)
                    {
                        getEventsListResponse = successData
                        setRecyclerViewData(getEventsListResponse.evevntsList)

                        val formattedDate = convertDateFormat(getEventsListResponse.date)
                        monthText.text = formattedDate

                        Log.d("EventTrckerLIst : ","$getEventsListResponse")
                    }
                })
            }
        })
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun setRecyclerViewData(eventsList: List<Evevnts>) {
        val eventTrackerDataList = eventsList.map { event ->
            EventTrackerDataClass(event.imageUrl, event.eventName, event.eventFlag == 1, event.eventId, event.eventFlag, toggleButtonState = event.eventFlag == 1 )
        }.toMutableList()

        eventTrackerAdapter.updateEvents(eventTrackerDataList)
    }



    private fun convertDateFormat(inputDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.UK)
        val date = inputFormat.parse(inputDate)
        return outputFormat.format(date)
    }

    private fun updateIndexApiCall() {
        val isCompleted = 1
        loginResponse?.loginDetails?.let {
            updateTakingControlIndexViewModel.updateTakingControlIndexData(
                it.clientID,
                courseTempId,
                isCompleted,
                it.patientID,
                it.patientLocationID,
                accessToken
            )
        }
    }

    private fun createEventTrackAPICall() {
        createEventTrackingViewModel.clear()
        val request = createEventTrackRequest()
        Log.d("Event Create Request:","$request")
        createEventTrackingViewModel.createEventTrackerList(request, accessToken)
        observeCreateEventTracker()
    }

    private fun observeCreateEventTracker()
    {
        createEventTrackingViewModel.loadingLiveData.observe(viewLifecycleOwner, Observer { isLoading->
            if(isLoading){
                customProgressDialog.show(getString(R.string.loading))
            }
            else{
                customProgressDialog.dialogDismiss()
            }
        })

        createEventTrackingViewModel.successLiveData.observe(viewLifecycleOwner, Observer { isSuccess->
            if(isSuccess)
            {
                createEventTrackingViewModel.saveResponseLiveData.observe(viewLifecycleOwner,
                    Observer { successData->
                        if(successData != null)
                        {
                            commonAPICallDialog.showDialog(successData.statusResponse.responseMessage)

                            commonAPICallDialog.setOnDismissListener {
                                loadFragment(TakingControlFragment())
                                createEventTrackingViewModel.clear()
                            }

                        }
                    }
                )
            }
        })
    }

    private fun createEventTrackRequest(): CreateEventTrackerRequest {
        val alcoholList = mutableListOf<CreateAlcohol>()

        // Assuming you have a list of items to loop through and collect data
        val items = eventTrackerAdapter.getEvents()

        val logInDetails = loginResponse?.loginDetails
        for (item in items) {

            Log.d("CreateEventTrackRequest", "EventId: ${item.eventId}, ToggleButtonState: ${item.toggleButtonState}")

            val createAlcohol = logInDetails?.let {
                CreateAlcohol(
                    activityDate = getCurrentFormattedDate(),
                    clientId = it.clientID,
                    eventFlag = if (item.toggleButtonState == true) 1 else 0,
                    eventId =  item.eventId,
                    flag = "U",
                    patientId = it.patientID,
                    plId = it.patientLocationID
                )
            }
            if (createAlcohol != null) {
                alcoholList.add(createAlcohol)
            }
        }

        return CreateEventTrackerRequest(alcohol = alcoholList)
    }
    private fun getCurrentFormattedDate(): String {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        return dateFormat.format(Date())
    }

}
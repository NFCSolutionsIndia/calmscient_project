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

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.R
import com.calmscient.adapters.DrinkTrackerAdapter
import com.calmscient.databinding.FragmentDrinkTrackerBinding
import com.calmscient.di.remote.DrinkTrackerItem
import com.calmscient.di.remote.request.Alcohol
import com.calmscient.di.remote.request.CreateDrinkTrackerRequest
import com.calmscient.di.remote.response.CreateDrinkTrackerResponse
import com.calmscient.di.remote.response.DrinkTrackerResponse
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomCalendarDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.CreateDrinkTrackerViewModel
import com.calmscient.viewmodels.GetDrinkTrackerViewModel
import com.calmscient.viewmodels.UpdateTakingControlIndexViewModel
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DrinkTrackerFragment : Fragment() , CustomCalendarDialog.OnDateSelectedListener{
    private lateinit var binding: FragmentDrinkTrackerBinding
    private lateinit var calendarView: ImageView
    private lateinit var monthText: TextView
    private lateinit var adapter: DrinkTrackerAdapter

    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonAPICallDialog: CommonAPICallDialog
    private val getDrinkTrackerViewModel: GetDrinkTrackerViewModel by activityViewModels()
    private lateinit var drinkTrackerResponse: DrinkTrackerResponse
    private var loginResponse: LoginResponse? = null
    private lateinit var accessToken: String
    private lateinit  var selectedDate : String

    private val createDrinkTrackerViewModel: CreateDrinkTrackerViewModel by activityViewModels()
    private lateinit var createDrinkTrackerResponse: CreateDrinkTrackerResponse
    private val updateTakingControlIndexViewModel: UpdateTakingControlIndexViewModel by activityViewModels()

    private var courseTempId = 0

    companion object {
        fun newInstance(
            courseId: Int
        ): DrinkTrackerFragment {
            val fragment = DrinkTrackerFragment()
            val args = Bundle()
            args.putInt("courseId",courseId)

            fragment.arguments = args
            return fragment
        }
    }

    override fun onDateSelected(date: CalendarDay) {

        val formattedDate = formatDate(date)

        selectedDate = formattedDate

        binding.tvDate.text = selectedDate

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDrinkTrackerBinding.inflate(inflater, container, false)

        binding.backIcon.setOnClickListener {
            loadFragment(TakingControlFragment())
        }


        accessToken = SharedPreferencesUtil.getData(requireContext(), "accessToken", "")
        val loginJsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString<LoginResponse>(loginJsonString)

        courseTempId = arguments?.getInt("courseId")!!

        commonAPICallDialog = CommonAPICallDialog(requireContext())
        customProgressDialog = CustomProgressDialog(requireContext())



        calendarView = binding.calenderview
        monthText = binding.tvDate



        binding.calenderview.setOnClickListener{
            val dialog = CustomCalendarDialog()
            dialog.setOnDateSelectedListener(this)
            dialog.show(parentFragmentManager, "CustomCalendarDialog")
            //customCalendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_SINGLE)

            /* dialog.setOnOkClickListener {
                 apiCall(selectedDate.toString())
             }*/
        }


        // Initialize the monthText with the current date
        val currentDate = SimpleDateFormat("dd MMMM yyyy", Locale.UK).format(Date())
        monthText.text = currentDate


        if (CommonClass.isNetworkAvailable(requireContext())) {
            apiCall()
            updateIndexApiCall()

        } else {
            CommonClass.showInternetDialogue(requireContext())
        }

        binding.saveButton.setOnClickListener {
            if (CommonClass.isNetworkAvailable(requireContext())) {
                createDrinkTrackerAPICall()

                /*val request = constructDrinkTrackerList()
               Log.d("DrinkTracker List:","$request")*/
            } else {
                CommonClass.showInternetDialogue(requireContext())
            }

        }
        return binding.root
    }

    /*override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeTotalItemCount()
    }*/
    private fun updateLabel(selectedDate: Calendar) {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.UK)
        val formattedDate = sdf.format(selectedDate.time)
        monthText.text = formattedDate
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun apiCall() {
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

        observeViewModel()
    }

    private fun observeViewModel() {
        getDrinkTrackerViewModel.loadingLiveData.observe(viewLifecycleOwner, Observer { isLoading ->
            if (isLoading) {
                customProgressDialog.show("Loading...")
            } else {
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
                            binding.tvTotalCount.text = drinkTrackerResponse.totalCount.toString()
                            bindDataToRecyclerView(drinkTrackerResponse)
                            //bindUIData(drinkTrackerResponse)
                        }
                    })
            }
        })
    }

    private fun bindUIData(drinkTracker: DrinkTrackerResponse) {

        binding.tvDate.text = drinkTracker.date
        //binding.tvTotalCount.text = drinkTracker.totalCount.toString()
    }

    private fun bindDataToRecyclerView(drinkTracker: DrinkTrackerResponse) {
        val drinkItems = mutableListOf<DrinkTrackerItem>()
        val drinksList = drinkTracker.drinksList
        drinksList.forEach { drink ->
            val drinkItem = DrinkTrackerItem(
                itemImage = drink.imageUrl,
                itemCount = drink.quantity,
                itemDescription = drink.drinkName
            )
            drinkItems.add(drinkItem)
        }
        setupRecyclerView(binding.drinkTrackerRecyclerView, drinkItems)
        adapter = binding.drinkTrackerRecyclerView.adapter as DrinkTrackerAdapter
        observeTotalItemCount()
    }


    private fun setupRecyclerView(
        recyclerView: RecyclerView,
        drinkItems: List<DrinkTrackerItem>
    ) {
        val adapter = DrinkTrackerAdapter(drinkItems)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter
    }


    private fun observeTotalItemCount() {
        adapter.totalItemCount.observe(viewLifecycleOwner, Observer { totalCount ->
            binding.tvTotalCount.text = totalCount.toString()
        })
    }

    private fun getCurrentDate(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        return currentDate.format(formatter)
    }

    //Create DrinkTrackerAPI call
    private fun createDrinkTrackerAPICall() {
        val request = constructDrinkTrackerList()
        createDrinkTrackerViewModel.createDrinkTrackerList(request, accessToken)

        observeCreateDrinkTrackerAPIData()


    }

    private fun observeCreateDrinkTrackerAPIData() {
        createDrinkTrackerViewModel.loadingLiveData.observe(
            viewLifecycleOwner,
            Observer { isLoading ->
                if (isLoading) {
                    customProgressDialog.show("Loading")
                } else {
                    customProgressDialog.dialogDismiss()
                }
            })

        createDrinkTrackerViewModel.successLiveData.observe(
            viewLifecycleOwner,
            Observer { isSuccess ->
                if (isSuccess) {
                    customProgressDialog.dialogDismiss()

                    createDrinkTrackerViewModel.saveResponseLiveData.observe(viewLifecycleOwner,
                        Observer { successData ->
                            if (successData != null) {

                                commonAPICallDialog.showDialog(successData.statusResponse.responseMessage)
                                commonAPICallDialog.setOnDismissListener {

                                    commonAPICallDialog.dismiss()
                                    loadFragment(TakingControlFragment())
                                }
                            }
                        })
                }
            })
    }

    private fun constructDrinkTrackerList(): CreateDrinkTrackerRequest {
        val alcoholList = mutableListOf<Alcohol>()
        val currentDate = getCurrentDate()
        val loginDetails = loginResponse?.loginDetails

        if (loginDetails != null && ::drinkTrackerResponse.isInitialized) {
            val clientId = loginDetails.clientID
            val patientId = loginDetails.patientID
            val plId = loginDetails.patientLocationID

            val quantities = adapter.getCurrentQuantities()

            // Iterate over the items in the drink tracker response and create Alcohol objects
            drinkTrackerResponse.drinksList.forEachIndexed { index, drink ->
                val quantity = quantities.getOrElse(index) { 0 }
                val flag = if (drink.quantity == 0) "I" else "U"

                // Add the item if the quantity is greater than 0
                // or if the quantity is 0 but the flag is "U"
                if (quantity > 0 || (quantity == 0 && flag == "U")) {
                    val alcohol = Alcohol(
                        flag = flag,
                        plId = plId,
                        patientId = patientId,
                        clientId = clientId,
                        quantity = quantity,
                        drinkId = drink.drinkId,
                        activityDate = currentDate
                    )
                    alcoholList.add(alcohol)
                }
            }
        }

        return CreateDrinkTrackerRequest(alcoholList)
    }

    private fun showDatePickerDialog(context: Context, onDateSelected: (Date) -> Unit) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_date_picker)

        val calendarView = dialog.findViewById<CalendarView>(R.id.calendarView)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnOk = dialog.findViewById<Button>(R.id.btnOk)

        val calendar = Calendar.getInstance()

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnOk.setOnClickListener {
            onDateSelected(calendar.time)
            dialog.dismiss()
        }

        dialog.show()
    }
    private fun formatDate(date: CalendarDay): String {
        val calendar = Calendar.getInstance()
        calendar.set(date.year, date.month , date.day)
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
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
}

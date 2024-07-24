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
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.R
import com.calmscient.adapters.DrinkTrackerAdapter
import com.calmscient.databinding.FragmentMyDrinkingHabitScreenTwoBinding
import com.calmscient.di.remote.DrinkTrackerItem
import com.calmscient.di.remote.request.Alcohol
import com.calmscient.di.remote.request.CreateDrinkTrackerRequest
import com.calmscient.di.remote.request.SaveMyDrinkingHabitAnswerRequest
import com.calmscient.di.remote.response.CreateDrinkTrackerResponse
import com.calmscient.di.remote.response.DrinkTrackerResponse
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.di.remote.response.MyDrinkingHabitResponse
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.CreateDrinkTrackerViewModel
import com.calmscient.viewmodels.GetDrinkTrackerViewModel
import com.calmscient.viewmodels.UpdateTakingControlIndexViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter
@AndroidEntryPoint
class MyDrinkingHabitScreenTwoFragment : Fragment() {

    private lateinit var binding: FragmentMyDrinkingHabitScreenTwoBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DrinkTrackerAdapter

    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonAPICallDialog: CommonAPICallDialog
    private val getDrinkTrackerViewModel: GetDrinkTrackerViewModel by activityViewModels()
    private lateinit var drinkTrackerResponse: DrinkTrackerResponse
    private var loginResponse: LoginResponse? = null
    private lateinit var accessToken: String

    private val createDrinkTrackerViewModel: CreateDrinkTrackerViewModel by activityViewModels()
    private lateinit var createDrinkTrackerResponse: CreateDrinkTrackerResponse


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().supportFragmentManager.popBackStack()
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMyDrinkingHabitScreenTwoBinding.inflate(inflater, container, false)

        accessToken = SharedPreferencesUtil.getData(requireContext(), "accessToken", "")
        val loginJsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString<LoginResponse>(loginJsonString)


        commonAPICallDialog = CommonAPICallDialog(requireContext())
        customProgressDialog = CustomProgressDialog(requireContext())

        binding.previousQuestion.setOnClickListener {

            loadFragment(MyDrinkingHabitFragment())
        }

        if (CommonClass.isNetworkAvailable(requireContext())) {
            apiCall()

        } else {
            CommonClass.showInternetDialogue(requireContext())
        }

        binding.backIcon.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.previousQuestion.setOnClickListener{
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.nextQuestion.setOnClickListener {
            createDrinkTrackerAPICall()
            //loadFragment()
        }
        recyclerView = binding.drinkTrackerRecyclerView

        return binding.root
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

                        }
                    })
            }
        })
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
        if (request.alcohol.isNotEmpty()) {
            createDrinkTrackerViewModel.createDrinkTrackerList(request, accessToken)
            observeCreateDrinkTrackerAPIData()
        } else {
            loadFragment(MyDrinkingHabitScreenThreeFragment())
        }
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
                                    loadFragment(MyDrinkingHabitScreenThreeFragment())
                                }
                            }
                        }
                    )
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


}
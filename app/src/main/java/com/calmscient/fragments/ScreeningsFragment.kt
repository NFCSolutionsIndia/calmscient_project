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
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.calmscient.Interface.CellClickListener
import com.calmscient.R
import com.calmscient.adapters.MedicationsCardAdapter
import com.calmscient.adapters.ScreeningsCardAdapter
import com.calmscient.databinding.CalendarFragmentLayoutBinding
import com.calmscient.databinding.FragmentScreeningsBinding
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.di.remote.response.MenuItem
import com.calmscient.di.remote.response.ScreeningItem
import com.calmscient.di.remote.response.ScreeningResponse
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.ScreeningViewModel
import dagger.hilt.android.AndroidEntryPoint



data class ScreeningsCardItem(
    val title: String,
    val historyImageResource: Int?,
    val nextOrKeyImageResource: Int?,
    )
@AndroidEntryPoint
class ScreeningsFragment : Fragment() {
    private lateinit var cardViewAdapter: ScreeningsCardAdapter
    private val cardViewItems = mutableListOf<ScreeningsCardItem>()
    private lateinit var tvPHQ: TextView
    private lateinit var tvGAD: TextView
    private lateinit var tvAUDIT: TextView
    private lateinit var tvDAST: TextView
    private var screeningResponse: List<ScreeningItem> = emptyList()
    private var medicalResponse :List<MenuItem> = emptyList()
    private var loginResponse : LoginResponse? = null

    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonDialog: CommonAPICallDialog


    private val screeningsMenuViewModel: ScreeningViewModel by viewModels()
    private  lateinit var accessToken : String



    private lateinit var binding: FragmentScreeningsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this){
            if (CommonClass.isNetworkAvailable(requireContext())) {
                loadFragment(MedicalRecordsFragment())
            } else {
                CommonClass.showInternetDialogue(requireContext())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScreeningsBinding.inflate(inflater, container, false)
        binding.backIcon.setOnClickListener {
            if (CommonClass.isNetworkAvailable(requireContext())) {
                loadFragment(MedicalRecordsFragment())
            } else {
                CommonClass.showInternetDialogue(requireContext())
            }
        }

        customProgressDialog = CustomProgressDialog(requireContext())

        commonDialog = CommonAPICallDialog(requireContext())

        val medicalMenuJsonString = SharedPreferencesUtil.getData(requireContext(), "myMedicalMenuResponse", "")

        val loginJsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
         loginResponse = JsonUtil.fromJsonString<LoginResponse>(loginJsonString)

        accessToken = SharedPreferencesUtil.getData(requireContext(), "accessToken", "")

        Log.d("Access Token ","$accessToken")

        if(medicalMenuJsonString.isNotEmpty())
        {
             medicalResponse = JsonUtil.fromJsonString<List<MenuItem>>(medicalMenuJsonString)
             binding.titleTextView.text = medicalResponse[2].menuName
        }



        if (CommonClass.isNetworkAvailable(requireContext())) {

            observeViewModel()
        }
        else{
            CommonClass.showInternetDialogue(requireContext())
        }

        return binding.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerViewMedications.layoutManager = LinearLayoutManager(requireContext())
        cardViewAdapter = ScreeningsCardAdapter(requireActivity().supportFragmentManager,cardViewItems,screeningResponse)
        binding.recyclerViewMedications.adapter = cardViewAdapter
        displayCardViews(screeningResponse)
    }


    /*private fun displayCardViews() {
        cardViewItems.clear()
        cardViewItems.addAll(
            listOf(
                ScreeningsCardItem("PHQ-9", R.drawable.ic_history, R.drawable.ic_next_new),
                ScreeningsCardItem("GAD-7", null, R.drawable.ic_next_new),
                ScreeningsCardItem("AUDIT", null, R.drawable.ic_next_new),
                ScreeningsCardItem("DAST-10", null, R.drawable.ic_next_new),
                ScreeningsCardItem("SBIRT", null, R.drawable.ic_key)

            )
        )
        cardViewAdapter.notifyDataSetChanged()
    }*/

   /* @SuppressLint("NotifyDataSetChanged")
    private fun displayCardViews(screeningList: List<ScreeningItem>) {
        cardViewItems.clear()
        for (screeningItem in screeningList) {
            val title = screeningItem.screeningType
            val historyImageResource = when (screeningItem.screeningStatus) {
                getString(R.string.completed) -> R.drawable.ic_history // Use appropriate icon for completed screenings
                else -> null // Use appropriate icon for in-progress screenings
            }
            val nextOrKeyImageResource = R.drawable.ic_next_new
            cardViewItems.add(ScreeningsCardItem(title, historyImageResource, nextOrKeyImageResource))
        }
        cardViewAdapter.notifyDataSetChanged()
    }*/

    @SuppressLint("NotifyDataSetChanged")
    private fun displayCardViews(screeningList: List<ScreeningItem>) {
        cardViewItems.clear()
        for (screeningItem in screeningList) {
            val title = screeningItem.screeningType
            val historyImageResource = R.drawable.ic_history // Always show history icon
            val nextOrKeyImageResource = R.drawable.ic_next_new
            cardViewItems.add(ScreeningsCardItem(title, historyImageResource, nextOrKeyImageResource))
        }
        cardViewAdapter.notifyDataSetChanged()
    }


    private fun loadFragment(fragment: Fragment) {

        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun observeViewModel()
    {

        loginResponse?.loginDetails?.let {
            screeningsMenuViewModel.getScreeningList(
                it.patientID,
                it.clientID,
                it.patientLocationID,
                accessToken
            )
        }

        screeningsMenuViewModel.loadingLiveData.observe(requireActivity()) { isLoading ->
            if (isLoading) {
                customProgressDialog.show("Loading...")
            } else {

                customProgressDialog.dialogDismiss()
            }
        }

        screeningsMenuViewModel.screeningListLiveData.observe(viewLifecycleOwner, Observer { successData->
            if(successData!= null)
            {
                Log.d("Observeeeeee:","$successData")
                screeningResponse = successData
                displayCardViews(screeningResponse)

                cardViewAdapter = ScreeningsCardAdapter(requireActivity().supportFragmentManager,cardViewItems,screeningResponse)
                binding.recyclerViewMedications.adapter = cardViewAdapter

                Log.d("OOOOObserve:","$screeningResponse")
            }
        })

        Log.d("Observe:","$screeningResponse")

    }

}
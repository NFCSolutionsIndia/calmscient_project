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

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.calmscient.R
import com.calmscient.adapters.ScreeningsCardAdapter
import com.calmscient.adapters.TakingControlScreeningAdapter
import com.calmscient.databinding.FragmentTakingControlIntroductionBinding
import com.calmscient.di.remote.TakingControlScreeningItem
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.di.remote.response.MenuItem
import com.calmscient.di.remote.response.ScreeningItem
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.NonSwipeRecyclerView
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.ScreeningViewModel

class TakingControlIntroductionFragment : Fragment() {



    private lateinit var binding: FragmentTakingControlIntroductionBinding

    private lateinit var recyclerView: NonSwipeRecyclerView
    private val screeningsMenuViewModel: ScreeningViewModel by activityViewModels()
    private var screeningResponse: List<ScreeningItem> = emptyList()
    private var loginResponse : LoginResponse? = null

    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonDialog: CommonAPICallDialog
    private  lateinit var accessToken : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentTakingControlIntroductionBinding.inflate(inflater,container,false)

        customProgressDialog = CustomProgressDialog(requireContext())

        commonDialog = CommonAPICallDialog(requireContext())

        val loginJsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString<LoginResponse>(loginJsonString)

        accessToken = SharedPreferencesUtil.getData(requireContext(), "accessToken", "")

        if (CommonClass.isNetworkAvailable(requireContext())) {

            observeViewModel()
        }
        else{
            CommonClass.showInternetDialogue(requireContext())
        }

        binding.firstScreenNextButton.setOnClickListener{

            binding.screenOne.visibility = View.GONE
            binding.screenThree.visibility = View.GONE
            binding.screenFour.visibility = View.GONE


            binding.screenTwo.visibility = View.VISIBLE
        }

        recyclerView = binding.takingControlScreeningRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = TakingControlScreeningAdapter(requireActivity().supportFragmentManager,getScreeningItems(), requireContext(),screeningResponse)

        binding.thirdScreenNextButton.setOnClickListener{

            binding.screenOne.visibility = View.GONE
            binding.screenTwo.visibility = View.GONE
            binding.screenThree.visibility = View.GONE


            binding.screenFour.visibility = View.VISIBLE
        }

        binding.takingControlScreeningBulbIcon.setOnClickListener{

                showCustomDialog(requireContext())
        }

        return  binding.root

    }

    private fun getScreeningItems(): List<TakingControlScreeningItem> {
        return listOf(
            TakingControlScreeningItem("AUDIT", "Doesn't apply for me"),
            TakingControlScreeningItem("DAST-10", "Doesn't apply for me"),
            TakingControlScreeningItem("CAGE", "Doesn't apply for me")
        )
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
                Log.d("TCF:","$successData")
                screeningResponse = successData


                recyclerView.adapter = TakingControlScreeningAdapter(requireActivity().supportFragmentManager,getScreeningItems(), requireContext(),screeningResponse)
                binding.takingControlScreeningRecyclerView.adapter = recyclerView.adapter

                Log.d("TCF:","$screeningResponse")
            }
        })

        Log.d("TCF:","$screeningResponse")

    }


    private fun showCustomDialog(context: Context) {
        // Create and configure the custom dialog
        val dialogView = LayoutInflater.from(context).inflate(R.layout.taking_control_introduction_dialog_item, null)
        val dialog = android.app.AlertDialog.Builder(context, R.style.CustomDialog)
            .setView(dialogView)
            .create()

       /* // Initialize views in the custom dialog layout
        val titleTextView = dialogView.findViewById<TextView>(R.id.dialog_title)
        val iconImageView = dialogView.findViewById<ImageView>(R.id.dialog_icon)
        val messageTextView = dialogView.findViewById<TextView>(R.id.dialog_message)
*/

        val closeIcon = dialogView.findViewById<ImageView>(R.id.dialogClose)
        // Show the custom dialog
        dialog.show()


        closeIcon.setOnClickListener{
            dialog.dismiss()
        }
    }


}
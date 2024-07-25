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

import android.app.Dialog
import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.calmscient.R
import com.calmscient.databinding.FragmentTakingControlMakeAPlanScreenFiveBinding
import com.calmscient.di.remote.response.GetAlcoholFreeDayResponse
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomCalendarView
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.GetAlcoholFreeDaysViewModel
import com.calmscient.viewmodels.UpdateTakingControlIndexViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import java.text.SimpleDateFormat
import java.util.Locale

class TakingControlMakeAPlanScreenFiveFragment : Fragment() {

    private lateinit var binding: FragmentTakingControlMakeAPlanScreenFiveBinding
    private var selectedNumber = 1
    private lateinit var customCalendarView: CustomCalendarView

    private val getAlcoholFreeDaysViewModel : GetAlcoholFreeDaysViewModel by activityViewModels()
    private val updateTakingControlIndexViewModel: UpdateTakingControlIndexViewModel by activityViewModels()
    private var courseTempId = ""

    private lateinit var getAlcoholFreeDayResponse: GetAlcoholFreeDayResponse

    private var loginResponse : LoginResponse? = null
    private  lateinit var accessToken : String
    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonDialog: CommonAPICallDialog



    companion object {
        fun newInstance(
            courseId: Int
        ): TakingControlMakeAPlanScreenFiveFragment {
            val fragment = TakingControlMakeAPlanScreenFiveFragment()
            val args = Bundle()
            args.putInt("courseId",courseId)

            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        binding = FragmentTakingControlMakeAPlanScreenFiveBinding.inflate(inflater, container, false)

        customProgressDialog = CustomProgressDialog(requireContext())
        commonDialog = CommonAPICallDialog(requireContext())

        val jsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString<LoginResponse>(jsonString)

        accessToken = SharedPreferencesUtil.getData(requireContext(), "accessToken", "")
        courseTempId = SharedPreferencesUtil.getData(requireContext(), "courseIdMakeAPlan", "")



        customCalendarView = binding.customCalendarView
        customCalendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_NONE)
        // Disable interactions for the CustomCalendarView
        customCalendarView.setInteractionsEnabled(false)


        binding.goalsTextView.setOnClickListener {
            showBottomSheet()
        }

        binding.backIcon.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.previousQuestion.setOnClickListener {
            loadFragment(TakingControlMakeAPlanScreenFourFragment())
        }

        binding.btnSet.setOnClickListener{

            showSetDialog()
        }


        binding.makeAPlanBulbIcon.setOnClickListener{
            showBulbDialog()
        }

        if(CommonClass.isNetworkAvailable(requireContext()))
        {
            getAlcoholFreeDaysApiCall()
        }
        else{
            CommonClass.showInternetDialogue(requireContext())
        }


        return binding.root
    }

    private fun getAlcoholFreeDaysApiCall()
    {

        getAlcoholFreeDaysViewModel.clear()
        loginResponse?.loginDetails?.let { getAlcoholFreeDaysViewModel.getAlcoholFreeDays(it.patientID,accessToken) }
        observeGetAlcoholFreeDays()
    }

    private fun observeGetAlcoholFreeDays()
    {

        getAlcoholFreeDaysViewModel.loadingLiveData.observe(viewLifecycleOwner, Observer { isLoading->
            if(isLoading)
            {
                customProgressDialog.show(getString(R.string.loading))
            }
            else{
                customProgressDialog.dialogDismiss()
            }
        })

        getAlcoholFreeDaysViewModel.successLiveData.observe(viewLifecycleOwner, Observer { isSuccess->
            if(isSuccess){
                getAlcoholFreeDaysViewModel.saveResponseLiveData.observe(viewLifecycleOwner, Observer { successData->
                    if(successData != null)
                    {
                        getAlcoholFreeDayResponse = successData
                        //bind the data to the UI like selected months and dates
                        bindDataToUI()

                        updateIndexApiCall()
                    }
                }
                )
            }
        })
    }

    private fun bindDataToUI() {
        // Pre-select the dates in the CustomCalendarView
        val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        val dates = getAlcoholFreeDayResponse.dates.map { dateStr ->
            dateFormatter.parse(dateStr)
        }
        customCalendarView.setSelectedDates(dates)

        binding.tvMonthlyDrinkCount.text = getAlcoholFreeDayResponse.suggestedMonthlyDrinkCount.toString()
    }

    private fun showBulbDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.make_a_plan_screen_five_dialog, null)

        val closeButton = dialogView.findViewById<ImageView>(R.id.closeButton)

        val dialogBuilder = AlertDialog.Builder(requireContext(), R.style.CustomDialog)
            .setView(dialogView)

        val dialog = dialogBuilder.create()
        dialog.show()

        closeButton.setOnClickListener {
            dialog.dismiss()
        }
    }


    private fun showSetDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.make_a_plan_congrats_dilalog, null)

        val closeButton = dialogView.findViewById<ImageView>(R.id.closeButton)
        val introButton = dialogView.findViewById<AppCompatButton>(R.id.btn_seeTheIntro)

        val dialogBuilder = AlertDialog.Builder(requireContext(), R.style.CustomDialog)
            .setView(dialogView)

        val dialog = dialogBuilder.create()
        dialog.show()

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        introButton.setOnClickListener{
            dialog.dismiss()
            loadFragment(TakingControlFragment())
        }
    }

    private fun showBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.layout_drink_goals_bottomsheet, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        val numberPicker = bottomSheetView.findViewById<NumberPicker>(R.id.number_picker)
        numberPicker.minValue = 1
        numberPicker.maxValue = 10
        numberPicker.value = selectedNumber

        val saveButton = bottomSheetView.findViewById<ImageView>(R.id.saveButton)
        saveButton.setOnClickListener {
            selectedNumber = numberPicker.value
            binding.goalsTextView.text = selectedNumber.toString()

            // Assuming you have a way to get selected days count
            val selectedDaysCount = customCalendarView.getSelectedDatesSize()
            if (selectedDaysCount * 2 < selectedNumber) {
                showCustomDialog()
            }

            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun showCustomDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.make_a_play_dialog_layout)  // Make sure the name matches your dialog layout file
        dialog.setCancelable(true)

        val btnDismiss = dialog.findViewById<AppCompatButton>(R.id.btn_dismiss)
        val btnChangeGoal = dialog.findViewById<AppCompatButton>(R.id.btn_changeGoal)
        val btnClose = dialog.findViewById<ImageView>(R.id.btn_close)

        btnDismiss.setOnClickListener {
            dialog.dismiss()
        }

        btnChangeGoal.setOnClickListener {
            // Handle the change goal action
            dialog.dismiss()
        }

        btnClose.setOnClickListener {
            // Handle the change goal action
            dialog.dismiss()
        }



        dialog.show()
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun updateIndexApiCall() {
        val isCompleted = 1
        loginResponse?.loginDetails?.let {
            updateTakingControlIndexViewModel.updateTakingControlIndexData(
                it.clientID,
                courseTempId.toInt(),
                isCompleted,
                it.patientID,
                it.patientLocationID,
                accessToken
            )
        }
    }
}

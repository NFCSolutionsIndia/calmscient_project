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

package com.calmscient.activities
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.Interface.OnToggleChangeListener
import com.calmscient.R
import com.calmscient.adapters.PrivacyAdapter
import com.calmscient.databinding.ActivityPrivacyBinding
import com.calmscient.di.remote.PrivacyItemDataClass
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.di.remote.response.PatientConsent
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.GetPatientPrivacyDetailsViewModel
import com.calmscient.viewmodels.UpdatePatientConsentViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PrivacyBottomSheet : BottomSheetDialogFragment(), OnToggleChangeListener {

    lateinit var binding: ActivityPrivacyBinding
    private var privacyItems: List<PatientConsent> = emptyList()
    private lateinit var accessToken : String
    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonDialog: CommonAPICallDialog

    private val updatePatientConsentViewModel: UpdatePatientConsentViewModel by viewModels()
    private val getPatientPrivacyDetailsViewModel: GetPatientPrivacyDetailsViewModel by viewModels()
    private lateinit var loginResponse: LoginResponse

    private var currentToast: Toast? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityPrivacyBinding.inflate(layoutInflater)
        accessToken = SharedPreferencesUtil.getData(requireContext(), "accessToken", "")
        val loginJsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString<LoginResponse>(loginJsonString)
        customProgressDialog = CustomProgressDialog(requireContext())
        commonDialog = CommonAPICallDialog(requireContext())

        if(CommonClass.isNetworkAvailable(requireContext())){
            getPrivacyDataAPICall()
        }else{
            CommonClass.showInternetDialogue(requireContext())
        }

        binding.closePrivacy.setOnClickListener{
            dismiss()
        }

        binding.titlePrivacy.setText(R.string.privacy)

        return binding.root
    }

   /* override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Create a list of items to display in the RecyclerView
        val privacyItemList = listOf(
            PrivacyItemDataClass(getString(R.string.journaling), false),
            PrivacyItemDataClass(getString(R.string.course_work), true),
            PrivacyItemDataClass(getString(R.string.mood), true),
            PrivacyItemDataClass(getString(R.string.sleep), true),
            PrivacyItemDataClass(getString(R.string.medication), false),
            PrivacyItemDataClass(getString(R.string.phq_9_screening), true),
            PrivacyItemDataClass(getString(R.string.gad_7_screening), true),
            PrivacyItemDataClass(getString(R.string.audit_screening), true),
            PrivacyItemDataClass(getString(R.string.dast_screening), true),
            )
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewPrivacy)

        val adapter = context?.let { PrivacyAdapter(privacyItemList, it) }
        recyclerView.adapter = adapter

        recyclerView.layoutManager = LinearLayoutManager(requireContext())


    }*/

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewPrivacy)
        val adapter = PrivacyAdapter(privacyItems, requireContext(),this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onToggleChanged(item: PatientConsent, isChecked: Boolean) {

        //Toast.makeText(requireContext(), "Consent List ID: ${item.consentListId} State: $isChecked",Toast.LENGTH_SHORT).show()
        Log.d("PrivacyAdapter", "Consent List ID: ${item.consentListId} State: $isChecked")
        updateConsentApiCall(item,isChecked)
    }

    private fun updateConsentApiCall(item: PatientConsent, isChecked: Boolean){
        updatePatientConsentViewModel.clear()

        val flag = if (isChecked) 1 else 0
        updatePatientConsentViewModel.updatePatientConsent(item.clientId,item.consentListId,flag,item.patientId,item.plId, accessToken)
        observeConsentApiCall()
    }

    private fun observeConsentApiCall(){

        updatePatientConsentViewModel.loadingLiveData.observe(this, Observer { isLoading->
            if(isLoading){
                customProgressDialog.show(getString(R.string.loading))
            }else{
                customProgressDialog.dialogDismiss()
            }
        })

        updatePatientConsentViewModel.successLiveData.observe(this, Observer{ isSuccess->
            if(isSuccess){
                updatePatientConsentViewModel.saveResponseLiveData.observe(this, Observer { successData->
                    if(successData != null && successData.statusResponse.responseCode == 200 ){

                        /*commonDialog.showDialog(successData.statusResponse.responseMessage)
                        commonDialog.setOnDismissListener {
                            updatePatientConsentViewModel.saveResponseLiveData.postValue(null)
                        }*/

                        showToast(successData.statusResponse.responseMessage)
                    }
                })
            }else{
                updatePatientConsentViewModel.failureLiveData.observe(this, Observer { failureData->
                    if(failureData != null){
                        Toast.makeText(requireContext(),failureData, Toast.LENGTH_SHORT).show()
                    }
                })
            }
        })
    }

    private fun showToast(message: String) {
        currentToast?.cancel()
        currentToast = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
        currentToast?.show()
    }


    private fun getPrivacyDataAPICall(){
        getPatientPrivacyDetailsViewModel.clear()
        getPatientPrivacyDetailsViewModel.getPatientPrivacyDetails(loginResponse.loginDetails.clientID,loginResponse.loginDetails.patientID,loginResponse.loginDetails.patientLocationID,accessToken)
        observePrivacyAPICall()
    }

    private fun observePrivacyAPICall(){

        getPatientPrivacyDetailsViewModel.loadingLiveData.observe(this,Observer{
            if(it){
                customProgressDialog.show(getString(R.string.loading))
            }else{
                customProgressDialog.dialogDismiss()
            }
        })

        getPatientPrivacyDetailsViewModel.successLiveData.observe(this, Observer { isSuccess->
            if(isSuccess){
                getPatientPrivacyDetailsViewModel.saveResponseLiveData.observe(this, Observer { successData->

                    if (successData != null && successData.patientConsent.isNotEmpty()) {
                        setupRecyclerView(successData.patientConsent)
                    }
                })
            }
        })
    }

    private fun setupRecyclerView(privacyItems: List<PatientConsent>) {
        val recyclerView = binding.recyclerViewPrivacy
        val adapter = PrivacyAdapter(privacyItems, requireContext(), this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

}

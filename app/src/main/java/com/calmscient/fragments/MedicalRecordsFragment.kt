package com.calmscient.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.calmscient.R
import com.calmscient.activities.SettingsActivity
import com.calmscient.databinding.FragmentMyMedicalRecordsBinding
import com.calmscient.di.remote.request.MenuItemRequest
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.di.remote.response.MenuItem
import com.calmscient.di.remote.response.MenuItemsResponse
import com.calmscient.di.remote.response.ScreeningItem
import com.calmscient.di.remote.response.ScreeningResponse
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.utils.network.ServerTimeoutHandler
import com.calmscient.viewmodels.MenuItemViewModel
import com.calmscient.viewmodels.ScreeningViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MedicalRecordsFragment : Fragment() {
    private lateinit var binding: FragmentMyMedicalRecordsBinding

    @Inject
    lateinit var myMedicalMenuViewModel: MenuItemViewModel
    private val screeningsMenuViewModel: ScreeningViewModel by viewModels()


    private lateinit var screeningsResponseDate: List<ScreeningItem>

    private lateinit var menuResponseDate: List<MenuItem>

    private lateinit var medicalMenuResponseDate: List<MenuItem>
    private lateinit var menuItemRequest: MenuItemRequest
    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonDialog: CommonAPICallDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (CommonClass.isNetworkAvailable(requireContext())) {

                loadFragment(HomeFragment())
                //menuItemRequest = MenuItemRequest(1,0,1,1)
                // myMedicalMenuViewModel.fetchMenuItems(loginResponse.loginDetails.patientLocationID,0,loginResponse.loginDetails.patientID,loginResponse.loginDetails.clientID)
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
        binding = FragmentMyMedicalRecordsBinding.inflate(inflater, container, false)

        customProgressDialog = CustomProgressDialog(requireContext())

        commonDialog = CommonAPICallDialog(requireContext())

        // Retrieve JSON string from SharedPreferences and convert back to object
        val medicalMenuJsonString =
            SharedPreferencesUtil.getData(requireContext(), "myMedicalMenuResponse", "")
        val medicalMenuResponse = JsonUtil.fromJsonString<List<MenuItem>>(medicalMenuJsonString)

        val loginJsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        val loginResponse = JsonUtil.fromJsonString<LoginResponse>(loginJsonString)

        val menuJsonString = SharedPreferencesUtil.getData(requireContext(), "menuResponse", "")
        val menuResponse = JsonUtil.fromJsonString<List<MenuItem>>(menuJsonString)


        if (menuResponse.isNotEmpty()) {
            Log.d("Menu Response in MedicalRecordsFragment", "$menuResponse")
            binding.titleTextView.text = menuResponse[0].menuName

        }
        if (medicalMenuResponse.isNotEmpty() && menuResponse.size >= 2) {
            Log.d("Medical Response", "$medicalMenuResponse")
            binding.MedicationsText.text = medicalMenuResponse[0].menuName
            binding.UpcomingMedicalAppointmentsText.text = medicalMenuResponse[1].menuName
            binding.ScreeningsText.text = medicalMenuResponse[2].menuName

        }

        myMedicalMenuViewModel.loadingLiveData.observe(requireActivity()) { isLoading ->
            if (isLoading) {
                customProgressDialog.show("Loading...")
            } else {

                customProgressDialog.dialogDismiss()
            }
        }

        binding.medicationsLayout.setOnClickListener {
            //Toast.makeText(requireActivity(), "message", Toast.LENGTH_LONG).show()
            loadFragment(CalendarFragment())
        }
        binding.upcomingsMedicalAppointmentsCard.setOnClickListener {
            //Toast.makeText(requireActivity(), "Coming Soon", Toast.LENGTH_LONG).show()
            loadFragment(NextAppointmentsFragment())
        }
        binding.screeningsLayout.setOnClickListener {
            //loadFragment(ResultsFragment())
            if (CommonClass.isNetworkAvailable(requireContext())) {

                screeningsMenuViewModel.getScreeningList(
                    loginResponse.loginDetails.patientID,
                    loginResponse.loginDetails.clientID,
                    loginResponse.loginDetails.patientLocationID
                )
                loadFragment(ScreeningsFragment())
            } else {
                CommonClass.showInternetDialogue(requireContext())
            }


            //Toast.makeText(requireActivity(), "Coming Soon", Toast.LENGTH_LONG).show()
        }

        screeningsMenuViewModel.loadingLiveData.observe(requireActivity()) { isLoading ->
            if (isLoading) {
                customProgressDialog.show("Loading...")
            } else {

                customProgressDialog.dialogDismiss()
            }
        }

        screeningsMenuViewModel.screeningsResultLiveData.observe(requireActivity()) { isSuccess ->
            if (isSuccess) {

                screeningsResponseDate = screeningsMenuViewModel.screeningListLiveData.value!!

                val jsonString = JsonUtil.toJsonString(screeningsResponseDate)
                SharedPreferencesUtil.saveData(requireContext(), "screeningsResponse", jsonString)
            }
            else{
                screeningsMenuViewModel.errorLiveData.value?.let { failureMessage ->
                    failureMessage.let {
                        commonDialog.showDialog(
                            it
                        )
                    }
                }
                screeningsMenuViewModel.errorLiveData.value?.let { failureMessage ->
                    failureMessage.let{
                        ServerTimeoutHandler.handleTimeoutException(requireContext()) {
                            // Retry logic when the retry button is clicked
                            screeningsMenuViewModel.retryScreeningsFetchMenuItems()
                        }
                    }
                }
            }
        }


        binding.backIcon.setOnClickListener {

            if (CommonClass.isNetworkAvailable(requireContext())) {

                loadFragment(HomeFragment())
                //menuItemRequest = MenuItemRequest(1,0,1,1)
                // myMedicalMenuViewModel.fetchMenuItems(loginResponse.loginDetails.patientLocationID,0,loginResponse.loginDetails.patientID,loginResponse.loginDetails.clientID)
            } else {
                CommonClass.showInternetDialogue(requireContext())
            }


        }
        binding.icProfile.setOnClickListener {
            openSettingsActivity()
        }
        return binding.root
    }

    private fun openSettingsActivity() {
        val intent = Intent(activity, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
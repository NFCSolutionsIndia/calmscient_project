package com.calmscient.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.calmscient.R
import com.calmscient.adapters.SummaryOfProgressWorksAdapter
import com.calmscient.databinding.ProgressoncourseworkFragmentBinding
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.di.remote.response.PatientcourseWork
import com.calmscient.di.remote.response.SummaryOfCourseWorkResponse
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.GetSummaryOfCourseWorkViewModel
class ProgressOnCourseWorkFragment : Fragment() {
    private lateinit var binding: ProgressoncourseworkFragmentBinding


    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonAPICallDialog: CommonAPICallDialog
    private val getSummaryOfCourseWorkViewModel: GetSummaryOfCourseWorkViewModel by activityViewModels()
    private lateinit var summaryOfCourseWorkResponse: SummaryOfCourseWorkResponse
    private var loginResponse : LoginResponse? = null
    private  lateinit var accessToken : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            loadFragment(WeeklySummaryFragment())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ProgressoncourseworkFragmentBinding.inflate(inflater, container, false)

        commonAPICallDialog = CommonAPICallDialog(requireContext())
        customProgressDialog = CustomProgressDialog(requireContext())

        accessToken = SharedPreferencesUtil.getData(requireContext(), "accessToken", "")
        val loginJsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString<LoginResponse>(loginJsonString)


        binding.backIcon.setOnClickListener {
            loadFragment(WeeklySummaryFragment())
        }
        binding.needToTalkWithSomeOne.setOnClickListener {
            loadFragment(EmergencyResourceFragment())
        }


        if (CommonClass.isNetworkAvailable(requireContext()))
        {
            apiCall()
            observeViewModel()
        }
        else
        {
            CommonClass.showInternetDialogue(requireContext())
        }

        binding.backIcon.setOnClickListener{
            loadFragment(WeeklySummaryFragment())
        }
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun apiCall()
    {
        loginResponse?.loginDetails?.let { getSummaryOfCourseWorkViewModel.getSummaryOfCourseWork(it.patientID, accessToken) }

    }

    private fun observeViewModel()
    {

        getSummaryOfCourseWorkViewModel.loadingLiveData.observe(viewLifecycleOwner, Observer { isLoading ->
            if (isLoading) {
                customProgressDialog.dialogDismiss()
                customProgressDialog.show("Loading...")
            } else {
                customProgressDialog.dialogDismiss()
            }
        })

        getSummaryOfCourseWorkViewModel.saveResponseLiveData.observe(
            viewLifecycleOwner,
            Observer { successDate ->
                if (successDate != null) {
                    summaryOfCourseWorkResponse = successDate

                    val courseWorkList = successDate.patientcourseWorkList
                    setupRecyclerView(courseWorkList)

                  if(courseWorkList.isNotEmpty())
                  {
                      binding.totalPercentage.text = courseWorkList[0].completedPer.toString()
                      binding.progressbarCourseWork.progress = courseWorkList[0].completedPer.toInt()
                  }
                  else{

                      binding.totalPercentage.text = 0.toString()
                      binding.progressbarCourseWork.progress = 0
                  }

                    Log.d("CourseWork Response", "$successDate")

                    //handleApiResponse(summaryOfDASTResponse)
                }
            })

    }

    private fun setupRecyclerView(courseWorkList: List<PatientcourseWork>) {
        val adapter = SummaryOfProgressWorksAdapter(requireContext(), courseWorkList) { selectedCourseWork, position ->
            val fragment = ProgressOnCourseWorkCommonFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("selectedCourseWork", selectedCourseWork)
                    putInt("selectedPosition", position)
                }
            }
            loadFragment(fragment)
        }
        binding.courseProgressRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.courseProgressRecyclerView.adapter = adapter
        binding.totalPercentage.text = if(courseWorkList.isNotEmpty()) courseWorkList[0].completedPer.toString() else 0.toString()
    }


}

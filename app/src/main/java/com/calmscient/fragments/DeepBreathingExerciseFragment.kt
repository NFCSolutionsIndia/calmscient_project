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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.calmscient.R
import com.calmscient.databinding.DeepbreathingexerciseBinding
import com.calmscient.databinding.RunningexerciseBinding
import com.calmscient.di.remote.request.SavePatientExercisesFavoritesRequest
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.SavePatientExercisesFavoritesViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeepBreathingExerciseFragment(favourite: Int, source: String) : Fragment() {
    private lateinit var binding: DeepbreathingexerciseBinding
    private var isFavorite = favourite == 1
    private lateinit var favoritesIcon: ImageView

    private val savePatientExercisesFavoritesViewModel: SavePatientExercisesFavoritesViewModel by viewModels()
    private lateinit var loginResponse: LoginResponse

    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonDialog: CommonAPICallDialog
    private var fromSource = source

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if(fromSource == "Home"){
                loadFragment(HomeFragment())
            }else{
                loadFragment(ExerciseFragment())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DeepbreathingexerciseBinding.inflate(inflater, container, false)

        customProgressDialog = CustomProgressDialog(requireContext())
        commonDialog = CommonAPICallDialog(requireContext())

        val jsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString<LoginResponse>(jsonString)

        val res = SharedPreferencesUtil.getData(requireContext(), "breathingTechnicExercise", "")
        if(res.isNotEmpty()){
            isFavorite = res.toInt() == 1
        }else{
            isFavorite = false
        }

        val favoritesIcon = binding.favoritesIcon
        //Initially setting if it is favorite
        isFavorite = if (isFavorite) {
            favoritesIcon.setImageResource(R.drawable.heart_icon_fav) // Reset color
            false
        } else {
            favoritesIcon.setImageResource(R.drawable.mindfullexercise_heart__image)
            true
        }

        favoritesIcon.setOnClickListener {
            isFavorite = !isFavorite
            if (isFavorite) {
                favoritesIcon.setImageResource(R.drawable.mindfullexercise_heart__image) // Set your desired color
                favouritesAPICall(false)
                SharedPreferencesUtil.saveData(requireContext(),"breathingTechnicExercise",0.toString())
            } else {
                favoritesIcon.setImageResource(R.drawable.heart_icon_fav) // Reset color
                favouritesAPICall(true)
                SharedPreferencesUtil.saveData(requireContext(),"breathingTechnicExercise",1.toString())
            }
        }
        binding.menuicon.setOnClickListener {
            if(fromSource == "Home"){
                loadFragment(HomeFragment())
            }else{
                loadFragment(ExerciseFragment())
            }
        }
        binding.mindfulBE.setOnClickListener{
            loadFragment(MindfulBreathingExerciseFragment(fromSource))
        }
        binding.fourSevenEightBE.setOnClickListener{

            loadFragment(FourSevenEightBreathingExerciseFragment(fromSource))
        }
        binding.diaphragmaticBE.setOnClickListener{
            loadFragment(DiaphragmaticBreathingExerciseFragment(fromSource))
        }
        return binding.root
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun favouritesAPICall(isFavourite: Boolean) {
        savePatientExercisesFavoritesViewModel.clear()

        val isFav = if(isFavourite) 1 else 0
        val request = SavePatientExercisesFavoritesRequest(isFav,1, loginResponse.loginDetails.patientID,"Breathing technique")

        savePatientExercisesFavoritesViewModel.savePatientExercisesFavorites(request,loginResponse.token.access_token)

        Log.d("Favourite Resuest","$request")
        observeFavouritesAPICall()
    }

    private fun observeFavouritesAPICall(){

        savePatientExercisesFavoritesViewModel.loadingLiveData.observe(viewLifecycleOwner, Observer { isLoading->
            if(isLoading){
                customProgressDialog.show(getString(R.string.loading))
            }else{
                customProgressDialog.dialogDismiss()
            }
        })
        savePatientExercisesFavoritesViewModel.successLiveData.observe(viewLifecycleOwner, Observer { isSuccess->
            if(isSuccess){
                savePatientExercisesFavoritesViewModel.saveResponseLiveData.observe(viewLifecycleOwner, Observer { successData->
                    if(successData != null && successData.responseCode == 200){
                        Toast.makeText(requireContext(),successData.responseMessage,Toast.LENGTH_SHORT).show()
                    }
                })
            }
        })

    }
}

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


import android.content.DialogInterface
import android.content.Intent
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.calmscient.R
import com.calmscient.activities.BodyMovementExerciseActivity
import com.calmscient.activities.ButterflyHugExercisesActivity
import com.calmscient.activities.DancingExercisesActivity
import com.calmscient.activities.DeepBreathingExerciseActivity
import com.calmscient.activities.HandOverYourHeartActivity
import com.calmscient.activities.MindfulnessExercisesActivity
import com.calmscient.activities.RunningExerciseActivity
import com.calmscient.databinding.FragmentExerciseBinding
import com.calmscient.di.remote.request.GetPatientFavoritesRequest
import com.calmscient.di.remote.response.FavoriteItem
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.CommonClass
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.GetPatientFavouritesViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ExerciseFragment:Fragment() {
    private lateinit var binding:FragmentExerciseBinding


    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonDialog: CommonAPICallDialog
    private val getPatientFavouritesViewModel: GetPatientFavouritesViewModel by viewModels()
    private lateinit var loginResponse: LoginResponse

    private var mindfullness = -1
    private var progressiveMuscleRelaxation = -1
    private var touchButterflyHug = -1
    private var handOverYourHeart = -1
    private var mindfulWalking = -1
    private var movementDance = -1
    private var movementRunning = -1
    private var mindfulBodyMovement = -1
    private var breathingTechnic = -1
    private var fourSevenEightExercise = -1
    private var mindfulBreathingExercise = -1
    private var diaphragmaticBreathingExercise = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentExerciseBinding.inflate(inflater, container, false)

        customProgressDialog = CustomProgressDialog(requireContext())
        commonDialog = CommonAPICallDialog(requireContext())
        val jsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString<LoginResponse>(jsonString)


        if(CommonClass.isNetworkAvailable(requireContext())){
            getPatientFavoritesAPICall()
        }else{
            CommonClass.showInternetDialogue(requireContext())
        }


        binding.mindfulnessExerciseCard.setOnClickListener {
            // Toast.makeText(requireActivity(), "Coming Soon", Toast.LENGTH_SHORT).show()
            loadFragment(MindfulnessExercisesFragment(mindfullness,0))
        }
        binding.handOverYourHeartCard.setOnClickListener {
            loadFragment(HandOverYourHeartFragment(handOverYourHeart))
        }
        binding.butterflyCard.setOnClickListener {
            loadFragment(ButterflyHugExercisesFragment(touchButterflyHug))
        }
        binding.dancingExercisesCard.setOnClickListener {
            loadFragment(DancingExercisesFragment(movementDance))
        }
        binding.runningExerciseCard.setOnClickListener {
            loadFragment(RunningExerciseFragment(movementRunning))
        }
        binding.bodyMovementExerciseCard.setOnClickListener {
            loadFragment(BodyMovementExerciseFragment(mindfulBodyMovement))
        }
        binding.deepBreathingCard.setOnClickListener {
            loadFragment(DeepBreathingExerciseFragment(breathingTechnic,"Exercise"))
        }
        binding.walkingCard.setOnClickListener {
            loadFragment(MindfulWalkingExerciseFragment(mindfulWalking))
        }
        binding.muscleRelaxationCard.setOnClickListener {
            loadFragment(MuscleRelaxationExerciseFragment(progressiveMuscleRelaxation))
        }
        return binding.root
    }
    fun onBackPressed() {
        showExitConfirmationDialog()
    }
    private fun showExitConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.plz_confirm))
        builder.setMessage(getString(R.string.exit_app))
        builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
            // User clicked "Yes," so exit the app
            requireActivity().finishAffinity() // This closes the entire app
        }
        builder.setNegativeButton(getString(R.string.no)) { _, _ ->
            // User clicked "No," so dismiss the dialog and stay on the current page
        }
        builder.setOnCancelListener(DialogInterface.OnCancelListener {
            // User canceled the dialog, do nothing
        })
        builder.show()
    }
    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun getPatientFavoritesAPICall(){

        getPatientFavouritesViewModel.clear()
        val request = GetPatientFavoritesRequest(loginResponse.loginDetails.patientID)
        getPatientFavouritesViewModel.getPatientFavourites(request, loginResponse.token.access_token )

        observeGetFavoritesAPICall()
    }

    private fun observeGetFavoritesAPICall() {
        getPatientFavouritesViewModel.loadingLiveData.observe(viewLifecycleOwner, Observer { isLoading ->
            if (isLoading) {
                customProgressDialog.show(getString(R.string.loading))
            } else {
                customProgressDialog.dialogDismiss()
            }
        })

        getPatientFavouritesViewModel.successLiveData.observe(viewLifecycleOwner, Observer { isSuccess ->
            if (isSuccess != null) {

                getPatientFavouritesViewModel.saveResponseLiveData.observe(viewLifecycleOwner, Observer { successData ->
                    if (successData != null) {

                        val exercisesList = successData.favorites.filter { it.isFromExercises == 1 }

                        exercisesList.forEach { exerciseFavoriteItem ->
                            when (exerciseFavoriteItem.title) {
                                getString(R.string.mindfulness_exercises) -> {
                                    mindfullness = 1
                                    Log.d("Favorites", "Mindfulness Exercise is favorite: $mindfullness")
                                }
                                getString(R.string.butterflyhug_exercises) -> {
                                    touchButterflyHug = 1
                                    Log.d("Favorites", "Butterfly Hug Exercise is favorite: $touchButterflyHug")
                                }
                                getString(R.string.handover_esxercises) -> {
                                    handOverYourHeart = 1
                                    Log.d("Favorites", "Hand Over Your Heart Exercise is favorite: $handOverYourHeart")
                                }
                                getString(R.string.mindfulwalking_exercises) -> {
                                    mindfulWalking = 1
                                    Log.d("Favorites", "Mindful Walking Exercise is favorite: $mindfulWalking")
                                }
                                getString(R.string.dance_exercises) -> {
                                    movementDance = 1
                                    Log.d("Favorites", "Dance Exercise is favorite: $movementDance")
                                }
                                getString(R.string.running_exercises) -> {
                                    movementRunning = 1
                                    Log.d("Favorites", "Running Exercise is favorite: $movementRunning")
                                }
                                getString(R.string.body_moment_exercies) -> {
                                    mindfulBodyMovement = 1
                                    Log.d("Favorites", "Body Movement Exercise is favorite: $mindfulBodyMovement")
                                }
                                getString(R.string.musclerelaxation_exercises) -> {
                                    progressiveMuscleRelaxation = 1
                                    Log.d("Favorites", "Muscle Relaxation Exercise is favorite: $progressiveMuscleRelaxation")
                                }
                                getString(R.string.breathing_technic) -> {
                                    breathingTechnic = 1
                                    Log.d("Favorites", "Breathing Technique is favorite: $breathingTechnic")
                                }
                                getString(R.string._4_7_8_breathing_exercise)->{
                                    fourSevenEightExercise = 1
                                    SharedPreferencesUtil.saveData(requireContext(),"fourSevenEightExercise",fourSevenEightExercise.toString())
                                }
                                getString(R.string.mindful_breathing_exercise)->{
                                    mindfulBreathingExercise = 1
                                    SharedPreferencesUtil.saveData(requireContext(),"mindfulBreathingExercise",mindfulBreathingExercise.toString())
                                }
                                getString(R.string.diaphragmatic_breathing_exercise)->{
                                    diaphragmaticBreathingExercise = 1
                                    SharedPreferencesUtil.saveData(requireContext(),"diaphragmaticBreathingExercise",diaphragmaticBreathingExercise.toString())
                                }
                            }
                        }
                    }
                })
            }
        })
    }

    private fun resetFavoritesData(){
        mindfullness = -1
        progressiveMuscleRelaxation = -1
        touchButterflyHug = -1
        handOverYourHeart = -1
        mindfulWalking = -1
        movementDance = -1
        movementRunning = -1
        mindfulBodyMovement = -1
        breathingTechnic = -1
        fourSevenEightExercise = -1
        diaphragmaticBreathingExercise = -1
        mindfulBreathingExercise = -1
    }

    override fun onResume() {
        super.onResume()
        resetFavoritesData()
    }

}
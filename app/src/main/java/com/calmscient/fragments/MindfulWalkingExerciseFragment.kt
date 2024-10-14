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

import android.app.ProgressDialog
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.calmscient.R
import com.calmscient.activities.GlossaryActivity
import com.calmscient.activities.WaveformView
import com.calmscient.databinding.MindfulWalkingExercisesBinding
import com.calmscient.di.remote.request.SavePatientExercisesFavoritesRequest
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SavePreferences
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.SavePatientExercisesFavoritesViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MindfulWalkingExerciseFragment : Fragment(), MediaPlayer.OnPreparedListener,
    MediaPlayer.OnBufferingUpdateListener {
    private lateinit var binding: MindfulWalkingExercisesBinding
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var waveformView: WaveformView
    private lateinit var playButton: ImageView
    private lateinit var handler: Handler
    private var isMediaPlayerInitialized = false
    lateinit var savePrefData: SavePreferences
    var isFavorite = true
    private lateinit var loadingDialog: ProgressDialog

    private val savePatientExercisesFavoritesViewModel: SavePatientExercisesFavoritesViewModel by viewModels()
    private lateinit var loginResponse: LoginResponse

    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonDialog: CommonAPICallDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*requireActivity().onBackPressedDispatcher.addCallback(this){
            loadFragment(WeeklySummaryFragment())
        }*/
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MindfulWalkingExercisesBinding.inflate(inflater, container, false)
        val favoritesIcon = binding.favoritesIcon
        savePrefData = SavePreferences(requireContext())

        customProgressDialog = CustomProgressDialog(requireContext())
        commonDialog = CommonAPICallDialog(requireContext())

        val jsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString<LoginResponse>(jsonString)


        favoritesIcon.setOnClickListener {
            isFavorite = !isFavorite
            if (isFavorite) {
                favoritesIcon.setImageResource(R.drawable.mindfullexercise_heart__image) // Set your desired color
                favouritesAPICall(false)
            } else {
                favoritesIcon.setImageResource(R.drawable.heart_icon_fav) // Reset color
                favouritesAPICall(true)
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backIcon.setOnClickListener {
            loadFragment(ExerciseFragment())
        }
        waveformView = requireActivity().findViewById(R.id.waveformView)
        playButton = requireActivity().findViewById(R.id.playButton)
        handler = Handler()
        mediaPlayer = MediaPlayer()
        mediaPlayer.setOnPreparedListener(this)
        mediaPlayer.setOnBufferingUpdateListener(this)
        binding.audioProgressBar.indeterminateDrawable =
            resources.getDrawable(R.drawable.circular_progressbar)

        // seekBar.max = mediaPlayer.duration
        binding.playButton.setOnClickListener {
            /*if (!isMediaPlayerInitialized) {
                loadingDialog.setCanceledOnTouchOutside(false)
                loadingDialog.show()
            }*/
            if (!isMediaPlayerInitialized) {
                /*loadingDialog.setCanceledOnTouchOutside(false)
                loadingDialog.show()*/
                // Initialize the mediaPlayer if it hasn't been initialized yet
                /* mediaPlayer = MediaPlayer.create(
                     requireActivity(),
                     Uri.parse("https://calmscient-videos.s3.ap-south-1.amazonaws.com/5+Mindful+walking+with+music+English.mp3")
                 )*/
                binding.audioProgressBar.visibility = View.VISIBLE
                if (savePrefData.getAslLanguageState() == true) {
                    mediaPlayer.setDataSource("https://calmscient-videos.s3.ap-south-1.amazonaws.com/5+Mindful+walking+with+music+English.mp3")
                } else if (savePrefData.getSpanLanguageState() == true) {
                    mediaPlayer.setDataSource("https://calmscient-videos.s3.ap-south-1.amazonaws.com/5+Mindful+walking+with+music+Spanish.mp3")
                } else {
                    mediaPlayer.setDataSource("https://calmscient-videos.s3.ap-south-1.amazonaws.com/5+Mindful+walking+with+music+English.mp3")
                }
                mediaPlayer.prepareAsync()
                mediaPlayer.setOnPreparedListener { mp ->
                    // Dismiss the buffering dialog when the media player is prepared
                    //loadingDialog.dismiss()
                    mp.start()
                    playButton.setImageResource(R.drawable.ic_audio_pause)
                    waveformView.visibility = View.VISIBLE
                    updateWaveformView()
                }
                mediaPlayer.setOnCompletionListener {
                    // Playback completed, reset the play button icon
                    playButton.setImageResource(R.drawable.ic_audio_play)
                }
                isMediaPlayerInitialized = true
                // Show the buffering dialog when the play button is clicked
                /*loadingDialog.setCanceledOnTouchOutside(false)
                loadingDialog.show()*/
            } else {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                    playButton.setImageResource(R.drawable.ic_audio_play)
                } else {
                    mediaPlayer.start()
                    playButton.setImageResource(R.drawable.ic_audio_pause)
                    waveformView.visibility = View.VISIBLE
                    updateWaveformView()
                    // No need to show the dialog here since it was already shown when the play button was clicked.
                }
            }
        }

        binding.audioBackward.setOnClickListener {
            skipBackward()
        }

        binding.audioForward.setOnClickListener {
            skipForward()
        }

        binding.backIcon.setOnClickListener {
            //onBackPressed()
            loadFragment(ExerciseFragment())
        }
        /*
                binding.informationIcon.setOnClickListener{
                    showInformationDialog()
                }*/
    }

    private fun updateWaveformView() {
        binding.audioProgressBar.visibility = View.GONE
        val audioDuration = mediaPlayer.duration
        val updateInterval = 100 // Update more frequently for smoother progression

        handler.post(object : Runnable {
            override fun run() {
                val currentPosition = mediaPlayer.currentPosition
                val progress = currentPosition.toFloat() / audioDuration.toFloat()
                // Set the waveform progress
                waveformView.setWaveformProgress(progress)
                // Check if playback has completed
                if (currentPosition < audioDuration) {
                    handler.postDelayed(this, updateInterval.toLong())
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        handler.removeCallbacksAndMessages(null)
    }

    /*override fun onBackPressed() {
        if (requireActivity().supportFragmentManager.backStackEntryCount > 0) {
            requireActivity().supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }*/

    private fun skipForward() {
        val currentPosition = mediaPlayer.currentPosition
        val newPosition = currentPosition + 10000 // Skip forward by 10 seconds (in milliseconds)
        if (newPosition <= mediaPlayer.duration) {
            mediaPlayer.seekTo(newPosition)
        }
    }

    private fun skipBackward() {
        val currentPosition = mediaPlayer.currentPosition
        val newPosition = currentPosition - 10000 // Skip backward by 10 seconds (in milliseconds)
        if (newPosition >= 0) {
            mediaPlayer.seekTo(newPosition)
        }
    }

    override fun onPause() {
        super.onPause()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }

    /*private fun showInformationDialog() {
        val dialogView = LayoutInflater.from(requireActivity()).inflate(R.layout.audio_information_dialog, null)
        //  val infoTextView = dialogView.findViewById<TextView>(R.id.dialogTextView)
        val closeButton = dialogView.findViewById<ImageView>(R.id.closeButton)
        val titleTextView = dialogView.findViewById<TextView>(R.id.titleTextView)

        *//*  // Retrieve the dialogText from intent extras
          val dialogText = intent.getStringExtra("dialogText")

          // Set the content of the dialog using dialogText
          infoTextView.text = dialogText*//*
        titleTextView.text = getString(R.string.information)

        val dialogBuilder = AlertDialog.Builder(this, R.style.CustomDialog)
            .setView(dialogView)

        val dialog = dialogBuilder.create()
        dialog.show()

        // Handle the close button click
        closeButton.setOnClickListener {
            dialog.dismiss()
        }
    }*/
    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onPrepared(mp: MediaPlayer?) {
        mediaPlayer.start()
    }

    override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
        // Show the custom loader when buffering is in progress
        if (percent < 100) {
            //binding.audioProgressBar.visibility = View.VISIBLE
        } else {
            // Hide the custom loader when buffering is complete
            //binding.audioProgressBar.visibility = View.GONE
        }
    }

    private fun favouritesAPICall(isFavourite: Boolean) {
        savePatientExercisesFavoritesViewModel.clear()

        val isFav = if(isFavourite) 1 else 0
        val request = SavePatientExercisesFavoritesRequest(isFav,1, loginResponse.loginDetails.patientID,"Mindful walking")

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
                        Toast.makeText(requireContext(),successData.responseMessage, Toast.LENGTH_SHORT).show()
                    }
                })
            }
        })

    }
}
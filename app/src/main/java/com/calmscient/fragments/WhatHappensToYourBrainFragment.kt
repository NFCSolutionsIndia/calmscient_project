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

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.calmscient.R
import com.calmscient.databinding.FragmentModerationDrinkingBinding
import com.calmscient.databinding.FragmentWhatHappensToYourBrainBinding
import com.calmscient.di.remote.BasicKnowledgeItem
import com.calmscient.di.remote.request.SavePatientExercisesFavoritesRequest
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SavePreferences
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.BasicKnowledgeSharedViewModel
import com.calmscient.viewmodels.SavePatientExercisesFavoritesViewModel
import com.calmscient.viewmodels.UpdateBasicKnowledgeIndexDataViewModel
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WhatHappensToYourBrainFragment(source : String) : Fragment() {

    private lateinit var binding: FragmentWhatHappensToYourBrainBinding

    private val updateBasicKnowledgeIndexDataViewModel: UpdateBasicKnowledgeIndexDataViewModel by viewModels()
    private val sharedViewModel: BasicKnowledgeSharedViewModel by activityViewModels()
    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonDialog: CommonAPICallDialog
    private lateinit var accessToken: String
    private lateinit var loginResponse: LoginResponse
    private lateinit var itemTemp: BasicKnowledgeItem
    private lateinit var playerView: PlayerView
    private lateinit var player: ExoPlayer
    private var isVideoPlaying = true
    private var isFavorite = false
    private var fromSource = source

    private lateinit var savePrefData: SavePreferences
    private val savePatientExercisesFavoritesViewModel: SavePatientExercisesFavoritesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWhatHappensToYourBrainBinding.inflate(inflater, container, false)

        accessToken = SharedPreferencesUtil.getData(requireContext(), "accessToken", "")
        customProgressDialog = CustomProgressDialog(requireContext())
        commonDialog = CommonAPICallDialog(requireContext())

        val loginJsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString(loginJsonString)

        val res = SharedPreferencesUtil.getData(requireContext(), "whatHappenToYourBrain", "")
        if(res.isNotEmpty()){
            isFavorite = res.toInt() == 1
        }else{
            isFavorite = false
            isFavorite = fromSource == "Home"
        }

        binding.backIcon.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        savePrefData = SavePreferences(requireContext())

        // Load thumbnail from URL using Glide
        Glide.with(this)
            .load("https://calmscient.blob.core.windows.net/taking-control-basic-knowledge-thumbnails/WhatHappensToYourBrainWhenYouDrink.png")
            .into(binding.videoThumbnail)

        // Initialize ExoPlayer
        player = SimpleExoPlayer.Builder(requireContext()).build()
        playerView = binding.playerViewLayout
        playerView.player = player

        // Set up play button on the thumbnail
        binding.playThumbnailButton.setOnClickListener {
            // Hide the thumbnail and play button
            binding.playThumbnailButton.visibility = View.GONE
            binding.videoThumbnail.visibility = View.GONE

            // Show the player
            binding.playerViewLayout.visibility = View.VISIBLE

            // Prepare the media item and load the video
            val mediaUri = if (savePrefData.getLanguageMode() == "en") {
                Uri.parse("https://calmscient.blob.core.windows.net/course/Tipsy_truth_with_subtitle.mp4")
            } else {
                Uri.parse("https://calmscient.blob.core.windows.net/exercises-videos/SPN%20Tipsy%20truth.mp4")
            }

            val mediaItem = MediaItem.fromUri(mediaUri)

            // Prepare the player with the media item
            player.setMediaItem(mediaItem)

            // Add a listener to start playing once the media is ready
            player.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        // Start the video when it's ready
                        player.playWhenReady = true
                    }
                }
            })

            // Prepare the player asynchronously (this will load the media)
            player.prepare()
        }


        // Hide thumbnail when video starts playing
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY || state == Player.STATE_BUFFERING) {
                    // Hide the thumbnail and show the player
                    binding.videoThumbnail.visibility = View.GONE
                    binding.playerViewLayout.visibility = View.VISIBLE
                }
                if (state == Player.STATE_ENDED) {
                    //  playPauseIcon.setImageResource(R.drawable.ic_play_icon)
                    isVideoPlaying = false
                }
            }
        })

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
                SharedPreferencesUtil.saveData(requireContext(),"whatHappenToYourBrain",0.toString())
            } else {
                favoritesIcon.setImageResource(R.drawable.heart_icon_fav) // Reset color
                favouritesAPICall(true)
                SharedPreferencesUtil.saveData(requireContext(),"whatHappenToYourBrain",1.toString())
            }
        }

        sharedViewModel.selectedItem.observe(viewLifecycleOwner, Observer { item ->
            if (item != null) {
                Log.d("WhatHappensToYourBrainFragment", "Name: ${item.name}, SectionId: ${item.sectionId}")
                itemTemp = item
                updateBasicKnowledgeAPICall()
            }
        })

        initializeBinding(binding.root)

        binding.completeButton.setOnClickListener{
            requireActivity().supportFragmentManager.popBackStack()
        }

        return binding.root
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        val isPortrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT

        // Change the layout based on the new orientation
        if (isPortrait) {
            requireActivity().setContentView(R.layout.fragment_what_happens_to_your_brain)
            binding = FragmentWhatHappensToYourBrainBinding.bind(requireActivity().findViewById(R.id.root_layout))
            rebindViews()
        } else {
            requireActivity().setContentView(R.layout.activity_player_landscape)
            binding = FragmentWhatHappensToYourBrainBinding.bind(requireActivity().findViewById(R.id.root_layout))
            rebindViews()
        }
    }

    private fun rebindViews() {
        playerView = binding.playerViewLayout
        playerView.player = player
        binding.favoritesIcon.setOnClickListener {
            isFavorite = !isFavorite
            if (isFavorite) {
                binding.favoritesIcon.setImageResource(R.drawable.ic_favorites_icon)
            } else {
                binding.favoritesIcon.setImageResource(R.drawable.ic_favorites_red)
            }
        }
        binding.backIcon.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        initializeBinding(binding.root)
    }

    private fun updateBasicKnowledgeAPICall() {
        updateBasicKnowledgeIndexDataViewModel.updateBasicKnowledgeIndexData(
            1,
            loginResponse.loginDetails.patientID,
            itemTemp.sectionId,
            accessToken
        )
        observeBasicKnowledgeViewModel()
    }

    private fun observeBasicKnowledgeViewModel() {
        updateBasicKnowledgeIndexDataViewModel.loadingLiveData.observe(viewLifecycleOwner, Observer { isLoading ->
            if (isLoading) {
                customProgressDialog.show(getString(R.string.loading))
            } else {
                customProgressDialog.dialogDismiss()
            }
        })
    }

    private fun initializeBinding(rootView: View) {
        binding.orientationBtn.setOnClickListener {
            toggleOrientation()
        }

        binding.backBtn.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun toggleOrientation() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            requireActivity().setContentView(R.layout.activity_player_landscape)
            binding = FragmentWhatHappensToYourBrainBinding.bind(requireActivity().findViewById(R.id.root_layout))
            rebindViews()
        } else {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            requireActivity().setContentView(R.layout.fragment_what_happens_to_your_brain)
            binding = FragmentWhatHappensToYourBrainBinding.bind(requireActivity().findViewById(R.id.root_layout))
            rebindViews()
        }
    }

    override fun onPause() {
        super.onPause()
        playerView.player!!.playWhenReady = false
    }

    override fun onStop() {
        super.onStop()
        playerView.player!!.release()
    }

    override fun onResume() {
        super.onResume()
        playerView.player!!.playWhenReady = true
    }

    override fun onDestroy() {
        super.onDestroy()
        playerView.player!!.playbackState
    }

    private fun favouritesAPICall(isFavourite: Boolean) {
        savePatientExercisesFavoritesViewModel.clear()

        val isFav = if(isFavourite) 1 else 0
        val request = SavePatientExercisesFavoritesRequest(isFav,1, loginResponse.loginDetails.patientID,"What happens to your brain when you drink?")

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

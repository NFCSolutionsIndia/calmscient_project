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

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.calmscient.R
import com.calmscient.databinding.FragmentDiaphragmaticBreathingExerciseBinding
import com.calmscient.di.remote.request.SavePatientExercisesFavoritesRequest
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SavePreferences
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.SavePatientExercisesFavoritesViewModel
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DiaphragmaticBreathingExerciseFragment(source: String)  : Fragment() {

    private lateinit var binding: FragmentDiaphragmaticBreathingExerciseBinding
    private var isFavorite = true
    private lateinit var playerView: PlayerView
    private lateinit var player: ExoPlayer
    private var isVideoPlaying = true
    lateinit var savePrefData: SavePreferences
    private var fromSource = source

    private val savePatientExercisesFavoritesViewModel: SavePatientExercisesFavoritesViewModel by viewModels()
    private lateinit var loginResponse: LoginResponse

    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var commonDialog: CommonAPICallDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentDiaphragmaticBreathingExerciseBinding.inflate(inflater,container,false)
        val res = SharedPreferencesUtil.getData(requireContext(), "diaphragmaticBreathingExercise", "")
        if(res.isNotEmpty()){
            isFavorite = res.toInt() == 1
        }

        customProgressDialog = CustomProgressDialog(requireContext())
        commonDialog = CommonAPICallDialog(requireContext())

        val jsonString = SharedPreferencesUtil.getData(requireContext(), "loginResponse", "")
        loginResponse = JsonUtil.fromJsonString<LoginResponse>(jsonString)
        savePrefData = SavePreferences(requireContext())


        // Load thumbnail from URL using Glide
        Glide.with(this)
            .load("https://calmscient.blob.core.windows.net/excercises-breathing-technique-thumbnails/478-excercise-video.png")
            .into(binding.videoThumbnail)


        // Thumbnail ImageView
        val videoThumbnail = binding.videoThumbnail

        // Initialize ExoPlayer
        player = SimpleExoPlayer.Builder(requireContext()).build()
        playerView = binding.playerViewLayout
        playerView.player = player

        /* // Set the media item and thumbnail
         val languageMode = savePrefData.getLanguageMode()
         val videoUri = if (languageMode == "en") {
             Uri.parse("https://calmscient.blob.core.windows.net/exercises-videos/4-7-8Breathing.mp4")
         } else {
             Uri.parse("https://calmscient.blob.core.windows.net/exercises-spanish-videos-audios/Spanish4-7-8Breathing.mp4")
         }*/
        /*player.setMediaItem(MediaItem.fromUri(videoUri))*/

        // Set up play button on the thumbnail
        binding.playThumbnailButton.setOnClickListener {
            // Hide the thumbnail and play button
            binding.playThumbnailButton.visibility = View.GONE
            binding.videoThumbnail.visibility = View.GONE

            // Show the player
            binding.playerViewLayout.visibility = View.VISIBLE

            // Prepare the media item and load the video
            val mediaUri = if (savePrefData.getLanguageMode() == "en") {
                Uri.parse("https://calmscient.blob.core.windows.net/exercises-videos/Diaphragmaticbreathing.mp4")
            } else {
                Uri.parse("https://calmscient.blob.core.windows.net/exercises-spanish-videos-audios/SpanishDiaphragmaticbreathing.mp4")
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
            } else {
                favoritesIcon.setImageResource(R.drawable.heart_icon_fav) // Reset color
                favouritesAPICall(true)
            }
        }


        /*player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    //  playPauseIcon.setImageResource(R.drawable.ic_play_icon)
                    isVideoPlaying = false
                }
                if (state == Player.STATE_READY || state == Player.STATE_BUFFERING) {
                    binding.videoThumbnail.visibility = View.GONE
                }
            }
        })*/

        binding.backIcon.setOnClickListener{
            loadFragment(DeepBreathingExerciseFragment(0,fromSource))
        }

        return binding.root

    }
    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
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
        val request = SavePatientExercisesFavoritesRequest(isFav,1, loginResponse.loginDetails.patientID,"Diaphragmatic breathing exercise")

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
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
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.calmscient.R
import com.calmscient.databinding.FragmentModerationDrinkingBinding
import com.calmscient.databinding.FragmentWhatHappensToYourBrainBinding
import com.calmscient.di.remote.BasicKnowledgeItem
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.utils.CommonAPICallDialog
import com.calmscient.utils.CustomProgressDialog
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil
import com.calmscient.viewmodels.BasicKnowledgeSharedViewModel
import com.calmscient.viewmodels.UpdateBasicKnowledgeIndexDataViewModel
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WhatHappensToYourBrainFragment : Fragment() {

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
    private var isFavorite = true

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

        binding.backIcon.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        player = SimpleExoPlayer.Builder(requireContext()).build()
        playerView = binding.playerViewLayout
        playerView.player = player
        player.setMediaItem(MediaItem.fromUri(Uri.parse("https://calmscient.blob.core.windows.net/course/Tipsy_truth_with_subtitle.mp4")))
        player.playWhenReady = true

        binding.favoritesIcon.setOnClickListener {
            isFavorite = !isFavorite
            if (isFavorite) {
                binding.favoritesIcon.setImageResource(R.drawable.ic_favorites_icon)
            } else {
                binding.favoritesIcon.setImageResource(R.drawable.ic_favorites_red)
            }
        }

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    isVideoPlaying = false
                }
            }
        })

        sharedViewModel.selectedItem.observe(viewLifecycleOwner, Observer { item ->
            if (item != null) {
                Log.d("WhatHappensToYourBrainFragment", "Name: ${item.name}, SectionId: ${item.sectionId}")
                itemTemp = item
                updateBasicKnowledgeAPICall()
            }
        })

        initializeBinding(binding.root)

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
}

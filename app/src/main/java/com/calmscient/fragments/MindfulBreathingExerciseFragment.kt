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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import com.calmscient.R
import com.calmscient.databinding.FragmentMindfulBreathingExerciseBinding
import com.calmscient.utils.common.SavePreferences
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView


class MindfulBreathingExerciseFragment : Fragment() {

    private lateinit var binding : FragmentMindfulBreathingExerciseBinding
    private var isFavorite = true
    private lateinit var playerView: PlayerView
    private lateinit var player: ExoPlayer
    private var isVideoPlaying = true
    lateinit var savePrefData: SavePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this){
            loadFragment(DeepBreathingExerciseFragment())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMindfulBreathingExerciseBinding.inflate(inflater,container,false)

        player = SimpleExoPlayer.Builder(requireContext()).build()
        playerView = binding.playerViewLayout
        savePrefData = SavePreferences(requireContext())
        playerView.player = player
        val languageMode = savePrefData.getLanguageMode()
        if(languageMode == "en") {
            player.setMediaItem(MediaItem.fromUri(Uri.parse("https://calmscient.blob.core.windows.net/exercises-videos/Mindfulbreathing.mp4")))
        }
        else{
            player.setMediaItem(MediaItem.fromUri(Uri.parse("https://calmscient.blob.core.windows.net/exercises-spanish-videos-audios/SpanishMindfulbreathing.mp4")))
        }
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
                    //  playPauseIcon.setImageResource(R.drawable.ic_play_icon)
                    isVideoPlaying = false
                }
            }
        })


        binding.backIcon.setOnClickListener{
            loadFragment(DeepBreathingExerciseFragment())
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


}
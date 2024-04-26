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

package com.calmscient.adapters

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.R
import com.calmscient.activities.AnxietyHideFragment
import com.calmscient.activities.AnxietyQuizActivity
import com.calmscient.activities.PlayerActivity
import com.calmscient.activities.RecognizeActivity
import com.calmscient.di.remote.ItemType
import com.calmscient.di.remote.CardItemDataClass
import com.calmscient.fragments.AnxietyAndAlcoholSubstancesFragment
import com.calmscient.fragments.AnxietyAndDietFragment
import com.calmscient.fragments.AnxietyAndSleepFragment
import com.calmscient.fragments.AnxietyBiasedThinkingFragment
import com.calmscient.fragments.AnxietyCalmingBodyFragment
import com.calmscient.fragments.AnxietyPostponeWorry
import com.calmscient.fragments.AnxietyQuestionsFragment
import com.calmscient.fragments.AnxietyQuizFragment
import com.calmscient.fragments.AnxietyQuizLessonsFragment
import com.calmscient.fragments.AnxietyRestructureBiased
import com.calmscient.fragments.AudioPlayerFragment
import com.calmscient.fragments.FastPaceFragment
import com.calmscient.fragments.MakeAPlanFragment
import com.calmscient.fragments.MakingConnectionFragment
import com.calmscient.fragments.ManagingStressToReduceAnxietyFragment
import com.calmscient.fragments.PlayerFragment
import com.calmscient.fragments.PostponeWorryFirstScreen
import com.calmscient.fragments.RecognizeFragment

class AnxietyIntroductionAdapter(private val diffCallback: DiffUtil.ItemCallback<CardItemDataClass>, private val fragmentManager: FragmentManager) :
    ListAdapter<CardItemDataClass, AnxietyIntroductionAdapter.ViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.anxiety_item_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cardItem = getItem(position)
        holder.bind(cardItem)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val contentIcon: ImageView = itemView.findViewById(R.id.imageView)
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val tickImageView: ImageView = itemView.findViewById(R.id.tickImageView)
        private val introCardView: CardView = itemView.findViewById(R.id.anxietyCard)

        fun bind(cardItem: CardItemDataClass) {
            val context = itemView.context

            contentIcon.setImageResource(cardItem.contentIcons[0])
            descriptionTextView.text = cardItem.description

            if (cardItem.isCompleted) {
                tickImageView.visibility = View.VISIBLE
            } else {
                tickImageView.visibility = View.GONE
            }
            itemView.setOnClickListener {

                val fragmentManager = fragmentManager //(context as FragmentActivity).supportFragmentManager

                when {
                    cardItem.availableContentTypes.contains(ItemType.AUDIO) -> {
                        if (cardItem.audioResourceId != null) {
                            if (cardItem.description == context.getString(R.string.meet_nora_austin)) {
                                /* val intent = Intent(context, AudioPlayerActivity::class.java)
                                intent.putExtra("audioResourceId", cardItem.audioResourceId)
                                intent.putExtra("heading", cardItem.heading)
                                intent.putExtra("description", cardItem.description)
                                intent.putExtra("summary", cardItem.summary)
                                intent.putExtra("dialogText", cardItem.dialogText)
                                context.startActivity(intent)*/

                                replaceFragmentWithPlayerFragment(
                                    fragmentManager,
                                    cardItem.videoResourceId,
                                    cardItem.audioResourceId,
                                    cardItem.heading,
                                    cardItem.description,
                                    cardItem.summary,
                                    cardItem.dialogText,
                                    AudioPlayerFragment()
                                )

                            } else if (cardItem.description == context.getString(R.string.moral_deficiency)) {
                                /* val intent = Intent(context, AudioPlayerActivity::class.java)
                                 intent.putExtra("audioResourceId", cardItem.audioResourceId)
                                 intent.putExtra("description", cardItem.description)
                                 *//*intent.putExtra("heading", cardItem.heading)
                        intent.putExtra("summary", cardItem.summary)
                        intent.putExtra("dialogText", cardItem.dialogText)*//*
                        context.startActivity(intent)*/

                                replaceFragmentWithPlayerFragment(
                                    fragmentManager,
                                    cardItem.videoResourceId,
                                    cardItem.audioResourceId,
                                    cardItem.heading,
                                    cardItem.description,
                                    cardItem.summary,
                                    cardItem.dialogText,
                                    AudioPlayerFragment()
                                )

                            } else if (cardItem.description == context.getString(R.string.anxiety_worry)) {
                                /*  val intent = Intent(context, AudioPlayerActivity::class.java)
                                  intent.putExtra("audioResourceId", cardItem.audioResourceId)
                                  intent.putExtra("heading", cardItem.heading)
                                  intent.putExtra("description", cardItem.description)
                                  intent.putExtra("summary", cardItem.summary)
                                  intent.putExtra("dialogText", cardItem.dialogText)
                                  context.startActivity(intent)*/

                                replaceFragmentWithPlayerFragment(
                                    fragmentManager,
                                    cardItem.videoResourceId,
                                    cardItem.audioResourceId,
                                    cardItem.heading,
                                    cardItem.description,
                                    cardItem.summary,
                                    cardItem.dialogText,
                                    AudioPlayerFragment()
                                )
                            } else if (cardItem.description == context.getString(R.string.anxious_mind)) {
                                /*val intent = Intent(context, AudioPlayerActivity::class.java)
                                intent.putExtra("audioResourceId", cardItem.audioResourceId)
                                intent.putExtra("heading", cardItem.heading)
                                intent.putExtra("description", cardItem.description)
                                intent.putExtra("summary", cardItem.summary)
                                intent.putExtra("dialogText", cardItem.dialogText)
                                context.startActivity(intent)*/

                                replaceFragmentWithPlayerFragment(
                                    fragmentManager,
                                    cardItem.videoResourceId,
                                    cardItem.audioResourceId,
                                    cardItem.heading,
                                    cardItem.description,
                                    cardItem.summary,
                                    cardItem.dialogText,
                                    AudioPlayerFragment()
                                )
                            }else if(cardItem.description == context.getString(R.string.
                                making_connection
                                )){
                                replaceFragmentWithPlayerFragment(
                                    fragmentManager,
                                    null,
                                    cardItem.audioResourceId,
                                    null,
                                    cardItem.description,
                                    null,
                                    null,
                                    MakingConnectionFragment()
                                )
                            }

                            else {
                                Toast.makeText(context, "Comming soon", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "No audio", Toast.LENGTH_SHORT).show()
                        }
                    }
                    /*cardItem.availableContentTypes.contains(ItemType.VIDEO) -> {
                        if (cardItem.videoResourceId != null) {
                            val intent = Intent(context, AnxietyPlayerActivity::class.java)
                            intent.putExtra("contentUri", Uri.parse("android.resource://${context.packageName}/${cardItem.videoResourceId}"))
                            context.startActivity(intent)

                        }
                        else
                        {
                            Toast.makeText(context, "No Video Available", Toast.LENGTH_SHORT).show()
                        }
                    }*/
                    cardItem.availableContentTypes.contains(ItemType.VIDEO) -> {
                        if (cardItem.videoResourceId != null) {
                            if (cardItem.heading == context.getString(R.string.the_neuropsychology)) {
                                val intent = Intent(context, PlayerActivity::class.java)
                                intent.putExtra("mediaResourceId", cardItem.videoResourceId)
                                intent.putExtra("heading", cardItem.heading)
                                intent.putExtra("summary", cardItem.summary)
                                intent.putExtra("dialogText", cardItem.dialogText)
                                intent.putExtra("titleText", cardItem.description)
                                context.startActivity(intent)

                                /*replaceFragmentWithPlayerFragment(
                                    fragmentManager,
                                    cardItem.videoResourceId,
                                    cardItem.audioResourceId,
                                    cardItem.heading,
                                    cardItem.description,
                                    cardItem.summary,
                                    cardItem.dialogText,
                                    PlayerFragment()
                                )*/

                            } else if (cardItem.description == context.getString(R.string.page_2_1)) {
                                val intent = Intent(context, PlayerActivity::class.java)
                                intent.putExtra("mediaResourceId", cardItem.videoResourceId)
                                intent.putExtra("heading", cardItem.heading)
                                intent.putExtra("summary", cardItem.summary)
                                intent.putExtra("dialogText", cardItem.dialogText)
                                intent.putExtra("titleText", cardItem.description)
                                context.startActivity(intent)

                                /* replaceFragmentWithPlayerFragment(
                                     fragmentManager,
                                     cardItem.videoResourceId,
                                     cardItem.audioResourceId,
                                     cardItem.heading,
                                     cardItem.description,
                                     cardItem.summary,
                                     cardItem.dialogText,
                                     PlayerFragment()
                                 )*/
                            } else if (cardItem.description == context.getString(R.string.make_plan_card6_text2)) {
                                val intent = Intent(context, PlayerActivity::class.java)
                                intent.putExtra("mediaResourceId", cardItem.videoResourceId)
                                intent.putExtra("heading", cardItem.heading)
                                intent.putExtra("summary", cardItem.summary)
                                intent.putExtra("dialogText", cardItem.dialogText)
                                intent.putExtra("titleText", cardItem.description)
                                context.startActivity(intent)

                                /* replaceFragmentWithPlayerFragment(
                                     fragmentManager,
                                     cardItem.videoResourceId,
                                     null,
                                     cardItem.heading,
                                     null,
                                     null,
                                     cardItem.dialogText,
                                     PlayerFragment()
                                 )*/
                            } else if (cardItem.description == context.getString(R.string.anxiety_exercise)) {
                                val intent = Intent(context, PlayerActivity::class.java)
                                intent.putExtra("mediaResourceId", cardItem.videoResourceId)
                                intent.putExtra("heading", cardItem.heading)
                                intent.putExtra("summary", cardItem.summary)
                                intent.putExtra("dialogText", cardItem.dialogText)
                                intent.putExtra("titleText", cardItem.description)
                                context.startActivity(intent)

                                /*replaceFragmentWithPlayerFragment(
                                    fragmentManager,
                                    cardItem.videoResourceId,
                                    cardItem.audioResourceId,
                                    cardItem.heading,
                                    cardItem.description,
                                    cardItem.summary,
                                    cardItem.dialogText,
                                    PlayerFragment()
                                )*/
                            }
                        } else {
                            Toast.makeText(context, "No Video Available", Toast.LENGTH_SHORT).show()
                        }
                    }

                    cardItem.availableContentTypes.contains(ItemType.LESSON) -> {
                        //Toast.makeText(context, "No Lesson Available", Toast.LENGTH_SHORT).show()
                        /*val intent = Intent(context, AnxietyQuestionsActivity::class.java)
                        intent.putExtra("description", cardItem.description)
                        context.startActivity(intent)*/
                        if (cardItem.description == context.getString(R.string.page_2_1)) {
                            replaceFragmentWithPlayerFragment(
                                fragmentManager,
                                null,
                                null,
                                null,
                                cardItem.description,
                                null,
                                null,
                                AnxietyQuestionsFragment()
                            )

                        } else if (cardItem.description == context.getString(R.string.title_toolbar_pace)) {
                            replaceFragmentWithPlayerFragment(
                                fragmentManager,
                                null,
                                null,
                                null,
                                cardItem.description,
                                null,
                                null,
                                FastPaceFragment()
                            )
                        } else if (cardItem.description == context.getString(R.string.let_make_plan)) {
                            replaceFragmentWithPlayerFragment(
                                fragmentManager,
                                null,
                                null,
                                null,
                                cardItem.description,
                                null,
                                null,
                                MakeAPlanFragment()
                            )
                        } else if (cardItem.description == context.getString(R.string.title_toolbar_recognize)) {
                            /*val intent = Intent(context, RecognizeActivity::class.java)
                            intent.putExtra("description", cardItem.description)
                            context.startActivity(intent)*/
                            replaceFragmentWithPlayerFragment(
                                fragmentManager,
                                null,
                                null,
                                null,
                                cardItem.description,
                                null,
                                null,
                                RecognizeFragment()
                            )
                        } else if (cardItem.description == context.getString(R.string.anxiety_hide)) {
                            replaceFragmentWithPlayerFragment(
                                fragmentManager,
                                null,
                                null,
                                null,
                                cardItem.description,
                                null,
                                null,
                                AnxietyHideFragment()
                            )
                        } else if (cardItem.description == context.getString(R.string.calming_body)) {
                            replaceFragmentWithPlayerFragment(
                                fragmentManager,
                                null,
                                null,
                                null,
                                cardItem.description,
                                null,
                                null,
                                AnxietyCalmingBodyFragment()
                            )
                        } else if (cardItem.description == context.getString(R.string.postpone_worry)) {
                            replaceFragmentWithPlayerFragment(
                                fragmentManager,
                                null,
                                null,
                                null,
                                cardItem.description,
                                null,
                                null,
                                //PostponeWorryFirstScreen()
                                AnxietyPostponeWorry()
                            )
                        } else if (cardItem.description == context.getString(R.string.biased_think)) {
                            replaceFragmentWithPlayerFragment(
                                fragmentManager,
                                null,
                                null,
                                null,
                                cardItem.description,
                                null,
                                null,
                                //PostponeWorryFirstScreen()
                                AnxietyBiasedThinkingFragment()
                            )
                        } else if (cardItem.description == context.getString(R.string.restructure_biased)) {
                            replaceFragmentWithPlayerFragment(
                                fragmentManager,
                                null,
                                null,
                                null,
                                cardItem.description,
                                null,
                                null,
                                //PostponeWorryFirstScreen()
                                AnxietyRestructureBiased()
                            )
                        }else if (cardItem.description == context.getString(R.string.anxiety_sleep)) {
                            replaceFragmentWithPlayerFragment(
                                fragmentManager,
                                null,
                                null,
                                null,
                                cardItem.description,
                                null,
                                null,
                                AnxietyAndSleepFragment()
                            )
                        }
                        else if (cardItem.description == context.getString(R.string.anxiety_and_diet)) {
                            replaceFragmentWithPlayerFragment(
                                fragmentManager,
                                null,
                                null,
                                null,
                                cardItem.description,
                                null,
                                null,
                                AnxietyAndDietFragment()
                            )
                        }
                        else if (cardItem.description == context.getString(R.string.anxiety_alcohol)) {
                            replaceFragmentWithPlayerFragment(
                                fragmentManager,
                                null,
                                null,
                                null,
                                cardItem.description,
                                null,
                                null,
                                AnxietyAndAlcoholSubstancesFragment()
                            )
                        }
                        else if (cardItem.description == context.getString(R.string.managing_stress)) {
                            replaceFragmentWithPlayerFragment(
                                fragmentManager,
                                null,
                                null,
                                null,
                                cardItem.description,
                                null,
                                null,
                                ManagingStressToReduceAnxietyFragment()
                            )
                        }
                    }

                    cardItem.availableContentTypes.contains(ItemType.QUIZ) -> {
                        // Toast.makeText(context, "No Quiz Available", Toast.LENGTH_SHORT).show()
                        /*val intent = Intent(context, AnxietyQuizActivity::class.java)
                        context.startActivity(intent)*/
                        replaceFragmentWithPlayerFragment(
                            fragmentManager,
                            null,
                            null,
                            null,
                            cardItem.description,
                            null,
                            null,
                            AnxietyQuizFragment()
                        )
                        if (cardItem.description == context.getString(R.string.quiz_title3_2)) {
                            replaceFragmentWithPlayerFragment(
                                fragmentManager,
                                null,
                                null,
                                null,
                                cardItem.description,
                                null,
                                null,
                                AnxietyQuizLessonsFragment()
                            )
                        }
                    }

                    else -> {
                        // Handle other content types here
                    }
                }
            }
        }
    }

    private fun replaceFragmentWithPlayerFragment(
        fragmentManager: FragmentManager,
        videoResourceId: String?,
        audioResourceId: String?,
        heading: String?,
        description: String?,
        summary: String?,
        dialogText: String?,
        fragment: Fragment
    ) {
        val args = Bundle()
        args.putString("audioResourceId", audioResourceId)
        args.putString("videoResourceId", videoResourceId)
        args.putString("heading", heading)
        args.putString("description", description)
        args.putString("summary", summary)
        args.putString("dialogText", dialogText)
        fragment.arguments = args

        fragmentManager.beginTransaction()
            .replace(R.id.flFragment, fragment)
            .addToBackStack(null)
            .commit()
    }

}

class CardItemDiffCallback : DiffUtil.ItemCallback<CardItemDataClass>() {
    override fun areItemsTheSame(oldItem: CardItemDataClass, newItem: CardItemDataClass): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(
        oldItem: CardItemDataClass,
        newItem: CardItemDataClass
    ): Boolean {
        return oldItem == newItem
    }
}
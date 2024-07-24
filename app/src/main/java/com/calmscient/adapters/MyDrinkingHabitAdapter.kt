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

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.R
import com.calmscient.di.remote.DrinkTrackerItem
import com.calmscient.di.remote.MakingConnectionDataClass

class MyDrinkingHabitAdapter(val context: Context,private val items: MutableList<MakingConnectionDataClass>, private val drinkItemList: List<DrinkTrackerItem>,private val lifecycleOwner: LifecycleOwner,private val count : Int) :  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val VIEW_TYPE_TYPE_A: Int = 0
    val VIEW_TYPE_TYPE_B: Int = 1
    val VIEW_TYPE_TYPE_C: Int = 2
    val VIEW_TYPE_TYPE_D: Int = 3
    val VIEW_TYPE_TYPE_E: Int = 4


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            /*VIEW_TYPE_TYPE_A -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.fragment_my_drinking_habit_screen_one, parent, false)
                TypeViewHolderOne(view)
            }
            VIEW_TYPE_TYPE_B -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.fragment_my_drinking_habit_screen_two, parent, false)
                TypeViewHolderTwo(view,lifecycleOwner)
            }
            VIEW_TYPE_TYPE_C -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.fragment_my_drinking_habit_screen_three, parent, false)
                TypeViewHolderThree(view)
            }
            VIEW_TYPE_TYPE_D -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.fragment_my_drinking_habit_screen_four, parent, false)
                TypeViewHolderFour(view)
            }
            VIEW_TYPE_TYPE_E -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.fragment_my_drinking_habit_screen_five, parent, false)
                TypeViewHolderFive(view)
            }*/

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }



    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is TypeViewHolderOne -> {
                holder.bind(item)
            }
            is TypeViewHolderTwo -> {
                holder.bind(drinkItemList,count)
            }
        }

    }

    class TypeViewHolderThree(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(itemsList: MakingConnectionDataClass) {
            // Bind data to your views here if needed
        }

        private val optionSets: List<List<LinearLayout>> = listOf(
            listOf(
                itemView.findViewById(R.id.layoutOne),
                itemView.findViewById(R.id.layoutTwo),
                itemView.findViewById(R.id.layoutThree),
                itemView.findViewById(R.id.layoutFour),
            )
        )

        init {
            // Set OnClickListener for each set
            optionSets.forEachIndexed { setIndex, set ->
                set.forEachIndexed { optionIndex, layout ->
                    layout.setOnClickListener {
                        onOptionClicked(setIndex, optionIndex)
                    }
                }
            }
        }

        private val selectedOptionIndices: MutableList<Int> = MutableList(optionSets.size) { -1 }

        private fun onOptionClicked(setIndex: Int, selectedIndex: Int) {
            if (selectedIndex != selectedOptionIndices[setIndex]) {
                selectedOptionIndices[setIndex] = selectedIndex
                clearSelection(setIndex)
                selectOption(setIndex, selectedIndex)
            }
        }

        private fun clearSelection(setIndex: Int) {
            optionSets[setIndex].forEach { layout ->
                layout.setBackgroundResource(R.drawable.card_default_background)
                val textViewOne = layout.getChildAt(0) as TextView
                val textViewTwo = layout.getChildAt(1) as TextView
                textViewOne.setTextColor(Color.parseColor("#424242"))
                textViewTwo.setTextColor(Color.parseColor("#424242"))
            }
        }

        private fun selectOption(setIndex: Int, selectedIndex: Int) {
            val selectedLayout = optionSets[setIndex][selectedIndex]
            selectedLayout.setBackgroundResource(R.drawable.card_selected_background)
            val textViewOne = selectedLayout.getChildAt(0) as TextView
            val textViewTwo = selectedLayout.getChildAt(1) as TextView
            textViewOne.setTextColor(Color.parseColor("#FFFFFF"))
            textViewTwo.setTextColor(Color.parseColor("#FFFFFF"))
        }
    }

    class TypeViewHolderFour(itemView: View) :RecyclerView.ViewHolder(itemView){

        fun bind(itemsList: MakingConnectionDataClass) {


        }

    }
    class TypeViewHolderFive(itemView: View) :RecyclerView.ViewHolder(itemView){

        fun bind(itemsList: MakingConnectionDataClass) {


        }

        private val optionSets: List<List<TextView>> = listOf(
            listOf
                (
                itemView.findViewById(R.id.optionOne),
                itemView.findViewById(R.id.optionTwo),
                itemView.findViewById(R.id.optionThree),
                itemView.findViewById(R.id.optionFour),
                itemView.findViewById(R.id.optionFive),
                itemView.findViewById(R.id.optionSix),
                itemView.findViewById(R.id.optionSeven),
                itemView.findViewById(R.id.optionEight),
                itemView.findViewById(R.id.optionNine)
            ),
        )


        init {
            // Set OnClickListener for each set
            optionSets.forEachIndexed { setIndex, set ->
                set.forEachIndexed { optionIndex, textView ->
                    textView.setOnClickListener {
                        onOptionClicked(setIndex, optionIndex)
                    }
                }
            }
        }

        private val selectedOptionIndices: MutableList<Int> = MutableList(optionSets.size)
        {
            -1
        }

        private fun onOptionClicked(setIndex: Int, selectedIndex: Int) {
            if (selectedIndex != selectedOptionIndices[setIndex]) {
                selectedOptionIndices[setIndex] = selectedIndex
                clearSelection(setIndex)
                selectOption(setIndex, selectedIndex)
            }
        }

        private fun clearSelection(setIndex: Int) {
            optionSets[setIndex].forEach { textView ->
                textView.setBackgroundResource(R.drawable.card_default_background)
                textView.setTextColor(Color.parseColor("#424242"))
            }
        }

        private fun selectOption(setIndex: Int, selectedIndex: Int) {
            optionSets[setIndex][selectedIndex].setBackgroundResource(R.drawable.card_selected_background)
            optionSets[setIndex][selectedIndex].setTextColor(Color.parseColor("#FFFFFF"))
        }
    }

    class TypeViewHolderOne(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(itemsList: MakingConnectionDataClass) {


        }
        private val optionSets: List<List<TextView>> = listOf(
            listOf
                (
                itemView.findViewById(R.id.none),
                itemView.findViewById(R.id.lessThanTwo),
                itemView.findViewById(R.id.threeToFive),
                itemView.findViewById(R.id.almostEveryday),
                itemView.findViewById(R.id.everyday)
            ),
        )


        init {
            // Set OnClickListener for each set
            optionSets.forEachIndexed { setIndex, set ->
                set.forEachIndexed { optionIndex, textView ->
                    textView.setOnClickListener {
                        onOptionClicked(setIndex, optionIndex)
                    }
                }
            }
        }

        private val selectedOptionIndices: MutableList<Int> = MutableList(optionSets.size)
        {
            -1
        }

        private fun onOptionClicked(setIndex: Int, selectedIndex: Int) {
            if (selectedIndex != selectedOptionIndices[setIndex]) {
                selectedOptionIndices[setIndex] = selectedIndex
                clearSelection(setIndex)
                selectOption(setIndex, selectedIndex)
            }
        }

        private fun clearSelection(setIndex: Int) {
            optionSets[setIndex].forEach { textView ->
                textView.setBackgroundResource(R.drawable.card_default_background)
                textView.setTextColor(Color.parseColor("#424242"))
            }
        }

        private fun selectOption(setIndex: Int, selectedIndex: Int) {
            optionSets[setIndex][selectedIndex].setBackgroundResource(R.drawable.card_selected_background)
            optionSets[setIndex][selectedIndex].setTextColor(Color.parseColor("#FFFFFF"))
        }
    }


    class TypeViewHolderTwo(itemView: View, private val lifecycleOwner: LifecycleOwner) : RecyclerView.ViewHolder(itemView) {
        private val recyclerView: RecyclerView = itemView.findViewById(R.id.drinkTrackerRecyclerView)
        private val totalCount: TextView = itemView.findViewById(R.id.tv_totalCount)

        fun bind(drinkItemList: List<DrinkTrackerItem>,countTemp : Int) {
            val adapter = DrinkTrackerAdapter(drinkItemList)
            recyclerView.apply {
                layoutManager = GridLayoutManager(itemView.context, 2)
                this.adapter = adapter
            }
            totalCount.text = countTemp.toString()

            // Observe totalItemCount from DrinkTrackerAdapter using lifecycleOwner
            adapter.totalItemCount.observe(lifecycleOwner) { count ->

                totalCount.text = count.toString()
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].theViewType
    }
}
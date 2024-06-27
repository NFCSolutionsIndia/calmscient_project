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

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.R
import com.calmscient.activities.GlossaryActivity
import com.calmscient.adapters.MakingConnectionAdapter
import com.calmscient.adapters.MyDrinkingHabitAdapter
import com.calmscient.databinding.FragmentDiscoveryBinding
import com.calmscient.databinding.FragmentMyDrinkingHabitBinding
import com.calmscient.di.remote.DrinkTrackerItem
import com.calmscient.di.remote.MakingConnectionDataClass
import com.calmscient.di.remote.response.DrinkTrackerResponse
import com.calmscient.di.remote.response.LoginResponse
import com.calmscient.utils.common.JsonUtil
import com.calmscient.utils.common.SharedPreferencesUtil


class MyDrinkingHabitFragment : Fragment() {

    private lateinit var binding: FragmentMyDrinkingHabitBinding

    private lateinit var myDrinkingAdapter: MyDrinkingHabitAdapter
    private var currentQuestionIndex = 0
    private var previousQuestionIndex = -1
    private val restructureText = mutableListOf<MakingConnectionDataClass>()

    private lateinit var drinkTrackerResponse: DrinkTrackerResponse

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val jsonString = SharedPreferencesUtil.getData(requireContext(), "drinkTrackerData", "")
        drinkTrackerResponse = JsonUtil.fromJsonString<DrinkTrackerResponse>(jsonString)

        Log.d("MyDrinkingHabitFragment","$drinkTrackerResponse")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMyDrinkingHabitBinding.inflate(inflater, container, false)

        binding.backIcon.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.previousQuestion.visibility = View.GONE

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupNavigation()
        initializeAdapter()

        displayRestructureViews()


        binding.backIcon.setOnClickListener {
            //loadFragment(ManageAnxietyFragment())
            requireActivity().supportFragmentManager.popBackStack()
        }


        val pagerSnapHelper = PagerSnapHelper()
        pagerSnapHelper.attachToRecyclerView(binding.myDrinkingHabitRecyclerView)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun displayRestructureViews() {
        restructureText.add(MakingConnectionDataClass(myDrinkingAdapter.VIEW_TYPE_TYPE_A, 0));
        restructureText.add(MakingConnectionDataClass(myDrinkingAdapter.VIEW_TYPE_TYPE_B, 1));
        restructureText.add(MakingConnectionDataClass(myDrinkingAdapter.VIEW_TYPE_TYPE_C, 2));
        restructureText.add(MakingConnectionDataClass(myDrinkingAdapter.VIEW_TYPE_TYPE_D, 3));
        restructureText.add(MakingConnectionDataClass(myDrinkingAdapter.VIEW_TYPE_TYPE_E, 4));

        myDrinkingAdapter.notifyDataSetChanged()
    }

    private fun initializeAdapter() {
        binding.myDrinkingHabitRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val drinkItems = mutableListOf<DrinkTrackerItem>()
        val drinksList = drinkTrackerResponse.drinksList
        drinksList.forEach { drink ->
            val drinkItem = DrinkTrackerItem(
                itemImage = drink.imageUrl,
                itemCount = drink.quantity,
                itemDescription = drink.drinkName
            )
            drinkItems.add(drinkItem)
        }

        myDrinkingAdapter = MyDrinkingHabitAdapter(requireContext(), restructureText, drinkItems,viewLifecycleOwner,drinkTrackerResponse.totalCount)
        binding.myDrinkingHabitRecyclerView.adapter = myDrinkingAdapter
    }




    private fun setupNavigation() {


        binding.nextQuestion.setOnClickListener {
            navigateToQuestion(currentQuestionIndex + 1, true)
        }

        binding.previousQuestion.setOnClickListener {
            navigateToQuestion(currentQuestionIndex - 1, false)
        }

        binding.myDrinkingHabitRecyclerView.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Check if the user is scrolling horizontally
                if (Math.abs(dx) > Math.abs(dy)) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    if (firstVisibleItemPosition != currentQuestionIndex) {
                        previousQuestionIndex = currentQuestionIndex
                        currentQuestionIndex = firstVisibleItemPosition

                    }
                }
            }
        })
    }

    fun navigateToQuestion(index: Int, isNext: Boolean) {
        if (index in 0 until restructureText.size) {
            if (isNext) {
                previousQuestionIndex = currentQuestionIndex
            }
            currentQuestionIndex = index
            binding.myDrinkingHabitRecyclerView.smoothScrollToPosition(currentQuestionIndex)

        }
        if(currentQuestionIndex == 0)
        {
            binding.previousQuestion.visibility = View.GONE
        }
        else{
            binding.previousQuestion.visibility = View.VISIBLE

        }
        if(currentQuestionIndex >= restructureText.size-1)
        {
            binding.nextQuestion.visibility = View.GONE
        }else
        {
            binding.nextQuestion.visibility = View.VISIBLE
        }
    }


    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

}
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
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.calmscient.Interface.BottomSheetListener
import com.calmscient.Interface.CellClickListener
import com.calmscient.R
import com.calmscient.adapters.JournalEntryAdapter
import com.calmscient.di.remote.JournalEntryDataClass
import com.calmscient.databinding.CalendarDayLayoutBinding
import com.calmscient.databinding.FragmentJournalEntryBinding
import com.calmscient.utils.getColorCompat
import com.kizitonwose.calendar.core.Week
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.core.yearMonth
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekDayBinder
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import kotlin.random.Random

class JournalEntryFragment : Fragment(), BottomSheetListener {

    private lateinit var binding: FragmentJournalEntryBinding
    private lateinit var adapter: JournalEntryAdapter
    private var selectedDate = LocalDate.now()
    private val dateFormatter = DateTimeFormatter.ofPattern("dd")
    private val cardDataList = mutableListOf<JournalEntryDataClass>()
    private var adapterPositionToDelete = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            loadFragment(WeeklySummaryFragment())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentJournalEntryBinding.inflate(inflater, container, false)
        binding.tvJournalEntry.text = getString(R.string.journal_entry)

        binding.backIcon.setOnClickListener {
            loadFragment(WeeklySummaryFragment())
        }

        // Initialize RecyclerView
        val recyclerView = binding.recyclerViewJournalEntry
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager
        adapter = JournalEntryAdapter(cardDataList,this)
        recyclerView.adapter = adapter

        // Add the Log.d statement here to check the cardDataList size
        Log.d("JournalEntryFragment", "Card Data List Size: ${cardDataList.size}")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        class DayViewContainer(view: View) : ViewContainer(view) {
            val bind = CalendarDayLayoutBinding.bind(view)
            lateinit var day: WeekDay

            init {
                view.setOnClickListener {
                    if (selectedDate != day.date) {
                        val oldDate = selectedDate
                        selectedDate = day.date
                        incrementDateByOne()
                        binding.exSevenCalendar.notifyDateChanged(day.date)
                        oldDate?.let { binding.exSevenCalendar.notifyDateChanged(it) }

                        // Update card views for the new selected date
                        displayCardViewsForSelectedDate()
                    }
                }
            }

            fun bind(day: WeekDay) {
                this.day = day
                bind.exSevenDateText.text = dateFormatter.format(day.date)
                bind.exSevenDayText.text = day.date.dayOfWeek.displayText()
                val colorRes = if (day.date == selectedDate) {
                    R.color.white
                } else {
                    R.color.black
                }
                bind.exSevenDateText.setTextColor(view.context.getColorCompat(colorRes))
                bind.exSevenDayText.setTextColor(view.context.getColorCompat(colorRes))
                val colorResLayout = if (day.date == selectedDate) {
                    R.drawable.calendar_custom_border
                } else {
                    R.color.example_7_calendar
                }
                bind.layoutDate.setBackgroundResource(colorResLayout)
            }
        }

        binding.exSevenCalendar.dayBinder = object : WeekDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: WeekDay) = container.bind(data)
        }

        binding.exSevenCalendar.weekScrollListener = { weekDays ->
            val text = binding.exSevenToolbar.title
            binding.exSevenToolbar.title = getWeekPageTitle(weekDays)
            binding.exSevenToolbar.setTitleTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.black
                )
            )
        }

        val currentMonth = YearMonth.now()
        binding.exSevenCalendar.setup(
            currentMonth.minusMonths(24).atStartOfMonth(),
            currentMonth.plusMonths(24).atEndOfMonth(),
            firstDayOfWeekFromLocale(),
        )
        binding.exSevenCalendar.scrollToDate(LocalDate.now())
        binding.recyclerViewJournalEntry.layoutManager = LinearLayoutManager(requireContext())
        adapter = JournalEntryAdapter(cardDataList,this)
        binding.recyclerViewJournalEntry.adapter = adapter
        binding.journalNeedToTalkButton.setOnClickListener {
            loadFragment(EmergencyResourceFragment())
        }
        // Display card views for the initial selected date
        displayCardViewsForSelectedDate()
    }

    fun incrementDateByOne() {
        val sdf = SimpleDateFormat("MM/dd/yyyy")
        for (i in 0..6) {
            val calendar: Calendar = GregorianCalendar()
            calendar.add(Calendar.DATE, i)
            val day: String = sdf.format(calendar.time)
            Log.i(ContentValues.TAG, day)
        }
    }

    fun getWeekPageTitle(week: Week): String {
        val firstDate = week.days.first().date
        val lastDate = week.days.last().date
        return when {
            firstDate.yearMonth == lastDate.yearMonth -> {
                firstDate.yearMonth.displayText()
            }

            firstDate.year == lastDate.year -> {
                "${firstDate.month.displayText(short = false)} - ${lastDate.yearMonth.displayText()}"
            }

            else -> {
                "${firstDate.yearMonth.displayText()} - ${lastDate.yearMonth.displayText()}"
            }
        }
    }

    fun YearMonth.displayText(short: Boolean = false): String {
        return "${this.month.displayText(short = short)} ${this.year}"
    }

    fun Month.displayText(short: Boolean = true): String {
        val style = if (short) TextStyle.SHORT else TextStyle.FULL
        return getDisplayName(style, Locale.ENGLISH)
    }

    fun DayOfWeek.displayText(uppercase: Boolean = false): String {
        return getDisplayName(TextStyle.SHORT, Locale.ENGLISH).let { value ->
            if (uppercase) value.uppercase(Locale.ENGLISH) else value
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun displayCardViewsForSelectedDate() {
        // Update card data based on the selected date
        cardDataList.clear()

        // Sample data initialization (replace with your actual data source)
        initializeSampleData()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initializeSampleData() {
        // Clear existing card data
        cardDataList.clear()

        // Get the selected date in the format "MM/dd/yyyy"
        val selectedDateString = selectedDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))

        // Get the card size for the selected date from the cardSizes array
        val cardCount = cardSizes[selectedDate.dayOfMonth - 1] // Adjust index as needed

        if (cardCount == 0) {
            // Show "No entries" TextView when card count is 0
            binding.tvNoEntry.text = getString(R.string.no_entries)
            binding.recyclerViewJournalEntry.visibility = View.GONE
            binding.tvNoEntry.visibility = View.VISIBLE // Make the TextView visible
        } else {
            binding.recyclerViewJournalEntry.visibility = View.VISIBLE
            binding.tvNoEntry.visibility = View.GONE // Hide the "No entries" TextView
        }

        // Add entries for the selected date based on the calculated card count and descriptions
        for (i in 1..cardCount) {
            val description = getCardDescription(i)

            cardDataList.add(
                JournalEntryDataClass(
                    selectedDateString,
                    description,
                    cardCount
                )
            )
        }

        // Notify the adapter that the data has changed
        adapter.notifyDataSetChanged()
    }



    companion object {
        // Create a static array to store card sizes for each date
        private val cardSizes = intArrayOf(
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1
        )
    }

    private fun getCardDescription(cardPosition: Int): String {
        return when (cardPosition) {
            1 -> getString(R.string.journal_entry_card1_text)
            2 -> getString(R.string.journal_entry_card2_text)
            3 -> getString(R.string.journal_entry_card3_text)
            4 -> getString(R.string.journal_entry_card4_text)
            5 -> getString(R.string.journal_entry_card5_text)
            6 -> getString(R.string.journal_entry_card6_text)
            7 -> getString(R.string.journal_entry_card7_text)
            8 -> getString(R.string.journal_entry_card8_text)
            else -> "Default Description"
        }
    }

    private fun handleDeleteEntry() {
        val selectedPosition =
            adapterPositionToDelete // Create a variable to store the selected position
        if (selectedPosition != -1) {
            adapter.removeEntry(selectedPosition) // Remove the entry from the adapter
            adapterPositionToDelete = -1 // Reset the selected position
        }
    }

    override fun onShowBottomSheet() {
        val bottomSheetFragment = BottomSheetAddFragment()
        bottomSheetFragment.show(requireActivity().supportFragmentManager, bottomSheetFragment.tag)
    }

    override fun onShowEditBottomSheet(view: View, position: Int, description: String) {
        val bottomSheetEditFragment = BottomSheetEditFragment(view,position,description)
        bottomSheetEditFragment.show(requireActivity().supportFragmentManager, bottomSheetEditFragment.tag)
    }
}
package com.example.nailschedule.view.activities.view.owner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nailschedule.R
import com.example.nailschedule.databinding.ActivityOwnerBinding
import com.google.firebase.firestore.FirebaseFirestore

class OwnerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOwnerBinding
    private var date: String? = null
    private val ownerScheduleAdapter: OwnerAdapter by lazy {
        OwnerAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOwnerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()
        setupAdapter()
        setupCalendarViewDatesMinAndMax()
        setCalendarListener()
    }

    private fun setupToolbar() = binding.apply {
        toolbar.apply {
            title = getString(R.string.general_schedule)
            setSupportActionBar(this)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setCalendarListener() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val monthOk = month + 1
            date = if (monthOk <= 9) {
                "$dayOfMonth-0${monthOk}-$year"
            } else {
                "$dayOfMonth-${monthOk}-$year"
            }
            getAvailableTimeList()
        }
    }

    private fun setupAdapter() {
        binding.rvSchedules.apply {
            layoutManager =
                LinearLayoutManager(this@OwnerActivity)
            adapter = ownerScheduleAdapter
        }
    }

    private fun setupCalendarViewDatesMinAndMax() = binding.apply {
        calendarView.minDate = System.currentTimeMillis()
        val sixDaysAtMillis = 518400000
        val aWeekAfter = System.currentTimeMillis() + sixDaysAtMillis
        calendarView.maxDate = aWeekAfter
    }

    private fun getAvailableTimeList() {
        var helpList = mutableListOf<Pair<String, Boolean>>()
        val availableTimeList = mutableListOf<Pair<String, Boolean>>()
        FirebaseFirestore.getInstance().collection("calendarField")
            .document(date!!).get().addOnSuccessListener { documentSnapshot ->
                documentSnapshot.data?.let {
                    val timeList = it["timeList"] as List<*>
                    timeList.forEach { time ->
                        time?.let {
                            availableTimeList.add(Pair(it.toString(), true))
                        }
                    }
                    //Removing the item "Selecione a hora"
                    availableTimeList.removeAt(0)
                    helpList = mutableListOf(
                        Pair("08:00", false), Pair("10:00", false), Pair("12:00", false),
                        Pair("14:00", false), Pair("16:00", false), Pair("18:00", false)
                    )
                    helpList.forEachIndexed { index, pair ->
                        if (availableTimeList.contains(Pair(pair.first, true))) {
                            helpList[index] = Pair(pair.first, true)
                        }
                    }
                } ?: run {
                    helpList = mutableListOf(
                        Pair("08:00", true), Pair("10:00", true), Pair("12:00", true),
                        Pair("14:00", true), Pair("16:00", true), Pair("18:00", true)
                    )
                }
                with(ownerScheduleAdapter) {
                    clearItemsList()
                    setItemsList(helpList)
                }
            }
    }
}
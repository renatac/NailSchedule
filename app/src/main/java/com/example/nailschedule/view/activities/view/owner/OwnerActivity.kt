package com.example.nailschedule.view.activities.view.owner

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nailschedule.R
import com.example.nailschedule.databinding.ActivityOwnerBinding
import com.example.nailschedule.view.activities.utils.showToast
import com.google.firebase.firestore.FirebaseFirestore

class OwnerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOwnerBinding
    private var date: String? = null
    private val ownerScheduleAdapter: OwnerAdapter by lazy {
        OwnerAdapter(::seeSchedule)
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
            hideCardView()
            showRecycler()
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
        val mutableTimeList = mutableListOf<String>()
        FirebaseFirestore.getInstance().collection("calendarField")
            .document(date!!).get().addOnSuccessListener { documentSnapshot ->
                documentSnapshot.data?.let {
                    val timeList = it["timeList"] as List<*>
                    timeList.forEachIndexed { index, time ->
                        time?.let { t ->
                            if (index != 0) {
                                mutableTimeList.add(t.toString())
                            }
                        }
                    }
                } ?: run {
                    showToast(this, R.string.all_free)
                }
                with(ownerScheduleAdapter) {
                    clearItemsList()
                    setItemsList(mutableTimeList)
                }
            }
    }

    private fun seeSchedule(info: String) = binding.apply {
        setBtnListener()
        hideRecycler()
        showCardView()
        setupUserInfo(info)
    }

    private fun setBtnListener() {
        binding.ivClose.setOnClickListener {
            hideCardView()
            showRecycler()
        }
    }

    private fun hideCardView() {
        binding.cardViewUser.visibility = View.GONE
    }

    private fun showCardView() {
        binding.cardViewUser.visibility = View.VISIBLE
    }

    private fun hideRecycler() {
        binding.rvSchedules.visibility = View.GONE
    }

    private fun showRecycler() {
        binding.rvSchedules.visibility = View.VISIBLE
    }

    private fun setupUserInfo(info: String) = binding.apply {
        val list = info.split(";")
        tvUserTime.text =
            applicationContext.getString(R.string.user_time, list[0].removePrefix("time="))
        tvUserName.text =
            applicationContext.getString(R.string.user_name, list[2].removePrefix("name="))
        tvUserService.text = applicationContext.getString(
            R.string.user_service,
            list[3].trim().removePrefix("service="))
        btnDeleteSchedule.setOnClickListener {

        }
    }
}
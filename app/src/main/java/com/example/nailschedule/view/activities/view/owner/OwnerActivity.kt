package com.example.nailschedule.view.activities.view.owner

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nailschedule.R
import com.example.nailschedule.databinding.ActivityOwnerBinding
import com.example.nailschedule.view.activities.data.model.Time
import com.example.nailschedule.view.activities.utils.SharedPreferencesHelper
import com.example.nailschedule.view.activities.utils.showToast
import com.example.nailschedule.view.activities.view.ConnectivityViewModel
import com.example.nailschedule.view.activities.view.scheduled.ScheduledFragment
import com.google.firebase.firestore.FirebaseFirestore

class OwnerActivity : AppCompatActivity() {

    private lateinit var connectivityViewModel: ConnectivityViewModel
    private lateinit var binding: ActivityOwnerBinding
    private var date: String? = null
    private var time: String? = null

    private val ownerScheduleAdapter: OwnerAdapter by lazy {
        OwnerAdapter(::seeSchedule)
    }

    private val email = SharedPreferencesHelper.read(
        SharedPreferencesHelper.EXTRA_EMAIL, ""
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOwnerBinding.inflate(layoutInflater)
        connectivityViewModel =
            ViewModelProvider(this).get(ConnectivityViewModel::class.java)
        setContentView(binding.root)
        setupObserver()
        setupToolbar()
        setupAdapter()
        setupCalendarViewDatesMinAndMax()
        setCalendarListener()
    }

    private fun setupObserver() {
        connectivityViewModel.hasInternet.observe(this,
            {
                if (it.first) {
                    showProgress()
                    if (it.second == ScheduledFragment.USER_DATA_DELETION) {
                        FirebaseFirestore.getInstance().collection("users")
                            .document(email!!).delete()

                        val previousTimeList = mutableListOf<String>()
                        FirebaseFirestore.getInstance().collection("calendarField")
                            .document(date!!).get().addOnSuccessListener { documentSnapshot ->
                                documentSnapshot.data?.let {
                                    val timeList = it["timeList"] as List<*>
                                    timeList.forEach { t ->
                                        with(t.toString()) {
                                            if (this.contains(time!!)) {
                                                val finalIndex = this.indexOf(";true")
                                                val timeNew = this.substring(0, finalIndex)
                                                previousTimeList.add(timeNew.plus(";false"))
                                            } else {
                                                previousTimeList.add(this)
                                            }
                                        }
                                    }
                                    val hoursList = Time(previousTimeList)
                                    FirebaseFirestore.getInstance().collection("calendarField")
                                        .document(date!!).set(hoursList)
                                    hideProgress()
                                    hideBtnDeleteSchedule()
                                    getAvailableTimeList()
                                    showToast(this, R.string.scheduled_deleted)
                                    showUnscheduledLabel()
                                }
                            }
                    }
                } else {
                    showToast(this, R.string.no_internet)
                }
            })
    }

    private fun showUnscheduledLabel() = binding.tvUnscheduledTime.apply {
        text = getString(R.string.scheduled_deleted)
        visibility = View.VISIBLE
    }

    private fun hideUnscheduledLabel() {
        binding.tvUnscheduledTime.visibility = View.GONE
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
            hideCardView()
            showRecycler()
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
        time = info
        hideUnscheduledLabel()
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
            list[3].trim().removePrefix("service=")
        )
        btnDeleteSchedule.setOnClickListener {
            deleteFirebaseFirestoreData()
        }
    }

    private fun deleteFirebaseFirestoreData() {
        connectivityViewModel.checkForInternet(
            applicationContext,
            ScheduledFragment.USER_DATA_DELETION
        )
    }

    private fun showProgress() {
        binding.progressOwner.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        binding.progressOwner.visibility = View.GONE
    }

    private fun hideBtnDeleteSchedule() {
        binding.btnDeleteSchedule.visibility = View.GONE
    }
}

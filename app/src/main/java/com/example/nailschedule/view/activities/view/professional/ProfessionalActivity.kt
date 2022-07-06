package com.example.nailschedule.view.activities.view.professional

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nailschedule.R
import com.example.nailschedule.databinding.ActivityProfessionalBinding
import com.example.nailschedule.view.activities.viewmodels.CalendarFieldViewModel
import com.example.nailschedule.view.activities.data.model.Time
import com.example.nailschedule.view.activities.utils.showLoginScreen
import com.example.nailschedule.view.activities.utils.showToast
import com.example.nailschedule.view.activities.viewmodels.ConnectivityViewModel
import com.example.nailschedule.view.activities.view.activities.BottomNavigationActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.*


class ProfessionalActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener {

    private lateinit var connectivityViewModel: ConnectivityViewModel
    private lateinit var calendarFieldViewModel: CalendarFieldViewModel

    private lateinit var binding: ActivityProfessionalBinding
    private var date: String? = null
    private var time: String? = null
    private var email: String? = null

    private var timeList: List<String>? = null
    private var action: ActionEnum? = null

    private val professionalScheduleAdapter: ProfessionalAdapter by lazy {
        ProfessionalAdapter(::seeSchedule)
    }

    companion object {
        const val USER_SCHEDULE_DELETION = "user_schedule_deletion"
        const val SETUP_ADAPTER = "setup_adapter"
        const val SIX_DAYS_AT_MILLIS = 518400000
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfessionalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViewModels()
        setupRefresh()
        initialSetup()
    }

    private fun setupViewModels() {
        connectivityViewModel =
            ViewModelProvider(this).get(ConnectivityViewModel::class.java)
        calendarFieldViewModel =
            ViewModelProvider(this)[CalendarFieldViewModel::class.java]
    }


    @SuppressLint("SimpleDateFormat")
    private fun initialSetup() {
        setupCalendarViewDatesMinAndMax()
        setCalendarListener()
        setupObserver()
        setupToolbar()
        setupAdapter()
        hideRefresh()
        setupDrawerLayout()
    }

    private fun showNoIntern() {
        showToast(
            this@ProfessionalActivity,
            R.string.no_internet
        )
    }

    private fun setupDrawerLayout() {
        val navigationView: NavigationView = binding.navView
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )
        val headerView = binding.navView.getHeaderView(0)
        val navHeaderTvName = headerView.findViewById(R.id.nav_header_tv_name) as TextView
        navHeaderTvName.text = applicationContext.getString(R.string.professional_profile)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun setupObserver() {
        calendarFieldViewModel.calendarField.observe(this, { calendarField ->
            timeList = calendarField?.timeList
            when (action) {
                ActionEnum.IS_DELETION -> {
                    val previousTimeList = mutableListOf<String>()
                    calendarField.timeList.forEach { t ->
                        if (t.contains(time!!)) {
                            val finalIndex = t.indexOf(";true")
                            val timeNew = t.substring(0, finalIndex)
                            previousTimeList.add(timeNew.plus(";false"))
                        } else {
                            previousTimeList.add(t)
                        }
                    }
                    val hoursList = Time(previousTimeList)
                    updateCalendarField(hoursList)
                    hideProgress()
                    hideBtnDeleteSchedule()
                    with(professionalScheduleAdapter) {
                        clearItemsList()
                        previousTimeList.removeAt(0)
                        setItemsList(previousTimeList)
                    }
                    showUnscheduledLabel()
                    deleteUser()
                }
                ActionEnum.IS_SETUP_ADAPTER -> {
                    val mutableTimeList = mutableListOf<String>()
                    timeList?.forEachIndexed { index, t ->
                        if (index != 0) {
                            mutableTimeList.add(t)
                        }
                    } ?: run {
                        showToast(this, R.string.all_free)
                    }
                    with(professionalScheduleAdapter) {
                        clearItemsList()
                        setItemsList(mutableTimeList)
                    }
                }
            }
        })

        connectivityViewModel.hasInternet.observe(this,
            {
                if (it.first) {
                    showProgress()
                    when (it.second) {
                        USER_SCHEDULE_DELETION -> {
                            action = ActionEnum.IS_DELETION
                            calendarFieldViewModel.getCalendarFieldData(date!!)
                        }
                        SETUP_ADAPTER -> {
                            action = ActionEnum.IS_SETUP_ADAPTER
                            calendarFieldViewModel.getCalendarFieldData(date!!)
                        }
                        BottomNavigationActivity.LOG_OUT -> {
                            signOut()
                        }
                    }
                } else {
                    showNoIntern()
                }
            })
    }

    private fun updateCalendarField(hoursList: Time) {
        FirebaseFirestore.getInstance().collection("calendarField")
            .document(date!!).set(hoursList)
    }

    private fun deleteUser() {
        FirebaseFirestore.getInstance().collection("users")
            .document(email!!).delete()
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
        }
    }

    private fun setCalendarListener() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            saveDate(dayOfMonth, month, year)
            getAvailableTimeList()
            hideCardView()
            showRecycler()
        }
    }

    private fun saveDate(dayOfMonth: Int, month: Int, year: Int) {
        val monthOk = month + 1
        val day = if (dayOfMonth <= 9) {
            "0$dayOfMonth"
        } else {
            dayOfMonth
        }
        date = if (monthOk <= 9) {
            "$day-0${monthOk}-$year"
        } else {
            "$day-${monthOk}-$year"
        }
    }

    private fun setupAdapter() {
        binding.rvSchedules.apply {
            layoutManager =
                LinearLayoutManager(this@ProfessionalActivity)
            adapter = professionalScheduleAdapter
        }
    }

    private fun setupCalendarViewDatesMinAndMax() = binding.apply {
        calendarView.minDate = System.currentTimeMillis()
        val aWeekAfter = System.currentTimeMillis() + SIX_DAYS_AT_MILLIS
        calendarView.maxDate = aWeekAfter
    }

    private fun getAvailableTimeList() {
       connectivityViewModel.checkForInternet(
            applicationContext,
            SETUP_ADAPTER
        )
    }

    private fun seeSchedule(info: String) = binding.apply {
        time = info
        hideUnscheduledLabel()
        setBtnListener()
        hideRecycler()
        showCardView()
        hideProgress()
        showBtnDeleteSchedule()
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
        email = list[7].trim().removePrefix("email=")
    }

    private fun deleteFirebaseFirestoreData() {
        connectivityViewModel.checkForInternet(
            applicationContext,
            USER_SCHEDULE_DELETION
        )
    }

    private fun logOut() {
        connectivityViewModel.checkForInternet(
            applicationContext,
            BottomNavigationActivity.LOG_OUT
        )
    }

    private fun showProgress() {
        binding.progressProfessional.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        binding.progressProfessional.visibility = View.GONE
    }

    private fun hideBtnDeleteSchedule() {
        binding.btnDeleteSchedule.visibility = View.GONE
    }

    private fun showBtnDeleteSchedule() {
        binding.btnDeleteSchedule.visibility = View.VISIBLE
    }

    private fun setupRefresh() {
        binding.professionalSwipeRefreshLayout.setOnRefreshListener {
            initialSetup()
        }
    }

    private fun hideRefresh() {
        binding.professionalSwipeRefreshLayout.isRefreshing = false
    }

    private fun signOut() {
        Firebase.auth.signOut()
        showLoginScreen(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.log_off -> {
                logOut()
            }
            else -> {
                showLoginScreen(this)
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}

package com.example.nailschedule.view.activities.view.professional

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nailschedule.R
import com.example.nailschedule.databinding.ActivityProfessionalBinding
import com.example.nailschedule.view.activities.data.model.Time
import com.example.nailschedule.view.activities.utils.showLoginScreen
import com.example.nailschedule.view.activities.utils.showToast
import com.example.nailschedule.view.activities.view.activities.BottomNavigationActivity
import com.example.nailschedule.view.activities.viewmodels.CalendarFieldViewModel
import com.example.nailschedule.view.activities.viewmodels.ConnectivityViewModel
import com.example.nailschedule.view.activities.viewmodels.UsersViewModel
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class ProfessionalActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener {

    private lateinit var connectivityViewModel: ConnectivityViewModel
    private lateinit var calendarFieldViewModel: CalendarFieldViewModel
    private lateinit var usersViewModel: UsersViewModel

    private lateinit var binding: ActivityProfessionalBinding
    private var date: String? = null
    private var time: String? = null
    private var email: String? = null

    private var timeList: List<String>? = null
    private var professionalAction: ProfessionalActionEnum? = null

    private val professionalScheduleAdapter: ProfessionalAdapter by lazy {
        ProfessionalAdapter(::seeSchedule)
    }

    companion object {
        const val CALENDAR_FIELD_AND_USER_DELETION = "calendar_field_and_user_deletion"
        const val SETUP_ADAPTER = "setup_adapter"
        const val USER_DELETION = "user_deletion"
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
        usersViewModel =
            ViewModelProvider(this)[UsersViewModel::class.java]
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
            when (professionalAction) {
                ProfessionalActionEnum.IS_DELETION -> {
                    val previousTimeList = mutableListOf<String>()
                    timeList?.forEach { t ->
                        if (time?.let { time -> t.contains(time) } == true) {
                            val finalIndex = t.indexOf(";true")
                            val timeNew = t.substring(0, finalIndex)
                            previousTimeList.add(timeNew.plus(";false"))
                        } else {
                            previousTimeList.add(t)
                        }
                    }
                    date?.let { date ->
                        val hoursList = Time(previousTimeList)
                        calendarFieldViewModel.updateCalendarField(date, hoursList)
                        hideBtnDeleteSchedule()
                        val list = previousTimeList.subList(1, (previousTimeList.size - 1))
                        val professionalList = list.map { timeAvailability(it) }
                        professionalScheduleAdapter.submitList(professionalList)
                        showUnscheduledLabel()
                        deleteUser()
                    }
                }
                ProfessionalActionEnum.IS_SETUP_ADAPTER -> {
                    val mutableTimeList = mutableListOf<String>()
                    timeList?.forEachIndexed { index, t ->
                        if (index != 0) {
                            mutableTimeList.add(t)
                        }
                    } ?: run {
                        showToast(this, R.string.all_free)
                    }
                    val professionalList = mutableTimeList.map { timeAvailability(it) }
                    professionalScheduleAdapter.submitList(professionalList)
                }
            }
        })

        connectivityViewModel.hasInternet.observe(this,
            {
                if (it.first) {
                    when (it.second) {
                        CALENDAR_FIELD_AND_USER_DELETION -> {
                            date?.let { date ->
                                professionalAction = ProfessionalActionEnum.IS_DELETION
                                calendarFieldViewModel.getCalendarFieldData(this, date)
                            }
                        }
                        SETUP_ADAPTER -> {
                            date?.let { date ->
                                professionalAction = ProfessionalActionEnum.IS_SETUP_ADAPTER
                                calendarFieldViewModel.getCalendarFieldData(this, date)
                            }
                        }
                        USER_DELETION -> {
                            email?.let { email ->
                                usersViewModel.deleteUser(email)
                            }
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

    private fun deleteUser() {
        connectivityViewModel.checkForInternet(
            applicationContext,
            USER_DELETION
        )
    }

    private fun showUnscheduledLabel() = binding.tvUnscheduledTime.apply {
        text = getString(R.string.scheduled_deleted)
        isVisible = true
    }

    private fun hideUnscheduledLabel() {
        binding.tvUnscheduledTime.isVisible = false
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
        binding.cardViewUser.isVisible = false
    }

    private fun showCardView() {
        binding.cardViewUser.isVisible = true
    }

    private fun hideRecycler() {
        binding.rvSchedules.isVisible = false
    }

    private fun showRecycler() {
        binding.rvSchedules.isVisible = true
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
            deleteFirebaseUserAndCalendarField()
        }
        email = list[7].trim().removePrefix("email=")
    }

    private fun deleteFirebaseUserAndCalendarField() {
        connectivityViewModel.checkForInternet(
            applicationContext,
            CALENDAR_FIELD_AND_USER_DELETION
        )
    }

    private fun logOut() {
        connectivityViewModel.checkForInternet(
            applicationContext,
            BottomNavigationActivity.LOG_OUT
        )
    }

    private fun hideBtnDeleteSchedule() {
        binding.btnDeleteSchedule.isVisible = false
    }

    private fun showBtnDeleteSchedule() {
        binding.btnDeleteSchedule.isVisible = true
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

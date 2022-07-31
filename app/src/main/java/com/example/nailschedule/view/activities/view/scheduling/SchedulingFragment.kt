package com.example.nailschedule.view.activities.view.scheduling

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.nailschedule.R
import com.example.nailschedule.databinding.FragmentSchedulingBinding
import com.example.nailschedule.view.activities.data.model.Time
import com.example.nailschedule.view.activities.data.model.User
import com.example.nailschedule.view.activities.utils.*
import com.example.nailschedule.view.activities.utils.SharedPreferencesHelper.MIN_DATE
import com.example.nailschedule.view.activities.viewmodels.CalendarFieldViewModel
import com.example.nailschedule.view.activities.viewmodels.ConnectivityViewModel
import com.example.nailschedule.view.activities.viewmodels.UsersViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*


class SchedulingFragment : Fragment() {

    private var _binding: FragmentSchedulingBinding? = null

    private lateinit var galleryStartForResult: ActivityResultLauncher<Intent>

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var connectivityViewModel: ConnectivityViewModel
    private lateinit var calendarFieldViewModel: CalendarFieldViewModel
    private lateinit var usersViewModel: UsersViewModel

    private lateinit var arrayAdapter: ArrayAdapter<String>

    private var timeListOk = mutableListOf<String>()

    private var name = String.empty()
    private var service = String.empty()
    private var date = String.empty()
    private var time: String? = null
    private var uriString = String.empty()
    private var email = String.empty()

    private var previousDt = String.empty()
    private var previousTm = String.empty()
    private var currentTimeList: MutableList<String>? = null
    private var calendarUser: User? = null
    private var downloadUser: User? = null
    private var hourList: List<String>? = null
    private var dateCalendarFieldAddOrUpdate: String? = null
    private var addOrUpdateUser: User? = null

    private lateinit var originalList: List<String>

    private var isPreviousHourDeleted = false

    private var schedulingActionEnum: SchedulingActionEnum? = null

    private var dtMinusOne = String.empty()

    //About previous date chosen
    private var previousDate = String.empty()
    private var previousTime = String.empty()

    private var timeMutableList = mutableListOf<String>()

    companion object {
        fun newInstance() = SchedulingFragment()
        const val DELETE_ALL_DATA = "delete_all_data"
        const val SETUP_SPINNER = "setup_spinner"
        const val CALENDAR_FIELD = "calendar_field"
        const val DOWNLOAD = "download"
        const val ADD_OR_UPDATE_CALENDAR_FIELD = "add_or_update_calendar_field"
        const val USER_ADD_OR_UPDATE = "user_add_or_update"
        const val patternDate = "dd-MM-yyyy"
        const val patternTime = "HH:mm"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSchedulingBinding.inflate(inflater, container, false)
        val root: View = binding.root
        originalList = mutableListOf(
            requireContext().getString(R.string.select_the_hour),
            "08:00;false", "10:00;false",
            "12:00;false", "14:00;false",
            "16:00;false", "18:00;false"
        )
        setupViewModels()
        setupObserver()
        initializeAdapter(listOf(requireContext().getString(R.string.select_the_hour)))
        initializeEmail()
        setupCalendarViewDatesMinAndMax()
        saveMinDateAtSharedPreferences()
        hideSpinner()
        setupListeners()
        return root
    }

    private fun setupViewModels() {
        connectivityViewModel =
            ViewModelProvider(this)[ConnectivityViewModel::class.java]
        calendarFieldViewModel =
            ViewModelProvider(this)[CalendarFieldViewModel::class.java]
        usersViewModel =
            ViewModelProvider(this)[UsersViewModel::class.java]
    }

    @SuppressLint("SimpleDateFormat")
    private fun setupObserver() {
        calendarFieldViewModel.calendarField.observe(viewLifecycleOwner, { calendarField ->
            when (schedulingActionEnum) {
                SchedulingActionEnum.SETUP_SPINNER -> {
                    var isAddOrUpdateCalendarField = false
                    calendarField?.let { calendarField ->
                        val timeList = calendarField.timeList
                        timeList.forEach { time ->
                            timeListOk.add(time)
                        }
                    } ?: run {
                        isAddOrUpdateCalendarField = true
                        timeListOk.addAll(originalList)
                    }
                    if (timeListOk.size == 1) {
                        showToast(
                            requireContext(),
                            R.string.data_without_available_hour
                        )
                    } else {
                        val currentDate =
                            SimpleDateFormat(patternDate).format(Date())
                        if (date == currentDate) {
                            //If the hour is 7, so 7 + 2  = 9 ---> I can only dial
                            // from 10 o'clock down
                            timeListOk = deleteCurrentDayHour(timeListOk)
                        }
                        val listToAdapter = mutableListOf<String>()
                        if (timeListOk.size == 1) {
                            listToAdapter.add(timeListOk[0])
                            showToast(
                                requireContext(),
                                R.string.data_without_available_hour
                            )
                        } else {
                            timeListOk.forEach { time ->
                                if (time.hasSemicolonFalse()) {
                                    listToAdapter.add(time.removeSuffix(semicolonFalse))
                                }
                            }
                        }
                        initializeAdapter(listToAdapter)
                        if (isAddOrUpdateCalendarField && timeListOk.size > 1) {
                            addOrUpdateCalendarFieldAtFirestoreDatabase(
                                timeListOk,
                                date
                            )
                        }
                    }
                }
                SchedulingActionEnum.CALENDAR_FIELD -> {
                    val previousTimeList = mutableListOf<String>()
                    calendarField?.let {
                        val timeList = it.timeList
                        calendarUser?.let { calendarUser ->
                            if (previousDt == calendarUser.date) {
                                currentTimeList?.let { currentTimeList ->
                                    currentTimeList.forEach { t ->
                                        if (t.contains(previousTm) && t.hasSemicolonTrue()) {
                                            val finalIndex = t.indexOf(semicolonTrue)
                                            val timeNew = t.substring(0, finalIndex)
                                            previousTimeList.add(timeNew.plus(semicolonFalse))
                                        } else if (time?.let { time -> t.contains(time) } == true && t.hasSemicolonTrue()) {
                                            previousTimeList.add(replaceUserStr(t.plus(downloadUser.toString())))
                                        } else {
                                            previousTimeList.add(t)
                                        }
                                    }
                                    addOrUpdateUserAtFirestoreDatabase(
                                        previousTimeList,
                                        calendarUser
                                    )
                                }
                            } else {
                                timeList.forEach { t ->
                                    if (t.contains(previousTm) && t.hasSemicolonTrue()) {
                                        val finalIndex = t.indexOf(semicolonTrue)
                                        val timeNew =
                                            t.substring(0, finalIndex)
                                        previousTimeList.add(timeNew.plus(semicolonFalse))
                                    } else {
                                        previousTimeList.add(t)
                                    }
                                }
                                addOrUpdateCalendarFieldAtFirestoreDatabase(
                                    previousTimeList,
                                    previousDt
                                )
                                currentTimeList?.let { currentTimeList ->
                                    val currentTimeListWithUser = mutableListOf<String>()
                                    currentTimeList.forEach { t ->
                                        if (t.hasSemicolonTrue()) {
                                            currentTimeListWithUser.add(
                                                t.plus(
                                                    replaceUserStr(
                                                        downloadUser.toString()
                                                    )
                                                )
                                            )
                                        } else {
                                            currentTimeListWithUser.add(t)
                                        }
                                    }
                                    addOrUpdateUserAtFirestoreDatabase(
                                        currentTimeListWithUser,
                                        calendarUser
                                    )
                                }
                            }
                            clearFields()
                            setBottomVisibility(false)
                            setBtnSaveVisibility(true)
                        }
                    }
                }
                SchedulingActionEnum.CALENDAR_DOWNLOAD -> {
                    calendarField?.let { it ->
                        //About new date chosen
                        var canUseTime = false
                        val timeList = it.timeList
                        timeMutableList = mutableListOf()
                        timeList.forEach { t ->
                            var canSchedule = false
                            if ((time?.let { time -> t.contains(time) } == true) && t.hasSemicolonFalse()) {
                                canUseTime = true
                                canSchedule = true
                            }
                            if (canSchedule) {
                                timeMutableList.add(t.replace(semicolonFalse, semicolonTrue))
                            } else {
                                timeMutableList.add(t)
                            }
                        }

                        if (canUseTime) {
                            schedulingActionEnum = SchedulingActionEnum.USER_DOWNLOAD
                            usersViewModel.getUserData(requireContext(), email)
                        } else {
                            showToast(
                                requireContext(),
                                R.string.unavailable_time
                            )
                        }

                        binding.btnConfirm.setOnClickListener {
                            downloadUser?.let { downloadUser ->
                                getFirebaseFirestoreCalendarField(
                                    previousDate,
                                    previousTime,
                                    timeMutableList,
                                    downloadUser
                                )
                            }
                        }

                        binding.btnCancel.setOnClickListener {
                            clearFields()
                            setBottomVisibility(false)
                            setBtnSaveVisibility(true)
                        }
                    } ?: run {
                        val timeMutableList = mutableListOf<String>()
                        originalList.forEach { t ->
                            if (time?.let { time -> t.contains(time) } == true) {
                                timeMutableList.add(
                                    t.replace(semicolonFalse, semicolonTrue)
                                        .plus(replaceUserStr(downloadUser.toString()))
                                )
                            } else {
                                timeMutableList.add(t)
                            }
                        }
                        originalList.filter { it.contains(time.toString()) }
                        val timeMutableListWithUser =
                            mutableListOf<String>()
                        timeMutableList.forEach { t ->
                            if (t.hasSemicolonTrue()) {
                                timeMutableListWithUser.add(t.plus(replaceUserStr(downloadUser.toString())))
                            } else {
                                timeMutableListWithUser.add(t)
                            }
                        }
                        downloadUser?.let {
                            addOrUpdateUserAtFirestoreDatabase(
                                timeMutableListWithUser,
                                downloadUser!!
                            )
                        }
                    }
                }
                else -> {}
            }
        })

        usersViewModel.users.observe(viewLifecycleOwner, { user ->
            when (schedulingActionEnum) {
                SchedulingActionEnum.DELETE_ALL_DATA -> {
                    val userDate = user.date
                    if (userDate == dtMinusOne) {
                        usersViewModel.deleteUser(email)
                    }
                }
                SchedulingActionEnum.USER_DOWNLOAD -> {
                    user?.let {
                        previousDate = it.date
                        previousTime = it.time
                        //when the scheduled time was excluded, so I deal like
                        // a normal addition
                        if (isPreviousHourDeleted) {
                            addNewUserAndCalendarField(
                                timeMutableList
                            )
                        } else {
                            setupBottom(
                                previousDate,
                                previousTime
                            )
                            setBtnSaveVisibility(false)
                            setBottomVisibility(true)
                        }
                    } ?: run {
                        addNewUserAndCalendarField(
                            timeMutableList
                        )
                    }
                }
                else -> {
                }
            }
        })

        connectivityViewModel.hasInternet.observe(viewLifecycleOwner,
            { hasInternet ->
                if (hasInternet.first) {
                    when (hasInternet.second) {
                        DELETE_ALL_DATA -> {
                            for (i in 1 until 10) {
                                val now = Calendar.getInstance()
                                val f = SimpleDateFormat(patternDate)
                                val number = "-$i".toInt()
                                now.add(Calendar.DAY_OF_MONTH, number)
                                dtMinusOne = f.format(now.time)
                                schedulingActionEnum = SchedulingActionEnum.DELETE_ALL_DATA
                                calendarFieldViewModel.deleteCalendarField(dtMinusOne)
                                usersViewModel.getUserData(requireContext(), email)
                            }
                        }
                        SETUP_SPINNER -> {
                            schedulingActionEnum = SchedulingActionEnum.SETUP_SPINNER
                            calendarFieldViewModel.getCalendarFieldData(requireContext(), date)
                        }
                        CALENDAR_FIELD -> {
                            schedulingActionEnum = SchedulingActionEnum.CALENDAR_FIELD
                            calendarFieldViewModel.getCalendarFieldData(
                                requireContext(),
                                previousDt
                            )
                        }
                        DOWNLOAD -> {
                            schedulingActionEnum = SchedulingActionEnum.CALENDAR_DOWNLOAD
                            calendarFieldViewModel.getCalendarFieldData(requireContext(), date)
                        }
                        ADD_OR_UPDATE_CALENDAR_FIELD -> {
                            val time = Time(hourList!!)
                            FirebaseFirestore.getInstance().collection("calendarField")
                                .document(dateCalendarFieldAddOrUpdate!!).set(time)
                        }
                        USER_ADD_OR_UPDATE -> {
                            usersViewModel.updateUser(requireContext(), email, addOrUpdateUser!!)
                        }
                    }
                } else {
                    showToast(requireContext(), R.string.no_internet)
                }
            })
    }

    private fun replaceUserStr(str: String) = str.replace("User(", ";")
        .replace(")", "")
        .replace(",", ";")

    private fun addNewUserAndCalendarField(timeMutableList: MutableList<String>) {
        addOrUpdateUserAtFirestoreDatabase(
            timeMutableList,
            downloadUser!!
        )
        val timeMutableListWithUser =
            mutableListOf<String>()
        timeMutableList.forEach { t ->
            if ((time?.let { time -> t.contains(time) } == true) && t.hasSemicolonTrue()) {
                timeMutableListWithUser.add(t.plus(replaceUserStr(downloadUser.toString())))
            } else {
                timeMutableListWithUser.add(t)
            }
        }
        addOrUpdateCalendarFieldAtFirestoreDatabase(
            timeMutableListWithUser,
            date
        )
        clearFields()
        setBottomVisibility(false)
        setBtnSaveVisibility(true)
    }

    private fun initializeEmail() {
        email = SharedPreferencesHelper.read(
            SharedPreferencesHelper.EXTRA_EMAIL, ""
        ).orEmpty()
    }

    private fun initializeAdapter(list: List<String>) {
        arrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.support_simple_spinner_dropdown_item,
            list
        )
        binding.spinner.adapter = arrayAdapter
    }

    private fun deleteFirebaseFirestoreAllDates() {
        connectivityViewModel.checkForInternet(requireContext(), DELETE_ALL_DATA)
    }

    @SuppressLint("SimpleDateFormat")
    private fun saveMinDateAtSharedPreferences() {
        val previousMinDate: String? =
            SharedPreferencesHelper.read(MIN_DATE, "")

        val currentDateAndHour = Date()
        val currentDate = SimpleDateFormat(patternDate).format(currentDateAndHour)

        //Don't have any minDate saved before
        if (previousMinDate.isNullOrEmpty()) {
            SharedPreferencesHelper.write(MIN_DATE, currentDate)
            // Tne minDate changed
        } else if (previousMinDate != currentDate) {
            SharedPreferencesHelper.write(MIN_DATE, currentDate)
            deleteFirebaseFirestoreAllDates()
        }
    }

    private fun setupCalendarViewDatesMinAndMax() = binding.apply {
        calendarView.minDate = System.currentTimeMillis()
        val sixDaysAtMillis = 518400000
        val aWeekAfter = System.currentTimeMillis() + sixDaysAtMillis
        calendarView.maxDate = aWeekAfter
    }

    private fun hideSpinner() {
        binding.spinner.isVisible = false
    }

    private fun showSpinner() {
        binding.spinner.isVisible = true
    }

    @SuppressLint("WrongConstant", "ClickableViewAccessibility", "SimpleDateFormat")
    private fun setupListeners() = binding.apply {
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            initializeAdapter(mutableListOf(requireContext().getString(R.string.select_the_hour)))
            selectSpinnerPositionFirst()
            showSpinner()
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
            timeListOk.clear()
        }

        spinner.adapter = arrayAdapter
        spinner.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                timeListOk.clear()
                setupSpinnerWithFirebaseFirestoreDownload()
            }
            false
        }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                if (spinner.selectedItem.toString() != requireContext()
                        .getString(R.string.select_the_hour)
                ) {
                    time = spinner.selectedItem.toString()
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
            }
        }

        btnSave.setOnClickListener {
            name = txtName.editText?.text.toString().trim()
            service = txtService.editText?.text.toString().trim()

            if (isNotEmptyField(name) && isNotEmptyField(service) && isNotEmptyField(date) &&
                isNotEmptyField(time) && isNotEmptyField(uriString)
            ) {
                val user = User(
                    name = name, service = service, date = date,
                    time = time.orEmpty(), uriString = uriString, email = email
                )
                val currentDate = SimpleDateFormat(patternDate).format(Date())
                val currentHour = SimpleDateFormat(patternTime).format(Date())
                val currentHourToInt = currentHour.substring(0, 2).toInt()
                val timeToInt = time!!.substring(0, 2).toInt()

                if ((currentDate == date) &&
                    ((currentHourToInt <= 15) && (timeToInt <= currentHourToInt + 2)
                            || (currentHourToInt > 15))
                ) {
                    showToast(requireContext(), R.string.unavailable_time)
                } else {
                    downloadForFirebaseFirestore(user)
                }
            } else {
                printEmptyField()
            }
        }

        registerForActivityResult()

        btnChoosePhoneGallery.setOnClickListener {
            selectPhotoFromGallery()
        }

        ivPhoto.setOnClickListener {
            showOptionsToSelectPhoto()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun deleteCurrentDayHour(
        timeList: MutableList<String>
    ): MutableList<String> {

        val hoursLessThan2 = mutableListOf<String>()
        val currentDate = SimpleDateFormat(patternDate).format(Date())
        val currentHour = SimpleDateFormat(patternTime).format(Date())
        val currentHourToInt = currentHour.substring(0, 2).toInt()

        timeList.forEachIndexed { index, hour ->
            //Index 0 is "Selecione a hora"
            if (index != 0) {
                val timeToInt = hour.substring(0, 2).toInt()
                if ((currentDate == date) &&
                    ((currentHourToInt <= 15) && (timeToInt <= currentHourToInt + 2)
                            || (currentHourToInt > 15))
                ) {
                    hoursLessThan2.add(hour)
                }
            }
        }
        isPreviousHourDeleted =
            !hoursLessThan2.filter { it.hasTrueSemicolon() }.isNullOrEmpty()
        timeList.removeAll(hoursLessThan2)
        return timeList
    }

    private fun setupSpinnerWithFirebaseFirestoreDownload() {
        connectivityViewModel.checkForInternet(requireContext(), SETUP_SPINNER)
    }

    private fun getFirebaseFirestoreCalendarField(
        previousDt: String,
        previousTm: String,
        currentTimeList: MutableList<String>,
        calendarUser: User
    ) {
        this.previousDt = previousDt
        this.previousTm = previousTm
        this.currentTimeList = currentTimeList
        this.calendarUser = calendarUser
        connectivityViewModel.checkForInternet(requireContext(), CALENDAR_FIELD)
    }

    private fun downloadForFirebaseFirestore(downloadUser: User) {
        this.downloadUser = downloadUser
        connectivityViewModel.checkForInternet(requireContext(), DOWNLOAD)
    }

    private fun setupBottom(userDate: String, userTime: String) = binding.apply {
        tvRemark.apply {
            text = requireContext().getString(
                R.string.message_about_remark,
                userTime,
                userDate
            )
        }
    }

    private fun setBottomVisibility(visibility: Boolean) = binding.apply {
        tvRemark.isVisible = visibility
        btnConfirm.isVisible = visibility
        btnCancel.isVisible = visibility
    }

    private fun setBtnSaveVisibility(visibility: Boolean) {
        binding.btnSave.isVisible = visibility
    }

    private fun addOrUpdateCalendarFieldAtFirestoreDatabase(
        hourList: List<String>,
        dateCalendarFieldAddOrUpdate: String
    ) {
        this.hourList = hourList
        this.dateCalendarFieldAddOrUpdate = dateCalendarFieldAddOrUpdate
        connectivityViewModel.checkForInternet(
            requireContext(),
            ADD_OR_UPDATE_CALENDAR_FIELD
        )
    }

    //Firestore Database - Cloud Firestore
    private fun addOrUpdateUserAtFirestoreDatabase(
        hourAddOrUpdateList: List<String>,
        addOrUpdateUser: User
    ) {
        addOrUpdateCalendarFieldAtFirestoreDatabase(hourAddOrUpdateList, date)
        this.addOrUpdateUser = addOrUpdateUser
        connectivityViewModel.checkForInternet(
            requireContext(),
            USER_ADD_OR_UPDATE
        )
    }

    @SuppressLint("SimpleDateFormat")
    private fun clearFields() = binding.apply {
        txtName.editText?.setText("")
        txtService.editText?.setText("")
        calendarView.clearFocus()
        uriString = String.empty()
        hideSpinner()
        showOptionsToSelectPhoto()
    }

    private fun selectSpinnerPositionFirst() {
        binding.spinner.setSelection(0)
    }

    private fun printEmptyField() {
        when {
            isEmptyBlankOrNullField(name) -> {
                showToast(requireContext(), R.string.empty_name)
            }
            isEmptyBlankOrNullField(service) -> {
                showToast(requireContext(), R.string.empty_service)
            }
            isEmptyBlankOrNullField(date) -> {
                showToast(requireContext(), R.string.empty_date)
            }
            isEmptyBlankOrNullField(time) -> {
                if (timeListOk.size == 1) {
                    showToast(requireContext(), R.string.data_without_available_hour)
                } else {
                    showToast(requireContext(), R.string.empty_time)
                }
            }
            isEmptyBlankOrNullField(uriString) -> {
                showToast(requireContext(), R.string.empty_photo)
            }
        }
    }

    private fun selectPhotoFromGallery() {
        val intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        intent.type = "image/*"
        galleryStartForResult.launch(intent)
    }

    private fun registerForActivityResult() = binding.apply {
        galleryStartForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // The Task returned from this call is always completed, no need to attach
                    // a listener
                    uriString = result.data?.data.toString()
                    //Setting the image to imageView using Glide Library*/
                    Glide.with(requireContext()).load(uriString).into(ivPhoto)
                    showPhoto()
                }
            }
    }

    private fun showPhoto() = binding.apply {
        btnChoosePhoneGallery.isVisible = false
        tvInformationPhotoChange.isVisible = true
        ivPhoto.isVisible = true
    }

    private fun showOptionsToSelectPhoto() = binding.apply {
        tvInformationPhotoChange.isVisible = false
        ivPhoto.isVisible = false
        btnChoosePhoneGallery.isVisible = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showToast(context: Context, stringRes: Int) {
        Toast.makeText(
            context,
            context.getString(stringRes),
            Toast.LENGTH_LONG
        ).show()
    }
}
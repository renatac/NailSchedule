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
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.nailschedule.R
import com.example.nailschedule.databinding.FragmentSchedulingBinding
import com.example.nailschedule.view.activities.data.model.Time
import com.example.nailschedule.view.activities.data.model.User
import com.example.nailschedule.view.activities.utils.SharedPreferencesHelper
import com.example.nailschedule.view.activities.utils.SharedPreferencesHelper.MIN_DATE
import com.example.nailschedule.view.activities.view.gallery.GalleryViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*


class SchedulingFragment : Fragment() {

    private var _binding: FragmentSchedulingBinding? = null

    private lateinit var galleryStartForResult: ActivityResultLauncher<Intent>

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var galleryViewModel: GalleryViewModel

    private lateinit var arrayAdapter: ArrayAdapter<String>

    private var timeListOk = mutableListOf<String>()

    private var name: String? = null
    private var service: String? = null
    private var date: String? = null
    private var time: String? = null
    private var pos: Int = 0

    private var uriString: String? = null

    private var email: String? = null

    private var previousDt: String? = null
    private var previousTm: String? = null
    private var currentTimeList: MutableList<String>? = null
    private var calendarUser: User? = null
    private var downloadUser: User? = null
    private var hourList: List<String>? = null
    private var dateCalendarFieldAddOrUpdate: String? = null
    private var addOrUpdateUser: User? = null

    private lateinit var originalList: List<String>

    companion object {
        fun newInstance() = SchedulingFragment()
        const val DELETE_ALL_DATA = "delete_all_data"
        const val SETUP_SPINNER = "setup_spinner"
        const val CALENDAR_FIELD = "calendar_field"
        const val DOWNLOAD = "download"
        const val ADD_OR_UPDATE_CALENDAR_FIELD = "add_or_update_calendar_field"
        const val USER_ADD_OR_UPDATE = "user_add_or_update"
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
        galleryViewModel =
            ViewModelProvider(this).get(GalleryViewModel::class.java)
        setupObserver()
        initializeAdapter(listOf(requireContext().getString(R.string.select_the_hour)))
        initializeEmail()
        setupCalendarViewDatesMinAndMax()
        saveMinDateAtSharedPreferences()
        hideSpinner()
        setupListeners()
        return root
    }

    @SuppressLint("SimpleDateFormat")
    private fun setupObserver() {
        galleryViewModel.hasInternet.observe(
            viewLifecycleOwner,
            { it ->
                if (it.first) {
                    when (it.second) {
                        DELETE_ALL_DATA -> {
                            for (i in 1 until 10) {
                                val now = Calendar.getInstance()
                                val f = SimpleDateFormat("dd-MM-yyyy")
                                val number = "-$i".toInt()
                                now.add(Calendar.DAY_OF_MONTH, number)

                                val dtMinusOne = f.format(now.time)
                                FirebaseFirestore.getInstance().collection("calendarField")
                                    .document(dtMinusOne)
                                    .delete()

                                FirebaseFirestore.getInstance().collection("users")
                                    .document(email!!).get()
                                    .addOnSuccessListener { documentSnapshot ->
                                        documentSnapshot.data?.let {
                                            val userDate = it["date"] as String
                                            if (userDate == dtMinusOne) {
                                                FirebaseFirestore.getInstance().collection("users")
                                                    .document(email!!).delete()
                                            }
                                        }
                                    }
                            }
                        }
                        SETUP_SPINNER -> {
                            var isAddOrUpdateCalendarField = false
                            FirebaseFirestore.getInstance().collection("calendarField")
                                .document(date!!).get().addOnSuccessListener { documentSnapshot ->
                                    documentSnapshot.data?.let {
                                        val timeList = it["timeList"] as List<*>
                                        timeList.forEach { time ->
                                            timeListOk.add(time.toString())
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
                                            SimpleDateFormat("dd-MM-yyyy").format(Date())
                                        if (date == currentDate) {
                                            //If the hour is 7, so 7 + 2  = 9 ---> I can only dial
                                            // from 10 o'clock down
                                            timeListOk = deleteCurrentDayHour(timeListOk)
                                        }
                                        val listToAdapter = mutableListOf<String>()
                                        if(timeListOk.size == 1) {
                                            listToAdapter.add(timeListOk[0])
                                        } else {
                                            timeListOk.forEach { time ->
                                                if (time.contains(";false")) {
                                                    listToAdapter.add(time.removeSuffix(";false"))
                                                }
                                            }
                                        }
                                        initializeAdapter(listToAdapter)
                                        if (isAddOrUpdateCalendarField && timeListOk.size > 1) {
                                            addOrUpdateCalendarFieldAtFirestoreDatabase(
                                                timeListOk,
                                                date!!
                                            )
                                        }
                                    }
                                }.addOnFailureListener {
                                    print(it)
                                }
                        }
                        CALENDAR_FIELD -> {
                            val previousTimeList = mutableListOf<String>()
                            FirebaseFirestore.getInstance().collection("calendarField")
                                .document(previousDt!!).get()
                                .addOnSuccessListener { documentSnapshot ->
                                    documentSnapshot.data?.let {
                                        val timeList = it["timeList"] as List<*>
                                        if (previousDt.toString() == calendarUser!!.date) {
                                           /* timeList.forEach { t ->
                                                with(t.toString()) {
                                                    if (this.contains(previousTm.toString())) {
                                                        val finalIndex = this.indexOf(";true")
                                                        val timeNew = this.substring(0, finalIndex)
                                                        previousTimeList.add(timeNew.plus(";false"))
                                                    } else {
                                                        previousTimeList.add(this)
                                                    }
                                                }
                                            }
                                            addOrUpdateCalendarFieldAtFirestoreDatabase(
                                                previousTimeList,
                                                previousDt!!
                                            )
                                            print(currentTimeList)*/
                                            currentTimeList!!.forEach { t ->
                                                if (t.contains(previousTm.toString())) {
                                                    val finalIndex = t.indexOf(";true")
                                                    val timeNew = t.substring(0,finalIndex)
                                                    previousTimeList.add(timeNew.plus(";false"))
                                                } else if(t.contains(";true")) {
                                                    previousTimeList.add(t.plus(downloadUser.toString())
                                                        .replace("User(",";")
                                                        .replace(")","")
                                                        .replace(",",";"))
                                                } else {
                                                    previousTimeList.add(t)
                                                }
                                            }
                                            addOrUpdateUserAtFirestoreDatabase(
                                                previousTimeList,
                                                calendarUser!!
                                            )
                                        } else {
                                            timeList.forEach { t ->
                                                if (t.toString().contains(previousTm.toString())) {
                                                    val finalIndex = t.toString().indexOf(";true")
                                                    val timeNew = t.toString().substring(0,finalIndex)
                                                    previousTimeList.add(timeNew.plus(";false"))
                                                } else {
                                                    previousTimeList.add(t.toString())
                                                }
                                            }
                                            addOrUpdateCalendarFieldAtFirestoreDatabase(
                                                previousTimeList,
                                                previousDt!!
                                            )
                                            val currentTimeListWithUser = mutableListOf<String>()
                                            currentTimeList!!.forEach { t ->
                                                if (t.contains(";true")) {
                                                    currentTimeListWithUser.add(t.plus(downloadUser.toString()
                                                        .replace("User(",";")
                                                        .replace(")","")
                                                        .replace(",",";")))
                                                } else {
                                                    currentTimeListWithUser.add(t)
                                                }
                                            }
                                            addOrUpdateUserAtFirestoreDatabase(
                                                currentTimeListWithUser,
                                                calendarUser!!
                                            )
                                        }
                                        clearFields()
                                        setBottomVisibility(GONE)
                                        setBtnSaveVisibility(VISIBLE)
                                    }
                                }.addOnFailureListener {
                                    showToast(requireContext(), R.string.error_scheduling)
                                    print(it)
                                }
                        }
                        DOWNLOAD -> {
                            FirebaseFirestore.getInstance().collection("calendarField")
                                .document(date!!).get()
                                .addOnSuccessListener { documentSnapshot ->
                                    documentSnapshot.data?.let { it ->
                                        var canUseTime = false
                                        val timeList = it["timeList"] as MutableList<*>
                                        val timeMutableList = mutableListOf<String>()
                                        timeList.forEach { t ->
                                            var canSchedule = false
                                            if (t.toString()
                                                    .contains(time.toString()) &&
                                                t.toString()
                                                    .contains(";false")
                                            ) {
                                                canUseTime = true
                                                canSchedule = true
                                            }
                                            if (canSchedule) {
                                                timeMutableList.add(
                                                    t.toString().replace(";false", ";true")
                                                )
                                            } else {
                                                timeMutableList.add(t.toString())
                                            }
                                        }
                                        var previousDate = ""
                                        var previousTime = ""

                                        if (canUseTime) {
                                            FirebaseFirestore.getInstance().collection("users")
                                                .document(email!!).get()
                                                .addOnSuccessListener { documentSnapshot ->
                                                    documentSnapshot.data?.let {
                                                        previousDate = it["date"] as String
                                                        previousTime = it["time"] as String
                                                        setupBottom(previousDate, previousTime)
                                                        setBtnSaveVisibility(GONE)
                                                        setBottomVisibility(VISIBLE)
                                                    } ?: run {
                                                        addOrUpdateUserAtFirestoreDatabase(
                                                            timeMutableList,
                                                            downloadUser!!
                                                        )
                                                        val timeMutableListWithUser = mutableListOf<String>()
                                                        timeMutableList.forEach { t ->
                                                            if(t.contains(";true")) {
                                                                timeMutableListWithUser.add(
                                                                    t.plus(downloadUser.toString()
                                                                        .replace("User(",";")
                                                                        .replace(")","")
                                                                        .replace(",",";")))
                                                            } else {
                                                                timeMutableListWithUser.add(t)
                                                            }
                                                        }
                                                        addOrUpdateCalendarFieldAtFirestoreDatabase(
                                                            timeMutableListWithUser,
                                                            date!!
                                                        )
                                                        clearFields()
                                                        setBottomVisibility(GONE)
                                                        setBtnSaveVisibility(VISIBLE)
                                                    }
                                                }.addOnFailureListener {
                                                    showToast(
                                                        requireContext(),
                                                        R.string.error_scheduling
                                                    )
                                                    print(it)
                                                }
                                        } else {
                                            showToast(requireContext(), R.string.unavailable_time)
                                        }

                                        binding.btnConfirm.setOnClickListener {
                                            getFirebaseFirestoreCalendarField(
                                                previousDate,
                                                previousTime,
                                                timeMutableList,
                                                downloadUser!!
                                            )
                                        }

                                        binding.btnCancel.setOnClickListener {
                                            clearFields()
                                            setBottomVisibility(GONE)
                                            setBtnSaveVisibility(VISIBLE)
                                        }
                                    } ?: run {
                                        val timeMutableList = mutableListOf<String>()
                                        originalList.forEach { t ->
                                            if (it.toString().contains(time.toString())) {
                                                timeMutableList.add(t.replace(";false", ";true")
                                                    .plus(downloadUser.toString()
                                                    .replace("User(",";")
                                                    .replace(")","")
                                                    .replace(",",";")))
                                            } else {
                                                timeMutableList.add(t)
                                            }
                                        }
                                        originalList.filter { it.contains(time.toString()) }
                                        val timeMutableListWithUser = mutableListOf<String>()
                                        timeMutableList.forEach { t ->
                                            if(t.contains(";true")) {
                                                timeMutableListWithUser.add(
                                                    t.plus(downloadUser.toString()
                                                        .replace("User(",";")
                                                        .replace(")","")
                                                        .replace(",",";")))
                                            } else {
                                                timeMutableListWithUser.add(t)
                                            }
                                        }
                                        addOrUpdateUserAtFirestoreDatabase(
                                            timeMutableListWithUser,
                                            downloadUser!!
                                        )
                                    }
                                }.addOnFailureListener {
                                    showToast(requireContext(), R.string.error_scheduling)
                                    print(it)
                                }
                        }
                        ADD_OR_UPDATE_CALENDAR_FIELD -> {
                            val time = Time(hourList!!)
                            FirebaseFirestore.getInstance().collection("calendarField")
                                .document(dateCalendarFieldAddOrUpdate!!).set(time)
                        }
                        USER_ADD_OR_UPDATE -> {
                            FirebaseFirestore.getInstance().collection("users")
                                .document(email!!)
                                .set(addOrUpdateUser!!) //add the data if it doesn't already exist and update it if it already exists
                                .addOnSuccessListener {
                                    showToast(requireContext(), R.string.successful_scheduling)
                                }
                                .addOnFailureListener {
                                    print(it)
                                    showToast(requireContext(), R.string.error_scheduling)
                                }
                        }
                    }
                } else {
                    showToast(requireContext(), R.string.no_internet)
                }
            })
    }

    private fun initializeEmail() {
        email = SharedPreferencesHelper.read(
            SharedPreferencesHelper.EXTRA_EMAIL, ""
        )
    }

    private fun initializeAdapter(list: List<String>) {
        arrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.support_simple_spinner_dropdown_item,
            list
        )
        binding.spinner.adapter = arrayAdapter
    }

    @SuppressLint("SimpleDateFormat")
    private fun deleteFirebaseFirestoreAllDates() {
        galleryViewModel.checkForInternet(requireContext(), DELETE_ALL_DATA)
    }

    @SuppressLint("SimpleDateFormat")
    private fun saveMinDateAtSharedPreferences() {
        val previousMinDate: String? =
            SharedPreferencesHelper.read(MIN_DATE, "")

        val currentDateAndHour = Date()
        val currentDate = SimpleDateFormat("dd-MM-yyyy").format(currentDateAndHour)

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
        binding.spinner.visibility = GONE
    }

    private fun showSpinner() {
        binding.spinner.visibility = VISIBLE
    }

    @SuppressLint("WrongConstant", "ClickableViewAccessibility", "SimpleDateFormat")
    private fun setupListeners() = binding.apply {
        //calendar.date = 1640799751672
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            //selectSpinnerPosition(0)
            initializeAdapter(
                mutableListOf(
                    requireContext().getString(R.string.select_the_hour)
                )
            )
            selectSpinnerPosition(0)
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
        spinner.setOnTouchListener { v, event ->
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
                    pos = position
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
                    name = name!!, service = service!!, date = date!!,
                    time = time!!, uriString = uriString!!, isMarked = true
                )
                val currentDate = SimpleDateFormat("dd-MM-yyyy").format(Date())
                val currentHour = SimpleDateFormat("HH:mm").format(Date())
                val currentHourToInt = currentHour.substring(0, 2).toInt()
                val timeToInt = time!!.substring(0, 2).toInt()

                if ((currentDate == date) &&
                    ((currentHourToInt <= 15) && (timeToInt <= currentHourToInt + 2)
                    || (currentHourToInt > 15))) {
                    showToast(requireContext(), R.string.q)
                    //showToast(requireContext(), R.string.unavailable_time)
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
        val currentDate = SimpleDateFormat("dd-MM-yyyy").format(Date())
        val currentHour = SimpleDateFormat("HH:mm").format(Date())
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
            timeList.removeAll(hoursLessThan2)
            return timeList
        }

    private fun setupSpinnerWithFirebaseFirestoreDownload() {
        galleryViewModel.checkForInternet(requireContext(), SETUP_SPINNER)
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
        galleryViewModel.checkForInternet(requireContext(), CALENDAR_FIELD)
    }

    private fun downloadForFirebaseFirestore(downloadUser: User) {
        this.downloadUser = downloadUser
        galleryViewModel.checkForInternet(requireContext(), DOWNLOAD)
    }

    private fun setupBottom(userDate: String, userTime: String) = binding.apply {
        tvRemark.apply {
            text = requireContext().getString(
                R.string.message_about_remark,
                userTime, userDate
            )
        }
    }

    private fun setBottomVisibility(visibility: Int) = binding.apply {
        tvRemark.visibility = visibility
        btnConfirm.visibility = visibility
        btnCancel.visibility = visibility
    }

    private fun setBtnSaveVisibility(visibility: Int) {
        binding.btnSave.visibility = visibility
    }

    private fun addOrUpdateCalendarFieldAtFirestoreDatabase(
        hourList: List<String>,
        dateCalendarFieldAddOrUpdate: String
    ) {
        this.hourList = hourList
        this.dateCalendarFieldAddOrUpdate = dateCalendarFieldAddOrUpdate
        galleryViewModel.checkForInternet(
            requireContext(),
            ADD_OR_UPDATE_CALENDAR_FIELD
        )
    }

    //Firestore Database - Cloud Firestore
    private fun addOrUpdateUserAtFirestoreDatabase(
        hourAddOrUpdateList: List<String>,
        addOrUpdateUser: User
    ) {
        addOrUpdateCalendarFieldAtFirestoreDatabase(hourAddOrUpdateList, date!!)
        this.addOrUpdateUser = addOrUpdateUser
        galleryViewModel.checkForInternet(
            requireContext(),
            USER_ADD_OR_UPDATE
        )
    }

    @SuppressLint("SimpleDateFormat")
    private fun clearFields() = binding.apply {
        txtName.editText?.setText("")
        txtService.editText?.setText("")
        calendarView.clearFocus()
        uriString = null
        hideSpinner()
        showOptionsToSelectPhoto()
    }

    private fun selectSpinnerPosition(position: Int) {
        binding.spinner.setSelection(position)
    }

    private fun printEmptyField() {
        when {
            isEmptyField(name) -> {
                showToast(requireContext(), R.string.empty_name)
            }
            isEmptyField(service) -> {
                showToast(requireContext(), R.string.empty_service)
            }
            isEmptyField(date) -> {
                showToast(requireContext(), R.string.empty_date)
            }
            isEmptyField(time) -> {
                if (timeListOk.size == 1) {
                    showToast(requireContext(), R.string.data_without_available_hour)
                } else {
                    showToast(requireContext(), R.string.empty_time)
                }
            }
            isEmptyField(uriString) -> {
                showToast(requireContext(), R.string.empty_photo)
            }
        }
    }

    private fun isNotEmptyField(field: String?): Boolean {
        return field != null && field.isNotBlank() && field.isNotEmpty()
    }

    private fun isEmptyField(field: String?): Boolean {
        return field == null || field.isBlank() || field.isEmpty()
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
        btnChoosePhoneGallery.visibility = GONE
        tvInformationPhotoChange.visibility = VISIBLE
        ivPhoto.visibility = VISIBLE
    }

    private fun showOptionsToSelectPhoto() = binding.apply {
        tvInformationPhotoChange.visibility = GONE
        ivPhoto.visibility = GONE
        btnChoosePhoneGallery.visibility = VISIBLE
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
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
import com.example.nailschedule.view.activities.data.model.TimeOk
import com.example.nailschedule.view.activities.data.model.User
import com.example.nailschedule.view.activities.utils.SharedPreferencesHelper
import com.example.nailschedule.view.activities.utils.SharedPreferencesHelper.DATE
import com.example.nailschedule.view.activities.utils.SharedPreferencesHelper.MIN_DATE
import com.example.nailschedule.view.activities.utils.SharedPreferencesHelper.NAME
import com.example.nailschedule.view.activities.utils.SharedPreferencesHelper.POSITION
import com.example.nailschedule.view.activities.utils.SharedPreferencesHelper.SERVICE
import com.example.nailschedule.view.activities.utils.SharedPreferencesHelper.TIME
import com.example.nailschedule.view.activities.utils.SharedPreferencesHelper.URI_STRING
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class SchedulingFragment : Fragment() {

    private lateinit var schedulingViewModel: SchedulingViewModel
    private var _binding: FragmentSchedulingBinding? = null

    private lateinit var galleryStartForResult: ActivityResultLauncher<Intent>

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var arrayAdapter: ArrayAdapter<String>

    private var name: String? = null
    private var service: String? = null
    private var date: String? = null
    private var time: String? = null
    private var pos: Int = 0

    private var uriString: String? = null

    private var email: String? = null

    companion object {
        fun newInstance() = SchedulingFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        schedulingViewModel =
            ViewModelProvider(this).get(SchedulingViewModel::class.java)
        _binding = FragmentSchedulingBinding.inflate(inflater, container, false)
        val root: View = binding.root
        arrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.support_simple_spinner_dropdown_item,
            listOf(requireContext().getString(R.string.select_the_hour)
            )
        )
        email = SharedPreferencesHelper.read(
            SharedPreferencesHelper.EXTRA_EMAIL, ""
        )
        setupCalendarViewDatesMinAndMax()
        saveMinDateAtSharedPreferences()
        hideSpinner()
        setupListeners()
        return root
    }

    private fun deleteFirebaseFirestoreAllDates(previousMinDate: String) {
        FirebaseFirestore.getInstance().collection("schedules")
            .document(previousMinDate)
            .delete()
        FirebaseFirestore.getInstance().collection("calendarField")
            .document(previousMinDate)
            .delete()

        FirebaseFirestore.getInstance().collection("users")
            .document(email!!).get().addOnSuccessListener { documentSnapshot ->
                documentSnapshot.data?.let {
                    val userDate = it["date"] as String
                    if(userDate == previousMinDate) {
                        FirebaseFirestore.getInstance().collection("users")
                            .document(email!!).delete()
                    }
                }
            }

    }

    @SuppressLint("SimpleDateFormat")
    private fun saveMinDateAtSharedPreferences() {
        val previousMinDate: String? =
            SharedPreferencesHelper.read(MIN_DATE,"")

        val currentDateAndHour = Date()
        val currentDate = SimpleDateFormat("dd-MM-yyyy").format(currentDateAndHour)

        //Don't have any minDate saved before
        if(previousMinDate.isNullOrEmpty()) {
            SharedPreferencesHelper.write(MIN_DATE, currentDate)
        // Tne minDate changed
        } else if(previousMinDate != currentDate) {
            SharedPreferencesHelper.write(MIN_DATE, currentDate)
            deleteFirebaseFirestoreAllDates(previousMinDate)
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
            showSpinner()
            val monthOk = month + 1
            date = if(monthOk <= 9) { "$dayOfMonth-0${monthOk}-$year"}
               else { "$dayOfMonth-${monthOk}-$year" }
        }

        spinner.adapter = arrayAdapter
        spinner.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
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
                        .getString(R.string.select_the_hour)) {
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
                    time = time!!, uriString = uriString!!
                )
                val currentHour = SimpleDateFormat("HH:mm").format(Date())
                val currentHourToLong = currentHour.toString().substring(0,2).toLong()
                val timeToLong = time!!.substring(0,2).toLong()

                if(timeToLong < currentHourToLong + 2) {
                    showToast(requireContext(), R.string.unavailable_time)
                } else {
                    downloadForFirebaseFirestore(date!!, user)
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
        timeList: MutableList<String>)
    : MutableList<String> {
        val currentDateAndHour = Date()
        val currentDate = SimpleDateFormat("dd-MM-yyyy").format(currentDateAndHour)
        val currentHour = SimpleDateFormat("HH:mm").format(currentDateAndHour)

        val hoursLessThan2 = mutableListOf<String>()
        if(date == currentDate) {
            timeList.forEachIndexed { index, hour ->
                if(index != 0) {
                    val currentHourToLong = currentHour.toString().substring(0,2).toLong()
                    val hourToLong = hour.substring(0,2).toLong()
                    if(hourToLong < currentHourToLong + 2) {
                        hoursLessThan2.add(hour)
                    }
                }
            }
        }
        timeList.removeAll(hoursLessThan2)
        return timeList
    }

    private fun setupSpinnerWithFirebaseFirestoreDownload() {
        var timeListOk = mutableListOf<String>()
        FirebaseFirestore.getInstance().collection("calendarField")
            .document(date!!).get().addOnSuccessListener { documentSnapshot ->
                documentSnapshot.data?.let {
                    val timeList = it["timeList"] as List<*>
                    timeList.forEach { time ->
                        timeListOk.add(time.toString())
                    }
                } ?: run {
                    val originalList = mutableListOf(
                        requireContext().getString(R.string.select_the_hour),
                        "08:00", "10:00", "12:00", "14:00", "16:00", "18:00")
                    timeListOk.addAll(originalList)
                    addOrUpdateCalendarFieldFirestoreDatabase(originalList, date!!)
                }
                timeListOk = deleteCurrentDayHour(timeListOk)
                addOrUpdateCalendarFieldFirestoreDatabase(timeListOk, date!!)
                arrayAdapter =
                    ArrayAdapter(
                        requireContext(),
                        R.layout.support_simple_spinner_dropdown_item,
                        timeListOk
                    )
                binding.spinner.adapter = arrayAdapter
            }.addOnFailureListener {
                print(it)
            }
    }

    private fun getFirebaseFirestoreCalendarField(
        previousDate: String,
        previousTime: String,
        actualTimeList: MutableList<String>,
        user: User) {
        val previousTimeList = mutableListOf<String>()
        FirebaseFirestore.getInstance().collection("calendarField")
            .document(previousDate).get().addOnSuccessListener { documentSnapshot ->
                documentSnapshot.data?.let {
                    val timeList = it["timeList"] as List<*>
                    timeList.forEach { time ->
                        previousTimeList.add(time.toString())
                    }
                    previousTimeList.apply {
                        add(previousTime)
                        sort()
                    }
                    if(user.date == previousDate) {
                        actualTimeList.apply {
                            add(previousTime)
                            sort()
                        }
                        addOrUpdateFirestoreDatabase(actualTimeList, user)
                    } else {
                        addOrUpdateCalendarFieldFirestoreDatabase(previousTimeList, previousDate)
                        addOrUpdateFirestoreDatabase(actualTimeList, user)
                    }
                    clearFields()
                    setBottomVisibility(GONE)
                    setBtnSaveVisibility(VISIBLE)
                    clearSharedPreferencesDatas()
                }
            }
    }

    private fun downloadForFirebaseFirestore(date: String, user: User) {
        FirebaseFirestore.getInstance().collection("calendarField")
            .document(date).get().addOnSuccessListener { documentSnapshot ->
                documentSnapshot.data?.let {
                    var canUseTime = false
                    val timeList = it["timeList"] as List<*>
                    val actualTimeList = mutableListOf<String>()
                    timeList.forEach { t ->
                        if (time == t.toString()) {
                            canUseTime = true
                        } else {
                            actualTimeList.add(t.toString())
                        }
                    }

                    var previousDate = ""
                    var previousTime = ""

                    if (canUseTime) {
                        FirebaseFirestore.getInstance().collection("users")
                            .document(email!!).get().addOnSuccessListener { documentSnapshot ->
                                documentSnapshot.data?.let {
                                    previousDate = it["date"] as String
                                    previousTime = it["time"] as String
                                    setupBottom(previousDate, previousTime)
                                    setBtnSaveVisibility(GONE)
                                    setBottomVisibility(VISIBLE)
                                } ?: run {
                                    addOrUpdateFirestoreDatabase(actualTimeList, user)
                                    clearFields()
                                    clearSharedPreferencesDatas()
                                }
                            }
                    } else {
                        showToast(requireContext(), R.string.unavailable_time)
                    }

                    binding.btnConfirm.setOnClickListener {
                        getFirebaseFirestoreCalendarField(previousDate, previousTime, actualTimeList, user)
                    }

                    binding.btnCancel.setOnClickListener {
                        clearFields()
                        clearSharedPreferencesDatas()
                        setBottomVisibility(GONE)
                        setBtnSaveVisibility(VISIBLE)
                    }

                }
            }.addOnFailureListener {
                print(it)
            }
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

    private fun addOrUpdateCalendarFieldFirestoreDatabase(
        hourList: List<String>,
        dateOk: String) {
        val time = TimeOk(hourList)
        FirebaseFirestore.getInstance().collection("calendarField")
            .document(dateOk).set(time)
    }

    //Firestore Database - Cloud Firestore
    private fun addOrUpdateFirestoreDatabase(hourList: List<String>, user: User) {
        addOrUpdateCalendarFieldFirestoreDatabase(hourList, date!!)

        FirebaseFirestore.getInstance().collection("users")
            .document(email!!)
            .set(user) //add the data if it doesn't already exist and update it if it already exists
            .addOnSuccessListener {
                showToast(requireContext(), R.string.successful_scheduling)
            }
            .addOnFailureListener {
                print(it)
                showToast(requireContext(), R.string.error_scheduling)
            }

        val example: MutableMap<String, User> = HashMap()
        example[user.time] = user
        example[user.time] = user

        FirebaseFirestore.getInstance().collection("schedules")
            .document(date!!)
            .set(example) //add the data if it doesn't already exist and update it if it already exists
            .addOnSuccessListener {
                showToast(requireContext(), R.string.successful_scheduling)
            }
            .addOnFailureListener {
                print(it)
                showToast(requireContext(), R.string.error_scheduling)
            }
    }

    @SuppressLint("SimpleDateFormat")
    private fun clearFields() = binding.apply {
        txtName.editText?.setText("")
        txtService.editText?.setText("")
        calendarView.clearFocus()
        uriString = null
        spinner.setSelection(0)
        hideSpinner()
        showOptionsToSelectPhoto()
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
                showToast(requireContext(), R.string.empty_time)
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

    //Clears data except data and time
    private fun clearSharedPreferencesDatas() {
         SharedPreferencesHelper.write(NAME, "")
         SharedPreferencesHelper.write(SERVICE, "")
         SharedPreferencesHelper.write(POSITION, 0)
         SharedPreferencesHelper.write(URI_STRING, "")
    }

    override fun onPause() {
        super.onPause()
        with(binding) {
            fillInAllSharedPreferencesFields(
                name = txtName.editText?.text.toString().trim(),
                service = txtService.editText?.text.toString().trim(),
                date = date.toString(),
                time = time.toString(),
                position = pos,
                uriString = uriString ?: ""
            )
        }
    }

    private fun fillInAllSharedPreferencesFields(
        name: String,
        service: String,
        date: String,
        time: String,
        position: Int,
        uriString: String
    ) {
        SharedPreferencesHelper.write(NAME, name)
        SharedPreferencesHelper.write(SERVICE, service)
        SharedPreferencesHelper.write(DATE, date)
        SharedPreferencesHelper.write(TIME, time)
        SharedPreferencesHelper.write(POSITION, position)
        SharedPreferencesHelper.write(URI_STRING, uriString)
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
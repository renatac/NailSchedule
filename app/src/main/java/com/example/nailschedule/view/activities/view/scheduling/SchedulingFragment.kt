package com.example.nailschedule.view.activities.view.scheduling

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
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
import com.example.nailschedule.view.activities.utils.SharedPreferencesHelper.DAY
import com.example.nailschedule.view.activities.utils.SharedPreferencesHelper.MONTH
import com.example.nailschedule.view.activities.utils.SharedPreferencesHelper.NAME
import com.example.nailschedule.view.activities.utils.SharedPreferencesHelper.SERVICE
import com.example.nailschedule.view.activities.utils.SharedPreferencesHelper.TIME
import com.example.nailschedule.view.activities.utils.SharedPreferencesHelper.URI_STRING
import com.example.nailschedule.view.activities.utils.SharedPreferencesHelper.YEAR
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class SchedulingFragment : Fragment() {

    private lateinit var schedulingViewModel: SchedulingViewModel
    private var _binding: FragmentSchedulingBinding? = null

    private lateinit var galleryStartForResult: ActivityResultLauncher<Intent>

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var name: String? = null
    private var service: String? = null
    private var date: String? = null
    private var _year: Int? = null
    private var _month: Int? = null
    private var _day: Int? = null
    private var time: String? = null

    private var uriString: String? = null

    private var originalHoursList: MutableList<String> = mutableListOf()
    private var hoursList: MutableList<String> = mutableListOf()

    private val calendar = Calendar.getInstance()

    companion object {
        fun newInstance() = SchedulingFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        schedulingViewModel =
            ViewModelProvider(this).get(SchedulingViewModel::class.java)

        _binding = FragmentSchedulingBinding.inflate(inflater, container, false)
        val root: View = binding.root

        /* val textView: TextView = binding.textDashboard
        scheduleViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        }) */

        getSharedPreferencesDatas()
        validateEmptyFields()
        fillHourByTodayDate()
        setupListeners()
        return root
    }

    @SuppressLint("SimpleDateFormat")
    private fun fillHourByTodayDate() {
        val currentDate = Date()
        val currentDateFormat = SimpleDateFormat("dd-MM-yyyy").format(currentDate)
        getTimeForDate(currentDateFormat)
    }

    private fun setupSpinner(timeList: List<String>? = null, hourList: List<String>? = null) =
        binding.apply {
            if (timeList?.isNullOrEmpty() == false &&
                ((originalHoursList.isEmpty()) || (timeList.size >= originalHoursList.size))
            ) {
                originalHoursList = timeList as MutableList<String>
            } else if (hourList?.isNullOrEmpty() == false) {
                originalHoursList = hourList.toMutableList()
            }
            val arrayAdapter = ArrayAdapter(
                requireContext(), R.layout.support_simple_spinner_dropdown_item, originalHoursList
            )
            spinner.adapter = arrayAdapter
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parentView: AdapterView<*>?,
                    selectedItemView: View?,
                    position: Int,
                    id: Long
                ) {
                    if (spinner.selectedItem.toString() != "Selecione o Horário") {
                        time = spinner.selectedItem.toString()
                        tvSelectedTime.text = requireContext()
                            .getString(R.string.scheduled_time, time)
                    } else {
                        time = ""
                        tvSelectedTime.text = ""
                    }
                }

                override fun onNothingSelected(parentView: AdapterView<*>?) {
                }
            }
        }

    private fun validateEmptyFields() {
        with(binding) {
            if (name?.isNotEmpty() == true && name != null) {
                txtName.editText?.setText(name)
            }
            if (service?.isNotEmpty() == true && service != null) {
                txtService.editText?.setText(service)
            }
            if (date?.isNotEmpty() == true && date != null) {
                tvSelectedDate.text = date
                if (_year != null && _year != 0) {
                    calendar.set(_year!!, _month!! -1, _day!!)
                    calendarView.date = calendar.timeInMillis
                }
            }
            if (time?.isNotEmpty() == true && time != null) {
                tvSelectedTime.text = time
            }
            if (uriString?.isNotEmpty() == true && uriString != null) {
                //Setting the image to imageView using Glide Library
                Glide.with(requireContext()).load(uriString).into(ivPhoto)
                showPhoto()
            }
        }
    }

    @SuppressLint("WrongConstant")
    private fun setupListeners() = binding.apply {
        //calendar.date = 1640799751672
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            with(binding.tvSelectedDate) {
                text = requireContext()
                    .getString(R.string.scheduled_date, dayOfMonth, month + 1, year)
            }
            date = "${dayOfMonth}/${month + 1}/$year"
            _year = year
            _month = month + 1
            _day = dayOfMonth
            getTimeForDate(date!!)
        }
        /* btnSchedule.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener =
                TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                    cal.set(Calendar.HOUR_OF_DAY, hour)
                    cal.set(Calendar.MINUTE, minute)
                    time = SimpleDateFormat("HH:mm").format(cal.time)
                    tvSelectedTime.text = requireContext().getString(
                        R.string.scheduled_time,
                        time
                    )
                }
            TimePickerDialog(
                requireContext(),
                timeSetListener,
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
        } */

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
                addOrUpdateFirestoreDatabase(user)
                clearFields()
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

    private fun getTimeForDate(date: String) {
        val formattedDate = date.replace("/", "-")
        //val pair: Pair<String, MutableList<String>> = Pair(formattedDate!!, hoursList)
        FirebaseFirestore.getInstance().collection("calendarField")
            .document(formattedDate).get().addOnSuccessListener { documentSnapshot ->
                documentSnapshot.data?.let {
                    val timeList = it["timeList"] as List<*>
                    val timeListOk = mutableListOf<String>()
                    timeList.forEach { time ->
                        timeListOk.add(time.toString())
                    }
                    setupSpinner(timeList = timeListOk)
                } ?: run {
                    val hourList = mutableListOf(
                        "Selecione o Horário", "08:00", "10:00", "12:00",
                        "14:00", "16:00", "18:00"
                    )
                    setupSpinner(hourList = hourList)
                    uploadForFirebaseFirestore(formattedDate, hourList)
                }
            }.addOnFailureListener {
                print(it)
            }
    }

    private fun uploadForFirebaseFirestore(formattedDate: String, hourList: List<String>) {
        val time = TimeOk(hourList)
        FirebaseFirestore.getInstance().collection("calendarField")
            .document(formattedDate).set(time)
    }

    @SuppressLint("SimpleDateFormat")
    private fun clearFields() = binding.apply {
        txtName.editText?.setText("")
        txtService.editText?.setText("")
        calendarView.clearFocus()
        tvSelectedDate.text = ""
        tvSelectedTime.text = ""
        uriString = null
        spinner.setSelection(0)

        val daySimpleDataFormat = SimpleDateFormat("dd")
        val monthSimpleDataFormat = SimpleDateFormat("MM")
        val yearSimpleDataFormat = SimpleDateFormat("yyyy")

        val currentDay = daySimpleDataFormat.format(Date()).toInt()
        val currentMonth = monthSimpleDataFormat.format(Date()).toInt()
        val currentYear = yearSimpleDataFormat.format(Date()).toInt()
        calendar.set(currentYear, currentMonth, currentDay)
        calendarView.date = calendar.timeInMillis

        showOptionsToSelectPhoto()
        //Setting the image to imageView using Glide Library*/
        //Glide.with(requireContext()).load(null).into(ivPhoto)
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
        btnChoosePhoneGallery.visibility = View.GONE
        tvInformationPhotoChange.visibility = View.VISIBLE
        ivPhoto.visibility = View.VISIBLE
    }

    private fun showOptionsToSelectPhoto() = binding.apply {
        tvInformationPhotoChange.visibility = View.GONE
        ivPhoto.visibility = View.GONE
        btnChoosePhoneGallery.visibility = View.VISIBLE
    }

    private fun getSharedPreferencesDatas() {
        name = SharedPreferencesHelper.read(
            NAME, ""
        )
        service = SharedPreferencesHelper.read(
            SERVICE, ""
        )
        date = SharedPreferencesHelper.read(
            DATE, ""
        )
        _year = SharedPreferencesHelper.read(
            YEAR, 0
        )
        _month = SharedPreferencesHelper.read(
            MONTH, 0
        )
        _day = SharedPreferencesHelper.read(
            DAY, 0
        )
        time = SharedPreferencesHelper.read(
            TIME, ""
        )
        uriString = SharedPreferencesHelper.read(
            URI_STRING, ""
        )
    }

    @SuppressLint("SimpleDateFormat")
    private fun convertDateToMilliSeconds(
        dateFormat: String,
        date: String
    ): Long {
        val formatter = SimpleDateFormat(dateFormat)
        val formattedDate = formatter.parse(date)
        return formattedDate.time
    }

    @SuppressLint("SimpleDateFormat")
    private fun convertTimeToMilliSeconds(
        time: String
    ): Long {
        val timeList = time.split(":")
        val hours = timeList[0].toInt()
        val minutes = timeList[1].toInt()
        return ((hours * 60 * 60 * 1000) + (minutes * 60 * 1000)).toLong()
    }

    private fun convertMillisecondToSeconds(milis: Long) = milis / 1000

    //Firestore Database - Cloud Firestore
    private fun addOrUpdateFirestoreDatabase(user: User) {
        val email = SharedPreferencesHelper.read(
            SharedPreferencesHelper.EXTRA_EMAIL, ""
        )
        /* var soma = convertDateToMilliSeconds("MM/dd/yyyy", "02/10/2020") +
                 convertTimeToMilliSeconds("01:00")
         print(soma)
         soma = convertMillisecondToSeconds(soma)

         val timestamp = Timestamp(soma, 0)
         val time = Time(timestamp)
         */
        val formattedDate = date?.replace("/", "-")
        hoursList.clear()
        originalHoursList.forEach {
            hoursList.add(it)
        }
        hoursList.remove(time!!)
        val time = TimeOk(hoursList)
        //val pair: Pair<String, MutableList<String>> = Pair(formattedDate!!, hoursList)
        FirebaseFirestore.getInstance().collection("calendarField")
            .document(formattedDate!!).set(time)
        hoursList = originalHoursList

        FirebaseFirestore.getInstance().collection("users").document(email!!)
            .set(user) //add the data if it doesn't already exist and update it if it already exists
            .addOnSuccessListener {
                showToast(requireContext(), R.string.successful_scheduling)
            }
            .addOnFailureListener {
                print(it)
                showToast(requireContext(), R.string.error_scheduling)
            }
    }

    override fun onPause() {
        super.onPause()
        with(binding) {
            fillInAllSharedPreferencesFields(
                name = txtName.editText?.text.toString().trim(),
                service = txtService.editText?.text.toString().trim(),
                date = tvSelectedDate.text.toString(),
                time = tvSelectedTime.text.toString(),
                uriString = uriString ?: "",
                year = _year,
                month = _month,
                day = _day
            )
        }
    }

    private fun fillInAllSharedPreferencesFields(
        name: String,
        service: String,
        date: String,
        time: String,
        uriString: String,
        year: Int?,
        month: Int?,
        day: Int?
    ) {
        SharedPreferencesHelper.write(NAME, name)
        SharedPreferencesHelper.write(SERVICE, service)
        SharedPreferencesHelper.write(DATE, date)
        SharedPreferencesHelper.write(TIME, time)
        SharedPreferencesHelper.write(URI_STRING, uriString)
        SharedPreferencesHelper.write(YEAR, year)
        SharedPreferencesHelper.write(MONTH, month)
        SharedPreferencesHelper.write(DAY, day)
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
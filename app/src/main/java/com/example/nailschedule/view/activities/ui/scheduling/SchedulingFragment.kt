package com.example.nailschedule.view.activities.ui.scheduling

import android.annotation.SuppressLint
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.nailschedule.R
import com.example.nailschedule.databinding.FragmentSchedulingBinding
import com.example.nailschedule.view.activities.data.model.User
import com.example.nailschedule.view.activities.utils.SharedPreferencesHelper
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
    private var time: String? = null

    private var uriString: String? = null

    companion object {
        const val NAME = "name"
        const val SERVICE = "service"
        const val DATE = "date"
        const val TIME = "time"
        const val URI_STRING = "uri_string"

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
        setListeners()

        return root
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

    @SuppressLint("SimpleDateFormat")
    private fun convertDateToMilliSeconds(
        dateFormat: String,
        milliSeconds: Long
    ): String {
        val formatter = SimpleDateFormat(dateFormat)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }

    @SuppressLint("WrongConstant")
    private fun setListeners() = binding.apply {
        //calendar.date = 1640799751672
        calendar.setOnDateChangeListener { _, year, month, dayOfMonth ->
            binding.tvSelectedDate.text = requireContext()
                .getString(R.string.scheduled_date, dayOfMonth, month + 1, year)
            date = "${dayOfMonth}/${month + 1}/$year"
        }

        btnSchedule.setOnClickListener {
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

    private fun clearFields() = binding.apply {
        txtName.editText?.setText("")
        txtService.editText?.setText("")
        calendar.clearFocus()
        tvSelectedDate.text = ""
        tvSelectedTime.text = ""
        uriString = null
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
            SharedPreferencesHelper.NAME, ""
        )
        service = SharedPreferencesHelper.read(
            SharedPreferencesHelper.SERVICE, ""
        )
        date = SharedPreferencesHelper.read(
            SharedPreferencesHelper.DATE, ""
        )
        time = SharedPreferencesHelper.read(
            SharedPreferencesHelper.TIME, ""
        )
        uriString = SharedPreferencesHelper.read(
            SharedPreferencesHelper.URI_STRING, ""
        )
    }

    //Firestore Database - Cloud Firestore
    private fun addOrUpdateFirestoreDatabase(user: User) {
        val email = SharedPreferencesHelper.read(
            SharedPreferencesHelper.EXTRA_EMAIL, ""
        )
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
                uriString = uriString ?: ""
            )
        }
    }

    private fun fillInAllSharedPreferencesFields(
        name: String, service: String,
        date: String, time: String, uriString: String
    ) {
        SharedPreferencesHelper.write(NAME, name)
        SharedPreferencesHelper.write(SERVICE, service)
        SharedPreferencesHelper.write(DATE, date)
        SharedPreferencesHelper.write(TIME, time)
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
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

    private lateinit var startForResult: ActivityResultLauncher<Intent>

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
            val googleId = SharedPreferencesHelper.read(
                SharedPreferencesHelper.GOOGLE_ID, ""
            )
            //val facebookId = SharedPreferenceHelper.read(
            //    SharedPreferenceHelper.FACEBOOK_ID, "")

            name = txtName.editText?.text.toString().trim()
            service = txtService.editText?.text.toString().trim()
            if ((name != null)
                && (service != null)
                && (date != null) && (time != null)
            ) {
                val user = User(
                    name = name!!, service = service!!, date = date!!,
                    time = time!!, uriString = uriString!!
                )
                addOrUpdateFirestoreDatabase(user, googleId!!)
            } else {
                when {
                    name.isNullOrEmpty() -> {
                        showToast(requireContext(), R.string.empty_name)
                    }
                    service.isNullOrEmpty() -> {
                        showToast(requireContext(), R.string.empty_service)
                    }
                    date.isNullOrEmpty() -> {
                        showToast(requireContext(), R.string.empty_date)
                    }
                    time.isNullOrEmpty() -> {
                        showToast(requireContext(), R.string.empty_time)
                    }
                    uriString.isNullOrEmpty() -> {
                        showToast(requireContext(), R.string.empty_photo)
                    }

                }
            }
        }

        registerForActivityResult()

        btnTakeAPicture.setOnClickListener {
            selectPhotoFromCamera()
        }

        btnChoosePhoneGallery.setOnClickListener {
            selectPhotoFromGallery()
        }

        ivPhoto.setOnClickListener {
            showOptionsToSelectPhoto()
        }
    }

    private fun selectPhotoFromCamera() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startForResult.launch(intent)
    }

    private fun selectPhotoFromGallery() {
        val intent = Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startForResult.launch(intent)
    }

    private fun registerForActivityResult() = binding.apply {
        startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // The Task returned from this call is always completed, no need to attach
                    // a listener.
                    uriString = result.data?.data.toString()
                    //Setting the image to imageView using Glide Library
                    Glide.with(requireContext()).load(uriString).into(ivPhoto)
                    showPhoto()
                }
            }
    }

    private fun showPhoto() = binding.apply {
        linearLayout.visibility = View.GONE
        tvInformationPhotoChange.visibility = View.VISIBLE
        ivPhoto.visibility = View.VISIBLE
    }

    private fun showOptionsToSelectPhoto() = binding.apply {
        tvInformationPhotoChange.visibility = View.GONE
        ivPhoto.visibility = View.GONE
        linearLayout.visibility = View.VISIBLE
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
    private fun addOrUpdateFirestoreDatabase(user: User, googleId: String) {
        FirebaseFirestore.getInstance().collection("users").document(googleId)
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
            SharedPreferencesHelper.write(NAME, txtName.editText?.text.toString().trim())
            SharedPreferencesHelper.write(SERVICE, txtService.editText?.text.toString().trim())
            SharedPreferencesHelper.write(DATE, tvSelectedDate.text.toString())
            SharedPreferencesHelper.write(TIME, tvSelectedTime.text.toString())
            SharedPreferencesHelper.write(URI_STRING, uriString)
        }
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
package com.example.nailschedule.view.activities.ui.scheduling

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.nailschedule.R
import com.example.nailschedule.databinding.FragmentSchedulingBinding
import com.example.nailschedule.view.activities.data.model.User
import com.example.nailschedule.view.activities.utils.SharedPreferenceHelper
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*


class SchedulingFragment : Fragment() {

    private lateinit var schedulingViewModel: SchedulingViewModel
    private var _binding: FragmentSchedulingBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var name: String? = null
    private var service: String? = null
    private var date: String? = null
    private var time : String? = null

    companion object {
        const val NAME = "name"
        const val SERVICE = "service"
        const val DATE = "date"
        const val TIME = "time"
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

        name = SharedPreferenceHelper.read(
            SharedPreferenceHelper.NAME, "")
        service = SharedPreferenceHelper.read(
            SharedPreferenceHelper.SERVICE, "")
        date = SharedPreferenceHelper.read(
            SharedPreferenceHelper.DATE, "")
        time = SharedPreferenceHelper.read(
            SharedPreferenceHelper.TIME, "")

        with(binding) {
            if(name?.isNotEmpty()==true && name != null) {
                txtName.editText?.setText(name)
            }
            if(service?.isNotEmpty()==true && service != null) {
                txtService.editText?.setText(service)
            }
            if(date?.isNotEmpty()==true && date != null)  {
                tvSelectedDate.text = date
            }
            if(time?.isNotEmpty()==true && time != null)  {
                tvSelectedTime.text = time
            }

            calendar.setOnDateChangeListener{ _, year, month, dayOfMonth ->
                binding.tvSelectedDate.text = requireContext()
                    .getString(R.string.scheduled_date, dayOfMonth, month+1, year)
                date = "${dayOfMonth}/${month+1}/$year"
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
                val googleId = SharedPreferenceHelper.read(
                    SharedPreferenceHelper.GOOGLE_ID, "")
                //val facebookId = SharedPreferenceHelper.read(
                //    SharedPreferenceHelper.FACEBOOK_ID, "")

                name = txtName.editText?.text.toString().trim()
                service = txtService.editText?.text.toString().trim()
                if((name != null)
                    && (service != null)
                    && (date != null) && (time != null)) {
                    val user = User(name = name!!, service = service!!, date = date!!, time = time!!)
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
                    }
                }
            }
        }

        return root
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
            SharedPreferenceHelper.write(NAME, txtName.editText?.text.toString().trim())
            SharedPreferenceHelper.write(SERVICE, txtService.editText?.text.toString().trim())
            SharedPreferenceHelper.write(DATE, tvSelectedDate.text.toString())
            SharedPreferenceHelper.write(TIME, tvSelectedTime.text.toString())
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
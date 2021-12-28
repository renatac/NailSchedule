package com.example.nailschedule.view.activities.ui.scheduled

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.nailschedule.R
import com.example.nailschedule.databinding.FragmentScheduledBinding
import com.example.nailschedule.view.activities.data.model.User
import com.example.nailschedule.view.activities.ui.scheduling.SchedulingFragment
import com.example.nailschedule.view.activities.utils.SharedPreferencesHelper
import com.example.nailschedule.view.activities.utils.showToast
import com.google.firebase.firestore.FirebaseFirestore


class ScheduledFragment : Fragment() {

    private lateinit var scheduledViewModel: ScheduledViewModel
    private var _binding: FragmentScheduledBinding? = null

    private var user: User? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getDocumentsFromFirestoreDatabase()
    }

    //Firestore Database - Cloud Firestore
    private fun getDocumentsFromFirestoreDatabase() {
        val googleId = SharedPreferencesHelper.read(
            SharedPreferencesHelper.GOOGLE_ID, "")
        FirebaseFirestore.getInstance().collection("users").document(googleId!!).get()
            .addOnSuccessListener { documentSnapshot ->
                    with(documentSnapshot.data) {
                        user = User(
                                this?.get("name") as String,
                                this["service"] as String,
                                this["date"] as String,
                                this["time"] as String
                            )

                }
                if(user == null){
                    showEmptyState()
                } else {
                    setupFields()
                    hideEmptyState()
                }
            }
            .addOnFailureListener {
                print(it)
                showToast(requireContext(), R.string.error_scheduling)
            }
    }

    private fun setupFields() = binding.apply {
        tvName.text =  user?.name
        tvServiceValue.text = user?.service
        tvDateValue.text = user?.date
        tvTimeValue.text = user?.time
        btnEdit.setOnClickListener {
            redirectToSchedulingFragment()
        }
    }

    private fun redirectToSchedulingFragment() {
        saveAtSharedPreferences()
        parentFragmentManager
            .beginTransaction()
            .add(R.id.container_scheduled, SchedulingFragment.newInstance(), "schedulingFragment")
            .commit()
    }

    private fun saveAtSharedPreferences() = binding.apply {
        SharedPreferencesHelper.write(SchedulingFragment.NAME, tvName.text.toString().trim())
        SharedPreferencesHelper.write(SchedulingFragment.SERVICE, tvServiceValue.text.toString().trim())
        SharedPreferencesHelper.write(SchedulingFragment.DATE, tvDateValue.text.toString())
        SharedPreferencesHelper.write(SchedulingFragment.TIME, tvTimeValue.text.toString())
    }

    private fun showEmptyState() = binding.apply {
        noScheduled.visibility = View.VISIBLE
        containerScheduled.visibility = View.GONE
    }

    private fun hideEmptyState() = binding.apply {
        noScheduled.visibility = View.GONE
        containerScheduled.visibility = View.VISIBLE
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        scheduledViewModel =
            ViewModelProvider(this).get(ScheduledViewModel::class.java)

        _binding = FragmentScheduledBinding.inflate(inflater, container, false)
        val root: View = binding.root
        /* val textView: TextView = binding.textNotifications
        scheduledViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        }) */
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
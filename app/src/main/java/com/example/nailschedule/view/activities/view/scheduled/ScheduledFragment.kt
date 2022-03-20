package com.example.nailschedule.view.activities.view.scheduled

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.nailschedule.R
import com.example.nailschedule.databinding.FragmentScheduledBinding
import com.example.nailschedule.view.activities.data.model.User
import com.example.nailschedule.view.activities.view.activities.PhotoActivity
import com.example.nailschedule.view.activities.view.scheduling.SchedulingFragment
import com.example.nailschedule.view.activities.utils.SharedPreferencesHelper
import com.example.nailschedule.view.activities.utils.showToast
import com.google.firebase.firestore.FirebaseFirestore


class ScheduledFragment : Fragment() {

    private var _binding: FragmentScheduledBinding? = null

    private var user: User? = null

    companion object {
        const val EXTRA_URI_STRING = "extra_uri_string"
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getDocumentsFromFirestoreDatabase()
    }

    //Firestore Database - Cloud Firestore
    private fun getDocumentsFromFirestoreDatabase() {
        val email = SharedPreferencesHelper.read(
            SharedPreferencesHelper.EXTRA_EMAIL, "")
        FirebaseFirestore.getInstance().collection("users")
            .document(email!!).get()
            .addOnSuccessListener { documentSnapshot ->

                documentSnapshot.data?.let {
                        print(this)
                        user = User(
                                it["name"] as String,
                                it["service"] as String,
                                it["date"] as String,
                                it["time"] as String,
                                it["uriString"] as String
                            )
                }
                if(user == null){
                    showEmptyState()
                } else {
                    setupFields()
                    setupListeners()
                    hideEmptyState()
                }
            }
            .addOnFailureListener {
                print(it)
                showEmptyState()
                showToast(requireContext(), R.string.error_scheduling)
            }
    }

    private fun setupListeners() = binding.apply {
        btnEdit.setOnClickListener {
            redirectToSchedulingFragment()
        }
        ivNail.setOnClickListener {
            showExpandedPhoto()
        }
    }
    private fun setupFields() = binding.apply {
        tvName.text =  user?.name
        tvServiceValue.text = user?.service
        tvDateValue.text = user?.date
        tvTimeValue.text = user?.time
        Glide.with(requireContext()).load(user?.uriString).into(ivNail)
    }

    private fun redirectToSchedulingFragment() {
        binding.btnEdit.visibility = View.GONE
        saveAtSharedPreferences()
        parentFragmentManager
            .beginTransaction()
            .add(R.id.container_scheduled, SchedulingFragment.newInstance(), "schedulingFragment")
            .commit()
    }

    private fun saveAtSharedPreferences() = binding.apply {
        SharedPreferencesHelper.write(SharedPreferencesHelper.NAME, tvName.text.toString().trim())
        SharedPreferencesHelper.write(SharedPreferencesHelper.SERVICE, tvServiceValue.text.toString().trim())
        SharedPreferencesHelper.write(SharedPreferencesHelper.DATE, tvDateValue.text.toString())
        SharedPreferencesHelper.write(SharedPreferencesHelper.TIME, tvTimeValue.text.toString())
        SharedPreferencesHelper.write(SharedPreferencesHelper.URI_STRING, user?.uriString)
    }

    private fun showEmptyState() = binding.apply {
        noScheduled.visibility = View.VISIBLE
        containerScheduled.visibility = View.GONE
    }

    private fun hideEmptyState() = binding.apply {
        noScheduled.visibility = View.GONE
        containerScheduled.visibility = View.VISIBLE
    }

    private fun showExpandedPhoto() {
        val intent = Intent(activity, PhotoActivity::class.java)
        intent.putExtra(EXTRA_URI_STRING, user?.uriString)
        startActivity(intent)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduledBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
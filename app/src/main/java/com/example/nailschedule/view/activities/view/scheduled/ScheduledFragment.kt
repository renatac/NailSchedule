package com.example.nailschedule.view.activities.view.scheduled

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.nailschedule.R
import com.example.nailschedule.databinding.FragmentScheduledBinding
import com.example.nailschedule.view.activities.data.model.Time
import com.example.nailschedule.view.activities.data.model.User
import com.example.nailschedule.view.activities.utils.SharedPreferencesHelper
import com.example.nailschedule.view.activities.utils.showToast
import com.example.nailschedule.view.activities.view.activities.PhotoActivity
import com.example.nailschedule.view.activities.viewmodels.ConnectivityViewModel
import com.example.nailschedule.view.activities.view.scheduling.SchedulingFragment
import com.google.firebase.firestore.FirebaseFirestore


class ScheduledFragment : Fragment() {

    private var _binding: FragmentScheduledBinding? = null

    private lateinit var connectivityViewModel: ConnectivityViewModel

    private var user: User? = null

    private val email = SharedPreferencesHelper.read(
        SharedPreferencesHelper.EXTRA_EMAIL, ""
    )

    private var date: String? = null
    private var time: String? = null

    companion object {
        const val EXTRA_URI_STRING = "extra_uri_string"
        const val VIEW_FLIPPER_LOADING = 0
        const val VIEW_FLIPPER_NO_INTERNET = 1
        const val VIEW_FLIPPER_EMPTY_STATE = 2
        const val VIEW_FLIPPER_SCHEDULED = 3
        const val USER_DATA_DOWNLOAD = "user_data_download"
        const val USER_DATA_DELETION = "user_data_deletion"
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connectivityViewModel =
            ViewModelProvider(this).get(ConnectivityViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObserver()
        setupRefresh()
        initialSetup()
    }

    private fun initialSetup() {
        showProgress()
        downloadUserDataFromFirestoreDatabase()
    }

    private fun setupRefresh() {
        binding.scheduledSwipeRefreshLayout.setOnRefreshListener {
            initialSetup()
        }
    }

    private fun setupObserver() {
        connectivityViewModel.hasInternet.observe(viewLifecycleOwner,
            {
                if (it.first) {
                    showProgress()
                    hideRefresh()
                    if (it.second == USER_DATA_DOWNLOAD) {
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
                                        it["uriString"] as String,
                                        it["email"] as String,
                                    )
                                    date = user?.date
                                    time = user?.time
                                }
                                if (user == null) {
                                    showEmptyState()
                                } else {
                                    setupFields()
                                    setupListeners()
                                    showScheduled()
                                }
                            }
                            .addOnFailureListener { exception ->
                                print(exception)
                                showEmptyState()
                                showToast(requireContext(), R.string.error_scheduling)
                            }
                    } else if(it.second == USER_DATA_DELETION) {
                        FirebaseFirestore.getInstance().collection("users")
                            .document(email!!).delete()

                        val previousTimeList = mutableListOf<String>()
                        FirebaseFirestore.getInstance().collection("calendarField")
                            .document(date!!).get().addOnSuccessListener { documentSnapshot ->
                                documentSnapshot.data?.let {
                                    val timeList = it["timeList"] as List<*>
                                    timeList.forEach { t->
                                        with(t.toString()) {
                                            if (this.contains(time!!)) {
                                                val finalIndex = this.indexOf(";true")
                                                val timeNew = this.substring(0, finalIndex)
                                                previousTimeList.add(timeNew.plus(";false"))
                                            } else {
                                                previousTimeList.add(this)
                                            }
                                        }
                                    }
                                    val hoursList = Time(previousTimeList)
                                    FirebaseFirestore.getInstance().collection("calendarField")
                                        .document( date!!).set(hoursList)
                                    showEmptyState()
                                }
                            }
                    }
                } else {
                    hideRefresh()
                    showNoInternet()
                }
            })
    }

    //Firestore Database - Cloud Firestore
    private fun downloadUserDataFromFirestoreDatabase() {
        connectivityViewModel.checkForInternet(requireContext(), USER_DATA_DOWNLOAD)
    }

    private fun setupListeners() = binding.apply {
        ivNail.setOnClickListener {
            showExpandedPhoto()
        }
        btnEdit.setOnClickListener {
            redirectToSchedulingFragment()
        }
        btnDelete.setOnClickListener {
            deleteFirebaseFirestoreData()
        }
    }

    private fun deleteFirebaseFirestoreData() {
        connectivityViewModel.checkForInternet(requireContext(), USER_DATA_DELETION)
    }

    private fun setupFields() = binding.apply {
        tvName.text = user?.name
        tvServiceValue.text = user?.service
        tvDateValue.text = user?.date
        tvTimeValue.text = user?.time
        activity?.let {
            Glide.with(it)
                .load(user?.uriString)
                .centerCrop()
                .into(ivNail)
        }
    }

    private fun redirectToSchedulingFragment() {
        hideButtons()
        saveAtSharedPreferences()
        parentFragmentManager
            .beginTransaction()
            .add(R.id.container_scheduled, SchedulingFragment.newInstance(), "schedulingFragment")
            .commit()
    }

    private fun hideButtons() = binding.apply {
        btnEdit.visibility = View.GONE
        btnDelete.visibility = View.GONE
    }

    private fun saveAtSharedPreferences() = binding.apply {
        SharedPreferencesHelper.write(SharedPreferencesHelper.NAME, tvName.text.toString().trim())
        SharedPreferencesHelper.write(
            SharedPreferencesHelper.SERVICE,
            tvServiceValue.text.toString().trim()
        )
        SharedPreferencesHelper.write(SharedPreferencesHelper.DATE, tvDateValue.text.toString())
        SharedPreferencesHelper.write(SharedPreferencesHelper.TIME, tvTimeValue.text.toString())
        SharedPreferencesHelper.write(SharedPreferencesHelper.URI_STRING, user?.uriString)
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

    private fun showProgress() {
        binding.scheduledViewFlipper.displayedChild = VIEW_FLIPPER_LOADING
    }

    private fun showNoInternet() {
        binding.scheduledViewFlipper.displayedChild = VIEW_FLIPPER_NO_INTERNET
    }

    private fun showEmptyState() {
        binding.scheduledViewFlipper.displayedChild = VIEW_FLIPPER_EMPTY_STATE
    }

    private fun showScheduled() {
        binding.scheduledViewFlipper.displayedChild = VIEW_FLIPPER_SCHEDULED
    }

    private fun hideRefresh() {
        binding.scheduledSwipeRefreshLayout.isRefreshing = false
    }
}
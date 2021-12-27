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
import com.example.nailschedule.view.activities.utils.showToast
import com.google.firebase.firestore.FirebaseFirestore

class ScheduledFragment : Fragment() {

    private lateinit var scheduledViewModel: ScheduledViewModel
    private var _binding: FragmentScheduledBinding? = null

    private var userList = arrayListOf<User>()

    private val scheduledAdapter: ScheduledAdapter by lazy {
        ScheduledAdapter(::onClick)
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
        FirebaseFirestore.getInstance().collection("users").get()
            .addOnSuccessListener {
                it.documents.forEach { documentSnapshot ->
                    with(documentSnapshot.data) {
                        userList.add(
                            User(
                                this?.get("name") as String,
                                this["service"] as String,
                                this["date"] as String,
                                this["time"] as String
                            )
                        )
                    }
                }
                if(userList.isEmpty()){
                    showEmptyState()
                } else {
                    setupAdapter()
                    hideEmptyState()
                }
            }
            .addOnFailureListener {
                print(it)
                showToast(requireContext(), R.string.error_scheduling)
            }
    }

    private fun showEmptyState() = binding.apply {
        noScheduled.visibility = View.VISIBLE
        recyclerScheduled.visibility = View.GONE
    }

    private fun hideEmptyState() = binding.apply {
        noScheduled.visibility = View.GONE
        recyclerScheduled.visibility = View.VISIBLE
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

    private fun setupAdapter() {
        binding.recyclerScheduled.adapter = scheduledAdapter
        scheduledAdapter.setItemScheduledList(userList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onClick(user: User) {
        print(user)
    }
}
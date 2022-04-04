package com.example.nailschedule.view.activities.view.gallery

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.nailschedule.databinding.FragmentGalleryBinding
import com.example.nailschedule.view.activities.utils.SharedPreferencesHelper
import com.example.nailschedule.view.activities.view.activities.PhotoActivity
import com.example.nailschedule.view.activities.view.scheduled.ScheduledFragment
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.util.*


class GalleryFragment : Fragment() {

    private var email: String? = null

    //private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentGalleryBinding? = null

    private lateinit var galleryViewModel: GalleryViewModel

    private lateinit var storage: StorageReference

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var startForResult: ActivityResultLauncher<Intent>

    private var selectedUri: Uri? = null

    companion object {
        const val VIEW_FLIPPER_LOADING = 0
        const val VIEW_FLIPPER_NO_INTERNET = 1
        const val VIEW_FLIPPER_EMPTY_STATE = 2
        const val VIEW_FLIPPER_HAS_PHOTO = 3
    }

    private val galleryAdapter: GalleryAdapter by lazy {
        GalleryAdapter(::onShortClick, ::hideTrash, ::deletePhotosFromCloudStorage)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerForActivityResult()
        storage = Firebase.storage.reference
        galleryViewModel =
            ViewModelProvider(this).get(GalleryViewModel::class.java)
        email = SharedPreferencesHelper.read(
            SharedPreferencesHelper.EXTRA_EMAIL, ""
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root
        showProgress()
        galleryAdapter.clearList()
        downloadPhotosFromCloudStorage()

        with(binding) {
            btnSelectPhoto.setOnClickListener {
                selectPhoto()
            }
            btnAddPhotos.setOnClickListener {
                selectPhoto()
            }
            ivDelete.setOnClickListener {
                galleryAdapter.clickToRemove(root.context)
            }
        }
        setupAdapter()
        return root
    }

    private fun downloadPhotosFromCloudStorage() {
        galleryViewModel.hasPhoto.observe(viewLifecycleOwner, {
            if (galleryViewModel.hasPhoto.value!!) {
                showRecyclerView()
            } else {
                showEmptyState()
            }
        })
        val a = isOnline(requireContext())

        //galleryViewModel.hasPhoto.value = false
        storage.child("/images").child("/$email").listAll()
            .addOnSuccessListener { listResult ->
                if (listResult.items.size != 0) {
                    listResult.items.forEach {
                        it.downloadUrl.addOnSuccessListener { uri ->
                            galleryViewModel.hasPhoto.value = true
                            galleryAdapter.setItemList(uri)
                        }.addOnFailureListener { exception ->
                            print(exception)
                        }
                    }
                } else {
                    showEmptyState()
                }
            }.addOnFailureListener {
                print(it)
            }
    }

    private fun setupAdapter() {
        binding.recyclerHome.layoutManager =
            GridLayoutManager(requireContext(), 2)
        binding.recyclerHome.adapter = galleryAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun selectPhoto() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startForResult.launch(intent)
    }

    private fun registerForActivityResult() {
        startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // The Task returned from this call is always completed, no need to attach
                    // a listener.
                    selectedUri = result.data?.data

                    try {
                        selectedUri?.let {
                            galleryAdapter.setItemList(it)
                            showRecyclerView()
                            uploadPhotoToCloudStorage()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

    }

    private fun showRecyclerView() {
        binding.galleryViewFlipper.displayedChild = VIEW_FLIPPER_HAS_PHOTO
    }

    private fun showEmptyState() {
       binding.galleryViewFlipper.displayedChild = VIEW_FLIPPER_EMPTY_STATE
    }

    private fun uploadPhotoToCloudStorage() {
        //val filename = "_${UUID.randomUUID().toString().substring(0,6)}_"
        val filename = "_${Date()}_"
        val ref = FirebaseStorage.getInstance()
            .getReference("images/${email}/${filename}")
        selectedUri?.let {
            ref.putFile(it)
        }
    }

    private fun onShortClick(uriString: String) {
        showExpandedPhoto(uriString)
        Toast.makeText(requireContext(), "", Toast.LENGTH_LONG).show()
    }

    private fun showExpandedPhoto(uriString: String) {
        val intent = Intent(activity, PhotoActivity::class.java)
        intent.putExtra(ScheduledFragment.EXTRA_URI_STRING, uriString)
        startActivity(intent)
    }

    private fun showProgress() {
        binding.galleryViewFlipper.displayedChild = VIEW_FLIPPER_LOADING
    }

    private fun hideTrash() {
        binding.ivDelete.visibility = View.GONE
    }

    private fun deletePhotosFromCloudStorage(uriList: List<Uri>, areAllItems: Boolean) {
        if (areAllItems) {
            storage.child("/images").child("/$email")
                .listAll().addOnSuccessListener { listResult ->
                    listResult.items.forEach {
                        it.delete()
                    }
                }
            showEmptyState()
        } else {
            uriList.forEach { uri ->
                val uriString = uri.toString()
                val initialIndex = uriString.indexOf("_")
                //val finalIndexOk = initialIndex + 8
                val finalIndexOk = uriString.lastIndexOf("_")
                if (initialIndex != -1 && finalIndexOk != -1) {
                    val filename = uriString.substring(initialIndex, finalIndexOk)
                    storage.child("/images").child("/$email/$filename").delete()
                }
            }
        }
    }

    private fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnected
    }
}
package com.example.nailschedule.view.activities.view.gallery

import android.app.Activity
import android.content.Intent
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
            SharedPreferencesHelper.EXTRA_EMAIL, "")
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
            hideProgress()
            if(galleryViewModel.hasPhoto.value!!) {
                hideEmptyState()
                showRecyclerView()
            } else {
                showEmptyState()
                hideRecyclerView()
            }
        })
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
                    hideProgress()
                    showEmptyState()
                    hideRecyclerView()
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
                            hideEmptyState()
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
        with(binding) {
            recyclerHome.visibility = View.VISIBLE
            btnAddPhotos.visibility = View.VISIBLE
            ivDelete.visibility = View.VISIBLE
        }
    }

    private fun hideRecyclerView() {
        with(binding) {
            btnAddPhotos.visibility = View.GONE
            ivDelete.visibility = View.GONE
            recyclerHome.visibility = View.GONE
        }
    }

    private fun showEmptyState() {
        with(binding) {
            bottomDescription.visibility = View.VISIBLE
            llHome.visibility = View.VISIBLE
        }
    }

    private fun hideEmptyState() {
        with(binding) {
            bottomDescription.visibility = View.GONE
            llHome.visibility = View.GONE
        }
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

    private fun onShortClick(uri: Uri) {
        Toast.makeText(requireContext(), "", Toast.LENGTH_LONG).show()
    }

    private fun showProgress() {
        binding.progressGallery.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        binding.progressGallery.visibility = View.GONE
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
            hideRecyclerView()
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
}
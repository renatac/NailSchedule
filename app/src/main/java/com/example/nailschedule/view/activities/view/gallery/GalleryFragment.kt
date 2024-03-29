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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.nailschedule.R
import com.example.nailschedule.databinding.FragmentGalleryBinding
import com.example.nailschedule.view.activities.data.model.GalleryUri
import com.example.nailschedule.view.activities.utils.SharedPreferencesHelper
import com.example.nailschedule.view.activities.utils.empty
import com.example.nailschedule.view.activities.utils.showToast
import com.example.nailschedule.view.activities.view.activities.PhotoActivity
import com.example.nailschedule.view.activities.view.scheduled.ScheduledFragment
import com.example.nailschedule.view.activities.viewmodels.ConnectivityViewModel
import com.example.nailschedule.view.activities.viewmodels.StorageViewModel
import java.util.*


class GalleryFragment : Fragment() {

    private var email: String? = null
    private var selectedUriList: List<Uri>? = null
    private var uriList: List<Uri>? = null
    private var areAllItems = false

    private var _binding: FragmentGalleryBinding? = null

    private lateinit var connectivityViewModel: ConnectivityViewModel
    private lateinit var storageViewModel: StorageViewModel

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var startForResult: ActivityResultLauncher<Intent>

    private var selectedUri: Uri? = null

    private var galleryActionEnum: GalleryActionEnum? = null

    private val galleryAdapter: GalleryAdapter by lazy {
        GalleryAdapter(::onShortClick, ::hideTrash, ::deletePhotosFromCloudStorage)
    }

    companion object {
        const val VIEW_FLIPPER_LOADING = 0
        const val VIEW_FLIPPER_NO_INTERNET = 1
        const val VIEW_FLIPPER_EMPTY_STATE = 2
        const val VIEW_FLIPPER_HAS_PHOTO = 3
        const val DOWNLOAD = "download"
        const val UPLOAD = "upload"
        const val DELETE = "delete"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerForActivityResult()
        setupGalleryViewModel()
        getUserEmail()
    }

    private fun setupGalleryViewModel() {
        connectivityViewModel =
            ViewModelProvider(this).get(ConnectivityViewModel::class.java)
        storageViewModel =
            ViewModelProvider(this).get(StorageViewModel::class.java)
    }

    private fun getUserEmail() {
        email = SharedPreferencesHelper.read(
            SharedPreferencesHelper.EXTRA_EMAIL, String.empty()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root
        setupObservers()
        setupRefresh()
        initialSetup()
        return root
    }

    private fun initialSetup() {
        setupAdapter()
        downloadPhotosFromCloudStorage()
        setupClickListeners()
    }

    private fun setupClickListeners() = binding.apply {
        btnSelectPhoto.setOnClickListener {
            selectPhoto()
        }
        btnAddPhotos.setOnClickListener {
            selectPhoto()
        }
        ivDelete.setOnClickListener {
            galleryAdapter.clickToRemove()
        }
    }

    private fun setupObservers() {
        storageViewModel.listResult.observe(viewLifecycleOwner, { listResult ->
            listResult?.let {
                when (galleryActionEnum) {
                    GalleryActionEnum.DOWNLOAD -> {
                        if (listResult.items.size != 0) {
                            val uriList = arrayListOf<GalleryUri>()
                            listResult.items.forEach {
                                it.downloadUrl.addOnSuccessListener { uri ->
                                    uriList.add(GalleryUri(uri))
                                    if(uriList.size == listResult.items.size) {
                                        galleryAdapter.submitList(uriList) {
                                            showRecyclerView()
                                        }
                                    }
                                }.addOnFailureListener { exception ->
                                    print(exception)
                                }
                            }
                        } else {
                            showEmptyState()
                        }
                    }
                    GalleryActionEnum.DELETE_ALL_ITEMS -> {
                        listResult.items.forEach { storageReference ->
                            storageViewModel.deleteImageReference(storageReference)
                        }
                        showEmptyState()
                    }
                }
            }
        })

        connectivityViewModel.hasInternet.observe(viewLifecycleOwner,
            {
                if (it.first) {
                    showProgress()
                    hideRefresh()
                    email?.let { email ->
                        if (it.second == DOWNLOAD) {
                            galleryActionEnum = GalleryActionEnum.DOWNLOAD
                            storageViewModel.getImagesList(requireContext(), email)
                        } else if (it.second == UPLOAD) {
                            val filename = "_${Date()}_"
                            val refOneImage = storageViewModel.getImageReference(
                                requireContext(),
                                email,
                                filename
                            )
                            selectedUri?.let { uri ->
                                refOneImage?.putFile(uri)
                            }
                            print("Upload")
                        } else if (it.second == DELETE) {
                            printMessageAboutExclusion()
                            if (areAllItems) {
                                galleryActionEnum = GalleryActionEnum.DELETE_ALL_ITEMS
                                storageViewModel.getImagesList(requireContext(), email)
                            } else {
                                selectedUriList?.forEach { uri ->
                                    val uriString = uri.toString()
                                    val initialIndex = uriString.indexOf("_")
                                    val finalIndexOk = uriString.lastIndexOf("_") + 1
                                    if (initialIndex != -1 && finalIndexOk != -1) {
                                        val filename =
                                            uriString.substring(initialIndex, finalIndexOk)
                                                .replace("%20", " ").replace("%3A", ":")
                                        storageViewModel.deleteChildImage("$email/$filename")
                                    }
                                }
                                showRecyclerView()
                            }
                        }
                    }
                } else {
                    hideRefresh()
                    showNoIntern()
                }
            })
    }

    private fun downloadPhotosFromCloudStorage() {
        connectivityViewModel.checkForInternet(requireContext(), DOWNLOAD)
    }

    private fun setupAdapter() {
        binding.recyclerGallery.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = galleryAdapter
        }
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
                        selectedUri?.let { uriSelected ->
                            val list: List<GalleryUri> = galleryAdapter.currentList.map { it.copy() }
                                 .toMutableList().plus(GalleryUri(uriSelected))
                            galleryAdapter.submitList(list) {
                                val lastPosition = galleryAdapter.currentList.size - 1
                                binding.recyclerGallery.scrollToPosition(lastPosition)
                                showRecyclerView()
                            }
                            showTrash()
                            uploadPhotoToCloudStorage()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
    }

    private fun uploadPhotoToCloudStorage() {
        connectivityViewModel.checkForInternet(requireContext(), UPLOAD)
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

    private fun setupRefresh() {
        binding.gallerySwipeRefreshLayout.setOnRefreshListener {
            initialSetup()
        }
    }

    private fun hideRefresh() {
        binding.gallerySwipeRefreshLayout.isRefreshing = false
    }

    private fun hideTrash() {
        binding.ivDelete.isVisible = false
    }

    private fun showTrash() {
        binding.ivDelete.isVisible = true
    }

    private fun showProgress() {
        binding.galleryViewFlipper.displayedChild = VIEW_FLIPPER_LOADING
    }

    private fun showNoIntern() {
        binding.galleryViewFlipper.displayedChild = VIEW_FLIPPER_NO_INTERNET
    }

    private fun showEmptyState() {
        binding.galleryViewFlipper.displayedChild = VIEW_FLIPPER_EMPTY_STATE
    }

    private fun showRecyclerView() {
        binding.galleryViewFlipper.displayedChild = VIEW_FLIPPER_HAS_PHOTO
    }

    private fun printMessageAboutExclusion() {
        when {
            selectedUriList?.isEmpty() == true -> {
                showToast(requireContext(), R.string.no_selected_photo)
            }
            selectedUriList?.size == 1 -> {
                showToast(requireContext(), R.string.photo_deleted)
            }
            uriList?.size == selectedUriList?.size -> {
                showToast(requireContext(), R.string.all_photos_deleted)
            }
            else -> {
                showToast(requireContext(), R.string.photos_deleted)
            }
        }
    }

    private fun deletePhotosFromCloudStorage(
        selectedUriList: List<Uri>,
        areAllItems: Boolean,
        uriList: List<Uri>?
    ) {
        this.selectedUriList = selectedUriList
        this.areAllItems = areAllItems
        this.uriList = uriList
        connectivityViewModel.checkForInternet(requireContext(), DELETE)
    }
}
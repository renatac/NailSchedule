package com.example.nailschedule.view.activities.ui.home

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.nailschedule.databinding.FragmentHomeBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.util.*


class HomeFragment : Fragment() {

    //private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    private lateinit var storage: StorageReference

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var startForResult: ActivityResultLauncher<Intent>

    private var selectedUri: Uri? = null

    private val homeAdapter: HomeAdapter by lazy {
        HomeAdapter(::onShortClick, ::hideTrash, ::deletePhotosFromCloudStorage)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerForActivityResult()
        storage = Firebase.storage.reference
    }

    private fun downloadPhotosFromCloudStorage() {
        //var hasPhotos = false
        storage.child("/images").listAll().addOnSuccessListener { listResult ->
            listResult.items.forEach {
                it.downloadUrl.addOnSuccessListener { uri ->
                    //hasPhotos = true
                    homeAdapter.setItemList(uri)
                }.addOnFailureListener { exception ->
                    print(exception)
                }
            }
            hideEmptyState()
            showRecyclerView()
        }
        /*if(!hasPhotos) {
            showEmptyState()
            //hideRecyclerView()
        }*/
    }

    /* private fun includesForCreateReference() {
        val storage = Firebase.storage

        // ## Create a Reference

        // [START create_storage_reference]
        // Create a storage reference from our app
        var storageRef = storage.reference
        // [END create_storage_reference]

        // [START create_child_reference]
        // Create a child reference
        // imagesRef now points to "images"
        // Create a child reference
        // imagesRef now points to "images"
        var imagesRef = storageRef.child("images")

        // Child references can also take paths
        // spaceRef now points to "images/space.jpg
        // imagesRef still points to "images"
        var spaceRef = storageRef.child("images/space.jpg")
        // [END create_child_reference]

        // ## Navigate with References

        // [START navigate_references]
        // parent allows us to move our reference to a parent node
        // imagesRef now points to 'images'
        imagesRef = spaceRef.parent

        // root allows us to move all the way back to the top of our bucket
        // rootRef now points to the root
        val rootRef = spaceRef.root
        // [END navigate_references]

        // [START chain_navigation]
        // References can be chained together multiple times
        // earthRef points to 'images/earth.jpg'
        val earthRef = spaceRef.parent?.child("earth.jpg")

        // nullRef is null, since the parent of root is null
        val nullRef = spaceRef.root.parent
        // [END chain_navigation]

        // ## Reference Properties

        // [START reference_properties]
        // Reference's path is: "images/space.jpg"
        // This is analogous to a file path on disk
        spaceRef.path

        // Reference's name is the last segment of the full path: "space.jpg"
        // This is analogous to the file name
        spaceRef.name

        // Reference's bucket is the name of the storage bucket that the files are stored in
        spaceRef.bucket
        // [END reference_properties]

        // ## Full Example

        // [START reference_full_example]
        // Points to the root reference
        storageRef = storage.reference

        // Points to "images"
        imagesRef = storageRef.child("images")

        // Points to "images/space.jpg"
        // Note that you can use variables to create child values
        val fileName = "space.jpg"
        spaceRef = imagesRef.child(fileName)

        // File path is "images/space.jpg"
        val path = spaceRef.path

        // File name is "space.jpg"
        val name = spaceRef.name

        // Points to "images"
        imagesRef = spaceRef.parent
        // [END reference_full_example]
    }
     */

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /* homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java) */

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        /*homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        }) */

        downloadPhotosFromCloudStorage()

        with(binding) {
            btnSelectPhoto.setOnClickListener {
                selectPhoto()
            }

            btnAddPhotos.setOnClickListener {
                selectPhoto()
            }

            ivDelete.setOnClickListener {
                homeAdapter.clickToRemove(root.context)
            }
        }

        setupAdapter()

        return root
    }

    private fun setupAdapter() {
        binding.recyclerHome.layoutManager =
            GridLayoutManager(requireContext(), 2)
        binding.recyclerHome.adapter = homeAdapter
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
                    Log.i("Teste", selectedUri.toString())

                    try {
                        selectedUri?.let {
                            homeAdapter.setItemList(it)
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
            btnAddPhotos.visibility = View.VISIBLE
            ivDelete.visibility = View.VISIBLE
            recyclerHome.visibility = View.VISIBLE
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
            title.visibility = View.VISIBLE
            btnSelectPhoto.visibility = View.VISIBLE
            textHome.visibility = View.VISIBLE
        }
    }

    private fun hideEmptyState() {
        with(binding) {
            title.visibility = View.GONE
            btnSelectPhoto.visibility = View.GONE
            textHome.visibility = View.GONE
        }
    }

    private fun uploadPhotoToCloudStorage() {
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("images/${filename}")
        selectedUri?.let {
            ref.putFile(it)
        }
    }

    private fun onShortClick(uri: Uri) {
        Toast.makeText(requireContext(), "", Toast.LENGTH_LONG).show()
    }

    private fun hideTrash() {
        binding.ivDelete.visibility = View.GONE
    }

    private fun deletePhotosFromCloudStorage(uriList: List<Uri>, areAllItems: Boolean) {
        if(areAllItems) {
            storage.child("/images").listAll().addOnSuccessListener { listResult ->
                listResult.items.forEach {
                    it.delete().addOnSuccessListener {
                    }
                }
            }
        } else {
            uriList.forEach { uri ->
                val uriString = uri.toString()
                val initialIndex = uriString.indexOf("F")
                val finalIndex = uriString.indexOf("?")
                val filename = uriString.substring(initialIndex + 1, finalIndex)
                print(filename)
                storage.child("/images/$filename").delete()
            }
        }
    }
}
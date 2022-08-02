package com.example.nailschedule.view.activities.view.gallery

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nailschedule.R
import com.example.nailschedule.databinding.ItemListGalleryBinding
import com.example.nailschedule.view.activities.data.model.GalleryUri

class GalleryAdapter(
    private val onShortClick: (String) -> Unit,
    private val hideTrash: () -> Unit,
    private val deletePhotosFromCloudStorage:
        (List<Uri>, Boolean, List<Uri>?) -> Unit
): ListAdapter<GalleryUri, GalleryAdapter.MyViewHolder>(GalleryUriDiffCallback) {

    private var hasLongClick = false
    private var selectedUriList: ArrayList<Uri> = arrayListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GalleryAdapter.MyViewHolder = MyViewHolder(
        ItemListGalleryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
    )

    override fun onBindViewHolder(holder: GalleryAdapter.MyViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    private fun addUriSelected(uri: Uri?) {
        uri?.let { selectedUriList.add(uri) }
    }

    private fun removeUriSelected(uri: Uri?) {
        uri?.let { selectedUriList.remove(uri) }
        if (currentList.isEmpty()) {
            hideTrash.invoke()
        }
    }

    fun clickToRemove() {
        val areAllItems = currentList.size == selectedUriList.size
        deletePhotosFromCloudStorage.invoke(selectedUriList, areAllItems, currentList.map {it.uri})
        val list = currentList.map { it.copy() }.toMutableList()
        list.removeAll(selectedUriList.map { GalleryUri(it) })
        if (list.isEmpty()) {
            hideTrash.invoke()
        }
        submitList(list)
        selectedUriList.clear()
    }

    private fun ensureAppearance(uri: Uri?, binding: ItemListGalleryBinding) {
        with(binding) {
            if (selectedUriList.contains(uri)) {
                item.setBackgroundResource(R.color.purple_200)
            } else {
                item.setBackgroundColor(
                    ContextCompat.getColor(
                        item.rootView.context,
                        R.color.transparent
                    )
                )
            }
        }
    }

    private fun onLongClick(uri: Uri?, binding: ItemListGalleryBinding) {
        with(binding) {
            if (selectedUriList.contains(uri)) {
                item.setBackgroundColor(
                    ContextCompat.getColor(
                        item.rootView.context,
                        R.color.transparent
                    )
                )
                removeUriSelected(uri)
            } else {
                item.setBackgroundResource(R.color.purple_200)
                addUriSelected(uri)
            }
        }
        if (selectedUriList.isEmpty()) {
            hasLongClick = false
        }
    }

    private fun shortClick(uri: Uri?) {
        uri?.let { onShortClick.invoke(uri.toString()) }
    }

    inner class MyViewHolder(private val binding: ItemListGalleryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(galleryUri: GalleryUri?) = binding.apply {
            ensureAppearance(galleryUri?.uri, binding)
            with(item) {
                setOnLongClickListener {
                    hasLongClick = true
                    onLongClick(galleryUri?.uri, binding)
                    return@setOnLongClickListener true
                }
                setOnClickListener {
                    if (hasLongClick) {
                        onLongClick(galleryUri?.uri, binding)
                    } else {
                        shortClick(galleryUri?.uri)
                    }
                }
            }
            //Setting the image to imageView using Glide Library
            Glide.with(item.rootView.context).load(galleryUri?.uri.toString()).into(ivItem)
        }
    }

    object GalleryUriDiffCallback: DiffUtil.ItemCallback<GalleryUri>() {
        override fun areItemsTheSame(oldItem: GalleryUri, newItem: GalleryUri): Boolean {
            return oldItem.uri == newItem.uri
        }

        override fun areContentsTheSame(oldItem: GalleryUri, newItem: GalleryUri): Boolean {
            return oldItem == newItem
        }
    }
}
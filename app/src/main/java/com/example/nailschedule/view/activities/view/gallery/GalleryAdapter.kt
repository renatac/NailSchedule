package com.example.nailschedule.view.activities.view.gallery

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nailschedule.R
import com.example.nailschedule.databinding.ItemListGalleryBinding
import com.example.nailschedule.view.activities.utils.showToast

class GalleryAdapter(
    private val onShortClick: (String) -> Unit,
    private val hideTrash: () -> Unit,
    private val deletePhotosFromCloudStorage: (List<Uri>, areAllItems: Boolean) -> Unit
) :
    RecyclerView.Adapter<GalleryAdapter.MyViewHolder>() {

    private var uriList: ArrayList<Uri>? = arrayListOf()
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
        holder.bind(uriList?.get(position))
    }

    override fun getItemCount() = uriList?.size ?: 0

    fun setItemList(uri: Uri) {
        uriList?.apply {
            add(uri)
            sort()
        }
        notifyDataSetChanged()
    }

    fun clearList() = uriList?.clear()

    private fun addUriSelected(uri: Uri?) {
        uri?.let { selectedUriList.add(uri) }
    }

    private fun removeUriSelected(uri: Uri?) {
        uri?.let { selectedUriList.remove(uri) }
        if(uriList?.isEmpty() == true) { hideTrash.invoke()}
    }

    fun clickToRemove(context: Context) {
        printMessageAboutExclusion(context)
        val areAllItems = uriList?.size == selectedUriList.size
        uriList?.removeAll(selectedUriList)
        if (uriList?.isEmpty() == true) {
            hideTrash.invoke()
        }
        deletePhotosFromCloudStorage.invoke(selectedUriList, areAllItems)
        selectedUriList.clear()
        notifyDataSetChanged()
    }

    private fun printMessageAboutExclusion(context: Context) {
        when {
            selectedUriList.isEmpty() -> {
                showToast(context, R.string.no_selected_photo)
            }
            selectedUriList.size == 1 -> {
                showToast(context, R.string.photo_deleted)
            }
            uriList?.size == selectedUriList.size -> {
                showToast(context, R.string.all_photos_deleted)
            }
            else -> {
                showToast(context, R.string.photos_deleted)
            }
        }
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
        if(selectedUriList.isEmpty()) { hasLongClick = false }
    }

    private fun shortClick(uri: Uri?) {
        uri?.let { onShortClick.invoke(uri.toString()) }
    }

    inner class MyViewHolder(private val binding: ItemListGalleryBinding) :
        RecyclerView.ViewHolder(binding.root) {
            fun bind(uri: Uri?) = binding.apply {
                ensureAppearance(uri, binding)
                with(item) {
                    setOnLongClickListener {
                        hasLongClick = true
                        onLongClick(uri, binding)
                        return@setOnLongClickListener true
                    }
                    setOnClickListener {
                        if(hasLongClick) {
                            onLongClick(uri, binding)
                        } else {
                            shortClick(uri)
                        }
                    }
                }
                //Setting the image to imageView using Glide Library
                Glide.with(item.rootView.context).load(uri.toString()).into(ivItem)
            }
        }
}
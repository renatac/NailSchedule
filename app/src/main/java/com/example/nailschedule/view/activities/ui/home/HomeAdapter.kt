package com.example.nailschedule.view.activities.ui.home

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nailschedule.R
import com.example.nailschedule.databinding.ItemListHomeBinding
import com.example.nailschedule.view.activities.utils.showToast

class HomeAdapter(private val onShortClick: (Uri) -> Unit,
                  private val hideTrash: () -> Unit,
                  private val deletePhotosFromCloudStorage: () -> Unit) :
    RecyclerView.Adapter<HomeAdapter.MyViewHolder>() {

    private var uriList: ArrayList<Uri>? = arrayListOf()
    private var hasLongClick = false
    private var selectedUriList: ArrayList<Uri> = arrayListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HomeAdapter.MyViewHolder = MyViewHolder(
        ItemListHomeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
    )

    override fun onBindViewHolder(holder: HomeAdapter.MyViewHolder, position: Int) {
        holder.bind(uriList?.get(position))
    }

    override fun getItemCount() = uriList?.size ?: 0

    fun setItemList(uri: Uri) {
        uriList?.add(uri)
        notifyDataSetChanged()
    }

    private fun addUriSelected(uri: Uri?) {
        uri?.let { selectedUriList.add(uri) }
    }

    private fun removeUriSelected(uri: Uri?) {
        uri?.let { selectedUriList.remove(uri) }
        if(uriList?.isEmpty() == true) { hideTrash.invoke()}
    }

    fun clickToRemove(context: Context) {
        printMessageAboutExclusion(context)
        uriList?.removeAll(selectedUriList)
        selectedUriList.clear()
        notifyDataSetChanged()
        if (uriList?.isEmpty() == true) {
            hideTrash.invoke()
        }
        deletePhotosFromCloudStorage.invoke()
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

    private fun ensureAppearance(uri: Uri?, binding: ItemListHomeBinding) {
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

    private fun onLongClick(uri: Uri?, binding: ItemListHomeBinding) {
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
        uri?.let { onShortClick.invoke(uri) }
    }

    inner class MyViewHolder(private val binding: ItemListHomeBinding) :
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
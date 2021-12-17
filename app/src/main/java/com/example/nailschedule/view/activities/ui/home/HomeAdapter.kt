package com.example.nailschedule.view.activities.ui.home

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.nailschedule.R
import com.example.nailschedule.databinding.ItemListHomeBinding

class HomeAdapter(private val onShortClick: (Bitmap) -> Unit,
                  private val hideTrash: () -> Unit) :
    RecyclerView.Adapter<HomeAdapter.MyViewHolder>() {

    private var bitmapList: MutableList<Bitmap>? = arrayListOf()
    private var hasLongClick = false
    private var selectedBitmapList: ArrayList<Bitmap> = arrayListOf()

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
        holder.bind(bitmapList?.get(position))
    }

    override fun getItemCount() = bitmapList?.size ?: 0

    fun setItemList(itemList: Bitmap) {
        bitmapList?.add(itemList)
        notifyDataSetChanged()
    }

    private fun addBitmapSelected(bitmap: Bitmap?) {
        bitmap?.let { selectedBitmapList.add(bitmap) }
    }

    private fun removeBitmapSelected(bitmap: Bitmap?) {
        bitmap?.let { selectedBitmapList.remove(bitmap) }
        if(selectedBitmapList.isEmpty()) { hideTrash.invoke()}
    }

    fun clickToRemove(context: Context) {
        if(selectedBitmapList.isEmpty()) {
            Toast.makeText(context,context.getString(R.string.no_selected_photo),Toast.LENGTH_LONG).show()
        }
        bitmapList?.removeAll(selectedBitmapList)
        notifyDataSetChanged()
    }

    private fun ensureAppearance(bitmap: Bitmap?, binding: ItemListHomeBinding) {
        with(binding) {
            if (selectedBitmapList.contains(bitmap)) {
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

    private fun onLongClick(bitmap: Bitmap?, binding: ItemListHomeBinding) {
        with(binding) {
            if (selectedBitmapList.contains(bitmap)) {
                    item.setBackgroundColor(
                        ContextCompat.getColor(
                            item.rootView.context,
                            R.color.transparent
                        )
                    )
                removeBitmapSelected(bitmap)
            } else {
                item.setBackgroundResource(R.color.purple_200)
                addBitmapSelected(bitmap)
            }
        }
        if(selectedBitmapList.isEmpty()) { hasLongClick = false }
    }

    private fun shortClick(bitmap: Bitmap?) {
        bitmap?.let { onShortClick.invoke(bitmap) }
    }

    inner class MyViewHolder(private val binding: ItemListHomeBinding) :
        RecyclerView.ViewHolder(binding.root) {
            fun bind(bitmap: Bitmap?) = binding.apply {
                ensureAppearance(bitmap, binding)
                with(item) {
                    setOnLongClickListener {
                        hasLongClick = true
                        onLongClick(bitmap, binding)
                        return@setOnLongClickListener true
                    }
                    setOnClickListener {
                        if(hasLongClick) {
                            onLongClick(bitmap, binding)
                        } else {
                            shortClick(bitmap)
                        }
                    }
                }
                ivItem.setImageBitmap(bitmap)
            }
        }
}
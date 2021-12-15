package com.example.nailschedule.view.activities.ui.home

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nailschedule.databinding.ItemListHomeBinding

class HomeAdapter: RecyclerView.Adapter<HomeAdapter.MyViewHolder>() {

    private var bitmapList: ArrayList<Bitmap>? = arrayListOf()

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

    fun setItemList(itemList: ArrayList<Bitmap>?) {
        bitmapList = itemList
        notifyDataSetChanged()
    }

    inner class MyViewHolder(private val binding: ItemListHomeBinding) :
        RecyclerView.ViewHolder(binding.root) {
            fun bind(bitmap: Bitmap?) = binding.apply {
                ivItem.setImageBitmap(bitmap)
            }
        }
}
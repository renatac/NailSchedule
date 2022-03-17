package com.example.nailschedule.view.activities.view.owner

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.nailschedule.R
import com.example.nailschedule.databinding.ItemHourBinding

class OwnerAdapter : RecyclerView.Adapter<OwnerAdapter.MyViewHolder>() {
    private var availableTimeList: ArrayList<Pair<String, Boolean>> = arrayListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OwnerAdapter.MyViewHolder = MyViewHolder(
        ItemHourBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
    )

    override fun onBindViewHolder(holder: OwnerAdapter.MyViewHolder, position: Int) {
        holder.bind(availableTimeList[position])
    }

    override fun getItemCount() = availableTimeList.size

    fun setItemsList(timeList: List<Pair<String, Boolean>>) {
        availableTimeList.addAll(timeList)
        notifyDataSetChanged()
    }

    fun clearItemsList() {
        availableTimeList.clear()
        notifyDataSetChanged()
    }

    inner class MyViewHolder(private val binding: ItemHourBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(availableTime: Pair<String, Boolean>) = binding.apply {
            val hour = "${availableTime.first} h"
            tvHour.text = hour
            with(tvAvailability) {
                if (availableTime.second) {
                    text = binding.root.context.getString(R.string.available)
                    setTextColor(ContextCompat.getColor(context, R.color.purple_500))
                } else {
                    text = binding.root.context.getString(R.string.unavailable)
                    setTextColor(ContextCompat.getColor(context, R.color.red))
                }
            }
        }
    }
}
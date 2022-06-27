package com.example.nailschedule.view.activities.view.owner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nailschedule.R
import com.example.nailschedule.databinding.ItemHourBinding

class OwnerAdapter(val btnListener: (info: String)-> Unit)
: RecyclerView.Adapter<OwnerAdapter.MyViewHolder>() {
    private var availableTimeList: ArrayList<String> = arrayListOf()

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

    fun setItemsList(timeList: List<String>) {
        availableTimeList.addAll(timeList)
        notifyDataSetChanged()
    }

    fun clearItemsList() {
        availableTimeList.clear()
        notifyDataSetChanged()
    }

    inner class MyViewHolder(private val binding: ItemHourBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(info: String) = binding.apply {
            btnSeeSchedule.setOnClickListener {
                btnListener.invoke(info)
            }
            val hour = "${info.subSequence(0, 5)} h"
            tvHour.text = hour
            if (info.subSequence(5, info.length) == ";false") {
                tvAvailability.text = binding.root.context.getString(R.string.available)
                btnSeeSchedule.visibility = View.GONE
                tvAvailability.visibility = View.VISIBLE
            } else {
                btnSeeSchedule.visibility = View.VISIBLE
                tvAvailability.visibility = View.GONE
            }
        }
    }

    fun clearList() = availableTimeList.clear()
}
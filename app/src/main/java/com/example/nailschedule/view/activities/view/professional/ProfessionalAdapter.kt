package com.example.nailschedule.view.activities.view.professional

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.nailschedule.R
import com.example.nailschedule.databinding.ItemHourBinding

class ProfessionalAdapter(private val onBtnSeeScheduleClicked: (info: String)-> Unit)
: RecyclerView.Adapter<ProfessionalAdapter.MyViewHolder>() {
    private var availableTimeList: ArrayList<String> = arrayListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProfessionalAdapter.MyViewHolder = MyViewHolder(
        ItemHourBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
    )

    override fun onBindViewHolder(holder: ProfessionalAdapter.MyViewHolder, position: Int) {
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
                onBtnSeeScheduleClicked.invoke(info)
            }
            val hour = "${info.subSequence(0, 5)} h"
            tvHour.text = hour
            if (info.subSequence(5, info.length) == ";false") {
                tvAvailability.text = binding.root.context.getString(R.string.available)
                btnSeeSchedule.isVisible = false
                tvAvailability.isVisible = true
            } else {
                btnSeeSchedule.isVisible = true
                tvAvailability.isVisible = false
            }
        }
    }

    fun clearList() = availableTimeList.clear()
}
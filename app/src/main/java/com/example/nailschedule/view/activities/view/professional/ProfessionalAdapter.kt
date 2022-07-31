package com.example.nailschedule.view.activities.view.professional

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nailschedule.R
import com.example.nailschedule.databinding.ItemHourBinding

class ProfessionalAdapter(private val onBtnSeeScheduleClicked: (info: String)-> Unit)
: ListAdapter<timeAvailability, ProfessionalAdapter.MyViewHolder>(ProfessionalDiffCallback) {

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
        holder.bind(currentList[position])
    }

    inner class MyViewHolder(private val binding: ItemHourBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(timeAvailability: timeAvailability) = binding.apply {
            btnSeeSchedule.setOnClickListener {
                onBtnSeeScheduleClicked.invoke(timeAvailability.time)
            }
            val hour = "${timeAvailability.time.subSequence(0, 5)} h"
            tvHour.text = hour
            if (timeAvailability.time.subSequence(5, timeAvailability.time.length) == ";false") {
                tvAvailability.text = binding.root.context.getString(R.string.available)
                btnSeeSchedule.isVisible = false
                tvAvailability.isVisible = true
            } else {
                btnSeeSchedule.isVisible = true
                tvAvailability.isVisible = false
            }
        }
    }

    object ProfessionalDiffCallback: DiffUtil.ItemCallback<timeAvailability>() {
        override fun areItemsTheSame(oldItem: timeAvailability, newItem: timeAvailability) = true

        override fun areContentsTheSame(oldItem: timeAvailability, newItem: timeAvailability): Boolean {
            return oldItem == newItem
        }
    }
}
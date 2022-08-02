package com.example.nailschedule.view.activities.view.professional

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nailschedule.R
import com.example.nailschedule.databinding.ItemHourBinding
import com.example.nailschedule.view.activities.data.model.TimeAvailability

class ProfessionalAdapter(private val onBtnSeeScheduleClicked: (info: String) -> Unit) :
    ListAdapter<TimeAvailability, ProfessionalAdapter.MyViewHolder>(ProfessionalDiffCallback) {

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
        fun bind(timeAvailability: TimeAvailability) = binding.apply {
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

    object ProfessionalDiffCallback : DiffUtil.ItemCallback<TimeAvailability>() {
        override fun areItemsTheSame(oldItem: TimeAvailability, newItem: TimeAvailability) =
            oldItem.time == newItem.time

        override fun areContentsTheSame(
            oldItem: TimeAvailability,
            newItem: TimeAvailability
        ): Boolean {
            return oldItem == newItem
        }
    }
}
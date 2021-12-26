package com.example.nailschedule.view.activities.ui.scheduled

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nailschedule.R
import com.example.nailschedule.databinding.ItemListScheduledBinding
import com.example.nailschedule.view.activities.data.model.User

class ScheduledAdapter(private val onClick: (User) -> Unit) :
    RecyclerView.Adapter<ScheduledAdapter.MyViewHolder>() {

    private var userList: ArrayList<User>? = arrayListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ScheduledAdapter.MyViewHolder = MyViewHolder(
        ItemListScheduledBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
    )

    override fun onBindViewHolder(holder: ScheduledAdapter.MyViewHolder, position: Int) {
        holder.bind(userList?.get(position))
    }

    fun setItemScheduledList(user: ArrayList<User>) {
        userList = user
        notifyDataSetChanged()
    }

    override fun getItemCount() = userList?.size ?: 0

    inner class MyViewHolder(private val binding: ItemListScheduledBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User?) = binding.apply {
            with(cardView) {
                setOnClickListener {
                    onClick.invoke(user!!)
                }
            }
            val context = cardView.context
            with(binding) {
                tvName.text = context.getString(R.string.lb_client, user?.name)
                tvService.text = context.getString(R.string.lb_service, user?.service)
                tvDate.text = context.getString(R.string.lb_date, user?.date)
                tvTime.text = context.getString(R.string.lb_time, user?.time)
            }
        }
    }
}

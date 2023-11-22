package com.hms.socialsteps.ui.activities

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hms.socialsteps.R
import com.hms.socialsteps.data.model.Activities
import com.hms.socialsteps.databinding.CardActivityBinding

class ActivityAdapter(private val onClickListener: ActivityOnClickListener) :
    RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder>() {

    private val callback = object : DiffUtil.ItemCallback<Activities>() {
        override fun areItemsTheSame(oldItem: Activities, newItem: Activities): Boolean {
            return oldItem.activityStartTime == newItem.activityStartTime
        }

        override fun areContentsTheSame(oldItem: Activities, newItem: Activities): Boolean {
            return oldItem.activityStartTime == newItem.activityStartTime
        }
    }

    val differ = AsyncListDiffer(this, callback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val binding = CardActivityBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return ActivityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val activity = differ.currentList[position]
        holder.bind(activity)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    inner class ActivityViewHolder(val binding: CardActivityBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(activity: Activities) {
            if (activity.activityType == "walk") {
                binding.ivActivity.setImageResource(R.drawable.ic_baseline_directions_walk_24)
            } else {
                binding.ivActivity.setImageResource(R.drawable.ic_baseline_directions_run_24)
            }
            binding.apply {
                root.setOnClickListener {
                    onClickListener.onActivityClicked(activity)
                }
                tvActivityName.text = activity.activityType
                tvActivityDate.text = activity.activityStartTime
                tvCaloriesText.text = activity.calories.toString()
                tvDurationText.text = activity.duration
            }
        }
    }

    interface ActivityOnClickListener {
        fun onActivityClicked(activity: Activities)
    }
}
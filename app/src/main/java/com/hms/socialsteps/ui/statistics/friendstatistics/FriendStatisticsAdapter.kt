package com.hms.socialsteps.ui.statistics.friendstatistics

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hms.socialsteps.core.util.Constants.CALORIE
import com.hms.socialsteps.core.util.Constants.STEP
import com.hms.socialsteps.core.util.loadImage
import com.hms.socialsteps.data.model.Users
import com.hms.socialsteps.databinding.StatisticsItemBinding

class FriendStatisticsAdapter : RecyclerView.Adapter<FriendStatisticsAdapter.FriendStatisticsViewHolder>() {

    private val callback = object: DiffUtil.ItemCallback<Users>() {
        override fun areItemsTheSame(oldItem: Users, newItem: Users): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: Users, newItem: Users): Boolean {
            return oldItem.uid == newItem.uid
        }
    }

    var dataType = STEP

    val differ = AsyncListDiffer(this, callback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendStatisticsViewHolder {
        val binding = StatisticsItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return FriendStatisticsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendStatisticsViewHolder, position: Int) {
        val user = differ.currentList[position]
        holder.bind(user, position)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    inner class FriendStatisticsViewHolder (val binding: StatisticsItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(users: Users, position: Int) {
            binding.apply {
                root.context.loadImage(users.photo, ivUserPhoto)
                tvRankNumber.text = (position + 1).toString()
                tvUsername.text = users.username
                if (dataType == STEP){
                    tvStepOrCalorieValue.text = users.dailyStepsCount
                }else if (dataType == CALORIE) {
                    tvStepOrCalorieValue.text = users.caloriesBurned
                }
            }
        }
    }
}
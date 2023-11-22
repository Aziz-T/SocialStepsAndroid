package com.hms.socialsteps.ui.groupdetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hms.socialsteps.R
import com.hms.socialsteps.core.util.Constants.STEP
import com.hms.socialsteps.core.util.loadImage
import com.hms.socialsteps.data.model.TargetType
import com.hms.socialsteps.data.model.Users
import com.hms.socialsteps.databinding.StatisticsItemBinding


class GroupDetailAdapter : RecyclerView.Adapter<GroupDetailAdapter.FriendStatisticsViewHolder>() {

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
            val rank = (position + 1)
            binding.apply {
                when(rank){
                    1 -> parentLayout.setBackgroundColor(binding.root.context.getColor(R.color.gold))
                    2 -> parentLayout.setBackgroundColor(binding.root.context.getColor(R.color.silver))
                    3 -> parentLayout.setBackgroundColor(binding.root.context.getColor(R.color.bronze))
                    else -> {
                        parentLayout.setBackgroundColor(binding.root.context.getColor(com.google.android.material.R.color.mtrl_btn_transparent_bg_color))
                    }
                }

                root.context.loadImage(users.photo, ivUserPhoto)
                tvRankNumber.text = rank.toString()
                tvUsername.text = users.username
                if (dataType == TargetType.STEP.toString()){
                    tvStepOrCalorieValue.text = users.dailyStepsCount
                }else if (dataType == TargetType.CALORIE.toString()) {
                    tvStepOrCalorieValue.text = users.caloriesBurned
                }else
                    tvStepOrCalorieValue.text = "ERR"
            }
        }
    }

    /*fun sortByType() {
        val sortedList = differ.currentList
        sortedList.sortByDescending {
            it.dailyStepsCount.toInt()
        }
        val displayOrderList = differ.currentList
        for (i in sortedList.indices) {
            val toPos = sortedList.indexOf(displayOrderList[i])
            notifyItemMoved(i, toPos)
            listMoveTo(displayOrderList, i, toPos)
        }
    }


    private fun listMoveTo(list: MutableList<Users>, fromPos: Int, toPos: Int) {
        val fromValue = list[fromPos]
        val delta = if (fromPos < toPos) 1 else -1
        var i = fromPos
        while (i != toPos) {
            list[i] = list[i + delta]
            i += delta
        }
        list[toPos] = fromValue
    }*/

}
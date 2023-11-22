package com.hms.socialsteps.ui.groups

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hms.socialsteps.data.model.Group
import com.hms.socialsteps.data.model.Notifications
import com.hms.socialsteps.data.model.TargetType
import com.hms.socialsteps.databinding.GroupsItemBinding

class GroupsAdapter(
    private val onClickListener: (Group) -> Unit
): RecyclerView.Adapter<GroupsAdapter.GroupsViewHolder>() {

    private val callback = object: DiffUtil.ItemCallback<Group>() {
        override fun areItemsTheSame(oldItem: Group, newItem: Group): Boolean {
            return oldItem.groupInformation.groupID == newItem.groupInformation.groupID
        }

        override fun areContentsTheSame(oldItem: Group, newItem: Group): Boolean {
            return oldItem.groupInformation.groupID == newItem.groupInformation.groupID
        }
    }

    val differ = AsyncListDiffer(this, callback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupsViewHolder {
        val binding = GroupsItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupsViewHolder, position: Int) {
        val group = differ.currentList[position]
        holder.bind(group)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    inner class GroupsViewHolder(val binding: GroupsItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(group: Group) {
            binding.apply {
                //TODO group image will arranged in here
                tvGroupName.text = group.groupInformation.groupName
                tvTargetType.text = "Target Type: " + group.groupInformation.targetType
                if (group.groupInformation.targetType == TargetType.STEP.toString()) {
                    tvTargetValue.text = "Target: " + group.groupInformation.stepTarget
                }else {
                    tvTargetValue.text = "Target: " + group.groupInformation.calorieTarget
                }
                tvMemberCount.text = group.members.size.toString()
            }
            binding.root.setOnClickListener {
                onClickListener(group)
            }
        }
    }
}
package com.hms.socialsteps.ui.statistics.groupstatistics

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hms.socialsteps.core.util.loadImage
import com.hms.socialsteps.data.model.GroupMembers
import com.hms.socialsteps.data.model.Users
import com.hms.socialsteps.databinding.MemberItemBinding

class GroupMemberAdapter(

): RecyclerView.Adapter<GroupMemberAdapter.GroupMemberViewHolder>() {

    val memberList = ArrayList<GroupMembers>()
    var groupId = ""

    private val callback = object: DiffUtil.ItemCallback<Users>() {
        override fun areItemsTheSame(oldItem: Users, newItem: Users): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: Users, newItem: Users): Boolean {
            return oldItem.uid == newItem.uid
        }
    }

    val differ = AsyncListDiffer(this, callback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupMemberAdapter.GroupMemberViewHolder {
        val binding = MemberItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return GroupMemberViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupMemberViewHolder, position: Int) {
        val user = differ.currentList[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }


    inner class GroupMemberViewHolder(val binding: MemberItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(user: Users) {
            binding.apply {
                root.context.loadImage(user.photo, ivUserPhoto)
                tvUsername.text = user.username
                btnAdd.setOnClickListener {
                    val groupMembers = GroupMembers()
                    groupMembers.groupMembersID = user.uid + System.currentTimeMillis()
                    groupMembers.groupID = groupId
                    groupMembers.userID = user.uid
                    memberList.add(groupMembers)
                    btnAdd.visibility = View.INVISIBLE
                    btnRemove.visibility = View.VISIBLE
                }
                btnRemove.setOnClickListener {
                    var index = 0
                    binding.root.context.run loop@ {
                        memberList.forEach {
                            if (user.uid == it.userID) {
                                memberList.removeAt(index)
                                return@loop
                            }
                            index++
                        }
                    }
                    btnAdd.visibility = View.VISIBLE
                    btnRemove.visibility = View.INVISIBLE
                }

            }
        }
    }
}
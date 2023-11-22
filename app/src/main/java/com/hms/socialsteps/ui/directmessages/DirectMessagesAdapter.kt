package com.hms.socialsteps.ui.directmessages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hms.socialsteps.core.util.loadImage
import com.hms.socialsteps.data.model.LastMessage
import com.hms.socialsteps.data.model.Users
import com.hms.socialsteps.databinding.LayoutDirectMessageBinding

class DirectMessagesAdapter(
    val clickListener: DirectMessageClickListener
) :
    RecyclerView.Adapter<DirectMessagesAdapter.DirectMessagesViewHolder>() {

    private val callback = object : DiffUtil.ItemCallback<LastMessage>() {
        override fun areItemsTheSame(oldItem: LastMessage, newItem: LastMessage): Boolean {
            return oldItem.user.uid == newItem.user.uid
        }

        override fun areContentsTheSame(oldItem: LastMessage, newItem: LastMessage): Boolean {
            return oldItem.message.lastMessage == newItem.message.lastMessage
        }
    }

    val differ = AsyncListDiffer(this, callback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DirectMessagesViewHolder {
        val binding = LayoutDirectMessageBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return DirectMessagesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DirectMessagesViewHolder, position: Int) {
        val lastMessage = differ.currentList[position]
        holder.bind(lastMessage)
    }

    override fun getItemCount(): Int = differ.currentList.size

    inner class DirectMessagesViewHolder(val binding: LayoutDirectMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(lastMessage: LastMessage) {
            binding.root.context.loadImage(lastMessage.user.photo,binding.ivUserPhoto)
            binding.tvUsername.text=lastMessage.user.username
            binding.tvLastMessage.text=lastMessage.message.lastMessage

            binding.root.setOnClickListener {
                clickListener.onItemClicked(lastMessage.user)
            }
        }
    }

    interface DirectMessageClickListener{
        fun onItemClicked(users: Users)
    }

}
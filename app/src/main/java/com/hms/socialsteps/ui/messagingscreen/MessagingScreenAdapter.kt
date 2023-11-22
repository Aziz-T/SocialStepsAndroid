package com.hms.socialsteps.ui.messagingscreen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hms.socialsteps.core.util.loadImage
import com.hms.socialsteps.data.model.MessageItem
import com.hms.socialsteps.data.model.Users
import com.hms.socialsteps.databinding.LayoutMessageReceiveBinding
import com.hms.socialsteps.databinding.LayoutMessageSendBinding

class MessagingScreenAdapter(
    private val receiverId: String,
    val clickListener: MessagingScreenClickListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var user1: Users? = null
    private var user2: Users? = null

    fun setUsers(list: List<Users>) {
        if (list[0].uid == receiverId) {
            user1 = list[1]
            user2 = list[0]
        } else {
            user1 = list[0]
            user2 = list[1]
        }
    }

    private val callback = object : DiffUtil.ItemCallback<MessageItem>() {
        override fun areItemsTheSame(oldItem: MessageItem, newItem: MessageItem): Boolean {
            return oldItem.messageId == newItem.messageId
        }

        override fun areContentsTheSame(oldItem: MessageItem, newItem: MessageItem): Boolean {
            return oldItem.text == newItem.text
        }
    }

    val differ = AsyncListDiffer(this, callback)

    override fun getItemViewType(position: Int): Int =
        if (differ.currentList[position].senderId != receiverId) 0 else 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                SenderViewHolder(
                    LayoutMessageSendBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }
            else -> {
                ReceiverViewHolder(
                    LayoutMessageReceiveBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            0 -> {
                val message = differ.currentList[position]
                (holder as SenderViewHolder).bind(message)
            }
            else -> {
                val message = differ.currentList[position]
                (holder as ReceiverViewHolder).bind(message)
            }
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    inner class SenderViewHolder(val binding: LayoutMessageSendBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(messageItem: MessageItem) {
            binding.root.context.loadImage(user1!!.photo, binding.ivProfile)
            binding.tv.text = messageItem.text
            /*//show image if there is one
            if (messageItem.additionalData.isNotBlank() && messageItem.type == "image"){
                binding.root.context.loadImage(messageItem.additionalData,binding.ivData)
            }*/
            binding.root.setOnClickListener {
                clickListener.onItemClicked(user1!!)
            }
        }
    }

    inner class ReceiverViewHolder(val binding: LayoutMessageReceiveBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(messageItem: MessageItem) {
            binding.root.context.loadImage(user2!!.photo, binding.ivProfile)
            binding.tv.text = messageItem.text
            /*//show image if there is one
            if (messageItem.additionalData.isNotBlank() && messageItem.type == "image"){
                binding.root.context.loadImage(messageItem.additionalData,binding.ivData)
            }*/
            binding.root.setOnClickListener {
                clickListener.onItemClicked(user2!!)
            }
        }
    }

    interface MessagingScreenClickListener {
        fun onItemClicked(users: Users)
    }

}
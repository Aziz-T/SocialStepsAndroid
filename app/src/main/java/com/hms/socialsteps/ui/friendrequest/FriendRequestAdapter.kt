package com.hms.socialsteps.ui.friendrequest

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.bold
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hms.socialsteps.R
import com.hms.socialsteps.core.util.loadImage
import com.hms.socialsteps.data.model.Friendship
import com.hms.socialsteps.databinding.CardFriendRequestBinding

class FriendRequestAdapter(
    private val acceptListener: (Friendship) -> Unit,
    private val rejectListener: (Friendship) -> Unit,
    private val profilePhotoListener: (Friendship) -> Unit
): RecyclerView.Adapter<FriendRequestAdapter.FriendRequestViewHolder>() {

    private val callback = object: DiffUtil.ItemCallback<Friendship>() {
        override fun areItemsTheSame(oldItem: Friendship, newItem: Friendship): Boolean {
            return oldItem.friendShipId == newItem.friendShipId
        }

        override fun areContentsTheSame(oldItem: Friendship, newItem: Friendship): Boolean {
            return oldItem.friendShipId == newItem.friendShipId
        }
    }

    val differ = AsyncListDiffer(this, callback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendRequestViewHolder {
        val binding = CardFriendRequestBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return FriendRequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendRequestViewHolder, position: Int) {
        val friendship = differ.currentList[position]
        holder.bind(friendship)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    inner class FriendRequestViewHolder(val binding: CardFriendRequestBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(friendship: Friendship) {
            binding.apply {
                tvNotificationMessage.text = SpannableStringBuilder()
                    .bold { append(friendship.user2name) }
                    .append(" ")
                    .append(root.context.getString(R.string.sent_you_a_friend_request))
                root.context.loadImage(friendship.user1Photo, ivUserPhoto)
            }
            binding.ibAccept.setOnClickListener { acceptListener(friendship) }
            binding.ibReject.setOnClickListener { rejectListener(friendship) }
            binding.ivUserPhoto.setOnClickListener { profilePhotoListener(friendship) }
        }
    }
}
package com.hms.socialsteps.ui.notification

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hms.socialsteps.R
import com.hms.socialsteps.core.util.Constants.COMMENT
import com.hms.socialsteps.core.util.Constants.FRIEND
import com.hms.socialsteps.core.util.Constants.GROUP_ADD
import com.hms.socialsteps.core.util.Constants.LIKE
import com.hms.socialsteps.core.util.loadImage
import com.hms.socialsteps.data.model.Notifications
import com.hms.socialsteps.databinding.CardNotificationBinding

class NotificationAdapter(
    private val onClickListener: (Notifications) -> Unit
): RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    private val callback = object: DiffUtil.ItemCallback<Notifications>() {
        override fun areItemsTheSame(oldItem: Notifications, newItem: Notifications): Boolean {
            return oldItem.notificationID == newItem.notificationID
        }

        override fun areContentsTheSame(oldItem: Notifications, newItem: Notifications): Boolean {
            return oldItem.notificationID == newItem.notificationID
        }
    }

    val differ = AsyncListDiffer(this, callback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = CardNotificationBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = differ.currentList[position]
        holder.bind(notification)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    inner class NotificationViewHolder(val binding: CardNotificationBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(notifications: Notifications) {
            binding.apply {
                if (notifications.isRead){
                    clNotification.background = AppCompatResources.getDrawable(clNotification.context,com.google.android.material.R.drawable.m3_tabs_transparent_background)
                }
                root.context.loadImage(notifications.user1Photo, ivUserPhoto)
                ivNotification.apply {
                    when(notifications.notificationType) {
                        LIKE -> {
                            setImageResource(R.drawable.ic_baseline_thumb_up_24)
                            setColorFilter(ContextCompat.getColor(root.context, R.color.like_and_friend_color))
                            tvNotificationInfo.text = root.context.getString(R.string.liked_your_post)
                        }
                        COMMENT -> {
                            setImageResource(R.drawable.ic_baseline_mode_comment_24)
                            setColorFilter(ContextCompat.getColor(root.context, R.color.comment_and_group_color))
                            tvNotificationInfo.text = root.context.getString(R.string.commented_on_your_post)
                        }
                        FRIEND -> {
                            setImageResource(R.drawable.ic_baseline_person_24)
                            setColorFilter(ContextCompat.getColor(root.context, R.color.like_and_friend_color))
                            tvNotificationInfo.text = root.context.getString(R.string.accepted_your_friend_request)
                        }
                        GROUP_ADD -> {
                            setImageResource(R.drawable.ic_baseline_group_24)
                            setColorFilter(ContextCompat.getColor(root.context, R.color.comment_and_group_color))
                            tvNotificationInfo.text = root.context.getString(R.string.group_add_notification_text,notifications.additionalString)
                        }
                    }
                }
                tvUsername.text = notifications.user1Name
                tvNotificationDate.text = notifications.notificationDate
            }
            binding.root.setOnClickListener {
                onClickListener(notifications)
            }
        }
    }
}
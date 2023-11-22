package com.hms.socialsteps.ui.comment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hms.socialsteps.R
import com.hms.socialsteps.core.util.UserPreference
import com.hms.socialsteps.core.util.loadImage
import com.hms.socialsteps.data.model.Comments
import com.hms.socialsteps.data.model.Users
import com.hms.socialsteps.databinding.CardCommentBinding


class CommentAdapter() :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    private val callback = object : DiffUtil.ItemCallback<Comments>() {
        override fun areItemsTheSame(oldItem: Comments, newItem: Comments): Boolean {
            return oldItem.commentId == newItem.commentId
        }

        override fun areContentsTheSame(oldItem: Comments, newItem: Comments): Boolean {
            return oldItem.commentId == newItem.commentId
        }
    }

    val differ = AsyncListDiffer(this, callback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = CardCommentBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = differ.currentList[position]
        holder.bind(comment)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    inner class CommentViewHolder(val binding: CardCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(comments: Comments) {
            binding.root.context.loadImage(comments.userPhoto,binding.ivProfile)
            binding.tvCommentUserName.text=comments.userName
            binding.tvCommentDetail.text=comments.commentContent
        }
    }


}
package com.hms.socialsteps.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.hms.socialsteps.R
import com.hms.socialsteps.core.util.getCurrentDate
import com.hms.socialsteps.core.util.loadImage
import com.hms.socialsteps.data.model.Comments
import com.hms.socialsteps.data.model.Likes
import com.hms.socialsteps.data.model.Posts
import com.hms.socialsteps.databinding.CardFeedBinding
import com.hms.socialsteps.domain.repository.CloudDbWrapper
import com.hms.socialsteps.utils.binding.AutoUpdatableAdapter
import kotlin.properties.Delegates

class ProfileAdapter constructor(private val clickListener: ProfileOnClickListener, val userId: String) :
    RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>(),
    AutoUpdatableAdapter {
    private lateinit var itemBinding: CardFeedBinding

    var items: List<Posts> by Delegates.observable(emptyList()) { _, old, new ->
        autoNotify(old, new) { o, n -> o == n }
    }

    var likes: List<Likes> = mutableListOf()

    var comments: List<Comments> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        itemBinding = CardFeedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProfileViewHolder(itemBinding, clickListener)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        holder.bind(items[position])
        println("onbindViewHolder ${position.toString()}")
    }

    override fun getItemCount() = items.size

    inner class ProfileViewHolder(
        private val itemBinding: CardFeedBinding,
        private val clickListener: ProfileOnClickListener
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(post: Posts) {
            itemBinding.apply {

                val likeList: MutableList<Likes> = mutableListOf()
                likes.forEach {
                    if (it.postId == post.postId) {
                        likeList.add(it)
                    }
                }
                val likesList: MutableList<String> = mutableListOf()
                likeList.forEach {
                    likesList.add(it.userId)
                }

                val commentList: MutableList<Comments> = mutableListOf()
                comments.forEach {
                    if (it.postId == post.postId) {
                        commentList.add(it)
                    }
                }
                val commentsList: MutableList<String> = mutableListOf()
                commentList.forEach {
                    commentsList.add(it.userId)
                }

                tvCommentCount.text = commentsList.size.toString()
                tvLikeCount.text = likesList.size.toString()

                if (likesList.contains(userId)) {
                    ibLike.visibility = View.GONE
                    ibDislike.visibility = View.VISIBLE
                } else {
                    ibLike.visibility = View.VISIBLE
                    ibDislike.visibility = View.GONE
                }

                root.context.loadImage(post.photoUrl, ivProfile)
                tvPostUserName.text = post.username
                var currentDate = getCurrentDate().substring(0, 9)
                if (currentDate.equals(post.sharedDate.substring(0, 9))) {
                    tvDate.text = "Today ${post.sharedDate.substring(9)}"
                } else {
                    tvDate.text = post.sharedDate
                }
                when (post.postType) {
                    1 -> {
                        postImage.background = AppCompatResources.getDrawable(root.context,
                        R.drawable.solid_circle)
                        if (post.activityType == "walk") {
                            postImage.setImageResource(R.drawable.ic_baseline_directions_walk_24_white)
                            tvPostDetail.text =
                                root.context.getString(R.string.i_did_a_walking_activity)
                        } else {
                            postImage.setImageResource(R.drawable.ic_baseline_directions_run_24_white)
                            tvPostDetail.text =
                                root.context.getString(R.string.i_did_a_running_activity)
                        }
                    }
                    2 -> {
                        postImage.setImageResource(R.drawable.steps)
                        postImage.background = AppCompatResources.getDrawable(
                            postImage.context,
                            com.google.android.material.R.drawable.m3_tabs_transparent_background
                        )
                        tvPostDetail.text = root.context.resources.getString(
                            R.string.steps_post_text,
                            post.targetSteps,
                            post.reachedSteps
                        )
                    }
                    3 -> {
                        postImage.setImageResource(R.drawable.ic_burn_calories)
                        postImage.background = AppCompatResources.getDrawable(
                            postImage.context,
                            com.google.android.material.R.drawable.m3_tabs_transparent_background
                        )
                        tvPostDetail.text = root.context.resources.getString(
                            R.string.calories_post_text,
                            post.targetCalories,
                            post.reachedCalories
                        )
                    }
                }

                ivProfile.setOnClickListener {
                    clickListener.onImageClicked(post)
                }
                ibLike.setOnClickListener {
                    var likeCount = tvLikeCount.text.toString().toInt()
                    clickListener.onLikeClicked(post)
                    tvLikeCount.text = (likeCount + 1).toString()
                    ibLike.visibility = View.GONE
                    ibDislike.visibility = View.VISIBLE

                }
                ibDislike.setOnClickListener {
                    var likeCount = tvLikeCount.text.toString().toInt()
                    tvLikeCount.text = (likeCount - 1).toString()
                    clickListener.onRedoLikeClicked(post)
                    ibLike.visibility = View.VISIBLE
                    ibDislike.visibility = View.GONE

                }
                ibComment.setOnClickListener {
                    clickListener.onCommentClicked(post)
                }
                if (post.activityId != null) {
                    root.setOnClickListener {
                        clickListener.onActivityPostClicked(post)
                    }
                }
            }
        }
    }

    interface ProfileOnClickListener {
        fun onLikeClicked(post: Posts)
        fun onCommentClicked(post: Posts)
        fun onImageClicked(post: Posts)
        fun onRedoLikeClicked(post: Posts)
        fun onActivityPostClicked(post: Posts)
    }
}
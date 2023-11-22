package com.hms.socialsteps.ui.feed

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hms.socialsteps.R
import com.hms.socialsteps.core.util.*
import com.hms.socialsteps.data.model.Posts
import com.hms.socialsteps.databinding.FragmentFeedBinding
import com.hms.socialsteps.ui.MainViewModel
import com.hms.socialsteps.ui.base.BaseFragment
import com.hms.socialsteps.ui.comment.CommentViewModel
import com.hms.socialsteps.utils.binding.viewBinding
import com.huawei.agconnect.auth.AGConnectAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class FeedFragment : BaseFragment(R.layout.fragment_feed), FeedAdapter.FeedOnClickListener {

    private val binding by viewBinding(FragmentFeedBinding::bind)
    private val viewModel: FeedViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val commentViewModel: CommentViewModel by viewModels()
    private lateinit var feedAdapter: FeedAdapter

    @Inject
    lateinit var userPreference: UserPreference

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //this function must be on top
        userPreference.getStoredUser().uid?.let {
            feedAdapter = FeedAdapter(this, userPreference.getStoredUser().uid)
            binding.rvFeed.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = feedAdapter
            }
        }
        checkIfUserRegistered()

        initClickListeners()
    }

    private fun initClickListeners() {
        binding.btnNotifications.setOnClickListener {
            findNavController().navigate(R.id.notificationFragment)
        }

        binding.btnFriendRequest.setOnClickListener {
            findNavController().navigate(R.id.friendRequestFragment)
        }

        binding.btnRank.setOnClickListener {
            findNavController().navigate(R.id.navStats)
        }

        binding.buttonMessages.setOnClickListener {
            findNavController().navigate(R.id.directMessagesFragment)
        }
    }

    private fun checkIfUserRegistered() {
        val userId = AGConnectAuth.getInstance().currentUser.uid
        viewModel.checkIfUserRegistered(userId)
        viewModel.userRegistered.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Error -> {
                    hideProgressBar(binding.progressBar)
                   requireContext().showAlertDialog( "Error", result.message.toString())
                }
                is Resource.Loading -> {
                    showProgressBar(binding.progressBar)
                }
                is Resource.Success -> {
                    //if there is a user, no action needed.
                    showFeed()
                }
                is Resource.Empty -> {
                    hideProgressBar(binding.progressBar)
                    //There is no user in db, we need to record user.
                    findNavController().navigate(R.id.registerFragment)
                }
            }
        }
    }

    private fun showFeed() {
        viewModel.getUserFeed()

        viewModel.mediatorLiveData.observe(viewLifecycleOwner) { result ->
            if (result) {
                if (viewModel.postList.value is Resource.Empty) {
                    hideProgressBar(binding.progressBar)
                    binding.tvNoFeed.visibility = View.VISIBLE
                } else {
                    binding.tvNoFeed.visibility = View.INVISIBLE

                    viewModel.likes.value?.let {
                        if (it.data != null)
                            feedAdapter.likes = viewModel.likes.value!!.data!!
                    }

                    viewModel.postList.value?.let {
                        if (it.data != null) {
                            feedAdapter.items = viewModel.postList.value!!.data!!
                        }
                    }

                    viewModel.comments.value?.let {
                        if (it.data != null) {
                            feedAdapter.comments = viewModel.comments.value!!.data!!
                        }
                    }
                    hideProgressBar(binding.progressBar)
                }
            } else {
                viewModel.postList.observe(viewLifecycleOwner) { result ->

                    if (result is Resource.Empty) {
                        binding.tvNoFeed.visibility = View.VISIBLE
                    }

                }

            }
        }

    }

    override fun onLikeClicked(post: Posts) {
        viewModel.upsertLike(post.postId, post.userId)
        viewModel.upsertLikeCount(post.postId, true)
    }

    override fun onCommentClicked(post: Posts) {
        val bundle = Bundle()
        val postJsonString = com.hms.socialsteps.core.util.Utils.getGsonParser().toJson(post)
        bundle.putString("post", postJsonString)
        navigateTo(R.id.action_navFeed_to_commentFragment, bundle)
    }

    override fun onImageClicked(post: Posts) {
        val bundle = Bundle()
        bundle.putString("uid", post.userId)
        val uid = post.userId
        mainViewModel.uId = uid
        navigateTo(R.id.action_navFeed_to_visitedProfileFragment, bundle)
    }

    override fun onRedoLikeClicked(post: Posts) {
        viewModel.upsertLikeCount(post.postId, false)
        viewModel.deleteLike(userPreference.getStoredUser().uid, post.postId)
    }

    override fun onActivityPostClicked(post: Posts) {
        val bundle = Bundle()
        bundle.putString("activityId", post.activityId)
        navigateTo(R.id.action_navFeed_to_activityDetailsFragment, bundle)
    }

    override fun onPause() {
        super.onPause()
        viewModel.isLikeFetched.value = false
        viewModel.isPostFetched.value = false
        viewModel.mediatorLiveData.value = false
    }
}
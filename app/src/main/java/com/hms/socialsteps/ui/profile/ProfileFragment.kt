package com.hms.socialsteps.ui.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.hms.socialsteps.R
import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.core.util.UserPreference
import com.hms.socialsteps.core.util.hideProgressBar
import com.hms.socialsteps.core.util.navigateTo
import com.hms.socialsteps.data.model.Posts
import com.hms.socialsteps.databinding.FragmentProfileBinding
import com.hms.socialsteps.ui.activities.ActivitiesViewModel
import com.hms.socialsteps.ui.base.BaseFragment
import com.hms.socialsteps.ui.feed.FeedViewModel
import com.hms.socialsteps.ui.post.PostViewModel
import com.hms.socialsteps.ui.statistics.ViewPagerAdapter
import com.hms.socialsteps.utils.binding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : BaseFragment(R.layout.fragment_profile),
    ProfileAdapter.ProfileOnClickListener {
    val TAG = "ProfileFragment"

    private val binding by viewBinding(FragmentProfileBinding::bind)

    //    private val binding by viewBinding(FragmentProfileBinding::bind)
    private val viewModel by viewModels<ProfileViewModel>()

    //
    private val activitiesViewModel by viewModels<ActivitiesViewModel>()
    private val feedViewModel: FeedViewModel by viewModels()

    private val postViewModel: PostViewModel by activityViewModels()
    private lateinit var profileAdapter: ProfileAdapter

    private lateinit var adapter: ViewPagerAdapter

    @Inject
    lateinit var userPreference: UserPreference

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreference.getStoredUser().uid?.let {
            profileAdapter = ProfileAdapter(this, userPreference.getStoredUser().uid)
            binding.rvUserPosts.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = profileAdapter
            }
        }

        getUser()
        initObservers()
        initOnClickListeners()
        showFeed()

    }

    private fun initObservers() {

    }

    private fun getUser() {
        val user = userPreference.getStoredUser()
        hideProgressBar(binding.progressBar)
        Glide.with(requireContext())
            .load(user.photo)
            .error(R.mipmap.ic_launcher)
            .into(binding.ivProfile)
        with(binding) {
            tvName.text = user.fullName
            tvUsername.text = user.username
            tvAgeInfo.text = user.age.toString()
            tvHeightInfo.text = user.height.toString()
            tvWeightInfo.text = user.weight.toString()
            tvGenderInfo.text = user.gender
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
                            profileAdapter.likes = viewModel.likes.value!!.data!!
                    }

                    viewModel.postList.value?.let {
                        if (it.data != null) {
                            profileAdapter.items = viewModel.postList.value!!.data!!
                        }
                    }

                    viewModel.comments.value?.let {
                        if (it.data != null) {
                            profileAdapter.comments = viewModel.comments.value!!.data!!
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

    private fun initOnClickListeners() {
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_navProfile_to_editProfileFragment)
        }

    }

    override fun onLikeClicked(post: Posts) {
        feedViewModel.upsertLike(post.postId, post.userId)
        feedViewModel.upsertLikeCount(post.postId, true)

    }

    override fun onCommentClicked(post: Posts) {
        val bundle = Bundle()
        val postJsonString = com.hms.socialsteps.core.util.Utils.getGsonParser().toJson(post)
        bundle.putString("post", postJsonString)
        navigateTo(R.id.action_navProfile_to_commentFragment, bundle)

    }

    override fun onImageClicked(post: Posts) {
        feedViewModel.upsertLikeCount(post.postId, false)
        feedViewModel.deleteLike(userPreference.getStoredUser().uid, post.postId)
    }

    override fun onRedoLikeClicked(post: Posts) {
        feedViewModel.upsertLikeCount(post.postId, false)
        feedViewModel.deleteLike(userPreference.getStoredUser().uid, post.postId)
    }

    override fun onActivityPostClicked(post: Posts) {

    }

}
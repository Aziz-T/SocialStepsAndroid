package com.hms.socialsteps.ui.visitedprofile.visiteduserprofileinformation

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.hms.socialsteps.R
import com.hms.socialsteps.core.util.*
import com.hms.socialsteps.data.model.Posts
import com.hms.socialsteps.data.model.Users
import com.hms.socialsteps.databinding.FragmentVisitedUserProfileBinding
import com.hms.socialsteps.ui.MainViewModel
import com.hms.socialsteps.ui.base.BaseFragment
import com.hms.socialsteps.ui.feed.FeedViewModel
import com.hms.socialsteps.ui.friendrequest.FriendRequestViewModel
import com.hms.socialsteps.ui.visitedprofile.VisitedProfileAdapter
import com.hms.socialsteps.utils.binding.viewBinding
import com.huawei.agconnect.auth.AGConnectAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VisitedUserProfileFragment : BaseFragment(R.layout.fragment_visited_user_profile),
    VisitedProfileAdapter.VisitedProfileOnClickListener {

    private val binding by viewBinding(FragmentVisitedUserProfileBinding::bind)

    private val viewModel: VisitedUserProfileViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val friendRequestViewModel: FriendRequestViewModel by viewModels()
    private val feedViewModel: FeedViewModel by viewModels()
    private lateinit var visitedProfileAdapter: VisitedProfileAdapter

    @Inject
    lateinit var userPreference: UserPreference

    private val cUser = AGConnectAuth.getInstance().currentUser
    private var visitedUserUID = ""
    private var visitedUserName: String = ""
    private var isCancel = false
    private var isAccepted = false
    private var isReplyRequest = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        visitedUserUID = mainViewModel.uId

        userPreference.getStoredUser().uid?.let {
            visitedProfileAdapter = VisitedProfileAdapter(this, userPreference.getStoredUser().uid)
            binding.rvVisitedUserPosts.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = visitedProfileAdapter
            }
        }

        getUser(visitedUserUID)
        initObservers()
        initListeners()
        showFeed()

    }

    private fun initObservers() {
        Log.e("init observers upsertResult", "initObservers")
        viewModel.upsertResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    Log.e("init observers upsertResult", "$result")
                    showProgressBar(binding.pbLoading)
                }
                is Resource.Error -> {
                    Log.e("init observers upsertResult Error", "${result.message}")
                    requireContext().showAlertDialog("Error", result.message)
                    hideProgressBar(binding.pbLoading)
                }
                is Resource.Success -> {
                    Log.e("init observers upsertResult Success", "${result.message}")
                    requireContext().showToastLong("Request Sent Successfully")
                    hideProgressBar(binding.pbLoading)
                    binding.buttonAddFriend.visibility = View.GONE
                    binding.buttonRequestSent.visibility = View.VISIBLE

                }
                else -> {
                    //Impossible to enter this else
                }
            }
        }

        viewModel.friendship.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    Log.e("init observers friendship Loading", "${result.data}")
                    Log.e("init observers friendship Loading", "${result.message}")
                    showProgressBar(binding.pbLoading)
                }
                is Resource.Error -> {
                    Log.e("init observers friendship Error", "${result.message}")
                    binding.tvErrorText.visibility = View.VISIBLE
                    binding.tvErrorText.text = result.message.toString()
                    hideProgressBar(binding.pbLoading)
                }
                is Resource.Empty -> {
                    Log.e("init observers friendship Empty", "${result.message}")
                    setPrivateProfileVisibility()
                    binding.buttonAddFriend.visibility = View.VISIBLE
                    hideProgressBar(binding.pbLoading)
                }
                is Resource.Success -> {
                    Log.e("init observers friendship Success", "${result.message}")
                    if (isCancel) {
                        viewModel.friendship.value!!.data?.let { friendShip ->
                            friendRequestViewModel.replyFriendRequest(
                                friendShip, true
                            )
                        }
                    } else {
                        Log.e("result.data?.status", "${result.data?.status}")
                        when (result.data?.status) {
                            "1" -> {
                                setPrivateProfileVisibility()
                                binding.buttonRequestSent.visibility = View.VISIBLE
                                hideProgressBar(binding.pbLoading)
                            }
                            "2" -> {
                                setPendingRequestVisibility()
                                binding.ivSearchUser.visibility = View.VISIBLE
                            }
                            "3" -> {
                                setFriendProfileVisibility()
                            }
                        }
                    }
                }
            }
        }

        friendRequestViewModel.upsertOrDeleteResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    Log.e("init observers upsertOrDeleteResult", "${result.data}")
                    Log.e("init observers upsertOrDeleteResult", "${result.message}")
                    showProgressBar(binding.pbLoading)
                }
                is Resource.Error -> {
                    Log.e("init observers upsertOrDeleteResult Error", "${result.message}")
                    hideProgressBar(binding.pbLoading)
                    requireContext().showAlertDialog("Error", result.message)
                }
                is Resource.Success -> {
                    Log.e("init observers upsertOrDeleteResult Success", "${result.message}")
                    if (isReplyRequest) {
                        if (isAccepted) {
                            binding.ivLockImage.visibility = View.INVISIBLE
                            binding.tvPrivateProfile.visibility = View.INVISIBLE
                            setFriendProfileVisibility()
                            hidePendingRequestLayout()
                            hideProgressBar(binding.pbLoading)
                            requireContext().showToastLong("You accepted request")
                            isAccepted = false
                        } else {
                            hidePendingRequestLayout()
                            hideProgressBar(binding.pbLoading)
                            binding.buttonAddFriend.visibility = View.VISIBLE
                            requireContext().showToastLong("You rejected request")
                        }
                    } else {
                        binding.buttonAddFriend.visibility = View.VISIBLE
                        binding.buttonRequestSent.visibility = View.INVISIBLE
                        requireContext().showToastLong("Request withdrawn successfully")
                        hideProgressBar(binding.pbLoading)
                        isCancel = false
                    }
                }
                else -> {}
            }
        }
    }

    private fun setPendingRequestVisibility() {
        binding.apply {
            setPrivateProfileVisibility()
            tvPendingRequest.visibility = View.VISIBLE
            btnAccept.visibility = View.VISIBLE
            btnReject.visibility = View.VISIBLE
            hideProgressBar(binding.pbLoading)
        }
    }

    private fun setFriendProfileVisibility() {
        binding.apply {
            ivProfile.visibility = View.VISIBLE
            tvFullName.visibility = View.VISIBLE
            tvUsername.visibility = View.VISIBLE
            rvVisitedUserPosts.visibility = View.VISIBLE
            hideProgressBar(binding.pbLoading)
        }
    }

    private fun setPrivateProfileVisibility() {
        binding.apply {
            if (viewModel.user.value?.data?.isPrivate == true) {
                ivProfile.visibility = View.VISIBLE
                tvFullName.visibility = View.VISIBLE
                tvUsername.visibility = View.VISIBLE
                ivLockImage.visibility = View.VISIBLE
                tvPrivateProfile.visibility = View.VISIBLE
                rvVisitedUserPosts.visibility = View.INVISIBLE
            } else {
                ivProfile.visibility = View.VISIBLE
                tvFullName.visibility = View.VISIBLE
                tvUsername.visibility = View.VISIBLE
                rvVisitedUserPosts.visibility = View.VISIBLE
            }
        }
    }

    private fun hidePendingRequestLayout() {
        binding.apply {
            tvPendingRequest.visibility = View.INVISIBLE
            btnAccept.visibility = View.INVISIBLE
            btnReject.visibility = View.INVISIBLE
        }
    }

    private fun initListeners() {
        binding.buttonAddFriend.setOnClickListener {
            sendFriendRequest()
            isCancel = false
        }
        binding.buttonRequestSent.setOnClickListener {
            isReplyRequest = false
            isCancel = true
            cancelFriendRequest()
        }
        binding.btnAccept.setOnClickListener {
            isReplyRequest = true
            isAccepted = true
            viewModel.friendship.value!!.data?.let { friendShip ->
                friendRequestViewModel.replyFriendRequest(
                    friendShip, false
                )
            }
            feedViewModel.upsertNotification(
                "", viewModel.friendship.value!!.data!!.user2, null,
                Constants.FRIEND
            )
        }
        binding.btnReject.setOnClickListener {
            isReplyRequest = true
            isAccepted = false
            viewModel.friendship.value!!.data?.let { friendShip ->
                friendRequestViewModel.replyFriendRequest(
                    friendShip, true
                )
            }
        }
    }

    private fun cancelFriendRequest() {
        if (viewModel.friendship.value?.data == null) {
            viewModel.queryFriendShipStatus(cUser.uid, visitedUserUID)
        } else {
            viewModel.friendship.value?.data?.let {
                viewModel.friendship.value!!.data?.let { friendShip ->
                    friendRequestViewModel.replyFriendRequest(
                        friendShip, true
                    )
                }
            }
        }
    }

    private fun showFeed() {
        viewModel.getUserFeed(visitedUserUID)

        viewModel.mediatorLiveData.observe(viewLifecycleOwner) { result ->
            if (result) {
                if (viewModel.postList.value is Resource.Empty) {
                    hideProgressBar(binding.pbLoading)
                } else {
                    viewModel.likes.value?.let {
                        if (it.data != null)
                            visitedProfileAdapter.likes = viewModel.likes.value!!.data!!
                    }

                    viewModel.postList.value?.let {
                        if (it.data != null) {
                            visitedProfileAdapter.items = viewModel.postList.value!!.data!!
                        }
                    }

                    viewModel.comments.value?.let {
                        if (it.data != null) {
                            visitedProfileAdapter.comments = viewModel.comments.value!!.data!!
                        }
                    }
                    hideProgressBar(binding.pbLoading)
                }
            } else {
                viewModel.postList.observe(viewLifecycleOwner) { result ->
                    if (result is Resource.Empty) {

                    }

                }

            }
        }

    }

    private fun sendFriendRequest() {
        viewModel.sendFriendRequest(cUser.uid, visitedUserUID, visitedUserName)
    }

    private fun getUser(userId: String) {
        viewModel.getUser(userId)
        viewModel.user.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Error -> handleError(result.message!!)
                is Resource.Loading -> handleLoading()
                is Resource.Success -> {
                    handleSuccess(result.data!!)
                }
                is Resource.Empty -> {}  //empty result is impossible
            }
        }
    }

    private fun handleSuccess(user: Users) {
        /*checkIfSelfProfile(user)*/
        visitedUserName = user.username
        with(binding) {
            loadImage(user.photo ?: "", ivProfile)
            tvFullName.text = user.fullName
            tvUsername.text = user.username
            tvAgeInfo.text = user.age.toString()
            tvHeightInfo.text = user.height.toString()
            tvWeightInfo.text = user.weight.toString()
            tvGenderInfo.text = user.gender
            checkIfFriend()
        }
    }

    private fun handleLoading() {

        with(binding) {
            pbLoading.visibility = View.VISIBLE
            tvErrorText.visibility = View.INVISIBLE
        }
    }

    private fun handleError(message: String) {
        requireContext().showAlertDialog("Error", message)
        with(binding) {
            pbLoading.visibility = View.GONE
            tvErrorText.visibility = View.VISIBLE
            tvErrorText.text = message
        }
    }

    private fun checkIfFriend() {
        viewModel.queryFriendShipStatus(cUser.uid, visitedUserUID)
    }

    override fun onLikeClicked(post: Posts) {
        feedViewModel.upsertLike(post.postId, post.userId)
        feedViewModel.upsertLikeCount(post.postId, true)
    }

    override fun onCommentClicked(post: Posts) {
        val bundle = Bundle()
        val postJsonString = com.hms.socialsteps.core.util.Utils.getGsonParser().toJson(post)
        bundle.putString("post", postJsonString)
        navigateTo(R.id.action_visitedProfileFragment_to_commentFragment, bundle)
    }

    override fun onImageClicked(post: Posts) {
        val bundle = Bundle()
        bundle.putString("uid", post.userId)
        val uid = post.userId
        mainViewModel.uId = uid
        navigateTo(R.id.action_navFeed_to_visitedProfileFragment, bundle)
    }

    override fun onRedoLikeClicked(post: Posts) {
        feedViewModel.upsertLikeCount(post.postId, false)
        feedViewModel.deleteLike(userPreference.getStoredUser().uid, post.postId)
    }

    override fun onActivityPostClicked(post: Posts) {
        val bundle = Bundle()
        bundle.putString("activityId", post.activityId)
        navigateTo(R.id.action_navFeed_to_activityDetailsFragment, bundle)
    }

}
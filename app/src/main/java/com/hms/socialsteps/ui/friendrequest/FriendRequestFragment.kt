package com.hms.socialsteps.ui.friendrequest

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hms.socialsteps.R
import com.hms.socialsteps.core.util.*
import com.hms.socialsteps.core.util.Constants.FRIEND
import com.hms.socialsteps.data.model.Friendship
import com.hms.socialsteps.databinding.FragmentFriendRequestBinding
import com.hms.socialsteps.ui.MainActivity
import com.hms.socialsteps.ui.base.BaseFragment
import com.hms.socialsteps.ui.feed.FeedViewModel
import com.hms.socialsteps.utils.binding.viewBinding
import com.huawei.agconnect.auth.AGConnectAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendRequestFragment : BaseFragment(R.layout.fragment_friend_request) {

    private val binding by viewBinding(FragmentFriendRequestBinding::bind)
    private val viewModel: FriendRequestViewModel by viewModels()
    private val feedViewModel: FeedViewModel by viewModels()

    private lateinit var friendRequestAdapter: FriendRequestAdapter
    private var recordToRemove: Friendship ?= null
    private var isDelete = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        initObservers()
        viewModel.getFriendRequests(AGConnectAuth.getInstance().currentUser.uid)

        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun initObservers() {
        viewModel.friendRequests.observe(viewLifecycleOwner) { friendRequestList ->
            when (friendRequestList) {
                is Resource.Loading -> {
                    showProgressBar(binding.pbLoading)
                }
                is Resource.Empty -> {
                    hideProgressBar(binding.pbLoading)
                    binding.tvNoRequest.visibility = View.VISIBLE
                }
                is Resource.Error -> {
                    hideProgressBar(binding.pbLoading)
                   requireContext().showAlertDialog( "Error", friendRequestList.message)
                }
                is Resource.Success -> {
                    hideProgressBar(binding.pbLoading)
                    friendRequestAdapter.differ.submitList(friendRequestList.data)
                }
            }
        }

        viewModel.upsertOrDeleteResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    showProgressBar(binding.pbLoading)
                }
                is Resource.Error -> {
                    hideProgressBar(binding.pbLoading)
                   requireContext().showAlertDialog( "Error", result.message)
                    isDelete = false
                }
                is Resource.Success -> {
                    val friendShipList =
                        ArrayList<Friendship>(friendRequestAdapter.differ.currentList)
                    friendShipList.remove(recordToRemove)
                    friendRequestAdapter.differ.submitList(friendShipList)
                    hideProgressBar(binding.pbLoading)
                    if (isDelete) {
                        Toast.makeText(requireContext(), "You rejected request", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "You accepted request", Toast.LENGTH_SHORT).show()
                    }
                    isDelete = false
                }
                else -> {}
            }
        }
    }

    private fun initRecyclerView() {
        friendRequestAdapter = FriendRequestAdapter(
           acceptListener = {
               recordToRemove = it
               viewModel.replyFriendRequest(it, false)
               isDelete = false
               feedViewModel.upsertNotification("", it.user2, null, FRIEND)
            },
           rejectListener = {
               recordToRemove = it
               viewModel.replyFriendRequest(it, true)
               isDelete = true
            },
            profilePhotoListener = {
                val bundle = Bundle()
                bundle.putString("uid", it.user1)
                navigateTo(R.id.action_friendRequestFragment_to_visitedProfileFragment, bundle)
            }
        )
        binding.rvFriendRequests.apply {
            adapter = friendRequestAdapter
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        }
    }
}
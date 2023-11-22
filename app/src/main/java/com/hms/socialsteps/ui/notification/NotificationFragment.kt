package com.hms.socialsteps.ui.notification

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hms.socialsteps.R
import com.hms.socialsteps.core.util.*
import com.hms.socialsteps.core.util.Constants.COMMENT
import com.hms.socialsteps.core.util.Constants.FRIEND
import com.hms.socialsteps.core.util.Constants.LIKE
import com.hms.socialsteps.databinding.FragmentNotificationBinding
import com.hms.socialsteps.ui.base.BaseFragment
import com.hms.socialsteps.ui.feed.FeedViewModel
import com.hms.socialsteps.utils.binding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationFragment : BaseFragment(R.layout.fragment_notification) {

    private val binding by viewBinding(FragmentNotificationBinding::bind)
    private val viewModel: NotificationViewModel by viewModels()
    private val feedViewModel: FeedViewModel by viewModels()

    private lateinit var notificationAdapter: NotificationAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notificationAdapter = NotificationAdapter { notification ->
            feedViewModel.upsertNotification(notification.postID, notification.user2, notification, null)
            if (notification.notificationType == FRIEND) {
                val bundle = Bundle()
                bundle.putString("uid", notification.user1)
                navigateTo(R.id.action_notificationFragment_to_visitedProfileFragment, bundle)
            }else if(notification.notificationType == LIKE  || notification.notificationType == COMMENT) {
                val bundle = Bundle()
                bundle.putString("post", notification.postID)
                navigateTo(R.id.action_notificationFragment_to_commentFragment, bundle)
            }
            // TODO: add click listeners for groups
        }
        binding.rvNotifications.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = notificationAdapter
        }
        initObservers()
        viewModel.getNotificationList()

        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }

    }

    private fun initObservers() {
        viewModel.notificationListLiveData.observe(viewLifecycleOwner) { result ->
            when(result) {
                is Resource.Loading -> {
                    showProgressBar(binding.pbLoading)
                }
                is Resource.Error -> {
                   requireContext().showAlertDialog( "Error", result.message)
                    hideProgressBar(binding.pbLoading)
                }
                is Resource.Empty -> {
                    binding.tvNoNotification.visibility = View.VISIBLE
                    hideProgressBar(binding.pbLoading)
                }
                is Resource.Success -> {
                    notificationAdapter.differ.submitList(result.data)
                    hideProgressBar(binding.pbLoading)
                }
            }
        }
    }
}
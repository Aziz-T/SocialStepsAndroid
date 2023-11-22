package com.hms.socialsteps.ui.visitedprofile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.hms.socialsteps.R
import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.core.util.showAlertDialog
import com.hms.socialsteps.data.model.Users
import com.hms.socialsteps.databinding.FragmentVisitedProfileBinding
import com.hms.socialsteps.ui.MainViewModel
import com.hms.socialsteps.ui.base.BaseFragment
import com.hms.socialsteps.ui.friendrequest.FriendRequestViewModel
import com.hms.socialsteps.ui.statistics.ViewPagerAdapter
import com.hms.socialsteps.ui.visitedprofile.visiteduserprofileinformation.VisitedUserProfileFragment
import com.hms.socialsteps.ui.visitedprofile.visitedusertargetinformation.VisitedUserTargetFragment
import com.hms.socialsteps.utils.binding.viewBinding
import com.huawei.agconnect.auth.AGConnectAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VisitedProfileFragment : BaseFragment(R.layout.fragment_visited_profile) {

    private val binding by viewBinding(FragmentVisitedProfileBinding::bind)

    private val viewModel: VisitedProfileViewModel by viewModels()
    private val friendRequestViewModel: FriendRequestViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    private lateinit var adapter: ViewPagerAdapter

    private val cUser = AGConnectAuth.getInstance().currentUser
    private var visitedUserUID = ""
    private var visitedUserName: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ViewPagerAdapter(
            requireActivity().supportFragmentManager,
            requireActivity().lifecycle
        )
        adapter.addFragment(VisitedUserProfileFragment())
        adapter.addFragment(VisitedUserTargetFragment())

        binding.viewPager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.viewPager2.adapter = adapter

        val tabNameList = listOf(
            "User Information",
            "Targets"
        )

        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            tab.text = tabNameList[position]
        }.attach()

        visitedUserUID = mainViewModel.uId

        getUser(visitedUserUID)
//        initObservers()
//        initListeners()


        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }

    }

    private fun handleSuccess(user: Users) {
        /*checkIfSelfProfile(user)*/
        visitedUserName = user.username
        checkIfFriend()
    }

    private fun handleError(message: String) {
        requireContext().showAlertDialog("Error", message)
    }

    private fun getUser(userId: String) {
        viewModel.getUser(userId)
        viewModel.user.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Error -> handleError(result.message!!)
                is Resource.Loading -> {}
                is Resource.Success -> {
                    handleSuccess(result.data!!)
                }
                is Resource.Empty -> {}  //empty result is impossible
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

    private fun checkIfFriend() {
        viewModel.queryFriendShipStatus(cUser.uid, visitedUserUID)
    }
}
package com.hms.socialsteps.ui.statistics.friendstatistics

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.hms.socialsteps.R
import com.hms.socialsteps.core.util.*
import com.hms.socialsteps.core.util.Constants.CALORIE
import com.hms.socialsteps.core.util.Constants.STEP
import com.hms.socialsteps.data.model.Users
import com.hms.socialsteps.databinding.FragmentFriendStatisticsBinding
import com.hms.socialsteps.ui.base.BaseFragment
import com.hms.socialsteps.utils.binding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FriendStatisticsFragment : BaseFragment(R.layout.fragment_friend_statistics) {
    private val binding by viewBinding(FragmentFriendStatisticsBinding::bind)
    private val viewModel: FriendStatisticsViewModel by activityViewModels()

    @Inject
    lateinit var userPreference: UserPreference

    private  var friendStatisticsAdapter: FriendStatisticsAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObservers()
        viewModel.getUserFriends()
        friendStatisticsAdapter = FriendStatisticsAdapter()

        binding.rvRank.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = friendStatisticsAdapter
        }

        binding.btnSteps.setOnClickListener {
            if (viewModel.selectedFilter != STEP) {
                friendStatisticsAdapter?.dataType = STEP
                viewModel.selectedFilter = STEP
                viewModel.userList.value?.data?.sortByDescending {
                    it.dailyStepsCount.toInt()
                }
                friendStatisticsAdapter?.differ?.submitList(viewModel.userList.value?.data)
                friendStatisticsAdapter?.notifyDataSetChanged()
            }
            binding.apply {
                btnSteps.background =
                    AppCompatResources.getDrawable(requireContext(), R.drawable.step_selected)
                btnSteps.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                btnCalories.background =
                    AppCompatResources.getDrawable(requireContext(), R.drawable.step_unselected)
                btnCalories.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }
        }

        binding.btnCalories.setOnClickListener {
            if (viewModel.selectedFilter != CALORIE) {
                friendStatisticsAdapter?.dataType = CALORIE
                viewModel.selectedFilter = CALORIE
                viewModel.userList.value?.data?.sortByDescending {
                    it.caloriesBurned.toInt()
                }
                friendStatisticsAdapter?.differ?.submitList(viewModel.userList.value?.data)
                friendStatisticsAdapter?.notifyDataSetChanged()
            }
            binding.apply {
                btnCalories.background =
                    AppCompatResources.getDrawable(requireContext(), R.drawable.calorie_selected)
                btnCalories.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                btnSteps.background =
                    AppCompatResources.getDrawable(requireContext(), R.drawable.calorie_unselected)
                btnSteps.setTextColor(ContextCompat.getColor(requireContext(), R.color.calorie_red))
            }
        }
    }



    private fun initObservers() {
        viewModel.friendList.observe(viewLifecycleOwner) { friendList ->
            when (friendList) {
                is Resource.Loading -> {
                    showProgressBar(binding.progressBar)
                }
                is Resource.Empty -> {
                    hideProgressBar(binding.progressBar)
                }
                is Resource.Error -> {
                    hideProgressBar(binding.progressBar)
                   requireContext().showAlertDialog( "Error", friendList.message)
                }
                is Resource.Success -> {
                    friendList.data?.let { viewModel.getFriendsStatistics(it) }
                }
            }
        }

        viewModel.userList.observe(viewLifecycleOwner) { userList ->
            when (userList) {
                is Resource.Loading -> {
                    showProgressBar(binding.progressBar)
                }
                is Resource.Empty -> {
                    hideProgressBar(binding.progressBar)
                   requireContext().showAlertDialog( "Error", userList.message)
                }
                is Resource.Error -> {
                    hideProgressBar(binding.progressBar)
                   requireContext().showAlertDialog( "Error", userList.message)
                }
                is Resource.Success -> {
                    val localUserList = arrayListOf<Users>()
                    userList.data?.let { localUserList.addAll(it) }
                    val user = userPreference.getStoredUser()

                    if (user.lastDailyStepsUpdatedTime != null) {
                        if (getCurrentDate().substring(0, 9) != user.lastDailyStepsUpdatedTime.substring(0, 9)) {
                            user.dailyStepsCount = "0"
                        }
                    } else{
                        user.dailyStepsCount = "0"
                    }
                    if (user.lastDailyCaloriesUpdatedTime != null) {
                        if (getCurrentDate().substring(0, 9) != user.lastDailyCaloriesUpdatedTime.substring(0, 9)) {
                            user.caloriesBurned = "0"
                        }
                    } else{
                        user.caloriesBurned = "0"
                    }

                    localUserList.add(user)
                    localUserList.sortByDescending {
                        it.dailyStepsCount.toInt()
                    }
                    friendStatisticsAdapter?.differ?.submitList(localUserList)
                    /*friendStatisticsAdapter?.notifyDataSetChanged()*/
                    hideProgressBar(binding.progressBar)
                }
            }
        }
    }
}
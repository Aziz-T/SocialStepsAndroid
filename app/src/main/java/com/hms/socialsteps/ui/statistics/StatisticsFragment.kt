package com.hms.socialsteps.ui.statistics

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.hms.socialsteps.R
import com.hms.socialsteps.databinding.FragmentStatisticsBinding
import com.hms.socialsteps.ui.base.BaseFragment
import com.hms.socialsteps.ui.statistics.friendstatistics.FriendStatisticsFragment
import com.hms.socialsteps.ui.statistics.friendstatistics.FriendStatisticsViewModel
import com.hms.socialsteps.ui.statistics.groupstatistics.GroupStatisticsFragment
import com.hms.socialsteps.utils.binding.viewBinding

class StatisticsFragment : BaseFragment(R.layout.fragment_statistics) {

    private val binding by viewBinding(FragmentStatisticsBinding::bind)
    private val friendStatisticViewModel: FriendStatisticsViewModel by activityViewModels()

    private lateinit var adapter: ViewPagerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ViewPagerAdapter(
            childFragmentManager,
            lifecycle
        )
        adapter.addFragment(FriendStatisticsFragment())
        adapter.addFragment(GroupStatisticsFragment())

        binding.viewPager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.viewPager2.adapter = adapter

        val tabNameList = listOf(
            "Friends",
            "Groups"
        )

        TabLayoutMediator(binding.tabLayout, binding.viewPager2){ tab, position ->
            tab.text = tabNameList[position]
        }.attach()

        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onPause() {
        super.onPause()
        friendStatisticViewModel.userList.value = null
    }

}
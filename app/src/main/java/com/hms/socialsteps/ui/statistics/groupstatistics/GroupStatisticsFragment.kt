package com.hms.socialsteps.ui.statistics.groupstatistics

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import android.widget.SearchView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.hms.socialsteps.R
import com.hms.socialsteps.core.util.*
import com.hms.socialsteps.data.model.*
import com.hms.socialsteps.databinding.FragmentGroupStatisticsBinding
import com.hms.socialsteps.databinding.GroupCreationViewBinding
import com.hms.socialsteps.databinding.MemberSelectionViewBinding
import com.hms.socialsteps.ui.MainViewModel
import com.hms.socialsteps.ui.base.BaseFragment
import com.hms.socialsteps.ui.statistics.groupstatistics.ExpandableGroupModel.Companion.CHILD
import com.hms.socialsteps.ui.statistics.groupstatistics.ExpandableGroupModel.Companion.PARENT
import com.hms.socialsteps.utils.binding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class GroupStatisticsFragment : BaseFragment(R.layout.fragment_group_statistics), GroupsExpendableAdapter.GroupStatisticsClickListener{
    val TAG = "GroupStatisticsFragment"

    private val binding by viewBinding(FragmentGroupStatisticsBinding::bind)
    private val viewModel: GroupStatisticsViewModel by viewModels()
    private val mainViewModel by activityViewModels<MainViewModel>()

    lateinit var adapter: GroupsExpendableAdapter
    private lateinit var groupList: List<Group>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initGroups()
        setClickListeners()
    }


    private fun initGroups() {
        viewModel.queryUserGroups()
        viewModel.usersGroupsResult.observe(viewLifecycleOwner){ result ->
            //Handle progressbar visibility
            if (result is Resource.Loading) showProgressBar(binding.progressBar)
            else hideProgressBar(binding.progressBar)
            //Handle no group text visibility
            if (result is Resource.Empty)
                binding.tvNoGroup.visibility = View.VISIBLE
            else
                binding.tvNoGroup.visibility = View.INVISIBLE

            if (result is Resource.Success)
                binding.recyclerView.visibility = View.VISIBLE
            else
                binding.recyclerView.visibility = View.INVISIBLE

            when(result){
                is Resource.Empty -> {}
                is Resource.Error -> {
                    showSnackBarShort(result.message!!)
                }
                is Resource.Loading -> {}
                is Resource.Success -> {
                    val data = result.data
                    if (data != null) {
                        groupList = result.data
                        val expandableGroupModelList = groupList.toExpandableGroupModelList()
                        adapter = GroupsExpendableAdapter(requireContext(),this)
                        adapter.setList(expandableGroupModelList)
                        binding.recyclerView.adapter = adapter
                        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
                    }

                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!this::adapter.isInitialized)
            adapter = GroupsExpendableAdapter(requireContext(),this)
    }

    private fun <T: List<Group>> T.toExpandableGroupModelList(): MutableList<ExpandableGroupModel> {
        val expandableGroupModelList = mutableListOf<ExpandableGroupModel>()
        this.forEach {
            expandableGroupModelList.add(ExpandableGroupModel(PARENT,it))
            var childRank = 0
            it.members.forEach { it1 ->
                childRank++
                expandableGroupModelList.add(ExpandableGroupModel(CHILD,it1,true,childRank,it))
            }
        }
        return expandableGroupModelList
    }

    private fun setClickListeners() {

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (it.isNotEmpty()){
                        if(this@GroupStatisticsFragment::adapter.isInitialized)
                            adapter.setList(searchList(query).toExpandableGroupModelList())

                }
                    else
                        if(this@GroupStatisticsFragment::adapter.isInitialized)
                            adapter.setList(groupList.toExpandableGroupModelList())
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    if (it.isEmpty()) {
                        if(this@GroupStatisticsFragment::adapter.isInitialized)
                            adapter.setList(groupList.toExpandableGroupModelList())
                    } else {
                        if(this@GroupStatisticsFragment::adapter.isInitialized) {
                            val result = searchList(it)
                            adapter.setList(result.toExpandableGroupModelList())
                        }
                    }
                }
                return false
            }

        })
    }

    private fun searchList(query:String): List<Group> {
        val filteredList = groupList.filter {
            it.groupInformation.groupName
                .lowercase(Locale.getDefault())
                .contains(query.lowercase())
        }
        return filteredList
    }


    override fun onTitleClicked(group: Group) {
        mainViewModel.setSelectedGroup(group)
        navigateTo(R.id.groupDetailFragment)
    }

    override fun onMemberClicked(users: Users) {
        navigateTo(R.id.visitedProfileFragment,
        Bundle().apply
         {
             putString("uid", users.uid)
         })
    }
}
package com.hms.socialsteps.ui.groups

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
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
import com.hms.socialsteps.data.model.Group
import com.hms.socialsteps.data.model.GroupInformation
import com.hms.socialsteps.data.model.GroupMembers
import com.hms.socialsteps.data.model.TargetType
import com.hms.socialsteps.databinding.FragmentGroupsBinding
import com.hms.socialsteps.databinding.GroupCreationViewBinding
import com.hms.socialsteps.databinding.MemberSelectionViewBinding
import com.hms.socialsteps.ui.MainViewModel
import com.hms.socialsteps.ui.base.BaseFragment
import com.hms.socialsteps.ui.statistics.groupstatistics.GroupMemberAdapter
import com.hms.socialsteps.ui.statistics.groupstatistics.GroupStatisticsViewModel
import com.hms.socialsteps.utils.binding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GroupsFragment : BaseFragment(R.layout.fragment_groups) {

    private val binding by viewBinding(FragmentGroupsBinding::bind)
    private val viewModel: GroupStatisticsViewModel by viewModels()
    private val mainViewModel by activityViewModels<MainViewModel>()

    private lateinit var informationAlertDialog: AlertDialog
    private lateinit var informationViewBinding: GroupCreationViewBinding

    private lateinit var memberAlertDialog: AlertDialog
    private lateinit var memberDialogViewBinding: MemberSelectionViewBinding

    private lateinit var groupsAdapter: GroupsAdapter
    private lateinit var groupMemberAdapter: GroupMemberAdapter

    private lateinit var groupInformation: GroupInformation

    private var checkedRadioButtonIndex = 0

    @Inject
    lateinit var userPreference: UserPreference

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        informationViewBinding = GroupCreationViewBinding.inflate(LayoutInflater.from(requireContext()))
        memberDialogViewBinding = MemberSelectionViewBinding.inflate(LayoutInflater.from(requireContext()))

        createAlertDialogs()
        setClickListeners()

        groupMemberAdapter = GroupMemberAdapter()
        memberDialogViewBinding.rvGroupMembers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groupMemberAdapter
        }

        informationViewBinding.rgTargetType.setOnCheckedChangeListener { radioGroup, i ->
            val radioButton = radioGroup.findViewById<RadioButton>(i)
            checkedRadioButtonIndex = radioGroup.indexOfChild(radioButton)
            setConstraints(radioGroup.indexOfChild(radioButton))
        }

        groupsAdapter = GroupsAdapter() { group ->
            mainViewModel.setSelectedGroup(group)
            navigateTo(R.id.groupDetailFragment)
        }

        binding.rvGroups.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groupsAdapter
        }

        initGroups()
        initObservers()
    }

    private fun clearCachedFields() {
        informationViewBinding.apply {
            etGroupName.setText("")
            rbSteps.isChecked = true
            etStepTarget.setText("")
            etCalorieTarget.setText("")
        }

        groupMemberAdapter.memberList.clear()
        memberDialogViewBinding.rvGroupMembers.adapter = null

    }

    private fun setClickListeners() {
        binding.fabCreateGroup.setOnClickListener {
            informationAlertDialog.show()
        }

        informationViewBinding.ibCloseAlertDialog.setOnClickListener {
            informationAlertDialog.dismiss()
            clearCachedFields()
        }

        memberDialogViewBinding.buttonBack.setOnClickListener {
            memberAlertDialog.dismiss()
            informationAlertDialog.show()
        }

        memberDialogViewBinding.ibCloseAlertDialog.setOnClickListener {
            clearCachedFields()
            memberAlertDialog.dismiss()
        }

        memberDialogViewBinding.btnCreateGroup.setOnClickListener {
            viewModel.queryGroup(groupInformation.groupID)
        }

        //Create Group click listener
        informationViewBinding.btnNext.setOnClickListener {
            informationViewBinding.apply {
                if (etGroupName.text.toString().isEmpty() ||
                    (checkedRadioButtonIndex == 0 && etStepTarget.text.toString().isEmpty()) ||
                    (checkedRadioButtonIndex == 1 && etCalorieTarget.text.toString().isEmpty()) ||
                    (checkedRadioButtonIndex == 2 && (etStepTarget.text.toString().isEmpty() ||
                            etCalorieTarget.text.toString().isEmpty() ))
                ) {
                    requireContext().showToastLong( "Please fill all fields")
                } else {
                    memberDialogViewBinding.rvGroupMembers.adapter = groupMemberAdapter
                    informationAlertDialog.dismiss()
                    viewModel.getUserFriends()
                    groupInformation = GroupInformation()
                    groupInformation.apply {
                        groupAdmin = userPreference.getStoredUser().uid
                        groupID = userPreference.getStoredUser().uid +
                                informationViewBinding.etGroupName.text.toString()
                        groupName = informationViewBinding.etGroupName.text.toString()
                        targetType = when (checkedRadioButtonIndex) {
                            0 -> {
                                TargetType.STEP.toString()
                            }
                            1 -> {
                                TargetType.CALORIE.toString()
                            }
                            2 -> {
                                TargetType.STEPCALORIE.toString()
                            }
                            else -> {
                                "null"
                            }
                        }
                        when (checkedRadioButtonIndex) {
                            0 -> {
                                stepTarget = etStepTarget.text.toString()
                            }
                            1 -> {
                                calorieTarget = etCalorieTarget.text.toString()
                            }
                            2 -> {
                                stepTarget = etStepTarget.text.toString()
                                calorieTarget = etCalorieTarget.text.toString()
                            }
                        }
                        isPrivate = false
                    }

                    groupMemberAdapter.groupId = groupInformation.groupID
                    memberAlertDialog.show()
                }
            }
        }

        binding.svGroups.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filter(newText)
                return false
            }
        })
    }

    private fun filter(filterText: String) {
        val filteredList = arrayListOf<Group>()
        if(viewModel.usersGroupsResult.value?.data==null) return

        for (group in viewModel.usersGroupsResult.value?.data!!) {
            if (group.groupInformation.groupName.toLowerCase().contains(filterText.toLowerCase())){
                filteredList.add(group)
            }

            groupsAdapter.differ.submitList(filteredList)
        }
    }

    private fun createAlertDialogs() {
        informationAlertDialog = BaseFragment().createInformationAlertDialog(informationViewBinding, requireContext())
        memberAlertDialog = BaseFragment().createMemberAlertDialog(memberDialogViewBinding, requireContext())
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
                binding.rvGroups.visibility = View.VISIBLE
            else
                binding.rvGroups.visibility = View.INVISIBLE

            when(result){
                is Resource.Empty -> {}
                is Resource.Error -> {
                    showSnackBarShort(result.message!!)
                }
                is Resource.Loading -> {}
                is Resource.Success -> {
                    val data = result.data
                    if (data != null) {
                        groupsAdapter.differ.submitList(data)
                    }
                }
            }
        }
    }

    private fun initObservers() {
        Log.e("TAG", "OBSERVE FONKSIYONA GIRDI")
        viewModel.friendList.observe(viewLifecycleOwner) { friendList ->
            when (friendList) {
                is Resource.Loading -> {
                    Log.e("TAG", "RESOURCEw Loading")
                    showProgressBar(memberDialogViewBinding.progressBar)
                }
                is Resource.Empty -> {
                    Log.e("TAG", "RESOURCEw Empty")
                    memberDialogViewBinding.customToolbar.visibility = View.VISIBLE
                    memberDialogViewBinding.emptyFriendAlert.visibility = View.VISIBLE
                    hideProgressBar(memberDialogViewBinding.progressBar)
                }
                is Resource.Error -> {
                    Log.e("TAG", "RESOURCEw Error")
                    hideProgressBar(memberDialogViewBinding.progressBar)
                    requireContext().showAlertDialog( "Error", friendList.message)
                }
                is Resource.Success -> {
                    Log.e("TAG", "RESOURCEw Success")
                    friendList.data?.let { viewModel.getFriendsStatistics(it) }
                }
            }
        }

        viewModel.userList.observe(viewLifecycleOwner) { userList ->
            when (userList) {
                is Resource.Loading -> {
                    Log.e("TAG", "RESOURCEw Loading")
                    showProgressBar(memberDialogViewBinding.progressBar)
                }
                is Resource.Empty -> {
                    Log.e("TAG", "RESOURCEw Empty")
                    hideProgressBar(memberDialogViewBinding.progressBar)
                    requireContext().showAlertDialog( "Error", userList.message)
                }
                is Resource.Error -> {
                    Log.e("TAG", "RESOURCEw Error")
                    hideProgressBar(memberDialogViewBinding.progressBar)
                    requireContext().showAlertDialog( "Error", userList.message)
                }
                is Resource.Success -> {
                    Log.e("TAG", "RESOURCEw SUCCESS")
                    groupMemberAdapter.differ.submitList(userList.data)
                    hideProgressBar(memberDialogViewBinding.progressBar)
                    memberDialogViewBinding.customToolbar.visibility = View.VISIBLE
                    memberDialogViewBinding.btnCreateGroup.visibility = View.VISIBLE
                }
            }
        }

        viewModel.groupList.observe(viewLifecycleOwner) { result ->
            when(result) {
                is Resource.Loading -> {
                    showProgressBar(memberDialogViewBinding.progressBar)
                }
                is Resource.Error -> {
                    hideProgressBar(memberDialogViewBinding.progressBar)
                    requireContext().showAlertDialog( "Error", result.message)
                }
                is Resource.Success -> {
                    hideProgressBar(memberDialogViewBinding.progressBar)
                    requireContext().showToastLong( "There is already a group with same name created by you. Please change group name")
                }
                is Resource.Empty -> {
                    if(this::groupInformation.isInitialized)
                        viewModel.upsertGroup(groupInformation)
                }
            }
        }

        viewModel.upsertGroupInfoResult.observe(viewLifecycleOwner) { result ->
            when(result) {
                is Resource.Loading -> {
                    showProgressBar(memberDialogViewBinding.progressBar)
                }
                is Resource.Error -> {
                    hideProgressBar(memberDialogViewBinding.progressBar)
                    requireContext().showAlertDialog( "Error", result.message)
                }
                is Resource.Success -> if (this::groupInformation.isInitialized){
                    val loggedUser = GroupMembers()
                    loggedUser.apply {
                        groupMembersID = userPreference.getStoredUser().uid + System.currentTimeMillis()
                        groupID = groupInformation.groupID
                        userID = userPreference.getStoredUser().uid
                    }
                    groupMemberAdapter.memberList.add(loggedUser)
                    viewModel.upsertGroupMembers(groupMemberAdapter.memberList)
                    viewModel.sendGroupNotification(groupMemberAdapter.memberList, groupInformation)
                }
                else -> {}
            }
        }

        viewModel.upsertGroupMembersResult.observe(viewLifecycleOwner) { result ->
            when(result) {
                is Resource.Loading -> {
                    showProgressBar(memberDialogViewBinding.progressBar)
                }
                is Resource.Error -> {
                    hideProgressBar(memberDialogViewBinding.progressBar)
                    requireContext().showAlertDialog( "Error", result.message)
                }
                is Resource.Success -> {
                    hideProgressBar(memberDialogViewBinding.progressBar)
                    requireContext().showToastLong( "Group Created Successfully")
                    memberAlertDialog.dismiss()
                    clearCachedFields()
                    viewModel.queryUserGroups()
                }
                else -> {}
            }
        }
    }

    private fun setConstraints(index: Int) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(informationViewBinding.parentConstraintLayout)
        when(index) {
            0 -> {
                constraintSet.connect(R.id.tvStepTarget, ConstraintSet.TOP,
                    R.id.rgTargetType, ConstraintSet.BOTTOM)
                constraintSet.connect(R.id.tvStepTarget, ConstraintSet.START,
                    R.id.tvGroupName, ConstraintSet.START)

                constraintSet.connect(R.id.btnNext, ConstraintSet.TOP,
                    R.id.etStepTarget, ConstraintSet.BOTTOM)
                constraintSet.applyTo(informationViewBinding.parentConstraintLayout)
                informationViewBinding.apply {
                    tvCalorieTarget.visibility = View.INVISIBLE
                    etCalorieTarget.visibility = View.INVISIBLE
                }
                makeStepTargetVisible()
            }

            1-> {
                constraintSet.connect(R.id.tvCalorieTarget, ConstraintSet.TOP,
                    R.id.rgTargetType, ConstraintSet.BOTTOM )
                constraintSet.connect(R.id.tvCalorieTarget, ConstraintSet.START,
                    R.id.tvGroupName, ConstraintSet.START)
                constraintSet.applyTo(informationViewBinding.parentConstraintLayout)
                informationViewBinding.apply {
                    tvStepTarget.visibility = View.INVISIBLE
                    etStepTarget.visibility = View.INVISIBLE
                }
                makeCalorieTargetVisible()
            }
            2 -> {
                constraintSet.connect(R.id.tvStepTarget, ConstraintSet.TOP,
                    R.id.rgTargetType, ConstraintSet.BOTTOM)
                constraintSet.connect(R.id.tvStepTarget, ConstraintSet.START,
                    R.id.tvGroupName, ConstraintSet.START)

                constraintSet.connect(R.id.tvCalorieTarget, ConstraintSet.TOP,
                    R.id.tvStepTarget, ConstraintSet.BOTTOM)
                constraintSet.connect(R.id.tvCalorieTarget, ConstraintSet.START,
                    R.id.tvGroupName, ConstraintSet.START)

                constraintSet.connect(R.id.btnNext, ConstraintSet.TOP,
                    R.id.etCalorieTarget, ConstraintSet.BOTTOM)
                constraintSet.applyTo(informationViewBinding.parentConstraintLayout)
                makeStepTargetVisible()
                makeCalorieTargetVisible()
            }
        }
    }

    private fun makeStepTargetVisible() {
        informationViewBinding.apply {
            tvStepTarget.visibility = View.VISIBLE
            etStepTarget.visibility = View.VISIBLE
        }
    }

    private fun makeCalorieTargetVisible() {
        informationViewBinding.apply {
            tvCalorieTarget.visibility = View.VISIBLE
            etCalorieTarget.visibility = View.VISIBLE
        }
    }
}
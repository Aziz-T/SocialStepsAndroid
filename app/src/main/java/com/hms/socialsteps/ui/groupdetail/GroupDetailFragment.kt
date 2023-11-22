package com.hms.socialsteps.ui.groupdetail

import android.app.AlertDialog
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.text.backgroundColor
import androidx.core.text.bold
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hms.socialsteps.R
import com.hms.socialsteps.core.util.*
import com.hms.socialsteps.data.model.Group
import com.hms.socialsteps.data.model.TargetType
import com.hms.socialsteps.data.model.Users
import com.hms.socialsteps.databinding.FragmentGroupDetailBinding
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
class GroupDetailFragment : BaseFragment(R.layout.fragment_group_detail) {

    private val binding by viewBinding(FragmentGroupDetailBinding::bind)
    private val viewModel by viewModels<GroupDetailViewModel>()
    private val mainViewModel by activityViewModels<MainViewModel>()
    private val groupStatisticsViewModel: GroupStatisticsViewModel by viewModels()

    private var mAdapter: GroupDetailAdapter? = null

    private lateinit var memberAlertDialog: AlertDialog
    private lateinit var memberDialogViewBinding: MemberSelectionViewBinding

    private lateinit var informationAlertDialog: AlertDialog
    private lateinit var informationViewBinding: GroupCreationViewBinding

    private lateinit var groupMemberAdapter: GroupMemberAdapter

    private lateinit var group: Group

    var isAllFabsVisible = false

    var checkedRadioButtonIndex = 0

    @Inject
    lateinit var userPreference: UserPreference

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        memberDialogViewBinding = MemberSelectionViewBinding.inflate(LayoutInflater.from(requireContext()))
        informationViewBinding = GroupCreationViewBinding.inflate(LayoutInflater.from(requireContext()))

        groupMemberAdapter = GroupMemberAdapter()
        memberDialogViewBinding.rvGroupMembers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groupMemberAdapter
        }

        if (mainViewModel.selectedGroup.value != null)
            group = mainViewModel.selectedGroup.value!!
        else
            findNavController().navigateUp()

        memberAlertDialog = BaseFragment().createMemberAlertDialog(memberDialogViewBinding, requireContext())
        editMemberDialogViewForUpdate()

        informationAlertDialog = BaseFragment().createInformationAlertDialog(informationViewBinding, requireContext())
        editInformationViewForUpdate()

        setInformationViewForCurrentGroup()

        setFabVisibilities()

        initClickListeners()
        initRecyclerView()
        initComponents()
        initObservers()
    }

    private fun editMemberDialogViewForUpdate() {
        memberDialogViewBinding.apply {
            buttonBack.visibility = View.INVISIBLE
            tvSelectGroupMembers.text = "Add New Members"
            btnCreateGroup.text = "Update"
        }
    }

    private fun editInformationViewForUpdate() {
        informationViewBinding.apply {
            btnNext.text = "Update"
            tvCreateGroup.text = "Update Group"
            etGroupName.setText(group.groupInformation.groupName.toString())

        }
    }

    private fun setInformationViewForCurrentGroup() {
        informationViewBinding.apply {
            if (group.groupInformation.targetType == TargetType.STEP.toString()) {
                etStepTarget.setText(group.groupInformation.stepTarget)
                rbSteps.isChecked = true
                checkedRadioButtonIndex = 0
            }else {
                etCalorieTarget.setText(group.groupInformation.calorieTarget)
                rbCalories.isChecked = true
                tvStepTarget.visibility = View.INVISIBLE
                etStepTarget.visibility = View.INVISIBLE
                tvCalorieTarget.visibility = View.VISIBLE
                etCalorieTarget.visibility = View.VISIBLE
                checkedRadioButtonIndex = 1
            }
        }
    }

    private fun setFabVisibilities() {
        binding.apply {
            fabAddMember.visibility = View.INVISIBLE
            fabEditGroupInformation.visibility = View.INVISIBLE
            extFabDetail.shrink()
        }
        if (group.groupInformation.groupAdmin != userPreference.getStoredUser().uid){
            binding.extFabDetail.visibility = View.GONE
        }
    }

    private fun clearCachedFields() {
        groupMemberAdapter.memberList.clear()
        memberDialogViewBinding.rvGroupMembers.adapter = null

        informationViewBinding.etGroupName.setText(group.groupInformation.groupName)
        informationViewBinding.etStepTarget.setText(group.groupInformation.stepTarget)
        informationViewBinding.etCalorieTarget.setText(group.groupInformation.calorieTarget)
        if (group.groupInformation.targetType == TargetType.STEP.toString()){
            informationViewBinding.rbSteps.isChecked = true
        }else {
            informationViewBinding.rbCalories.isChecked = true
        }
    }

    private fun initObservers() {
        groupStatisticsViewModel.friendList.observe(viewLifecycleOwner) { friendList ->
            when (friendList) {
                is Resource.Loading -> {
                    showProgressBar(memberDialogViewBinding.progressBar)
                }
                is Resource.Empty -> {
                    hideProgressBar(memberDialogViewBinding.progressBar)
                }
                is Resource.Error -> {
                    hideProgressBar(memberDialogViewBinding.progressBar)
                    requireContext().showAlertDialog("Error", friendList.message)
                }
                is Resource.Success -> {
                    friendList.data?.let { groupStatisticsViewModel.getFriendsStatistics(it) }
                }
            }
        }

        groupStatisticsViewModel.userList.observe(viewLifecycleOwner) { userList ->
            when (userList) {
                is Resource.Loading -> {
                    showProgressBar(memberDialogViewBinding.progressBar)
                }
                is Resource.Empty -> {
                    hideProgressBar(memberDialogViewBinding.progressBar)
                    requireContext().showAlertDialog("Error", userList.message)
                }
                is Resource.Error -> {
                    hideProgressBar(memberDialogViewBinding.progressBar)
                    requireContext().showAlertDialog("Error", userList.message)
                }
                is Resource.Success -> {
                    val userToRemove = arrayListOf<Users>()
                    userList.data?.forEach { user ->
                        group.members.forEach { member ->
                            if (user.uid == member.uid) {
                                userToRemove.add(user)
                            }
                        }
                    }
                    userList.data?.removeAll(userToRemove.toSet())
                    groupMemberAdapter.differ.submitList(userList.data)
                    hideProgressBar(memberDialogViewBinding.progressBar)
                    memberDialogViewBinding.customToolbar.visibility = View.VISIBLE
                    if (userList.data?.isEmpty() == true) {
                        memberDialogViewBinding.tvInformation.visibility = View.VISIBLE
                    } else {
                        memberDialogViewBinding.btnCreateGroup.visibility = View.VISIBLE
                    }
                }
            }
        }

        groupStatisticsViewModel.upsertGroupMembersResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    showProgressBar(memberDialogViewBinding.progressBar)
                }
                is Resource.Error -> {
                    hideProgressBar(memberDialogViewBinding.progressBar)
                    requireContext().showAlertDialog("Error", result.message)
                }
                is Resource.Success -> {
                    hideProgressBar(memberDialogViewBinding.progressBar)
                    requireContext().showToastLong("New Members Added Successfully")
                    var updatedGroupMembers = arrayListOf<Users>()
                    updatedGroupMembers.addAll(group.members)
                    groupStatisticsViewModel.userList.value?.data?.forEach { user ->
                        groupMemberAdapter.memberList.forEach { groupMembers ->
                            if (user.uid == groupMembers.userID) {
                                updatedGroupMembers.add(user)
                            }
                        }
                    }
                    if (group.groupInformation.targetType == TargetType.STEP.toString()) {
                        updatedGroupMembers.sortByDescending {
                            it.dailyStepsCount.toInt()
                        }
                    } else {
                        updatedGroupMembers.sortByDescending {
                            it.caloriesBurned.toInt()
                        }
                    }
                    group = Group(group.groupInformation, updatedGroupMembers)
                    memberAlertDialog.dismiss()
                    binding.recyclerView.adapter = null
                    mAdapter = GroupDetailAdapter()
                    mAdapter?.dataType = group.groupInformation.targetType
                    binding.recyclerView.adapter = mAdapter
                    mAdapter?.differ?.submitList(group.members)
                    groupStatisticsViewModel.sendGroupNotification(
                        groupMemberAdapter.memberList,
                        group.groupInformation
                    )

                    groupMemberAdapter = GroupMemberAdapter()
                    memberDialogViewBinding.rvGroupMembers.adapter = groupMemberAdapter
                }
                else -> {}
            }
        }

        groupStatisticsViewModel.upsertGroupInfoResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "Group information updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    hideProgressBar(informationViewBinding.progressBar)
                    informationAlertDialog.dismiss()
                    binding.tvGroupName.text = group.groupInformation.groupName
                    if (!binding.tvTargetType.text.toString().contains( group.groupInformation.targetType)) {
                        var updatedGroupMembers = arrayListOf<Users>()
                        updatedGroupMembers.addAll(group.members)

                        if (group.groupInformation.targetType == TargetType.STEP.toString()) {
                            updatedGroupMembers.sortByDescending {
                                it.dailyStepsCount.toInt()
                            }
                        } else {
                            updatedGroupMembers.sortByDescending {
                                it.caloriesBurned.toInt()
                            }
                        }

                        group = Group(group.groupInformation, updatedGroupMembers)
                        binding.recyclerView.adapter = null
                        mAdapter = GroupDetailAdapter()
                        mAdapter?.dataType = group.groupInformation.targetType
                        binding.recyclerView.adapter = mAdapter
                        mAdapter?.differ?.submitList(group.members)
                        binding.tvTargetType.text = SpannableStringBuilder().append("Target Type: ").bold {
                            backgroundColor(resources.getColor(R.color.calorie_red,resources.newTheme())){}
                            append(group.groupInformation.targetType)
                        }
                    }
                }
                is Resource.Loading -> {
                    showProgressBar(informationViewBinding.progressBar)
                }
                is Resource.Error -> {
                    requireContext().showAlertDialog("Error", result.message.toString())
                }
                else -> {}
            }
        }
    }

    private fun initComponents() {
        binding.tvGroupName.text = group.groupInformation.groupName
        binding.tvTargetType.text = SpannableStringBuilder().append("Target Type: ").bold {
            backgroundColor(resources.getColor(R.color.calorie_red,resources.newTheme())){}
            append(group.groupInformation.targetType)
        }
        // TODO: set imageview and description when added
    }

    private fun initRecyclerView() {
        mAdapter = GroupDetailAdapter()
        with(binding.recyclerView){
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
        mAdapter?.differ?.submitList(group.members)
        mAdapter?.dataType = group.groupInformation.targetType
    }

    private fun initClickListeners() {
        binding.buttonMore.setOnClickListener {
            val popup = PopupMenu(requireContext(),it)
            popup.setOnMenuItemClickListener(menuItemClickListener)
            val inflater: MenuInflater = popup.menuInflater
            inflater.inflate(R.menu.group_details_menu, popup.menu)
            popup.show()
        }

        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.fabAddMember.setOnClickListener {
            shrinkExtendedFab()
            memberDialogViewBinding.rvGroupMembers.adapter = groupMemberAdapter
            groupMemberAdapter.groupId = group.groupInformation.groupID
            memberAlertDialog.show()
            groupStatisticsViewModel.getUserFriends()
        }

        memberDialogViewBinding.ibCloseAlertDialog.setOnClickListener {
            clearCachedFields()
            memberAlertDialog.dismiss()
        }

        memberDialogViewBinding.btnCreateGroup.setOnClickListener {
            groupStatisticsViewModel.upsertGroupMembers(groupMemberAdapter.memberList)
        }

        binding.fabEditGroupInformation.setOnClickListener {
            shrinkExtendedFab()
            informationAlertDialog.show()
        }

        informationViewBinding.ibCloseAlertDialog.setOnClickListener {
            clearCachedFields()
            informationAlertDialog.dismiss()
        }

        informationViewBinding.btnNext.setOnClickListener {
            if (checkedRadioButtonIndex == 0){
                if (informationViewBinding.etStepTarget.text.isNullOrEmpty()){
                    requireContext().showToastShort("Please add step target")
                }
                else {
                    if (group.groupInformation.groupName == informationViewBinding.etGroupName.text.toString()
                        && group.groupInformation.targetType == TargetType.STEP.toString()
                        && group.groupInformation.stepTarget == informationViewBinding.etStepTarget.text.toString()){
                        requireContext().showToastShort("Please change value for update.")
                    }else {
                        group.groupInformation.groupName = informationViewBinding.etGroupName.text.toString()
                        group.groupInformation.targetType = TargetType.STEP.toString()
                        group.groupInformation.stepTarget = informationViewBinding.etStepTarget.text.toString()
                        groupStatisticsViewModel.upsertGroup(group.groupInformation)
                    }
                }
            }else {
                if (informationViewBinding.etCalorieTarget.text.isNullOrEmpty()){
                    requireContext().showToastShort("Please add calorie target")
                }
                else {
                    if (group.groupInformation.groupName == informationViewBinding.etGroupName.text.toString()
                        && group.groupInformation.targetType == TargetType.CALORIE.toString()
                        && group.groupInformation.calorieTarget == informationViewBinding.etCalorieTarget.text.toString()){
                        requireContext().showToastShort("Please change value for update.")
                    }else {
                        group.groupInformation.groupName = informationViewBinding.etGroupName.text.toString()
                        group.groupInformation.targetType = TargetType.CALORIE.toString()
                        group.groupInformation.calorieTarget = informationViewBinding.etCalorieTarget.text.toString()
                        groupStatisticsViewModel.upsertGroup(group.groupInformation)
                    }
                }
            }
        }
        informationViewBinding.rgTargetType.setOnCheckedChangeListener { radioGroup, i ->
            val radioButton = radioGroup.findViewById<RadioButton>(i)
            checkedRadioButtonIndex = radioGroup.indexOfChild(radioButton)
            if (checkedRadioButtonIndex == 0) {
                informationViewBinding.apply {
                    tvCalorieTarget.visibility = View.INVISIBLE
                    etCalorieTarget.visibility = View.INVISIBLE
                    tvStepTarget.visibility = View.VISIBLE
                    etStepTarget.visibility = View.VISIBLE
                }
            } else if(checkedRadioButtonIndex == 1) {
                informationViewBinding.apply {
                    tvCalorieTarget.visibility = View.VISIBLE
                    etCalorieTarget.visibility = View.VISIBLE
                    tvStepTarget.visibility = View.INVISIBLE
                    etStepTarget.visibility = View.INVISIBLE
                }
            }
        }

        binding.extFabDetail.setOnClickListener {
            if (!isAllFabsVisible){
                extendExtendedFab()
            } else {
                shrinkExtendedFab()
            }
        }
    }

    private fun shrinkExtendedFab() {
        binding.fabAddMember.hide()
        binding.fabEditGroupInformation.hide()
        binding.fabAddMember.visibility = View.INVISIBLE
        binding.fabEditGroupInformation.visibility = View.INVISIBLE

        binding.extFabDetail.shrink()

        isAllFabsVisible = false
    }

    private fun extendExtendedFab() {
        binding.fabAddMember.show()
        binding.fabEditGroupInformation.show()
        binding.fabAddMember.visibility = View.VISIBLE
        binding.fabEditGroupInformation.visibility = View.VISIBLE

        binding.extFabDetail.extend()

        isAllFabsVisible = true
    }

    //Top-right menu item click listener
    private val menuItemClickListener = PopupMenu.OnMenuItemClickListener {
        return@OnMenuItemClickListener when (it.itemId) {
            R.id.menu_leave -> {
                viewModel.leaveGroup(viewModel.getCurrentUser().uid,group.groupInformation.groupID)
                viewModel.leaveResult.observe(viewLifecycleOwner) { result ->
                    if (result is Resource.Loading) showProgressBar(binding.progressBar) else hideProgressBar(binding.progressBar)
                    when (result) {
                        is Resource.Empty -> {}
                        is Resource.Error -> {
                            result.message?.let { it1 -> showSnackBarShort(it1) }
                        }
                        is Resource.Loading -> {
                        }
                        is Resource.Success -> {
                            findNavController().navigateUp()
                        }
                    }
                }
                true
            }
            else -> {
                false
            }
        }
    }


}
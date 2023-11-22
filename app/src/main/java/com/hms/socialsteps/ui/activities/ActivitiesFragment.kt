package com.hms.socialsteps.ui.activities

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.hms.socialsteps.R
import com.hms.socialsteps.core.util.*
import com.hms.socialsteps.data.model.Activities
import com.hms.socialsteps.data.model.PostType
import com.hms.socialsteps.data.model.Posts
import com.hms.socialsteps.databinding.DialogIntakeCaloriesBinding
import com.hms.socialsteps.databinding.FragmentActivitiesBinding
import com.hms.socialsteps.ui.MainViewModel
import com.hms.socialsteps.ui.base.BaseFragment
import com.hms.socialsteps.ui.post.PostViewModel
import com.hms.socialsteps.utils.binding.viewBinding
import com.huawei.agconnect.auth.AGConnectAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ActivitiesFragment : BaseFragment(R.layout.fragment_activities),
    ActivityAdapter.ActivityOnClickListener {

    private val binding by viewBinding(FragmentActivitiesBinding::bind)
    private val viewModel by viewModels<ActivitiesViewModel>()

    private val mainViewModel: MainViewModel by activityViewModels()

    @Inject
    lateinit var userPreference: UserPreference

    private val postViewModel: PostViewModel by activityViewModels()

    private lateinit var dialogBinding: DialogIntakeCaloriesBinding
    private lateinit var dialog: Dialog

    private lateinit var activityAdapter: ActivityAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObservers()
        initRecyclerView()
        getActivities()
        initClickListeners()
        getCalories()
        getSteps()

        val user = userPreference.getStoredUser()
        if (user.lastDailyCaloriesUpdatedTime != null)
            binding.tvLastUpdatedCalorieTime.text = "Last updated time:  ${user.lastDailyCaloriesUpdatedTime}"
        if (user.lastDailyStepsUpdatedTime != null)
            binding.tvLastUpdatedStepsTime.text = "Last updated time:  ${user.lastDailyStepsUpdatedTime}"

        binding.swipeRefresh.setOnRefreshListener {
            showProgressBar(binding.progressBar)
            viewModel.getLatestActivities()
            viewModel.getDailyCaloriesFromHealthKit()
            viewModel.getDailyStepsFromHealthKit()
        }
    }

    private fun initObservers() {
        viewModel.mediatorLiveData.observe(viewLifecycleOwner) { result ->
            if (result) {
                hideProgressBar(binding.progressBar)
                binding.swipeRefresh.isRefreshing = false
            }
        }

        viewModel.isOldDataFetched.observe(viewLifecycleOwner) {
            if (it) {
                hideProgressBar(binding.progressBar)
            }
        }

        viewModel.dailyCalories.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Success -> {
                    getCalories()
                }
                is Resource.Error -> {
                    if (result.message == "50005") {
                        requireContext().showAlertDialog(
                            getString(R.string.authorization_error),
                            "To solve the error, please open your profile settings page and click \"Authorize App\" button. Then give necessary permissions."
                        )
                    } else {
                        requireContext().showAlertDialog(
                            getString(R.string.unknown_error),
                            "Result code:" + result.message
                        )
                    }
                }
                is Resource.Empty -> {
                    requireContext().showAlertDialog( getString(R.string.empty_data), getString(
                            R.string.no_calories_data_found
                        )
                    )
                }
                else -> {

                }
            }
        }

        viewModel.healthActivities.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Error -> {
                    if (result.message == "50005") {
                        requireContext().showAlertDialog(
                            getString(R.string.authorization_error),
                            "To solve the error, please open your profile settings page and click \"Authorize App\" button. Then give necessary permissions."
                        )
                    } else {
                        requireContext().showAlertDialog(
                            getString(R.string.unknown_error),
                            "Result code:" + result.message
                        )
                    }
                    binding.swipeRefresh.isRefreshing = false
                    hideProgressBar(binding.progressBar)
                }
                else -> {

                }
            }
        }

        viewModel.upsertResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Empty -> {
                    hideProgressBar(binding.progressBar)
                }
                is Resource.Error -> {
                    hideProgressBar(binding.progressBar)
                   requireContext().showAlertDialog( "Error", result.message.toString())
                    dialogBinding.tvErrorText.visibility = View.VISIBLE
                    dialogBinding.btnSaveCalories.isClickable = true
                    dialogBinding.pbLoading.visibility = View.INVISIBLE
                }
                is Resource.Loading -> {
                    showProgressBar(binding.progressBar)
                    dialogBinding.tvErrorText.visibility = View.INVISIBLE
                    dialogBinding.btnSaveCalories.isClickable = false
                    dialogBinding.pbLoading.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    hideProgressBar(binding.progressBar)
                    dialogBinding.tvErrorText.visibility = View.INVISIBLE
                    dialogBinding.btnSaveCalories.isClickable = true
                    dialogBinding.pbLoading.visibility = View.INVISIBLE
                    dialog.dismiss()
                    getCalories()
                    showSnackBarShort("Saved Successfully.")
                }
            }
        }
        viewModel.isDailyStepsFetched.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Error -> {
                    if (result.message == "50005") {
                        requireContext().showAlertDialog(
                            getString(R.string.authorization_error),
                            "To solve the error, please open your profile settings page and click \"Authorize App\" button. Then give necessary permissions."
                        )
                    } else {
                        requireContext().showAlertDialog(
                            getString(R.string.unknown_error),
                            "Result code:" + result.message
                        )
                    }
                    hideProgressBar(binding.progressBar)
                    binding.swipeRefresh.isRefreshing = false
                }
                is Resource.Success -> {
                    binding.tvStepCountTargetValue.text = userPreference.getStoredUser().targetSteps
                    binding.tvStepCountValue.text = userPreference.getStoredUser().dailyStepsCount
                    binding.cpiStepCount.progress = viewModel.calculateCpiProgress(
                        userPreference.getStoredUser().targetSteps.toInt(),
                        userPreference.getStoredUser().dailyStepsCount.toInt()
                    )
                    hideProgressBar(binding.progressBar)
                    binding.swipeRefresh.isRefreshing = false
                }
                is Resource.Loading -> {
                    showProgressBar(binding.progressBar)
                }
                is Resource.Empty -> {
                    hideProgressBar(binding.progressBar)
                    binding.swipeRefresh.isRefreshing = false
                    requireContext().showAlertDialog(
                        getString(R.string.empty_data),
                        getString(R.string.no_steps_data_found)
                    )
                }
            }
        }
    }

    private fun initClickListeners() {
        with(binding) {
            btnShowCalories.setOnClickListener {
                btnAddCalories.visibility = View.VISIBLE
                btnShowCalories.isClickable = false
                btnShowSteps.isClickable = true

                binding.viewFlipper.setInAnimation(
                    requireContext(),
                    R.anim.slide_in_left
                )
                binding.viewFlipper.setOutAnimation(
                    requireContext(),
                    R.anim.slide_out_left
                )
                // It shows previous item.
                viewFlipper.showNext()
            }

            btnShowSteps.setOnClickListener {
                btnAddCalories.visibility = View.INVISIBLE
                btnShowCalories.isClickable = true
                btnShowSteps.isClickable = false
                binding.viewFlipper.setOutAnimation(
                    requireContext(),
                    R.anim.slide_in_right
                )
                binding.viewFlipper.setInAnimation(
                    requireContext(),
                    R.anim.slide_out_right
                )
                // It shows next item.
                viewFlipper.showNext()
            }


            btnAddCalories.setOnClickListener {
                showDialog()
            }

            ibInfo.setOnClickListener {
                showInfoDialog()
            }

            ibshare.setOnClickListener {
                insertPost(createPost())
            }

        }
    }

    private fun createPost(): Posts {
        val user = userPreference.getStoredUser()
        val post = Posts()
        var postId = getCurrentDate().replace("/", "").replace(":", "").replace(" ", "")
        postId += AGConnectAuth.getInstance().currentUser.uid
        post.userId = AGConnectAuth.getInstance().currentUser.uid
        post.postId = postId.substring(0, 16)
        post.postType = PostType.CALORIES.id
        post.targetCalories = user.targetCalories
        post.reachedCalories =
            (user.caloriesIntake.toInt() - user.caloriesBurned.toInt()).toString()
        post.sharedDate = getCurrentDate()
        post.name = user.fullName
        post.username = user.username
        post.photoUrl = user.photo
        post.sharedDateMillis = System.currentTimeMillis().toString()
        return post
    }

    private fun showInfoDialog() {
        val alertDialog = AlertDialog.Builder(requireContext()).create()
        alertDialog.apply {
            setTitle(getString(R.string.calorie_circle_info_title))
            setMessage(getString(R.string.calorie_circle_info_text))
            setButton(
                AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok)
            ) { dialog, _ ->
                dialog.dismiss()
            }
            show()
        }
    }

    private fun showDialog() {
        dialog = Dialog(requireActivity())
        dialogBinding = DialogIntakeCaloriesBinding.inflate(layoutInflater)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(dialogBinding.root)
        dialogBinding.btnSaveCalories.setOnClickListener {
            if (dialogBinding.etIntakeCalories.text.toString().isEmpty()) {
                requireContext().showToastLong( "Please fill the calorie area")
            } else {
                insertUser(dialogBinding.etIntakeCalories.text.toString().toInt())
            }
        }
        dialog.show()
    }

    private fun insertUser(calories: Int) {
        val user = userPreference.getStoredUser()
        val takenCalories = user.caloriesIntake.toInt()
        user.caloriesIntake =
            (takenCalories + calories).toString()
        viewModel.upsertUser(user)

    }

    private fun insertPost(post: Posts) {
        val user = userPreference.getStoredUser()
        if ((user.weightPreference.equals("Gain"))) {
            if (!binding.tvCaloriesBurnedValue.text.isNullOrEmpty()) {
                if (user.targetCalories.toInt() > (binding.tvCaloriesIntakeValue.text.toString()
                        .toInt() - binding.tvCaloriesBurnedValue.text.toString()
                        .toInt())
                ) {
                    requireContext().showAlertDialog(
                        getString(R.string.warning),
                        getString(R.string.share_calorie_achievement_error_message)
                    )
                } else {
                    postViewModel.upsertPost(post)
                    postViewModel.upsertResult.observe(viewLifecycleOwner) { result ->
                        when (result) {
                            is Resource.Empty -> {

                            }
                            is Resource.Error -> {
                               requireContext().showAlertDialog( "Error", result.message)
                            }
                            is Resource.Loading -> {

                            }
                            is Resource.Success -> {
                                showSnackBarShort("Shared Successfully.")
                            }
                        }
                    }
                }
            }
        } else if (user.weightPreference.equals("Lose")) {
            if (user.targetCalories.toInt() < (binding.tvCaloriesIntakeValue.text.toString()
                    .toInt() - binding.tvCaloriesBurnedValue.text.toString()
                    .toInt())
            ) {
                requireContext().showAlertDialog(
                    getString(R.string.warning),
                    getString(R.string.share_calorie_achievement_error_message_high)
                )
            } else {
                if (user.targetCalories.toInt() -
                    (binding.tvCaloriesIntakeValue.text.toString().toInt() - binding.tvCaloriesBurnedValue.text.toString().toInt())
                    >500
                ){
                    requireContext().showAlertDialog(
                        getString(R.string.warning),
                        getString(R.string.share_calorie_achievement_error_message_low)
                    )
                } else {
                    postViewModel.upsertPost(post)
                    postViewModel.upsertResult.observe(viewLifecycleOwner) { result ->
                        when (result) {
                            is Resource.Empty -> {

                            }
                            is Resource.Error -> {
                               requireContext().showAlertDialog( "Error", result.message)
                            }
                            is Resource.Loading -> {

                            }
                            is Resource.Success -> {
                                showSnackBarShort("Shared Successfully.")
                            }
                        }
                    }
                }
            }
        }

    }

    private fun initRecyclerView() {
        activityAdapter = ActivityAdapter(this)
        binding.rvActivities.apply {
            adapter = activityAdapter
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun getCalories() {
        with(binding) {
            val user = userPreference.getStoredUser()
            tvCaloriesGoalValue.text = user.targetCalories?.toString() ?: ""
            tvCaloriesBurnedValue.text = user.caloriesBurned
            tvCaloriesIntakeValue.text = user.caloriesIntake
            tvCaloriesGoalTitle.visibility = View.VISIBLE
            tvCaloriesBurnedTitle.visibility = View.VISIBLE
            tvCaloriesIntakeTitle.visibility = View.VISIBLE
            if (user.caloriesBurned.toInt() > user.caloriesIntake.toInt()) {
                cpiCalories.progress = 0
            } else {
                cpiCalories.progress = viewModel.calculateCpiProgress(
                    user.targetCalories.toInt(),
                    user.caloriesIntake.toInt(),
                    user.caloriesBurned.toInt()
                )
            }
        }
    }

    private fun getSteps() {
        with(binding) {
            val user = userPreference.getStoredUser()
            tvStepCountTargetValue.text = user.targetSteps?.toString() ?: ""
            tvStepCountValue.text = user.dailyStepsCount

            cpiStepCount.progress = viewModel.calculateCpiProgress(
                user.targetSteps.toInt(),
                user.dailyStepsCount.toInt()
            )

        }
    }

    private fun getActivities() {
        val userId = AGConnectAuth.getInstance().currentUser.uid
        viewModel.getActivities(userId, null)
        viewModel.activities.observe(viewLifecycleOwner) { activities ->
            when (activities) {
                is Resource.Error -> {
                   requireContext().showAlertDialog( "Error", activities.message.toString())
                }
                is Resource.Loading -> {
                    showProgressBar(binding.progressBar)
                }
                is Resource.Success -> {
                    if (activities.data?.isEmpty() == true) {
                        binding.tvEmptyActivity.visibility = View.VISIBLE
                    } else {
                        binding.tvEmptyActivity.visibility = View.INVISIBLE
                    }
                    activityAdapter.differ.submitList(activities.data)
                    binding.layoutSuccess.visibility = View.VISIBLE
                }
                is Resource.Empty -> {
                    hideProgressBar(binding.progressBar)
                    binding.tvEmptyActivity.visibility = View.VISIBLE
                    binding.layoutSuccess.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onActivityClicked(activity: Activities) {
        mainViewModel.setActivity(activity)
        navigateTo(R.id.action_navActivities_to_activityDetailsFragment)
    }
}
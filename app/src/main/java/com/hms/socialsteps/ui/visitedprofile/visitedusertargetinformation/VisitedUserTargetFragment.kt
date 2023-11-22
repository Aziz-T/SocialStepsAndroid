package com.hms.socialsteps.ui.visitedprofile.visitedusertargetinformation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.hms.socialsteps.R
import com.hms.socialsteps.core.util.*
import com.hms.socialsteps.data.model.Users
import com.hms.socialsteps.databinding.FragmentVisitedUserTargetBinding
import com.hms.socialsteps.ui.MainViewModel
import com.hms.socialsteps.ui.base.BaseFragment
import com.hms.socialsteps.utils.binding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class VisitedUserTargetFragment : BaseFragment(R.layout.fragment_visited_user_target) {

    private val binding by viewBinding(FragmentVisitedUserTargetBinding::bind)
    private val viewModel by viewModels<VisitedUserTargetViewModel>()

    private val mainViewModel: MainViewModel by activityViewModels()

    @Inject
    lateinit var userPreference: UserPreference
    private var visitedUserUID = ""
    private var user = Users()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        visitedUserUID = mainViewModel.uId

        getUser(visitedUserUID)
        initClickListeners()

        binding.swipeRefresh.setOnRefreshListener {
            showProgressBar(binding.progressBar)
            getCaloriesAndSteps()
        }
    }

    private fun initClickListeners() {

        binding.btnShowCalories.isClickable = false
        binding.btnShowCalories.setOnClickListener {
            binding.btnShowCalories.isClickable = false
            binding.btnShowSteps.isClickable = true

            binding.viewFlipper.setInAnimation(
                requireContext(),
                R.anim.slide_in_left
            )
            binding.viewFlipper.setOutAnimation(
                requireContext(),
                R.anim.slide_out_left
            )
            // It shows previous item.
            binding.viewFlipper.showPrevious()
        }

        binding.btnShowSteps.setOnClickListener {
            binding.btnShowCalories.isClickable = true
            binding.btnShowSteps.isClickable = false
            binding.viewFlipper.setOutAnimation(
                requireContext(),
                R.anim.slide_in_right
            )
            binding.viewFlipper.setInAnimation(
                requireContext(),
                R.anim.slide_out_right
            )
            // It shows next item.
            binding.viewFlipper.showNext()
        }
    }

    private fun getUser(userId: String) {
        viewModel.getUser(userId)
        viewModel.user.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Error -> handleError(result.message!!)
                is Resource.Loading -> handleLoading()
                is Resource.Success -> {
                    user = result.data!!
                    getCaloriesAndSteps()
                }
                is Resource.Empty -> {}  //empty result is impossible
            }
        }
    }

    private fun handleLoading() {
        with(binding) {
            progressBar.visibility = View.VISIBLE
        }
    }

    private fun handleError(message: String) {
        requireContext().showAlertDialog("Error", message)
        with(binding) {
            progressBar.visibility = View.GONE
        }
    }

    private fun getCaloriesAndSteps() {

        binding.swipeRefresh.isRefreshing = false
        hideProgressBar(binding.progressBar)

        if (user.lastDailyCaloriesUpdatedTime != null)
            binding.tvLastUpdatedCalorieTime.text = "Last updated time:  ${user.lastDailyCaloriesUpdatedTime}"
        if (user.lastDailyStepsUpdatedTime != null)
            binding.tvLastUpdatedStepsTime.text = "Last updated time:  ${user.lastDailyStepsUpdatedTime}"


        with(binding) {
            tvCaloriesGoalValue.text = user.targetCalories ?: ""
            tvCaloriesBurnedValue.text = user.caloriesBurned
            tvCaloriesIntakeValue.text = user.caloriesIntake
            tvCaloriesGoalTitle.visibility = View.VISIBLE
            tvCaloriesBurnedTitle.visibility = View.VISIBLE
            tvCaloriesIntakeTitle.visibility = View.VISIBLE
            tvStepCountTargetValue.text = user.targetSteps
            tvStepCountValue.text = user.dailyStepsCount
            if (getCurrentDate().substring(0, 9) != user.lastDailyCaloriesUpdatedTime?.substring(0, 9)) {
                cpiCalories.progress = 0
                tvCaloriesBurnedValue.text = "0"
                tvCaloriesIntakeValue.text = "0"
            } else {
                if (user.caloriesBurned.toString()
                        .toInt() > user.caloriesIntake.toString().toInt()
                ) {
                    cpiCalories.progress = 0
                } else {
                    cpiCalories.progress = viewModel.calculateCpiProgress(
                        user.targetCalories.toString().toInt(),
                        user.caloriesIntake.toString().toInt(),
                        user.caloriesBurned.toString().toInt()
                    )
                }
            }
            if (getCurrentDate().substring(0, 9) != user.lastDailyStepsUpdatedTime?.substring(0, 9)) {
                cpiStepCount.progress = 0
                tvStepCountValue.text = "0"
            } else {
                binding.cpiStepCount.progress = viewModel.calculateCpiProgress(
                    user.targetSteps.toInt(),
                    user.dailyStepsCount.toInt()
                )
            }


        }
    }
}
package com.hms.socialsteps.ui.activitydetails

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hms.socialsteps.R
import com.hms.socialsteps.core.util.*
import com.hms.socialsteps.data.model.PostType
import com.hms.socialsteps.data.model.Posts
import com.hms.socialsteps.databinding.FragmentActivityDetailsBinding
import com.hms.socialsteps.ui.MainViewModel
import com.hms.socialsteps.ui.activities.ActivitiesViewModel
import com.hms.socialsteps.ui.base.BaseFragment
import com.hms.socialsteps.ui.post.PostViewModel
import com.hms.socialsteps.utils.binding.viewBinding
import com.huawei.agconnect.auth.AGConnectAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ActivityDetailsFragment : BaseFragment(R.layout.fragment_activity_details) {
    private val binding: FragmentActivityDetailsBinding by viewBinding(
        FragmentActivityDetailsBinding::bind
    )

    private val mainViewModel: MainViewModel by activityViewModels()

    private val postViewModel: PostViewModel by activityViewModels()

    private val activitiesViewModel by viewModels<ActivitiesViewModel>()

    private var activityId: String? = null

    @Inject
    lateinit var userPreference: UserPreference


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.parentConstraintLayout.visibility = View.INVISIBLE

        activityId = arguments?.getString("activityId")

        initData()
        initOnClickListeners()
    }

    private fun initData() {
        if (activityId == null) {
            mainViewModel.activity.observe(viewLifecycleOwner) { activity ->
                with(binding) {
                    tvBurntCaloriesValue.text = activity.calories.toString()
                    tvDurationValue.text = activity.duration
                    tvTotalDistanceValue.text = activity.totalDistance + "m"
                    tvTotalStepsValue.text = activity.totalStep
                    tvDateValue.text = activity.activityStartTime

                    when (activity.activityType) {
                        "walk" -> {
                            loadImage(R.drawable.walk, ivActivityType)
                        }
                        "run" -> {
                            loadImage(R.drawable.runner, ivActivityType)
                        }
                        else -> {}
                    }

                }
                binding.parentConstraintLayout.visibility = View.VISIBLE
            }
        } else {
            activitiesViewModel.getActivities(null, activityId)
            activitiesViewModel.activities.observe(viewLifecycleOwner) { activityList ->
                when(activityList){
                    is Resource.Loading -> {
                        showProgressBar(binding.progressBar)
                    }
                    is Resource.Error -> {
                       requireContext().showAlertDialog( "Error", activityList.message)
                        hideProgressBar(binding.progressBar)
                    }
                    is Resource.Success -> {
                        with(binding) {
                            tvBurntCaloriesValue.text = activityList.data!![0].calories.toString()
                            tvDurationValue.text = activityList.data[0].duration
                            tvTotalDistanceValue.text = activityList.data[0].totalDistance + "m"
                            tvTotalStepsValue.text = activityList.data[0].totalStep
                            tvDateValue.text = activityList.data[0].activityStartTime
                            btnShareActivity.visibility = View.INVISIBLE

                            when (activityList.data[0].activityType) {
                                "walk" -> {
                                    loadImage(R.drawable.walk, ivActivityType)
                                }
                                "run" -> {
                                    loadImage(R.drawable.runner, ivActivityType)
                                }
                                else -> {}
                            }

                            hideProgressBar(progressBar)
                            binding.parentConstraintLayout.visibility = View.VISIBLE
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    private fun initOnClickListeners() {
        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnShareActivity.setOnClickListener {
            insertPost(createPost())
        }

    }

    private fun createPost(): Posts {
        val user = userPreference.getStoredUser()
        val post = Posts()
        var postId = getCurrentDate().replace("/", "").replace(":", "").replace(" ", "")
        postId += AGConnectAuth.getInstance().currentUser.uid
        post.userId = AGConnectAuth.getInstance().currentUser.uid
        post.postId = postId.substring(0, 16)
        post.postType = PostType.ACTIVITY.id
        post.sharedDate = getCurrentDate()
        post.activityId = mainViewModel.activity.value?.activityId
        post.activityType = mainViewModel.activity.value?.activityType
        post.name = user.fullName
        post.username = user.username
        post.photoUrl = user.photo
        post.sharedDateMillis = System.currentTimeMillis().toString()
        return post
    }

    private fun insertPost(post: Posts) {
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
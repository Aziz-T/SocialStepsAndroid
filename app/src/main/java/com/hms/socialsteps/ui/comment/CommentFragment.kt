package com.hms.socialsteps.ui.comment

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hms.socialsteps.R
import com.hms.socialsteps.core.util.*
import com.hms.socialsteps.data.model.Comments
import com.hms.socialsteps.data.model.PostType
import com.hms.socialsteps.data.model.Posts
import com.hms.socialsteps.databinding.FragmentCommentBinding
import com.hms.socialsteps.ui.base.BaseFragment
import com.hms.socialsteps.utils.binding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class CommentFragment : BaseFragment(R.layout.fragment_comment) {
    private val binding by viewBinding(FragmentCommentBinding::bind)
    private val viewModel by viewModels<CommentViewModel>()
    private var commentAdapter:CommentAdapter? = null

    var commentList = mutableListOf<Comments>()
    @Inject
    lateinit var userPreference: UserPreference
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = requireArguments()
        val postJsonString = args.getString("post")
        if (postJsonString?.contains("userId") == true) {
            val post = Utils.getGsonParser().fromJson(postJsonString, Posts::class.java)
            loadImage(post.photoUrl, binding.ivProfile)
            binding.tvPostUserName.text = post.username
            binding.tvDate.text = post.sharedDate
            if (!post.activityType.isNullOrEmpty()){
                if (post.activityType == "walk") {
                    binding.tvPostDetail.text = requireContext().getString(R.string.i_did_a_walking_activity)
                }else {
                    binding.tvPostDetail.text = requireContext().getString(R.string.i_did_a_running_activity)
                }
            }else {
                if (post.postType == PostType.CALORIES.id) {
                    binding.postImage.background = null
                    binding.postImage.setImageResource(R.drawable.ic_burn_calories)
                    binding.tvPostDetail.text = getString(
                        R.string.calories_post_text,
                        post.targetCalories,
                        post.reachedCalories
                    )
                }else {
                    binding.postImage.background = null
                    binding.postImage.setImageResource(R.drawable.steps)
                    binding.tvPostDetail.text = getString(
                        R.string.steps_post_text,
                        post.targetSteps,
                        post.reachedSteps
                    )
                }
            }

            commentAdapter= CommentAdapter()
            binding.recyclerView.adapter=commentAdapter
            binding.recyclerView.layoutManager=LinearLayoutManager(requireContext())
            /*getComment(post.postId)*/
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.getSnapshotComments(post.postId)
            }
            initListeners(post.postId, post.userId)
        } else {
            initObserver(args.getString("post"))
        }
    }

    private fun initObserver(postId: String?) {
        viewModel.getPost(postId!!)
        viewModel.postList.observe(viewLifecycleOwner) { result ->
            when(result) {
                is Resource.Loading -> {

                }
                is Resource.Error -> {
                    hideProgressBar(binding.progressBar)
                   requireContext().showAlertDialog( "Error", result.message)
                }
                is Resource.Success -> {
                    val post = result.data!![0]
                    loadImage(post.photoUrl, binding.ivProfile)
                    binding.tvPostUserName.text = post.username
                    binding.tvDate.text = post.sharedDate
                    if (!post.activityType.isNullOrEmpty()){
                        if (post.activityType == "walk") {
                            binding.tvPostDetail.text = requireContext().getString(R.string.i_did_a_walking_activity)
                        }else {
                            binding.tvPostDetail.text = requireContext().getString(R.string.i_did_a_running_activity)
                        }
                    }else {
                        if (post.postType == PostType.CALORIES.id) {
                            binding.postImage.background = null
                            binding.postImage.setImageResource(R.drawable.ic_burn_calories)
                            binding.tvPostDetail.text = getString(
                                R.string.calories_post_text,
                                post.targetCalories,
                                post.reachedCalories
                            )
                        }else {
                            binding.postImage.background = null
                            binding.postImage.setImageResource(R.drawable.steps)
                            binding.tvPostDetail.text = getString(
                                R.string.steps_post_text,
                                post.targetSteps,
                                post.reachedSteps
                            )
                        }
                    }

                    commentAdapter= CommentAdapter()
                    binding.recyclerView.adapter=commentAdapter
                    binding.recyclerView.layoutManager=LinearLayoutManager(requireContext())
                    /*getComment(post.postId)*/
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.getSnapshotComments(post.postId)
                    }
                    initListeners(post.postId, post.userId)
                }
                else -> {}
            }
        }
    }

    private fun initListeners(postId: String, userId: String) {
        binding.btnPublishComment.setOnClickListener {
            if ( !binding.etUserComment.text.isNullOrEmpty()){
                viewModel.insertComment(binding.etUserComment.text.toString(),postId, userId)
                viewModel.insertResult.observe(viewLifecycleOwner){
                        result ->
                    binding.btnPublishComment.isClickable= result !is Resource.Loading
                    when (result){
                        is Resource.Empty -> {
                            hideProgressBar(binding.progressBar)
                        }
                        is Resource.Error -> {
                            showSnackBarShort("Error: ${result.message} ")
                        }
                        is Resource.Loading ->{

                        }
                        is Resource.Success -> {
                            /*viewModel.getComments(postId)*/
                            with(binding.etUserComment){
                                text.clear()

                            }
                            val inputManager: InputMethodManager =
                                requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                            inputManager.hideSoftInputFromWindow(requireView().windowToken, 0)
                        }


                    }
                }
            }
        }
        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }


        viewModel.commentList.observe(viewLifecycleOwner) { newCommentList ->
            when(newCommentList) {
                is Resource.Success -> {
                    commentList.addAll(newCommentList.data!!.toList())
                    commentAdapter!!.differ.submitList(commentList)
                    commentAdapter!!.notifyDataSetChanged()
                    hideProgressBar(binding.progressBar)
                    binding.parentCL.visibility = View.VISIBLE
                }

                is Resource.Error -> {
                    hideProgressBar(binding.progressBar)
                    binding.parentCL.visibility = View.VISIBLE
                   requireContext().showAlertDialog( "Error", newCommentList.message)
                }
                else -> {

                }
            }
        }
    }
}
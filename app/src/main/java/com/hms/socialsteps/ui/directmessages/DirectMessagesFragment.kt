package com.hms.socialsteps.ui.directmessages

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.hms.socialsteps.R
import com.hms.socialsteps.core.util.*
import com.hms.socialsteps.data.model.MessageItemEditFields
import com.hms.socialsteps.data.model.Users
import com.hms.socialsteps.databinding.FragmentDirectMessagesBinding
import com.hms.socialsteps.ui.base.BaseFragment
import com.hms.socialsteps.utils.binding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DirectMessagesFragment : BaseFragment(R.layout.fragment_direct_messages), DirectMessagesAdapter.DirectMessageClickListener {

    private val binding by viewBinding(FragmentDirectMessagesBinding::bind)
    private val viewModel by viewModels<DirectMessagesViewModel>()

    private lateinit var adapter: DirectMessagesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initLastMessages()
        initListeners()
    }

    private fun initListeners() {
        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.buttonNew.setOnClickListener {
            navigateTo(R.id.newChatFragment)
        }
    }

    private fun initLastMessages() {
        adapter = DirectMessagesAdapter(this)
        binding.recyclerView.adapter = adapter
        viewModel.getLastMessages()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.lastMessages.collect{ result ->
                    if (result is Resource.Loading) showProgressBar(binding.progressBar) else hideProgressBar(binding.progressBar)
                    when(result){
                        is Resource.Empty -> adapter.differ.submitList(emptyList())
                        is Resource.Error -> showSnackBarShort(result.message!!)
                        is Resource.Loading -> {}
                        is Resource.Success -> {adapter.differ.submitList(result.data)
                            adapter.notifyDataSetChanged()}
                    }
                }
            }
        }
    }

    override fun onItemClicked(users: Users) {
        val bundle = Bundle().apply {
            putString(MessageItemEditFields.RECEIVER_ID,users.uid)
        }
        navigateTo(R.id.action_directMessagesFragment_to_messagingScreenFragment, bundle)
    }
}
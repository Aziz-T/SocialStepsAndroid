package com.hms.socialsteps.ui.messagingscreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hms.socialsteps.R
import com.hms.socialsteps.core.util.*
import com.hms.socialsteps.data.model.MessageItem
import com.hms.socialsteps.data.model.MessageItemEditFields
import com.hms.socialsteps.data.model.Users
import com.hms.socialsteps.databinding.FragmentMessagingScreenBinding
import com.hms.socialsteps.ui.base.BaseFragment
import com.hms.socialsteps.utils.binding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class MessagingScreenFragment : BaseFragment(R.layout.fragment_messaging_screen),
    MessagingScreenAdapter.MessagingScreenClickListener {

    private val binding by viewBinding(FragmentMessagingScreenBinding::bind)
    private val viewModel by viewModels<MessagingScreenViewModel>()

    private lateinit var receiverId: String
    private lateinit var adapter: MessagingScreenAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        receiverId = requireArguments().getString(MessageItemEditFields.RECEIVER_ID).toString()
        initMessages(receiverId)
        initListeners(receiverId)
    }

    private fun setViews(users: Users) {
        requireContext().loadImage(users.photo,binding.ivProfile)
        binding.tvUsername.text = users.username
    }

    private fun initListeners(receiverId: String) {
        binding.buttonSend.setOnClickListener {
            if (binding.etMessage.text.isNotBlank()){
                val messagingId = getMessagingId(viewModel.getUserId(), receiverId)
                val messageItem = MessageItem().apply {
                    messageId = UUID.randomUUID().toString()
                    senderId = viewModel.getUserId()
                    this.receiverId = receiverId
                    text = binding.etMessage.text.toString()
                    type = "text" //only text supported for now
                    sendTime = System.currentTimeMillis().toString()
                    this.messagingId = messagingId
                    additionalData = ""
                }
                viewModel.sendMessage(messageItem)
                binding.etMessage.text.clear()
            }
        }
        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun initMessages(receiverId: String) {
        adapter = MessagingScreenAdapter(receiverId,this)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
        viewModel.getMessages(receiverId)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.messages.collect{ result ->
                    if (result is Resource.Loading) showProgressBar(binding.progressBar) else hideProgressBar(binding.progressBar)
                    when(result){
                        is Resource.Empty -> {adapter.differ.submitList(emptyList())}
                        is Resource.Error -> {
                            showSnackBarShort(result.message!!)
                        }
                        is Resource.Loading -> {}
                        is Resource.Success -> {
                            result.data!!.userList.forEach {
                                if (it.uid == receiverId)
                                    setViews(it)
                            }
                            adapter.setUsers(result.data!!.userList)
                            adapter.differ.submitList(result.data.messages.reversed())
                            adapter.notifyDataSetChanged()

                            binding.recyclerView.postDelayed({
                                    binding.recyclerView.smoothScrollToPosition(0)
                                }, 200)
                        }
                    }
                }
            }
        }
    }

    override fun onItemClicked(users: Users) {
        val bundle = Bundle().apply {
            putString("uid", users.uid)
        }
        findNavController().navigate(R.id.visitedProfileFragment, bundle)
    }
}
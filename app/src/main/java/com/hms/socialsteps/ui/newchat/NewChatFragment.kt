package com.hms.socialsteps.ui.newchat


import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hms.socialsteps.R
import com.hms.socialsteps.core.util.*
import com.hms.socialsteps.data.model.MessageItemEditFields
import com.hms.socialsteps.data.model.Users
import com.hms.socialsteps.databinding.FragmentNewChatBinding
import com.hms.socialsteps.ui.base.BaseFragment
import com.hms.socialsteps.ui.search.SearchUserAdapter
import com.hms.socialsteps.utils.binding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class NewChatFragment : BaseFragment(R.layout.fragment_new_chat),
    SearchUserAdapter.AdapterOnClickListener {

    private val binding by viewBinding(FragmentNewChatBinding::bind)
    private val viewModel by viewModels<NewChatViewModel>()
    private var userList: ArrayList<Users> = arrayListOf()
    private lateinit var searchUserAdapter: SearchUserAdapter

    @Inject
    lateinit var userPreference: UserPreference

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initClickListeners()

        viewModel.getUserList()
        observeData()
        searchUserAdapter = SearchUserAdapter(requireContext(), this)
        binding.rvSearch.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchUserAdapter
        }


        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (it.length > 2)
                        searchUserAdapter.items = searchList(it)
                    else
                        searchUserAdapter.items = arrayListOf()
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    if (it.isEmpty()) {
                        binding.tvSearchInfo.visibility = View.VISIBLE
                        binding.tvNoResult.visibility = View.GONE
                        searchUserAdapter.items = arrayListOf()
                    } else {
                        binding.tvSearchInfo.visibility = View.GONE
                        val result = searchList(it)
                        if (result.size > 0 || it.isEmpty())
                            binding.tvNoResult.visibility = View.GONE
                        else
                            binding.tvNoResult.visibility = View.VISIBLE

                        searchUserAdapter.items = searchList(it)
                    }
                }
                return false
            }

        })
    }

    private fun initClickListeners() {
        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }


    private fun observeData() {
        viewModel.userListLiveData.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Empty -> {
                    hideProgressBar(binding.progressBar)
                }
                is Resource.Error -> {
                    hideProgressBar(binding.progressBar)
                    requireContext().showAlertDialog("Error", result.message.toString())
                }
                is Resource.Loading -> {
                    showProgressBar(binding.progressBar)
                }
                is Resource.Success -> {
                    hideProgressBar(binding.progressBar)
                    userList = result.data!!
                    binding.searchView.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun searchList(query: String): ArrayList<Users> {
        val filteredList = userList.filter {
            it.username.lowercase(Locale.getDefault()).contains(
                query.lowercase(
                    Locale.getDefault()
                )
            )
        } as ArrayList<Users>
        return filteredList
    }

    override fun itemClicked(uid: String) {
        if (uid == userPreference.getStoredUser().uid) {

        }else {
            val bundle = Bundle()
            bundle.putString(MessageItemEditFields.RECEIVER_ID, uid)
            navigateTo(R.id.messagingScreenFragment, bundle)
        }
    }

}
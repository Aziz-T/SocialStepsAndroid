package com.hms.socialsteps.ui.search

import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.hms.socialsteps.R
import com.hms.socialsteps.core.util.*
import com.hms.socialsteps.data.model.Users
import com.hms.socialsteps.databinding.FragmentSearchBinding
import com.hms.socialsteps.ui.MainViewModel
import com.hms.socialsteps.ui.base.BaseFragment
import com.hms.socialsteps.utils.binding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : BaseFragment(R.layout.fragment_search),
    SearchUserAdapter.AdapterOnClickListener {

    private val binding by viewBinding(FragmentSearchBinding::bind)
    private val viewModel: SearchViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private var userList: ArrayList<Users> = arrayListOf()
    private lateinit var searchUserAdapter: SearchUserAdapter

    @Inject
    lateinit var userPreference: UserPreference

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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


    private fun observeData() {
        viewModel.userListLiveData.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Empty -> {
                    hideProgressBar(binding.progressBar)
                    // TODO: Show no results text
                }
                is Resource.Error -> {
                    hideProgressBar(binding.progressBar)
                   requireContext().showAlertDialog( "Error", result.message.toString())
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
            navigateTo(R.id.action_navSearch_to_navProfile)
        } else {
            val bundle = Bundle()
            bundle.putString("uid", uid)
            mainViewModel.uId = uid
            navigateTo(R.id.action_navSearch_to_visitedProfileFragment, bundle)
        }
    }

}
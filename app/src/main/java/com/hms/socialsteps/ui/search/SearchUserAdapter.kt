package com.hms.socialsteps.ui.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hms.socialsteps.R
import com.hms.socialsteps.core.util.loadImage
import com.hms.socialsteps.data.model.Users
import com.hms.socialsteps.databinding.CardSearchUserBinding
import com.hms.socialsteps.utils.binding.AutoUpdatableAdapter
import kotlin.properties.Delegates

class SearchUserAdapter constructor(private val context: Context,
                                    private val clickListener: AdapterOnClickListener):
    RecyclerView.Adapter<SearchUserAdapter.SearchUserViewHolder>(),AutoUpdatableAdapter  {
    private lateinit var itemBinding: CardSearchUserBinding

    var items: List<Users> by Delegates.observable(emptyList()) {
            _, old, new ->
        autoNotify(old, new) { o, n -> o.uid == n.uid }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchUserViewHolder {
        itemBinding = CardSearchUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchUserViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: SearchUserViewHolder, position: Int) {
        holder.bind(user = items[position],context)
    }

    override fun getItemCount() = items.size

    inner class SearchUserViewHolder(private val itemBinding: CardSearchUserBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(user: Users, context: Context)  {
            itemBinding.apply {
                tvCardUsername.text = user.username
                tvCardFullname.text = context.resources.getString(R.string.fullname, user.fullName)
                tvCardAge.text = context.resources.getString(R.string.age, user.age.toString())
                context.loadImage(user.photo ?: "",ivSearchUser)

                root.setOnClickListener {
                    clickListener.itemClicked(user.uid)
                }
            }
        }
    }

    interface AdapterOnClickListener{
        fun itemClicked(uid: String)
    }
}
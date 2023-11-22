package com.hms.socialsteps.ui.statistics.groupstatistics

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hms.socialsteps.R
import com.hms.socialsteps.core.util.loadImage
import com.hms.socialsteps.data.model.Group
import com.hms.socialsteps.data.model.TargetType
import com.hms.socialsteps.data.model.Users
import com.hms.socialsteps.databinding.LayoutGroupStatisticsHeaderBinding
import com.hms.socialsteps.databinding.LayoutGroupStatisticsItemBinding

class GroupsExpendableAdapter(
    var context: Context,
    private val clickListener: GroupStatisticsClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var isFirstItemExpanded: Boolean = false
    private var actionLock = false

    private var groupModelList = mutableListOf<ExpandableGroupModel>()
    fun setList(list: MutableList<ExpandableGroupModel>) {
        groupModelList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ExpandableGroupModel.PARENT -> {
                GroupParentViewHolder(
                    LayoutGroupStatisticsHeaderBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            ExpandableGroupModel.CHILD -> {
                GroupChildViewHolder(
                    LayoutGroupStatisticsItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            else -> {
                GroupParentViewHolder(
                    LayoutGroupStatisticsHeaderBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun getItemCount(): Int = groupModelList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val row = groupModelList[position]
        when (row.type) {
            ExpandableGroupModel.PARENT -> {
                (holder as GroupParentViewHolder).groupName.text =
                    row.groupParent.groupInformation.groupName
                holder.targetName.text =
                    if (row.groupParent.groupInformation.targetType == TargetType.STEP.toString()) "STEP"
                    else if (row.groupParent.groupInformation.targetType == TargetType.CALORIE.toString()) "CALORIE"
                    else "STEP & CALORIE"
                if (row.isExpanded) holder.expandButton.setImageResource(R.drawable.ic_baseline_expand_less_24)
                else holder.expandButton.setImageResource(R.drawable.ic_baseline_expand_more_24)
                holder.expandButton.setOnClickListener {
                    if (row.isExpanded) {
                        row.isExpanded = false
                        collapseRow(position)
                        holder.expandButton.setImageResource(R.drawable.ic_baseline_expand_more_24)

                    } else {
                        row.isExpanded = true
                        holder.expandButton.setImageResource(R.drawable.ic_baseline_expand_less_24)
                        expandRow(position)
                    }
                }
                holder.binding.tvTitle.setOnClickListener {
                    clickListener.onTitleClicked(row.groupParent)
                }
            }


            ExpandableGroupModel.CHILD -> {
                (holder as GroupChildViewHolder).name.text = row.groupChild.username
                holder.progress.text =
                    if (row.groupParent.groupInformation.targetType == TargetType.STEP.toString())
                        row.groupChild.dailyStepsCount.toString()
                    else
                        row.groupChild.caloriesBurned.toString()

                holder.binding.root.context.loadImage(
                    row.groupChild.photo,
                    holder.binding.ivUserPhoto
                )
                holder.binding.tvRankNumber.text = row.childRank.toString()
                holder.binding.root.setOnClickListener {
                    clickListener.onMemberClicked(row.groupChild)
                }
            }
        }

    }


    override fun getItemViewType(position: Int): Int = groupModelList[position].type

    private fun expandRow(position: Int) {
        val row = groupModelList[position]
        var nextPosition = position
        when (row.type) {
            ExpandableGroupModel.PARENT -> {
                var childRank = 1
                for (child in row.groupParent.members) {
                    groupModelList.add(
                        ++nextPosition, ExpandableGroupModel(
                            ExpandableGroupModel.CHILD,
                            child, true, childRank = childRank, row.groupParent
                        )
                    )
                    childRank++
                }
                notifyDataSetChanged()
            }
            ExpandableGroupModel.CHILD -> {
                notifyDataSetChanged()
            }
        }
    }

    private fun collapseRow(position: Int) {
        val row = groupModelList[position]
        var nextPosition = position + 1
        when (row.type) {
            ExpandableGroupModel.PARENT -> {
                outerloop@ while (true) {
                    //  println("Next Position during Collapse $nextPosition size is ${shelfModelList.size} and parent is ${shelfModelList[nextPosition].type}")

                    if (nextPosition == groupModelList.size || groupModelList[nextPosition].type == ExpandableGroupModel.PARENT) {
                        /* println("Inside break $nextPosition and size is ${closedShelfModelList.size}")
                         closedShelfModelList[closedShelfModelList.size-1].isExpanded = false
                         println("Modified closedShelfModelList ${closedShelfModelList.size}")*/
                        break@outerloop
                    }

                    groupModelList.removeAt(nextPosition)
                }

                notifyDataSetChanged()
            }


        }
    }

    class GroupParentViewHolder(val binding: LayoutGroupStatisticsHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        internal var layout = binding.root
        internal var groupName: TextView = binding.tvTitle
        internal var targetName = binding.tvTargetType
        internal var expandButton = binding.buttonExpand

    }

    class GroupChildViewHolder(val binding: LayoutGroupStatisticsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        internal var layout = binding.root
        internal var name: TextView = binding.tvUsername
        internal var progress = binding.tvStepOrCalorieValue

    }

    interface GroupStatisticsClickListener {
        fun onTitleClicked(group: Group)
        fun onMemberClicked(users: Users)
    }
}
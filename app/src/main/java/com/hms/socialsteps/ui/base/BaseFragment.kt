package com.hms.socialsteps.ui.base

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.hms.socialsteps.data.model.FragmentsWithNoBottomBar
import com.hms.socialsteps.databinding.GroupCreationViewBinding
import com.hms.socialsteps.databinding.MemberSelectionViewBinding
import com.hms.socialsteps.ui.MainActivity
import timber.log.Timber

open class BaseFragment : Fragment {
    var baseContext: Context? = null

    constructor() : super()
    constructor(contentLayoutId: Int) : super(contentLayoutId)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        baseContext = context
        if (FragmentsWithNoBottomBar.fragmentsWithNoBottomBarList.contains(name())){
            (activity as MainActivity).hideBottomNavigation()
        }
    }

    override fun onDetach() {
        super.onDetach()
        if (FragmentsWithNoBottomBar.fragmentsWithNoBottomBarList.contains(name())) {
            (activity as MainActivity).showBottomNavigation()
        }
    }

    override fun onResume() {
        super.onResume()
        if (name() == "NotificationFragment" || name() == "StatisticsFragment") {
            (activity as MainActivity).hideBottomNavigation()
        }
        if(name() == "FeedFragment") {
            (activity as MainActivity).showBottomNavigation()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.v("LifeCycle", "${name()} onViewCreated")
    }


    private fun name(): String {
        return this.javaClass.simpleName
    }

    fun createMemberAlertDialog(memberDialogViewBinding: MemberSelectionViewBinding, context: Context):
    AlertDialog{
        val memberDialogBuilder = AlertDialog.Builder(context)
        memberDialogBuilder.setView(memberDialogViewBinding.root)
        val memberAlertDialog = memberDialogBuilder.create()
        memberAlertDialog.setCancelable(false)
        return memberAlertDialog
    }

    fun createInformationAlertDialog(informationViewBinding: GroupCreationViewBinding, context: Context):
            AlertDialog{
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setView(informationViewBinding.root)
        val informationAlertDialog = dialogBuilder.create()
        informationAlertDialog.setCancelable(false)
        return informationAlertDialog
    }
}
package com.hms.socialsteps.ui.register

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hms.socialsteps.R
import com.hms.socialsteps.core.util.*
import com.hms.socialsteps.data.model.Users
import com.hms.socialsteps.databinding.FragmentRegisterBinding
import com.hms.socialsteps.ui.base.BaseFragment
import com.hms.socialsteps.utils.binding.viewBinding
import com.huawei.agconnect.auth.AGConnectAuth
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class RegisterFragment : BaseFragment(R.layout.fragment_register) {
    val TAG = "RegisterFragment"

    private val viewModel by viewModels<RegisterViewModel>()
    private val binding by viewBinding(FragmentRegisterBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initOnClickListeners()
        initGenderSpinner()
        initWeightSpinner()
    }

    private fun initOnClickListeners() {
        binding.buttonDone.setOnClickListener {
            setUserProfileInfo()
        }
        binding.tvChangeProfilePhoto.setOnClickListener {
            Timber.tag(TAG).d( "initOnClickListeners: This function is not available.")
        }
        binding.btnPermissionInfo.setOnClickListener {
           requireContext().showAlertDialog( null, getString(R.string.share_permission_info))
        }
    }

    private fun initWeightSpinner() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.weight_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerGainOrLoseWeight.adapter = adapter
        }
    }

    private fun initGenderSpinner() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.gender_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerGender.adapter = adapter
        }
    }

    private fun setUserProfileInfo() {
        val user = Users()
        with(binding) {
            if (etFullName.text.toString().isNullOrEmpty() || etUsername.text.toString()
                    .isNullOrEmpty() ||
                etAge.text.toString().isNullOrEmpty() || etHeight.text.toString()
                    .isNullOrEmpty() || etWeight.text.toString()
                    .isNullOrEmpty() || etTargetSteps.text.toString().isNullOrEmpty()
            ) {
                requireContext().showToastLong( "Please fill the empty areas")
            } else {
                user.uid = AGConnectAuth.getInstance().currentUser.uid
                user.fullName = etFullName.text.toString()
                user.username = etUsername.text.toString()
                user.age = etAge.text.toString().toInt()
                user.height = etHeight.text.toString().toInt()
                user.weight = etWeight.text.toString().toInt()
                user.gender = spinnerGender.selectedItem.toString()
                user.weightPreference = spinnerGainOrLoseWeight.selectedItem.toString()
                user.targetSteps = etTargetSteps.text.toString()
                user.exerciseFrequency = setUserExerciseFrequency()
                user.dailyStepsCount = 0.toString()
                user.targetCalories = viewModel.calculateTargetCalories(
                    user.gender,
                    user.weight,
                    user.height,
                    user.age,
                    rgExerciseFrequency.checkedRadioButtonId,
                    spinnerGainOrLoseWeight.selectedItem.toString()
                )
                user.permissionToShareInfo = binding.permissionSwitch.isChecked
                user.lastUpdatedTime = getCurrentDate()
            }
        }
        viewModel.registerUser(user)
        viewModel.registerResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Empty -> {
                    hideProgressBar(binding.progressBar)
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
                    requireContext().showToastShort(
                        requireContext().resources.getString(R.string.register_success)
                    )
                    findNavController().navigate(R.id.navFeed)
                }
            }
        }
    }

    private fun setUserExerciseFrequency(): String {
        when (binding.rgExerciseFrequency.checkedRadioButtonId) {
            binding.rbSedentary.id -> {
                return binding.rbSedentary.text.toString()
            }
            binding.rbLightlyActive.id -> {
                return binding.rbLightlyActive.text.toString()
            }
            binding.rbModeratelyActive.id -> {
                return binding.rbModeratelyActive.text.toString()
            }
            binding.rbVeryActive.id -> {
                return binding.rbVeryActive.text.toString()
            }
            binding.rbExtraActive.id -> {
                return binding.rbExtraActive.text.toString()
            }
            else -> return ""
        }
    }
}
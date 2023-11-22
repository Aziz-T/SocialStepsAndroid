package com.hms.socialsteps.ui.profile.editprofile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.hms.socialsteps.R
import com.hms.socialsteps.core.util.*
import com.hms.socialsteps.data.model.Users
import com.hms.socialsteps.databinding.FragmentEditProfileBinding
import com.hms.socialsteps.ui.base.BaseFragment
import com.hms.socialsteps.ui.register.RegisterViewModel
import com.hms.socialsteps.utils.binding.viewBinding
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.hms.hihealth.SettingController
import com.huawei.hms.hihealth.data.Scopes
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class EditProfileFragment : BaseFragment(R.layout.fragment_edit_profile) {
    @Inject
    lateinit var mSettingsController: SettingController
    val TAG = "EditProfileFragment"

    @Inject
    lateinit var userPreference: UserPreference

    private val binding by viewBinding(FragmentEditProfileBinding::bind)
    private val viewModel by viewModels<EditProfileViewModel>()
    private val registerViewModel by viewModels<RegisterViewModel>()

    private var user: Users? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getUserData()
        initOnClickListeners()
        initWeightSpinner()
        initSpinner()
        observeData()
    }

    private fun getUserData() {
        val userId = AGConnectAuth.getInstance().currentUser.uid
        viewModel.getUser(userId)
        viewModel.user.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Empty -> {}
                is Resource.Error -> {
                    hideProgressBar(binding.pb)
                    showSnackBarShort("Data could not get! Please try again.")
                    Timber.tag(TAG).e( "getUserData: Error! ${result.message}")
                }
                is Resource.Loading -> {
                    showProgressBar(binding.pb)
                }
                is Resource.Success -> {
                    hideProgressBar(binding.pb)
                    user = result.data!!
                    showUserProfileInfo(result.data)
                }
            }
        }
    }

    private fun initOnClickListeners() {
        binding.buttonDone.setOnClickListener {
            requestAuth()
            setUserProfileInfo()
        }
        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.tvChangeProfilePhoto.setOnClickListener {
            pickMedia()
        }

//        binding.btnLogOut.setOnClickListener {
//            AGConnectAuth.getInstance().signOut()
//        }
            binding.btnObtainUserAuthorization.setOnClickListener {
            requestAuth()
        }
    }

    private fun requestAuth() {
        val scopes = arrayOf( // View and store step count data in Health Kit.
            Scopes.HEALTHKIT_STEP_READ,
            Scopes.HEALTHKIT_CALORIES_READ,
            Scopes.HEALTHKIT_ACTIVITY_RECORD_READ
        )

        val intent = mSettingsController.requestAuthorizationIntent(scopes, true)
        startActivityForResult(intent, Constants.REQUEST_HEALTH_AUTH)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_HEALTH_AUTH) {
            viewModel.requestHealthAuth(data!!, mSettingsController)
        }
    }

    private fun observeData() {
        viewModel.getRequestHealthAuthLiveData().observe(viewLifecycleOwner, Observer {
            handleSignInReturn(it)
        })
    }

    private fun handleSignInReturn(data: Resource<*>) {
        when (data) {
            is Resource.Success<*> -> {

            }
            is Resource.Error<*> -> {
                data.message?.let {
                    requireContext().showAlertDialog( "Error", it)
                }
            }
            else -> {

            }
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

    private fun initSpinner() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.gender_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerGender.adapter = adapter
        }
    }

    private fun showUserProfileInfo(user: Users) {
        Glide.with(requireContext())
            .load(user.photo)
            .error(R.mipmap.ic_launcher)
            .into(binding.ivProfile)
        binding.etFullName.setText(user.fullName)
        binding.etUsername.setText(user.username)
        binding.etAge.setText(user.age.toString())
        binding.etHeight.setText(user.height.toString())
        binding.etWeight.setText(user.weight.toString())
        binding.etTargetSteps.setText(user.targetSteps.toString())
        binding.spinnerGainOrLoseWeight.setSelection(showWeightPreference(user.weightPreference))
        if (!user.exerciseFrequency.isNullOrEmpty())
            showUserExerciseFrequency(user.exerciseFrequency)
        val genderArray = resources.getStringArray(R.array.gender_array)
        val genderPos = genderArray.indexOf(user.gender)
        binding.spinnerGender.setSelection(genderPos)
        binding.permissionSwitch.isChecked = user.permissionToShareInfo
        binding.profilePermissionSwitch.isChecked = user.isPrivate?:false
    }

    private fun showWeightPreference(userWeightPreference: String): Int {
        return if (userWeightPreference == "Gain")
            0
        else
            1
    }

    private fun showUserExerciseFrequency(exerciseFrequency: String) {
        when (exerciseFrequency) {
            binding.rbSedentary.text -> {
                binding.rbSedentary.isChecked = true
            }
            binding.rbLightlyActive.text -> {
                binding.rbLightlyActive.isChecked = true
            }
            binding.rbModeratelyActive.text -> {
                binding.rbModeratelyActive.isChecked = true
            }
            binding.rbVeryActive.text -> {
                binding.rbVeryActive.isChecked = true
            }
            binding.rbExtraActive.text -> {
                binding.rbExtraActive.isChecked = true
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

    private fun setUserProfileInfo() {
        val user = Users()
        with(binding) {
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
            user.targetCalories = registerViewModel.calculateTargetCalories(
                user.gender,
                user.weight,
                user.height,
                user.age,
                rgExerciseFrequency.checkedRadioButtonId,
                spinnerGainOrLoseWeight.selectedItem.toString()
            )
            user.photo = this@EditProfileFragment.user!!.photo
            user.permissionToShareInfo = binding.permissionSwitch.isChecked
            user.isPrivate = binding.profilePermissionSwitch.isChecked
            user.dailyStepsCount = userPreference.getStoredUser().dailyStepsCount
            user.caloriesBurned = userPreference.getStoredUser().caloriesBurned
            user.lastUpdatedTime = getCurrentDate()
        }
        viewModel.registerUser(user)
        viewModel.upsertResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Empty -> {}
                is Resource.Error -> {
                    hideProgressBar(binding.pb)
                   requireContext().showAlertDialog( "Error", result.message)
                }
                is Resource.Loading -> {
                    showProgressBar(binding.pb)
                }
                is Resource.Success -> {
                    hideProgressBar(binding.pb)
                    showSnackBarShort("Profile updated successfully.")
                    findNavController().navigateUp()
                }
            }
        }
    }

    private val photoPicker = registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            startCrop(uri)
        } else {
            showSnackBarShort(getString(R.string.epf_photo_picker_error))
        }
    }

    private fun setImageUri(uri: Uri) {
        viewModel.setImageUri(uri)
        viewModel.uploadResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Empty -> {}
                is Resource.Error -> {
                    hideProgressBar(binding.pb)
                    showSnackBarShort(result.message!!)
                }
                is Resource.Loading -> {
                    showProgressBar(binding.pb)
                }
                is Resource.Success -> {
                    hideProgressBar(binding.pb)
                    showSnackBarShort("Image uploaded successfully.")
                    loadImage(result.data, binding.ivProfile)
                }
            }
        }
    }

    private fun pickMedia() {
        photoPicker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
    }

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // Use the returned uri.
            val uriContent = result.uriContent
            setImageUri(uriContent!!)
        } else {
            // An error occurred.
            showSnackBarShort(result.error?.message ?: "Image crop failed!")
        }
    }

    private fun startCrop(uri: Uri) {
        cropImage.launch(
            CropImageContractOptions(
                uri = uri,
                CropImageOptions(
                    guidelines = CropImageView.Guidelines.ON, fixAspectRatio = true,
                    outputRequestWidth = 400, outputRequestHeight = 400
                )
            )
        )
    }
}
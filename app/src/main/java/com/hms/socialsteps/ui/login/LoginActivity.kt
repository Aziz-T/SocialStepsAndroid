package com.hms.socialsteps.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.hms.socialsteps.core.util.Constants.HUAWEI_ID_SIGN_IN
import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.core.util.UserPreference
import com.hms.socialsteps.core.util.showAlertDialog
import com.hms.socialsteps.databinding.ActivityLoginBinding
import com.hms.socialsteps.ui.MainActivity
import com.hms.socialsteps.ui.SplashViewModel
import com.hms.socialsteps.ui.profile.ProfileViewModel
import com.hms.socialsteps.utils.binding.viewBinding
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.hms.hihealth.data.Scopes
import com.huawei.hms.support.account.AccountAuthManager
import com.huawei.hms.support.account.request.AccountAuthParams
import com.huawei.hms.support.account.request.AccountAuthParamsHelper
import com.huawei.hms.support.account.service.AccountAuthService
import com.huawei.hms.support.api.entity.auth.Scope
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private val binding by viewBinding(ActivityLoginBinding::inflate)

    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var service: AccountAuthService
    private lateinit var authParams: AccountAuthParams
    private val profileViewModel by viewModels<ProfileViewModel>()
    private val splashViewModel by viewModels<SplashViewModel>()

    @Inject
    lateinit var userPreference: UserPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        observeData()

        authParams = AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
            .setScopeList(mutableListOf(
                Scope(Scopes.HEALTHKIT_STEP_READ),
                Scope(Scopes.HEALTHKIT_ACTIVITY_RECORD_READ),
                Scope(Scopes.HEALTHKIT_CALORIES_READ)
            ))
            .setAccessToken()
            .createParams()
        service = AccountAuthManager.getService(this, authParams)

        binding.buttonAuth.setOnClickListener {
            signInHuaweiID()
        }
    }

    private fun signInHuaweiID() {
        val signInIntent = service.signInIntent
        startActivityForResult(signInIntent, HUAWEI_ID_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == HUAWEI_ID_SIGN_IN) {
            loginViewModel.signInWithHuaweiId(requestCode, data)
        }
    }

    private fun observeData() {
        loginViewModel.getSignInHuaweiIdLiveData().observe(this, Observer {
            handleSignInReturn(it)
        })
    }

    private fun handleSignInReturn(data: Resource<*>) {
        when (data) {
            is Resource.Success<*> -> {
                if (userPreference.getStoredUser().uid.isNullOrEmpty()){
                    getUser()
                }else {
                    navigateToMainActivity()
                }
            }
            is Resource.Error<*> -> {
                data.message?.let {
                    showAlertDialog("Error", it)
                }
            }
            else -> {

            }
        }
    }

    private fun getUser(){
        val userId = AGConnectAuth.getInstance().currentUser.uid
        profileViewModel.getUser(userId)
        profileViewModel.user.observe(this){ result ->
            when(result){
                is Resource.Error -> {
                    //A logic should created to handle
                    navigateToMainActivity()
                }
                is Resource.Loading -> {
                    //A logic should created to handle
                }
                is Resource.Success -> {
                    val user = result.data!!
                    splashViewModel.saveLoggedUser(user)
                    navigateToMainActivity()
                }
                is Resource.Empty -> {
                    //A logic should created to handle
                    navigateToMainActivity()
                }
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
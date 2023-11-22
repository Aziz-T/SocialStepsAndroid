package com.hms.socialsteps.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.hms.socialsteps.core.util.*
import com.hms.socialsteps.databinding.ActivitySplashBinding
import com.hms.socialsteps.ui.login.LoginActivity
import com.hms.socialsteps.ui.profile.ProfileViewModel
import com.hms.socialsteps.utils.binding.viewBinding
import com.huawei.agconnect.auth.AGConnectAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    private val binding by viewBinding(ActivitySplashBinding::inflate)
    private val profileViewModel by viewModels<ProfileViewModel>()
    private val splashViewModel by viewModels<SplashViewModel>()

    @Inject
    lateinit var userPreference: UserPreference
    @Inject
    lateinit var agConnectAuth: AGConnectAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

    lifecycleScope.launch {
        delay(2000)

        if (agConnectAuth.currentUser != null) {
                getUser()
        } else {
            navigate(LoginActivity::class.java)
        }
      }
    }

    private fun navigate(targetClass: Class<*>) {
        val intent = Intent(this, targetClass)
        startActivity(intent)
        finish()
    }

    private fun getUser(){
        val userId = AGConnectAuth.getInstance().currentUser.uid
        profileViewModel.getUser(userId)
        profileViewModel.user.observe(this){ result ->
            when(result){
                is Resource.Error -> {
                    //A logic should created to handle
                    navigate(MainActivity::class.java)
                }
                is Resource.Loading -> {
                    //A logic should created to handle
                }
                is Resource.Success -> {
                    val user = result.data!!
                    splashViewModel.saveLoggedUser(user)
                    navigate(MainActivity::class.java)
                }
                is Resource.Empty -> {
                    //A logic should created to handle
                    navigate(MainActivity::class.java)
                }
            }
        }
    }
}
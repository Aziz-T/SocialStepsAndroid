package com.hms.socialsteps.ui

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.hms.socialsteps.R
import com.hms.socialsteps.core.util.Constants
import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.databinding.ActivityMainBinding
import com.hms.socialsteps.utils.binding.viewBinding
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.common.ApiException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"

    private val binding by viewBinding(ActivityMainBinding::inflate)
    private val viewModel by viewModels<MainViewModel>()

    private lateinit var snackbar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        getToken()

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)

        observeNetworkStatus()
    }
    private fun observeNetworkStatus(){
        snackbar = Snackbar.make(binding.root,
            "There is no internet connection!",
            Snackbar.LENGTH_INDEFINITE)

        viewModel.networkMonitor.observe(this){ connection ->
            if (!connection){
                snackbar.show()
            }else{
                snackbar.dismiss()
            }
        }
    }

    fun showBottomNavigation()
    {
        binding.navView.visibility = View.VISIBLE
    }

    fun hideBottomNavigation()
    {
        binding.navView.visibility = View.GONE
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun getToken(){
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val appId = Constants.APP_ID

                val tokenScope = "HCM"
                val token = HmsInstanceId.getInstance(this@MainActivity).getToken(appId, tokenScope)
                Timber.tag(TAG).i( "get token:$token")

                // Check whether the token is null.
                if (!TextUtils.isEmpty(token)) {
                    viewModel.setToken(token)
                }
            } catch (e: ApiException) {
                Timber.tag(TAG).e( "get token failed, $e")
            }
        }

        viewModel.tokenResult.observe(this){ result ->
            when(result){
                is Resource.Empty -> {}
                is Resource.Error -> {
                    Timber.tag(TAG).e( "getToken: ${result.message}")
                }
                is Resource.Loading -> {
                    Timber.tag(TAG).d( "getToken: Upsert token started.")
                }
                is Resource.Success -> {
                    Timber.tag(TAG).d( "getToken: Token set.")
                }
            }
        }
    }
}
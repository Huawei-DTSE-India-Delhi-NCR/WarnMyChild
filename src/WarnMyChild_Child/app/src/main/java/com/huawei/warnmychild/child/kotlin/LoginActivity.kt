/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huawei.warnmychild.child.kotlin

import android.Manifest.permission
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.HwIdAuthProvider
import com.huawei.hms.ads.AdListener
import com.huawei.hms.ads.AdParam
import com.huawei.hms.ads.banner.BannerView
import com.huawei.hms.support.api.entity.auth.Scope
import com.huawei.hms.support.api.entity.common.CommonConstant
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import com.huawei.warnmychild.child.R
import java.util.*

class LoginActivity : AppCompatActivity() {
    private var defaultBannerView: BannerView? = null
    private var progressDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val login = findViewById<View>(R.id.login_button) as Button
        val user = AGConnectAuth.getInstance().currentUser
        loadDefaultBannerAd()
        if (!hasPermissions(this, *RUNTIME_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, RUNTIME_PERMISSIONS, REQUEST_CODE)
        }
        if (user != null && user.uid.length > UID_LENGTH) {
            Toast.makeText(this, user.uid, Toast.LENGTH_LONG).show()
            login.visibility = View.GONE
            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            login.visibility = View.VISIBLE
        }
        login.setOnClickListener {

            progressDialog = ProgressDialog(this@LoginActivity)
            progressDialog!!.setTitle("Loading")
            progressDialog!!.show()

            val huaweiIdAuthParamsHelper = HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
            val scopeList: MutableList<Scope> = ArrayList()
            scopeList.add(Scope(CommonConstant.SCOPE.ACCOUNT_BASEPROFILE))
            huaweiIdAuthParamsHelper.setScopeList(scopeList)
            val authParams = huaweiIdAuthParamsHelper.setAccessToken().createParams()
            val service = HuaweiIdAuthManager.getService(this@LoginActivity, authParams)
            startActivityForResult(service.signInIntent, SIGN_IN_REQUEST_CODE)
        }
    }

    /**
     * Load the default banner ad.
     */
    private fun loadDefaultBannerAd() {
        defaultBannerView = findViewById(R.id.hw_banner_view)
        defaultBannerView?.setAdListener(adListener)
        defaultBannerView?.setBannerRefresh(REFRESH_TIME.toLong())
        val adParam = AdParam.Builder().build()
        defaultBannerView?.loadAd(adParam)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        progressDialog!!.dismiss()

        if (requestCode == SIGN_IN_REQUEST_CODE) {
            val authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data)
            if (authHuaweiIdTask.isSuccessful) {
                val huaweiAccount = authHuaweiIdTask.result
                Log.i(TAG, getString(R.string.access_token_lable) + huaweiAccount.accessToken)
                //establishconnection();
                val credential = HwIdAuthProvider.credentialWithToken(huaweiAccount.accessToken)
                AGConnectAuth.getInstance().signIn(credential).addOnSuccessListener { signInResult ->
                    // onSuccess
                    val user = signInResult.user
                    Log.w(getString(R.string.signin_success_lable), "" + user.uid)
                    val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                    startActivity(intent)
                    finish()

                    // establishconnection();
                }
                        .addOnFailureListener { e -> Log.w(getString(R.string.sign_in_failed_lable), e) }
            } else {
                Log.w(getString(R.string.sign_in_failed_lable), getString(R.string.on_error_lable))
            }
        }
    }

    /**
     * Ad listener.
     */
    private val adListener: AdListener = object : AdListener() {
        override fun onAdLoaded() {
            // Called when an ad is loaded successfully.
        }

        override fun onAdFailed(errorCode: Int) {
            // Called when an ad fails to be loaded.
        }

        override fun onAdOpened() {
            // Called when an ad is opened.
        }

        override fun onAdClicked() {
            // Called when a user taps an ad.
        }

        override fun onAdLeave() {
            // Called when a user has left the app.
        }

        override fun onAdClosed() {
            // Called when an ad is closed.
        }
    }

    companion object {
        const val TAG = "TAG"
        private const val REQUEST_CODE = 222
        private const val SIGN_IN_REQUEST_CODE = 8888
        private const val UID_LENGTH = 5
        private const val REFRESH_TIME = 30
        private val RUNTIME_PERMISSIONS = arrayOf(permission.ACCESS_COARSE_LOCATION,
                permission.ACCESS_FINE_LOCATION, permission.ACCESS_BACKGROUND_LOCATION, permission.ACCESS_NETWORK_STATE, permission.INTERNET)

        // Checking the all necessory permissions.
        private fun hasPermissions(context: Context, vararg permissions: String): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
                for (permission in permissions) {
                    if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                        return false
                    }
                }
            }
            return true
        }
    }
}
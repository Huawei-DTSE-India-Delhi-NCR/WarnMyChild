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
package com.huawei.parentapp.kotlin

import android.Manifest
import android.app.*
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
import androidx.core.app.NotificationCompat
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.HwIdAuthProvider
import com.huawei.agconnect.cloud.database.AGConnectCloudDB
import com.huawei.hms.support.api.entity.auth.Scope
import com.huawei.hms.support.api.entity.common.CommonConstant
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import com.huawei.parentapp.R
import java.util.*

class LoginActivity : AppCompatActivity() {
    private var login: Button? = null
    private var progressDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        login = findViewById<View>(R.id.login_button) as Button
        AGConnectCloudDB.initialize(this)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            Log.i(getString(R.string.LOGIN_ACTIVITY_TAG), "sdk < 28 Q")
            if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                val strings = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                ActivityCompat.requestPermissions(this, strings, 1)
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                            getString(R.string.BACKGROUND_LOCATION_PERMISSION)) != PackageManager.PERMISSION_GRANTED) {
                val strings = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        getString(R.string.BACKGROUND_LOCATION_PERMISSION))
                ActivityCompat.requestPermissions(this, strings, REQUEST_PERMISSION)
            }
        }
        login!!.setOnClickListener {

            progressDialog = ProgressDialog(this@LoginActivity)
            progressDialog!!.setTitle("Loading")
            progressDialog!!.show()

            val huaweiIdAuthParamsHelper = HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
            val scopeList: MutableList<Scope> = ArrayList()
            scopeList.add(Scope(CommonConstant.SCOPE.ACCOUNT_BASEPROFILE))
            huaweiIdAuthParamsHelper.setScopeList(scopeList)
            val authParams = huaweiIdAuthParamsHelper.setAccessToken().createParams()
            val service = HuaweiIdAuthManager.getService(this@LoginActivity, authParams)
            startActivityForResult(service.signInIntent, REQUEST_CODE_SIGN_IN)
        }
    }

    override fun onResume() {
        super.onResume()
        val user = AGConnectAuth.getInstance().currentUser
        if (user != null && user.uid.length > UID_LENGTH) {
            Toast.makeText(this, user.uid, Toast.LENGTH_LONG).show()
            login!!.visibility = View.GONE
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            intent.putExtra(getString(R.string.UNIQUE_ID_KEY), user.uid + "__" + user.providerId)
            startActivity(intent)
            finish()
        } else {
            login!!.visibility = View.VISIBLE
        }
    }

    private fun shownotification() {
        val mNotificationManager: NotificationManager
        val mBuilder = NotificationCompat.Builder(applicationContext, "notify_001")
        val ii = Intent(applicationContext, LoginActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, REQUEST_CODE_PENDING_INTENT, ii, 0)
        val bigText = NotificationCompat.BigTextStyle()
        bigText.bigText(getString(R.string.NOTIFICATION_BIG_TEXT))
        bigText.setBigContentTitle(getString(R.string.NOTIFICATION_BIG_TITLE))
        bigText.setSummaryText(getString(R.string.NOTIFICATION_SUMMARY))
        mBuilder.setContentIntent(pendingIntent)
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round)
        mBuilder.setContentTitle(getString(R.string.BUILDER_TITLE))
        mBuilder.setContentText(getString(R.string.BUILDER_TEXT))
        mBuilder.priority = Notification.PRIORITY_MAX
        mBuilder.setStyle(bigText)
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

// === Removed some obsoletes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = getString(R.string.NOTIFICATION_DEFAULT_CHANNEL_ID)
            val channel = NotificationChannel(
                    channelId,
                    getString(R.string.NOTIFICATION_DEFAULT_CHANNEL_NAME),
                    NotificationManager.IMPORTANCE_HIGH)
            mNotificationManager.createNotificationChannel(channel)
            mBuilder.setChannelId(channelId)
        }
        mNotificationManager.notify(0, mBuilder.build())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        progressDialog!!.dismiss()
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            val authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data)
            if (authHuaweiIdTask.isSuccessful) {
                val huaweiAccount = authHuaweiIdTask.result
                val credential = HwIdAuthProvider.credentialWithToken(huaweiAccount.accessToken)
                AGConnectAuth.getInstance().signIn(credential).addOnSuccessListener { signInResult -> // onSuccess
                    val user = signInResult.user
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                        .addOnFailureListener { e -> Log.d(getString(R.string.SIGN_IN_FAILED_TAG), e.localizedMessage) }
            } else {
                Log.d(getString(R.string.SIGN_IN_FAILED_TAG), getString(R.string.SIGN_IN_FAILED_MSG))
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_SIGN_IN = 8888
        private const val REQUEST_PERMISSION = 2
        private const val UID_LENGTH = 5
        private const val REQUEST_CODE_PENDING_INTENT = 0
    }
}
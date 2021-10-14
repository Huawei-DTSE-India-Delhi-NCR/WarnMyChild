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

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.AGConnectUser
import com.huawei.agconnect.cloud.database.*
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import com.huawei.hmf.tasks.OnSuccessListener
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.warnmychild.child.R
import com.huawei.warnmychild.child.kotlin.DBHelper.*
import com.huawei.warnmychild.child.kotlin.ui.slideshow.SlideshowFragment
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.concurrent.ThreadLocalRandom

class HomeActivity : AppCompatActivity() {
    private var mAppBarConfiguration: AppBarConfiguration? = null
    private var tv_user: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val headerView = navigationView.getHeaderView(0)
        tv_user = headerView.findViewById<View>(R.id.tv_user) as TextView
        mAppBarConfiguration = AppBarConfiguration.Builder(
                R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build()
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration!!)
        NavigationUI.setupWithNavController(navigationView, navController)
        customSharedPreference = CustomSharedPreference()
    }

    override fun onResume() {
        super.onResume()
        val navHostFragment = supportFragmentManager.primaryNavigationFragment
        val fragment = navHostFragment!!.childFragmentManager.fragments[0]
        if (fragment != null) {
            try {
                slideshowFragment = fragment as SlideshowFragment
                Log.d(getString(R.string.home_activity_tag), getString(R.string.fragment_instance_success))
            } catch (e: Exception) {
                Toast.makeText(this, getString(R.string.issue_in_scanning), Toast.LENGTH_SHORT).show()
                val exceptionLogger: ExceptionLogger? = ExceptionLogger.Companion.instance
                exceptionLogger?.printExceptionDetails(this@HomeActivity.localClassName, e)
            }
        } else {
            Toast.makeText(this, getString(R.string.null_fragment), Toast.LENGTH_SHORT).show()
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val navHostFragment = supportFragmentManager.primaryNavigationFragment
        val fragment = navHostFragment!!.childFragmentManager.fragments[0]
        if (resultCode != Activity.RESULT_OK || data == null) {
            return
        }
        if (requestCode == Constant.REQUEST_CODE) {
            val obj: Any? = data.getParcelableExtra(ScanUtil.RESULT)
            val hmsScan = obj as HmsScan?
            if (obj != null) {
                if (!TextUtils.isEmpty(obj.getOriginalValue())) {
                    Toast.makeText(this, hmsScan!!.getOriginalValue(), Toast.LENGTH_SHORT).show()
                    if (fragment != null) {
                        try {
                            val jsonObject = JSONObject(hmsScan.getOriginalValue())
                            slideshowFragment = fragment as SlideshowFragment
                        } catch (e: JSONException) {
                            Toast.makeText(this, getString(R.string.issue_in_scanning), Toast.LENGTH_SHORT).show()
                            val exceptionLogger: ExceptionLogger? = ExceptionLogger.Companion.instance
                            exceptionLogger?.printExceptionDetails(this@HomeActivity.localClassName, e)
                        }
                    } else {
                        Toast.makeText(this, getString(R.string.fragment_null), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, getString(R.string.scan_result_not_found), Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        AGConnectAuth.getInstance().signOut()
        customSharedPreference!!.removeValue(applicationContext)
        mCloudDBZone = null
        val loginscreen = Intent(this, LoginActivity::class.java)
        loginscreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(loginscreen)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        return (NavigationUI.navigateUp(navController, mAppBarConfiguration!!)
                || super.onSupportNavigateUp())
    }

    fun setAGC_DB() {
        user = AGConnectAuth.getInstance().currentUser
        if (user != null) {
            tv_user!!.text = user!!.displayName
        }
        // AGConnect Cloud DB...
        AGConnectCloudDB.initialize(applicationContext)
        mCloudDB = AGConnectCloudDB.getInstance()
        try {
            mCloudDB?.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo())
        } catch (e: AGConnectCloudDBException) {
            val exceptionLogger: ExceptionLogger? = ExceptionLogger.Companion.instance
            exceptionLogger?.printExceptionDetails(this@HomeActivity.localClassName, e)
        }
        mConfig = CloudDBZoneConfig(getString(R.string.test_cloud_db_name),
                CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
                CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC)
        mConfig!!.persistenceEnabled = true
        val openDBZoneTask = mCloudDB?.openCloudDBZone2(mConfig!!, true)
        openDBZoneTask?.addOnSuccessListener(OnSuccessListener { cloudDBZone ->
            Log.d(TAG, getString(R.string.open_cloud_db_success))
            mCloudDBZone = cloudDBZone
            if (mCloudDBZone == null) {
                slideshowFragment!!.setConnection(false)
                return@OnSuccessListener
            } else {
                slideshowFragment!!.setConnection(true)
                // Add subscription after opening cloudDBZone success
                addChildInfoSubscription()
                addGeoFenceSubscription()
            }
        })?.addOnFailureListener { e -> Log.d(TAG, getString(R.string.open_cloud_db_failed_for) + e) }
    }

    fun addChildInfoSubscription() {
        if (mCloudDBZone == null) {
            Log.d(TAG, getString(R.string.try_reopen_cloud_db))
            return
        }
        try {
            val snapshotQuery = CloudDBZoneQuery.where(ChildInfo::class.java)
                    .equalTo(getString(R.string.child_id_column_name), user!!.uid)
            mCloudDBZone!!.subscribeSnapshot(snapshotQuery,
                    CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY, ChildInfoSnapshotListener)
            Log.d(TAG, getString(R.string.subscribe_snapshot_result))
        } catch (e: AGConnectCloudDBException) {
            Log.d(TAG, getString(R.string.subscribe_snapshot_lable) + e)
        }
    }

    private fun addGeoFenceSubscription() {
        if (mCloudDBZone == null) {
            Log.d(TAG, getString(R.string.try_reopen_cloud_db))
            return
        }
        try {
            val snapshotQuery = CloudDBZoneQuery.where(GeofenceDetails::class.java)
                    .equalTo(getString(R.string.child_id_column_name), user!!.uid)
            mCloudDBZone!!.subscribeSnapshot(snapshotQuery,
                    CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY, mGeoFenceSnapshotListener)
            Log.d(TAG, getString(R.string.subscribe_snapshot_result))
        } catch (e: AGConnectCloudDBException) {
            Log.d(TAG, getString(R.string.subscribe_snapshot_lable) + e)
        }
    }

    private val ChildInfoSnapshotListener = OnSnapshotListener<ChildInfo> { cloudDBZoneSnapshot, e ->
        if (e != null) {
            Log.d(TAG, getString(R.string.on_snapshot) + e)
            return@OnSnapshotListener
        }
        val snapshotObjects = cloudDBZoneSnapshot.snapshotObjects
        val bookInfos: MutableList<ChildInfo> = ArrayList()
        try {
            if (snapshotObjects != null) {
                while (snapshotObjects.hasNext()) {
                    val bookInfo = snapshotObjects.next()
                    bookInfos.add(bookInfo)
                    runOnUiThread {
                        if (bookInfo.childID == user!!.uid) {
                            Log.d(TAG, getString(R.string.on_snapshot_ui_1))
                            slideshowFragment!!.enableMapview(bookInfo.parentID)
                            Log.d(TAG, getString(R.string.on_snapshot_ui_2))
                        }
                    }
                    break
                }
            }
        } catch (snapshotException: AGConnectCloudDBException) {
            Log.d(TAG, getString(R.string.on_snapshot_get_object) + snapshotException)
        } finally {
            cloudDBZoneSnapshot.release()
        }
    }
    private val mGeoFenceSnapshotListener = OnSnapshotListener<GeofenceDetails> { cloudDBZoneSnapshot, e ->
        if (e != null) {
            Log.d(TAG, getString(R.string.on_snapshot) + e)
            return@OnSnapshotListener
        }
        val snapshotObjects = cloudDBZoneSnapshot.snapshotObjects
        try {
            if (snapshotObjects != null) {
                if (snapshotObjects.size() > 0) {
                    val geoFenceInfo = snapshotObjects[snapshotObjects.size() - 1]
                    runOnUiThread {
                        val toast = Toast.makeText(applicationContext, "Geo Fence Data Received.", Toast.LENGTH_SHORT)
                        toast.show()
                        if (geoFenceInfo.childID == user!!.uid) {
                            slideshowFragment!!.startGeoFence(geoFenceInfo)
                        }
                    }
                }
            }
        } catch (snapshotException: AGConnectCloudDBException) {
            Log.d(TAG, getString(R.string.on_snapshot_get_object) + snapshotException)
        } finally {
            cloudDBZoneSnapshot.release()
        }
    }

    companion object {
        private const val TAG = "TAG"
        const val UID_LENGTH = 5
        const val DEFAULT_NOTIFICATION_ID = 114253
        private var slideshowFragment: SlideshowFragment? = null
        private var mCloudDB: AGConnectCloudDB? = null
        private var mConfig: CloudDBZoneConfig? = null
        private var mCloudDBZone: CloudDBZone? = null
        var user: AGConnectUser? = null
        private var customSharedPreference: CustomSharedPreference? = null
        fun upsertNotificationInfos(msg: String?) {
            if (mCloudDBZone == null) {
                Log.d(TAG, Constant.REOPEN_CLOUD_DB)
                return
            }
            val mNotificationDetails = NotificationDetails()
            if (user != null && user!!.uid.length > UID_LENGTH) {
                mNotificationDetails.childID = user!!.uid
            } else {
                mNotificationDetails.childID = Constant.NO_USER_FOUND
            }
            mNotificationDetails.dateTime = Calendar.getInstance().time
            var rand_int = DEFAULT_NOTIFICATION_ID
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                rand_int = ThreadLocalRandom.current().nextInt()
            }
            mNotificationDetails.id = rand_int.toString()
            mNotificationDetails.isValid = true
            mNotificationDetails.message = msg
            mNotificationDetails.parentID = customSharedPreference!!.getPrefsParentConnectionStatus(slideshowFragment!!.context)
            val upsertTask = mCloudDBZone!!.executeUpsert(mNotificationDetails)
            upsertTask.addOnSuccessListener { cloudDBZoneResult -> Log.d(TAG, Constant.NOTIFICATION_UPSERT_SUCCESS + cloudDBZoneResult + " records") }.addOnFailureListener { e -> Log.d(TAG, Constant.NOTIFICATION_INSERT_FAIL + e + Constant.RECORDS_LABLE) }
        }
    }
}
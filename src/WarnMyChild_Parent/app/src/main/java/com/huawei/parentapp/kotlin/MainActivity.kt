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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.parentapp.R
import com.huawei.parentapp.kotlin.ui.home.HomeFragment
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private var mAppBarConfiguration: AppBarConfiguration? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_kotlin)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.visibility = View.GONE
        fab.setOnClickListener { view ->
            Snackbar.make(view, getString(R.string.SNACK_BAR_TEXT), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_geofence, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build()
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration!!)
        NavigationUI.setupWithNavController(navigationView, navController)
        val user = AGConnectAuth.getInstance().currentUser
        val headerView = navigationView.getHeaderView(0)
        val navUsername = headerView.findViewById<View>(R.id.txt_username) as TextView
        navUsername.text = user.displayName
        if (user.photoUrl != null && user.photoUrl.length > PHOTO_URL_LENGTH) {
            val profileimage = headerView.findViewById<View>(R.id.img_profile) as ImageView
            Glide.with(this).load(user.photoUrl).into(profileimage)
        }
    }



    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val navHostFragment = supportFragmentManager.primaryNavigationFragment
        val fragment = navHostFragment!!.childFragmentManager.fragments[0]
        if (resultCode != Activity.RESULT_OK || data == null) {
            return
        }
        // Obtain the return value of HmsScan from the value returned by the onActivityResult method by using ScanUtil.RESULT as the key value.
        if (requestCode == REQUEST_CODE) {
            val obj: Any? = data.getParcelableExtra(ScanUtil.RESULT)
            val hmsScan = obj as HmsScan?
            if (obj != null) {
                if (!TextUtils.isEmpty(obj.getOriginalValue())) {
                    Toast.makeText(this, hmsScan!!.getOriginalValue(), Toast.LENGTH_SHORT).show()
                    if (fragment != null) {
                        try {
                            val jsonObject = JSONObject(hmsScan.getOriginalValue())

                            (fragment as HomeFragment).setScanResult(jsonObject)
                        } catch (e: JSONException) {
                            Toast.makeText(this, "Issue in processing scan result", Toast.LENGTH_SHORT).show()
                            ExceptionLogger.printExceptionDetails("MainActivity ", e)
                        }
                    } else {
                        Toast.makeText(this, "fragment null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Scanned result not available", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
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

    companion object {
        private const val PHOTO_URL_LENGTH = 5
        private const val REQUEST_CODE = 999
    }
}
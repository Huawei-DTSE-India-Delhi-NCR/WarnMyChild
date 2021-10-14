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
package com.huawei.parentapp.kotlin.ui.slideshow

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.cloud.database.AGConnectCloudDB
import com.huawei.agconnect.cloud.database.CloudDBZone
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import com.huawei.parentapp.*
import com.huawei.parentapp.kotlin.ExceptionLogger
import com.huawei.parentapp.kotlin.NotificationAdapter
import java.util.*

class NotificationFragment : Fragment() {
    private var currentview: View? = null
    private var recyclerView: RecyclerView? = null
    private var txt_norecords: TextView? = null
    private var mCloudDBZone: CloudDBZone? = null
    private var notificationAdapter: NotificationAdapter? = null
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_notification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentview = view
        recyclerView = view.findViewById<View>(R.id.recyclerview) as RecyclerView
        txt_norecords = view.findViewById<View>(R.id.text_norecordsfound) as TextView
        txt_norecords!!.visibility = View.VISIBLE
        recyclerView!!.visibility = View.GONE
        establishconnection()
    }

    fun establishconnection() {
        val mCloudDB = AGConnectCloudDB.getInstance()
        try {
            mCloudDB.createObjectType(com.huawei.parentapp.java.ObjectTypeInfoHelper.getObjectTypeInfo())
            val mConfig = CloudDBZoneConfig(getString(R.string.CLOUD_DB_NAME),
                    CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
                    CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC)
            mConfig.persistenceEnabled = true
            val openDBZoneTask = mCloudDB.openCloudDBZone2(mConfig, true)
            openDBZoneTask.addOnSuccessListener { cloudDBZone ->
                Log.d(getString(R.string.NOTIFICATION_FRAGMENT_TAG), getString(R.string.OPEN_CLOUD_DB_SUCCESS_MSG))
                mCloudDBZone = cloudDBZone
                Log.d(getString(R.string.NOTIFICATION_TAG), getString(R.string.OPEN_DB_SUCCESS))
                queryNotificationDetails()
            }.addOnFailureListener { e ->
                Log.d(getString(R.string.NOTIFICATION_TAG), getString(R.string.DB_OPEN_FAILED_REASON) + e)
                Toast.makeText(activity, getString(R.string.DB_OPEN_FAILED_REASON) + e, Toast.LENGTH_SHORT).show()
            }
        } catch (e: AGConnectCloudDBException) {
            ExceptionLogger.printExceptionDetails(getString(R.string.FRAGMENT_NAME_TAG), e)
        }
    }

    fun queryNotificationDetails() {
        if (mCloudDBZone == null) {
            Log.d(ContentValues.TAG, getString(R.string.TRY_REOPEN_MSG))
            return
        }
        val user = AGConnectAuth.getInstance().currentUser
        val query = CloudDBZoneQuery.where(com.huawei.parentapp.java.NotificationDetails::class.java).
        equalTo(getString(R.string.PARENT_ID_COLUMN_NAME), user.uid).orderByAsc(getString(R.string.DATE_TIME_COLUMN_NAME))


        val queryTask = mCloudDBZone!!.executeQuery(query,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY)
        queryTask.addOnSuccessListener { snapshot ->
            val bookInfoCursor = snapshot.snapshotObjects
            val notificationDetailslist: MutableList<com.huawei.parentapp.java.NotificationDetails> = ArrayList()
            try {
                while (bookInfoCursor.hasNext()) {
                    val NotificationDetails = bookInfoCursor.next()
                    notificationDetailslist.add(NotificationDetails)
                }
                if (notificationDetailslist.size > 0) {
                    recyclerView!!.visibility = View.VISIBLE
                    txt_norecords!!.visibility = View.GONE
                    val layoutManager = LinearLayoutManager(activity)
                    recyclerView!!.layoutManager = layoutManager
                    notificationAdapter = NotificationAdapter(activity, notificationDetailslist)
                    recyclerView!!.adapter = notificationAdapter
                } else {
                    recyclerView!!.visibility = View.GONE
                    txt_norecords!!.visibility = View.VISIBLE

                }
            } catch (e: AGConnectCloudDBException) {
                Log.d(ContentValues.TAG, getString(R.string.QUERY_RESULT_LABLE) + e)
            }
            snapshot.release()
        }.addOnFailureListener { Log.d(ContentValues.TAG, getString(R.string.TRY_REOPEN_MSG)) }
    }
}
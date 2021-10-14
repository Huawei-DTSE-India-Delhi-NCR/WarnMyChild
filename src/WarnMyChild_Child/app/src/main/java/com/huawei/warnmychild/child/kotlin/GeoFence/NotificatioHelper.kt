/*
    Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.huawei.warnmychild.child.kotlin.GeoFence

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.huawei.warnmychild.child.kotlin.Constant
import com.huawei.warnmychild.child.kotlin.MainActivity
import com.huawei.warnmychild.child.R

class NotificatioHelper(var mContext: Context, title: String?, text: String) {
    private val TAG = "@@@@@"
    val channelId = "10"
    private fun shownotifications(message: String) {
        val mNotificationManager: NotificationManager
        val mBuilder = NotificationCompat.Builder(mContext, channelId)
        val ii = Intent(mContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(mContext, 0, ii, 0)
        val bigText = NotificationCompat.BigTextStyle()
        bigText.bigText(Constant.BIG_TEXT)
        bigText.setBigContentTitle(Constant.BIG_CONTENT_TITLE)
        bigText.setSummaryText(message)
        mBuilder.setContentIntent(pendingIntent)
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round)
        mBuilder.setContentTitle(Constant.CONTENT_TITLE)
        mBuilder.setContentText(message)
        mBuilder.priority = Notification.PRIORITY_MAX
        mBuilder.setStyle(bigText)
        mBuilder.setAutoCancel(true)
        mNotificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = Constant.DEFAULT_CHANNEL_ID
            val channel = NotificationChannel(
                    channelId,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH)
            mNotificationManager.createNotificationChannel(channel)
            mBuilder.setChannelId(channelId)
        }
        mNotificationManager.notify(NOTIFYID_1, mBuilder.build())
    }

    companion object {
        private const val NOTIFYID_1 = 1
        private const val CHANNEL_NAME = "channel_name"
    }

    init {
        val manager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        shownotifications(text)
    }
}
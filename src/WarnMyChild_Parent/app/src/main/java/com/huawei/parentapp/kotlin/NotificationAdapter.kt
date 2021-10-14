/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.huawei.parentapp.kotlin

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


import com.huawei.parentapp.R
import com.huawei.parentapp.java.NotificationDetails
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(private val context: Context?, private val notificationDetailslist: List<NotificationDetails>) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var achievementImage: ImageView? = null
        var message: TextView
        var date: TextView

        init {
            message = view.findViewById(R.id.txt_message)
            date = view.findViewById(R.id.txt_date)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.notification_listitem, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val notificationDetails = notificationDetailslist[position]
        holder.message.text = notificationDetails.message
        holder.date.text = currentDate
    }

    override fun getItemCount(): Int {
        return notificationDetailslist.size
    }

    private val currentDate: String
        private get() {
            val currentDateFormat = SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault())
            val currentDate = Date()
            return currentDateFormat.format(currentDate)
        }

    companion object {
        private const val TAG = "NotificationAdapter"
    }

}
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
package com.huawei.warnmychild.child.kotlin.DBHelper

import android.content.Context
import android.content.SharedPreferences
import com.huawei.hms.support.hwid.result.AuthHuaweiId

class CustomSharedPreference {
    private var settings: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    fun saveAccountDetail(context: Context, huaweiAccount: AuthHuaweiId) {
        settings = context.getSharedPreferences(PREFS_, Context.MODE_PRIVATE)
        editor = settings?.edit()
        editor?.putString(PREFS_UID, huaweiAccount.uid)
        editor?.putString(PREFS_NAME, huaweiAccount.displayName)
        editor?.putString(PREFS_EMAIL_ID, huaweiAccount.email)
        editor?.commit()
    }

    fun getName(context: Context): String? {
        val settings: SharedPreferences
        val text: String?
        settings = context.getSharedPreferences(PREFS_, Context.MODE_PRIVATE)
        text = settings.getString(PREFS_NAME, null)
        return text
    }

    fun getUid(context: Context): String? {
        val text: String?
        settings = context.getSharedPreferences(PREFS_, Context.MODE_PRIVATE)
        text = settings?.getString(PREFS_UID, null)
        return text
    }

    fun getEmail(context: Context): String? {
        val text: String?
        settings = context.getSharedPreferences(PREFS_, Context.MODE_PRIVATE)
        text = settings?.getString(PREFS_EMAIL_ID, null)
        return text
    }

    fun clearSharedPreference(context: Context) {
        settings = context.getSharedPreferences(PREFS_, Context.MODE_PRIVATE)
        editor = settings?.edit()
        editor?.clear()
        editor?.commit()
    }

    fun removeValue(context: Context?) {
        settings = context!!.getSharedPreferences(PREFS_, Context.MODE_PRIVATE)
        editor = settings?.edit()
        editor?.remove(PREFS_NAME)
        editor?.remove(PREFS_UID)
        editor?.remove(PREFS_EMAIL_ID)
        editor?.remove(PREFS_PARENT_CONNECTION_STATUS)
        editor?.commit()
    }

    fun saveConnectionStatus(parentID: String?, context: Context?) {
        settings = context!!.getSharedPreferences(PREFS_, Context.MODE_PRIVATE)
        editor = settings?.edit()
        editor?.putString(PREFS_PARENT_CONNECTION_STATUS, parentID)
        editor?.commit()
    }

    fun getPrefsParentConnectionStatus(context: Context?): String? {
        val text: String?
        settings = context!!.getSharedPreferences(PREFS_, Context.MODE_PRIVATE)
        text = settings?.getString(PREFS_PARENT_CONNECTION_STATUS, null)
        return text
    }

    companion object {
        const val PREFS_ = "CHILD_PREFS"
        const val PREFS_KEY = "CHILD_PREFS_String"
        const val PREFS_NAME = "CHILD_NAME"
        const val PREFS_UID = "CHILD_UID"
        const val PREFS_EMAIL_ID = "CHILD_EMAIL_ID"
        const val PREFS_PARENT_CONNECTION_STATUS = "PARENT_CONNECTION_STATUS"
    }
}
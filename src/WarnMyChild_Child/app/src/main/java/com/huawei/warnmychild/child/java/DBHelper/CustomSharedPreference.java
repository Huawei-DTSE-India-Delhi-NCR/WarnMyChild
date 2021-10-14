/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huawei.warnmychild.child.java.DBHelper;

import android.content.Context;
import android.content.SharedPreferences;

import com.huawei.hms.support.hwid.result.AuthHuaweiId;

import static android.content.Context.*;

public class CustomSharedPreference {

    public static final String PREFS_ = "CHILD_PREFS";
    public static final String PREFS_KEY = "CHILD_PREFS_String";
    public static final String PREFS_NAME = "CHILD_NAME";
    public static final String PREFS_UID = "CHILD_UID";
    public static final String PREFS_EMAIL_ID = "CHILD_EMAIL_ID";
    public static final String PREFS_PARENT_CONNECTION_STATUS = "PARENT_CONNECTION_STATUS";

    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    public CustomSharedPreference() {
        super();
    }

    public void saveAccountDetail(Context context, AuthHuaweiId huaweiAccount) {
        settings = context.getSharedPreferences(PREFS_, MODE_PRIVATE);
        editor = settings.edit();
        editor.putString(PREFS_UID, huaweiAccount.getUid());
        editor.putString(PREFS_NAME, huaweiAccount.getDisplayName());
        editor.putString(PREFS_EMAIL_ID, huaweiAccount.getEmail());
        editor.commit();
    }

    public String getName(Context context) {
        SharedPreferences settings;
        String text;
        settings = context.getSharedPreferences(PREFS_, MODE_PRIVATE);
        text = settings.getString(PREFS_NAME, null);
        return text;
    }

    public String getUid(Context context) {

        String text;
        settings = context.getSharedPreferences(PREFS_, MODE_PRIVATE);
        text = settings.getString(PREFS_UID, null);
        return text;
    }

    public String getEmail(Context context) {

        String text;
        settings = context.getSharedPreferences(PREFS_, MODE_PRIVATE);
        text = settings.getString(PREFS_EMAIL_ID, null);
        return text;
    }

    public void clearSharedPreference(Context context) {

        settings = context.getSharedPreferences(PREFS_, MODE_PRIVATE);
        editor = settings.edit();
        editor.clear();
        editor.commit();
    }

    public void removeValue(Context context) {

        settings = context.getSharedPreferences(PREFS_, MODE_PRIVATE);
        editor = settings.edit();

        editor.remove(PREFS_NAME);
        editor.remove(PREFS_UID);
        editor.remove(PREFS_EMAIL_ID);
        editor.remove(PREFS_PARENT_CONNECTION_STATUS);
        editor.commit();
    }



    public void saveConnectionStatus(String parentID, Context context) {
        settings = context.getSharedPreferences(PREFS_, MODE_PRIVATE);
        editor = settings.edit();
        editor.putString(PREFS_PARENT_CONNECTION_STATUS, parentID);
        editor.commit();
    }

    public String getPrefsParentConnectionStatus(Context context) {
        String text;
        settings = context.getSharedPreferences(PREFS_, MODE_PRIVATE);
        text = settings.getString(PREFS_PARENT_CONNECTION_STATUS, null);
        return text;
    }


}


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

package com.huawei.parentapp.java;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.huawei.parentapp.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private static final String TAG = "NotificationAdapter";
    private final Context context;

    private List<NotificationDetails> notificationDetailslist;

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView achievementImage;


        TextView message;
        TextView date;

        public ViewHolder(View view) {
            super(view);

            message = view.findViewById(R.id.txt_message);
            date = view.findViewById(R.id.txt_date);

        }

    }


    public NotificationAdapter(Context mContext, List<NotificationDetails> list) {
        context = mContext;
        notificationDetailslist = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_listitem, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        final NotificationDetails notificationDetails = notificationDetailslist.get(position);
        holder.message.setText(notificationDetails.getMessage());
        holder.date.setText(getCurrentDate());

    }

    @Override
    public int getItemCount() {
        return notificationDetailslist.size();

    }

    private String getCurrentDate()
    {
        SimpleDateFormat currentDateFormat = new SimpleDateFormat(Constants.DATE_FORMAT,Locale.getDefault());
        Date currentDate = new Date();
        String currentDateInString = currentDateFormat.format(currentDate);
        return currentDateInString;
    }

}

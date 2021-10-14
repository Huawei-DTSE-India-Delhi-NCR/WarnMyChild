package com.huawei.parentapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.huawei.parentapp.java.LoginActivity;

public class EntryChoiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_choice);

        findViewById(R.id.btn_kotlin).setOnClickListener(view -> {
            Intent i = new Intent(EntryChoiceActivity.this, com.huawei.parentapp.kotlin.LoginActivity.class);
            startActivity(i);
            finish();
        });

        findViewById(R.id.btn_java).setOnClickListener(view -> {
            Intent i = new Intent(EntryChoiceActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        });
    }
}
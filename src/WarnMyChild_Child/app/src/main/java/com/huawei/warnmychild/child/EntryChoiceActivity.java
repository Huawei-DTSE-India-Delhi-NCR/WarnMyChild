package com.huawei.warnmychild.child;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.warnmychild.child.kotlin.LoginActivity;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import static com.huawei.warnmychild.child.java.GeoFence.GeoService.ONE_BYTE_LENGTH;

public class EntryChoiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_choice);

            findViewById(R.id.btn_kotlin).setOnClickListener(view -> {
            Intent intent = new Intent(EntryChoiceActivity.this, com.huawei.warnmychild.child.kotlin.LoginActivity.class);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btn_java).setOnClickListener(view -> {
            Intent intent = new Intent(EntryChoiceActivity.this, com.huawei.warnmychild.child.java.LoginActivity.class);
            startActivity(intent);
            finish();
        });



    }

}
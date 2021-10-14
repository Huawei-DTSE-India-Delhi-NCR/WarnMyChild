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
package com.huawei.warnmychild.child.java;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectAuthCredential;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.auth.HwIdAuthProvider;
import com.huawei.agconnect.auth.SignInResult;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.ads.AdListener;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.banner.BannerView;
import com.huawei.hms.support.api.entity.auth.Scope;
import com.huawei.hms.support.api.entity.hwid.HwIDConstant;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;
import com.huawei.warnmychild.child.R;

import java.util.ArrayList;
import java.util.List;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
public class LoginActivity extends AppCompatActivity {
    public static final String TAG = "TAG";
    private static final int REQUEST_CODE = 222;
    private static final int SIGN_IN_REQUEST_CODE = 8888;
    private static final int UID_LENGTH = 5;
    private BannerView defaultBannerView;
    private static final int REFRESH_TIME = 30;
    private ProgressDialog progressDialog;
    private static final String[] RUNTIME_PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION,
            ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION,Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button login = (Button) findViewById(R.id.login_button);
        AGConnectUser user = AGConnectAuth.getInstance().getCurrentUser();

        loadDefaultBannerAd();
        if (!hasPermissions(this, RUNTIME_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, RUNTIME_PERMISSIONS, REQUEST_CODE);
        }
        if (user != null && user.getUid().length() > UID_LENGTH) {
            //Toast.makeText(this, user.getUid(), Toast.LENGTH_LONG).show();
            login.setVisibility(View.GONE);
            Intent intent = new Intent(LoginActivity.this, com.huawei.warnmychild.child.java.HomeActivity.class);
            startActivity(intent);
            finish();

        } else {
            login.setVisibility(View.VISIBLE);
        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressDialog = new ProgressDialog(LoginActivity.this);
                progressDialog.setTitle("Loading");
                progressDialog.show();

                HuaweiIdAuthParamsHelper huaweiIdAuthParamsHelper = new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM);
                List<Scope> scopeList = new ArrayList<>();
                scopeList.add(new Scope(HwIDConstant.SCOPE.ACCOUNT_BASEPROFILE));
                huaweiIdAuthParamsHelper.setScopeList(scopeList);
                HuaweiIdAuthParams authParams = huaweiIdAuthParamsHelper.setAccessToken().createParams();
                HuaweiIdAuthService service = HuaweiIdAuthManager.getService(LoginActivity.this, authParams);
                startActivityForResult(service.getSignInIntent(), SIGN_IN_REQUEST_CODE);
            }
        });
    }

    /**
     * Load the default banner ad.
     */
    private void loadDefaultBannerAd() {
        defaultBannerView = findViewById(R.id.hw_banner_view);
        defaultBannerView.setAdListener(adListener);
        defaultBannerView.setBannerRefresh(REFRESH_TIME);

        AdParam adParam = new AdParam.Builder().build();
        defaultBannerView.loadAd(adParam);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        progressDialog.dismiss();

        if (requestCode == SIGN_IN_REQUEST_CODE) {
            Task<AuthHuaweiId> authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data);
            if (authHuaweiIdTask.isSuccessful()) {
                AuthHuaweiId huaweiAccount = authHuaweiIdTask.getResult();
                Log.i(TAG, getString(R.string.access_token_lable) + huaweiAccount.getAccessToken());
                //establishconnection();
                AGConnectAuthCredential credential = HwIdAuthProvider.credentialWithToken(huaweiAccount.getAccessToken());
                AGConnectAuth.getInstance().signIn(credential).addOnSuccessListener(new OnSuccessListener<SignInResult>() {
                    @Override
                    public void onSuccess(SignInResult signInResult) {
                        // onSuccess
                        AGConnectUser user = signInResult.getUser();


                        Log.w(getString(R.string.signin_success_lable), "" + user.getUid());

                        Intent intent = new Intent(LoginActivity.this, com.huawei.warnmychild.child.java.HomeActivity.class);
                        startActivity(intent);
                        finish();

                        // establishconnection();
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                Log.w(getString(R.string.sign_in_failed_lable), e);
                            }
                        });

            } else {
                Log.w(getString(R.string.sign_in_failed_lable), getString(R.string.on_error_lable));
            }
        }
    }

    /**
     * Ad listener.
     */
    private AdListener adListener = new AdListener()
    {
        @Override
        public void onAdLoaded() {
            // Called when an ad is loaded successfully.
        }

        @Override
        public void onAdFailed(int errorCode) {
            // Called when an ad fails to be loaded.
        }

        @Override
        public void onAdOpened() {
            // Called when an ad is opened.
        }

        @Override
        public void onAdClicked() {
            // Called when a user taps an ad.
        }

        @Override
        public void onAdLeave() {
            // Called when a user has left the app.
        }

        @Override
        public void onAdClosed() {
            // Called when an ad is closed.
        }
    };

    // Checking the all necessory permissions.
    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

}
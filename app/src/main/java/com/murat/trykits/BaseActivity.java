package com.murat.trykits;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.hmf.tasks.OnCompleteListener;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.push.HmsMessaging;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;
import com.murat.trykits.Services.Hms_Message_Service;
import com.murat.trykits.Utils.MyApplication;

public class BaseActivity extends Activity {

    protected MyApplication myApplication;
    String TAG = "TryKits";
    Context context;

    HuaweiIdAuthParams huaweiIdAuthParams;
    HuaweiIdAuthService huaweiIdAuthService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Context
        context = getApplicationContext();

        // Application
        myApplication = (MyApplication) this.getApplicationContext();

        // Start Message Service
        startService(new Intent(context, Hms_Message_Service.class));

        // SignInHuawei
        huaweiIdAuthParams = new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM).setAuthorizationCode().createParams();
        huaweiIdAuthService = HuaweiIdAuthManager.getService(this, huaweiIdAuthParams);

    }


    public void getToken() {
        new Thread() {
            @Override
            public void run() {
                try {
                    // read from agconnect-services.json
                    String appId = AGConnectServicesConfig.fromContext(context).getString("client/app_id");
                    String token = HmsInstanceId.getInstance(context).getToken(appId, "HCM");
                    showLog("Get Token", token);
                } catch (ApiException e) {
                    showLog("Get Token Failed", e.toString());
                }
            }
        }.start();
    }


    /*
    =======================================================================================
    HMS - Account Kit
     */
    public void checkHuaweiSignIn() {
        Task<AuthHuaweiId> authHuaweiIdTask = huaweiIdAuthService.silentSignIn();
        authHuaweiIdTask.addOnSuccessListener(new OnSuccessListener<AuthHuaweiId>() {
            @Override
            public void onSuccess(AuthHuaweiId authHuaweiId) {
                // Obtain HUAWEI ID information.
                //showLog("huaweiAccount", "displayName:" + authHuaweiId.getDisplayName());

                // If current activity is SplashActivity, go to MainActivity
                if (myApplication.getCurrentActivity().getLocalClassName().equals("SplashActivity")) {
                    startActivity(new Intent(context, MainActivity.class).putExtra("authHuaweiId", authHuaweiId));
                }
            }
        });

        authHuaweiIdTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // The sign-in fails. Try to sign in explicitly using getSignInIntent().
                if (e instanceof ApiException) {
                    ApiException apiException = (ApiException) e;
                    showLog("huaweiAccount", "sign failed status:" + apiException.getStatusCode());
                }
            }
        });
    }

    public void signInWithHuawei(View view) {
        startActivityForResult(huaweiIdAuthService.getSignInIntent(), 8888);
    }

    public void signOutWithHuawei(View view) {
        Task<Void> signOutTask = huaweiIdAuthService.signOut();

        signOutTask.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                // Processing after the sign-out.
                showLog("huaweiAccount", "signOut complete");

                // Cancel Authorization
                cancelAuthorizationHuaweiId();
            }
        });
    }

    public void cancelAuthorizationHuaweiId() {
        huaweiIdAuthService.cancelAuthorization().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                if (task.isSuccessful()) {
                    //do some thing while cancel success
                    showLog("huaweiAccount", "Cancel Auth onSuccess");

                    // If current activity is not SplashActivity, go to SplashActivity
                    if (!myApplication.getCurrentActivity().getLocalClassName().equals("SplashActivity")) {
                        startActivity(new Intent(context, SplashActivity.class));
                    }

                } else {
                    //do some thing while cancel success
                    Exception exception = task.getException();
                    if (exception instanceof ApiException) {
                        int statusCode = ((ApiException) exception).getStatusCode();
                        showLog("huaweiAccount", "Cancel Auth onFailure: " + statusCode);
                    }
                }
            }
        });
    }

    /*
    =======================================================================================
    HMS - Push Kit
     */
    public void subscribeHmsMessaging(final String topic) {
        try {
            HmsMessaging.getInstance(context).subscribe(topic).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(Task<Void> task) {
                    if (task.isSuccessful()) {
                        //showLog("HmsMessaging", "Subscribe Complete");
                        showToast("Subscribe Complete");
                    } else {
                        //showLog("HmsMessaging", "Subscribe failed: ret=" + task.getException().getMessage());
                        showToast("Subscribe failed: ret=" + task.getException().getMessage());
                    }
                }
            });
        } catch (Exception e) {
            showToast("Subscribe failed: exception=" + e.getMessage());
        }
    }

    public void unsubscribeHmsMessaging(final String topic) {
        try {
            HmsMessaging.getInstance(context).unsubscribe(topic).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(Task<Void> task) {
                    if (task.isSuccessful()) {
                        showToast("unsubscribe Complete");
                    } else {
                        showToast("unsubscribe failed: ret=" + task.getException().getMessage());
                    }
                }
            });
        } catch (Exception e) {
            showToast("Unsubscribe failed: exception=" + e.getMessage());
        }
    }


    /*
    =======================================================================================
     */
    protected void onResume() {
        super.onResume();
        myApplication.setCurrentActivity(this);
    }

    protected void onPause() {
        clearReferences();
        super.onPause();
    }

    protected void onDestroy() {
        clearReferences();
        super.onDestroy();
    }

    private void clearReferences() {
        Activity currActivity = myApplication.getCurrentActivity();
        if (this.equals(currActivity))
            myApplication.setCurrentActivity(null);
    }

    /*
    =======================================================================================
    Functions
     */
    public void showLog(String prefix, String log) {
        Log.i(TAG, prefix + " " + log);
    }

    public void showToast(String str) {
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }
}

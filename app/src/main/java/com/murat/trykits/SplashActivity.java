package com.murat.trykits;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;

public class SplashActivity extends BaseActivity {

    Button btn_signin_with_huawei, btn_signout_with_huawei;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        checkHuaweiSignIn();

        //Init
        /*
        btn_signin_with_huawei = findViewById(R.id.btn_signin_with_huawei);
        btn_signin_with_huawei.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithHuawei();
            }
        });

        btn_signout_with_huawei = findViewById(R.id.btn_signout_with_huawei);
        btn_signout_with_huawei.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutWithHuawei();
            }
        });
         */

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 8888) {
            Task<AuthHuaweiId> authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data);
            if (authHuaweiIdTask.isSuccessful()) {
                //login success
                AuthHuaweiId authHuaweiId = authHuaweiIdTask.getResult();
                // Jump to MainActivity
                startActivity(new Intent(context, MainActivity.class).putExtra("authHuaweiId", authHuaweiId));
            } else {
                showLog("huaweiAccount", "signIn get code failed: " + ((ApiException) authHuaweiIdTask.getException()).getStatusCode());
            }
        }
    }


}
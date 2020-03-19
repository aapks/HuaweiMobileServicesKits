package com.murat.trykits.Utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.agconnect.config.LazyInputStream;

import java.io.IOException;
import java.io.InputStream;

public class MyApplication extends Application {

    private Activity mCurrentActivity = null;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public Activity getCurrentActivity(){
        return mCurrentActivity;
    }
    public void setCurrentActivity(Activity mCurrentActivity){
        this.mCurrentActivity = mCurrentActivity;
    }


    // TODO: Add the following code:
    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        AGConnectServicesConfig config = AGConnectServicesConfig.fromContext(context);
        config.overlayWith(new LazyInputStream(context) {
            public InputStream get(Context context) {
                try {
                    return context.getAssets().open("agconnect-services.json");
                } catch (IOException e) {
                    return null;
                }
            }
        });
    }
    // TODO: End
}
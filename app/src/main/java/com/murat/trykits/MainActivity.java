package com.murat.trykits;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.huawei.hms.support.hwid.result.AuthHuaweiId;

public class MainActivity extends BaseActivity {

    TextView txt_user_name;
    Bundle bundle;
    Button btn_subscribe_to_topic, btn_unsubscribe_from_topic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bundle = getIntent().getExtras();
        AuthHuaweiId authHuaweiId = (AuthHuaweiId) bundle.getParcelable("authHuaweiId");

        // Init
        txt_user_name = findViewById(R.id.txt_user_name);
        btn_subscribe_to_topic = findViewById(R.id.btn_subscribe_to_topic);
        btn_unsubscribe_from_topic = findViewById(R.id.btn_unsubscribe_to_topic);

        // Subscribe To Topic
        btn_subscribe_to_topic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscribeHmsMessaging("topic_1");
            }
        });

        // Unsubscribe To Topic
        btn_unsubscribe_from_topic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unsubscribeHmsMessaging("topic_1");
            }
        });


        if (authHuaweiId != null) {
            txt_user_name.setText(authHuaweiId.getDisplayName());
        } else {
            txt_user_name.setText("İsim alınamadı");
        }

        // Get Device Token
        getToken();
    }
}
package com.ghostery.privacy.triangle_aar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ghostery.privacy.appnoticesdk.AppNotice;

public class HybridPrivacySettings extends AppCompatActivity {
    private final static String TAG = "HybridPrivacySettings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hybrid_privacy_settings);

        LinearLayout pref_in_app_privacy_layout = (LinearLayout)findViewById(R.id.pref_in_app_privacy_layout);
        pref_in_app_privacy_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppNotice appNotice = MainActivity.getAppNotice();
                if (appNotice != null) {
                    Toast.makeText(App.getContext(), "Manage Privacy Preferences from hybrid.", Toast.LENGTH_LONG).show();
                    appNotice.showManagePreferences();
                } else {
                    Toast.makeText(App.getContext(), "The App Notice SDK has not been initialized in this session. Please restart the app and try again.", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "The App Notice SDK has not been initialized in this session.");
                }
            }
        });
    }
}

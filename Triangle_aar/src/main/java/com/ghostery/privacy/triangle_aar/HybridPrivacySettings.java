package com.ghostery.privacy.triangle_aar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class HybridPrivacySettings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hybrid_privacy_settings);

        LinearLayout pref_in_app_privacy_layout = (LinearLayout)findViewById(R.id.pref_in_app_privacy_layout);
        pref_in_app_privacy_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(App.getContext(), "Manage Privacy Preferences from hybrid.", Toast.LENGTH_LONG).show();
                MainActivity.getAppNotice().showManagePreferences();
            }
        });
    }
}

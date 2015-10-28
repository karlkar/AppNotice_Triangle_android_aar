package com.ghostery.privacy.triangle_aar;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ghostery.privacy.appnoticesdk.callbacks.AppNotice_Callback;
import com.ghostery.privacy.appnoticesdk.AppNotice;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity {
    private final static String TAG = "Triangle_aar";
    private static Activity activity;

    // Remove the below line after defining your own ad unit ID.
    private static final String TOAST_TEXT_SHOW = "AdMob ads are being shown.";
    private static final String TOAST_TEXT_DISABLE = "AdMob ads are disabled.";
    private static final String TOAST_TEXT_NO_ADMOB = "AdMob state not found in privacy preferences.";
    private static final String TOAST_TEXT_NOPREFS = "No privacy preferences returned.";

    // Ghostery variables
    // Note: Use your custom values for the Company ID, Notice ID and all or your tracker IDs. These test values won't work in your environment.
    private static final int GHOSTERY_COMPANYID = 242; // My Ghostery company ID (NOTE: Use your value here)
    private static final int GHOSTERY_NOTICEID = 6106; // The Ghostery notice ID for this app (NOTE: Use your value here)
    private static final int GHOSTERY_TRACKERID_ADMOB = 464; // Tracker ID: AdMob (NOTE: you will need to define a variable for each tracker you have in your app)
    private static final boolean GHOSTERY_USEREMOTEVALUES = true; // If true, causes SDK to override local SDK settings with those defined in the Ghostery Admin Portal
    private AppNotice appNotice; // Ghostery App Notice SDK object
    private AppNotice_Callback appNotice_callback; // Ghostery App Notice callback handler
    private HashMap<Integer, Boolean> appNotice_privacyPreferences; // Map of non-essential trackers (by ID) and their on/off states

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;

        // Create the callback handler for the App Notice SDK
        appNotice_callback = new AppNotice_Callback() {

            // Called by the SDK when the user accepts or declines tracking from one of the consent flow dialogs
            @Override
            public void onOptionSelected(boolean isAccepted, HashMap<Integer, Boolean> appNotice_privacyPreferences) {
                // Handle your response
                if (isAccepted) {
                    Boolean adMobEnabled = false;
                    if (appNotice_privacyPreferences.size() > 0) {
                        adMobEnabled = appNotice_privacyPreferences.get(GHOSTERY_TRACKERID_ADMOB);
                        if (adMobEnabled == null)   // If ttracker was not found in list, assume it is disabled
                            Toast.makeText(activity, TOAST_TEXT_NO_ADMOB, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(activity, TOAST_TEXT_NOPREFS, Toast.LENGTH_LONG).show();
                    }
                    manageAdMob(adMobEnabled);
                } else {
                    try {
                        DeclineConfirmation_DialogFragment dialog = new DeclineConfirmation_DialogFragment();
                        dialog.show(getFragmentManager(), "DeclineConfirmation_DialogFragment");
                    } catch (IllegalStateException e) {
                        Log.e(TAG, "Error while trying to display the decline-confirmation dialog.", e);
                    }
                }
            }

            // Called by the SDK when startConsentFlow is called but the SDK state meets one or more of the following conditions:
            //   - The Implied Consent dialog has been already been displayed ghostery_ric_session_max_default times in the current session.
            //   - The Implied Consent dialog has already been displayed ghostery_ric_max_default times in the last 30 days.
            //   - The Explicit Consent dialog has already been accepted.
            @Override
            public void onNoticeSkipped() {
                appNotice_privacyPreferences = appNotice.getTrackerPreferences();
                manageAdMob(appNotice_privacyPreferences.get(GHOSTERY_TRACKERID_ADMOB));
            }

            // Called by the SDK when the app-user is finished managing their privacy preferences on the Manage Preferences screen and navigates back your app
            @Override
            public void onTrackerStateChanged(HashMap<Integer, Boolean> trackerHashMap) {
                appNotice_privacyPreferences = trackerHashMap;
                manageAdMob(appNotice_privacyPreferences.get(GHOSTERY_TRACKERID_ADMOB));
            }
        };

        // Instantiate and start the Ghostery consent flow
        appNotice = new AppNotice(this, GHOSTERY_COMPANYID, GHOSTERY_NOTICEID, GHOSTERY_USEREMOTEVALUES, appNotice_callback);
        appNotice.startConsentFlow();
    }

    private void manageAdMob(Boolean isOn) {
        // Get the AdMob banner view
        AdView adView = (AdView) findViewById(R.id.adView);

        if (isOn != null && isOn) {
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.setVisibility(View.VISIBLE);
            adView.loadAd(adRequest);

            // Toast the AdMob showing message
            Toast.makeText(this, TOAST_TEXT_SHOW, Toast.LENGTH_LONG).show();
        } else {
            adView.pause();
            adView.setVisibility(View.GONE);

            // Toast the AdMob disabled message
            Toast.makeText(this, TOAST_TEXT_DISABLE, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_privacyPreferences) {
            appNotice.showManagePreferences();
            return true;
        } else if (id == R.id.action_resetAppNoticeSdk) {
            appNotice.resetSDK();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}

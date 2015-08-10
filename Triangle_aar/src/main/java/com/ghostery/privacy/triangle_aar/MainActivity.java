package com.ghostery.privacy.triangle_aar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ghostery.privacy.inappconsentsdk.callbacks.InAppConsent_Callback;
import com.ghostery.privacy.inappconsentsdk.model.InAppConsent;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity {
    private final static String TAG = "Triangle_aar";

    // Remove the below line after defining your own ad unit ID.
    private static final String TOAST_TEXT_SHOW = "AdMob ads are being shown.";
    private static final String TOAST_TEXT_DISABLE = "AdMob ads are disabled.";

    // Ghostery variables
    private static final int GHOSTERY_COMPANYID = 242; // My Ghostery company ID
    private static final int GHOSTERY_NOTICEID = 4721; // The Ghostery notice ID for this app
    private static final int GHOSTERY_TRACKERID_ADMOB = 464; // Tracker ID: AdMob (note: you will need to define a variable for each tracker you have in your app)
    private static final boolean GHOSTERY_USEREMOTEVALUES = true; // If true, causes SDK to override local SDK settings with those defined in the Ghostery Admin Portal
    private InAppConsent inAppConsent; // Ghostery In-App Consent SDK object
    private InAppConsent_Callback inAppConsent_callback; // Ghostery In-App Consent callback handler
    private HashMap<Integer, Boolean> inAppConsent_privacyPreferences; // Map of non-essential trackers (by ID) and their on/off states

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the callback handler for the In-App Consent SDK
        inAppConsent_callback = new InAppConsent_Callback() {

            // Called by the SDK when the user accepts or declines tracking from one of the Consent flow dialogs
            @Override
            public void onOptionSelected(boolean isAccepted, HashMap<Integer, Boolean> trackerHashMap) {
                // Handle your response
                if (isAccepted) {
                    inAppConsent_privacyPreferences = trackerHashMap;
                    initAdMob(isAccepted && inAppConsent_privacyPreferences.get(GHOSTERY_TRACKERID_ADMOB));
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
                inAppConsent_privacyPreferences = inAppConsent.getTrackerPreferences();
                initAdMob(inAppConsent_privacyPreferences.get(GHOSTERY_TRACKERID_ADMOB));
            }

            // Called by the SDK when the app-user is finished managing their privacy preferences on the Manage Preferences screen and navigates back your app
            @Override
            public void onTrackerStateChanged(HashMap<Integer, Boolean> trackerHashMap) {
                inAppConsent_privacyPreferences = trackerHashMap;
                initAdMob(inAppConsent_privacyPreferences.get(GHOSTERY_TRACKERID_ADMOB));
            }
        };

        // Instantiate and start the Ghostery In-App Consent flow
        inAppConsent = new InAppConsent(this);
        inAppConsent.startConsentFlow(GHOSTERY_COMPANYID, GHOSTERY_NOTICEID, GHOSTERY_USEREMOTEVALUES, inAppConsent_callback);
    }

    private void initAdMob(boolean isOn) {
        // Load an ad into the AdMob banner view.
        AdView adView = (AdView) findViewById(R.id.adView);

        if (isOn) {
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.setVisibility(View.VISIBLE);
            adView.loadAd(adRequest);

            // Toasts the AdMob showing message
            Toast.makeText(this, TOAST_TEXT_SHOW, Toast.LENGTH_LONG).show();
        } else {
            adView.pause();
            adView.setVisibility(View.GONE);

            // Toasts the AdMob disabled message
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
            inAppConsent.showManagePreferences(GHOSTERY_COMPANYID, GHOSTERY_NOTICEID, GHOSTERY_USEREMOTEVALUES, inAppConsent_callback);
            return true;
        } else if (id == R.id.action_resetAppNoticeSdk) {
            inAppConsent.resetSDK();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}

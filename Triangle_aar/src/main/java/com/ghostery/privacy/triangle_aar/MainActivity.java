package com.ghostery.privacy.triangle_aar;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.ghostery.privacy.appnoticesdk.AppNotice;
import com.ghostery.privacy.appnoticesdk.callbacks.AppNotice_Callback;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.HashMap;

import io.fabric.sdk.android.Fabric;


public class MainActivity extends AppCompatActivity {
    private final static String TAG = "Triangle_aar";
    private static Activity activity;

    // Remove the below line after defining your own ad unit ID.
    private static final String TOAST_ADMOB_ENABLE = "AdMob ads are being shown.";
    private static final String TOAST_ADMOB_DISABLE = "AdMob ads are disabled.";
    private static final String TOAST_CRASHLYTICS_ENABLE = "Crashlytics is enabled.";
    private static final String TOAST_CRASHLYTICS_DISABLE = "Crashlytics is disabled.";
    private static final String TOAST_TEXT_NOPREFS = "No privacy preferences returned.";

    // Ghostery variables
    // Note: Use your custom values for the Company ID, Notice ID and all or your tracker IDs. These test values won't work in your environment.
    private static final String GHOSTERY_TOKEN = "baefa2fb063b4273a636591f8535dcf3"; // My Ghostery App Notice token (NOTE: Use your value here)

    // Ghostery tracker IDs (NOTE: you will need to define a variable for each tracker you have in your app)
    private static final int GHOSTERY_TRACKERID_ADMOB = 464; // Tracker ID: AdMob
    private static final int GHOSTERY_TRACKERID_CRASHLYTICS = 3140; // Tracker ID: Crashlytics

    private static AppNotice appNotice; // Ghostery App Notice SDK object
    private AppNotice_Callback appNotice_callback; // Ghostery App Notice callback handler
	private boolean appRestartRequired; // Ghostery parameter to track if app needs to be restarted after opt-out
    private AdView adView;
	private final boolean isTestingAds = true; // Switch to make it easy on changing ad-testing mode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Context context = App.getContext();
        activity = this;

        AppCompatTextView sdkVersionTextView = (AppCompatTextView)findViewById(R.id.sdk_version);
        sdkVersionTextView.setText("SDK v." + AppNotice.sdkVersionName + "." + String.valueOf(AppNotice.sdkVersionCode));

        // Get the AdMob banner view and set an ad-loaded listner
        adView = (AdView) findViewById(R.id.adView);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
				// Handle ad-loaded event
            }
        });

        // Create the callback handler for the App Notice SDK
        appNotice_callback = new AppNotice_Callback() {

            // Called by the SDK when the user accepts or declines tracking from one of the consent notice screens
            @Override
            public void onOptionSelected(boolean isAccepted, HashMap<Integer, Boolean> appNotice_privacyPreferences) {
                // Handle your response
                if (isAccepted) {
                    manageTrackers(appNotice_privacyPreferences);
                } else {
                    // Toast invalid response state
                    Toast.makeText(activity, R.string.decline_state_error, Toast.LENGTH_LONG).show();
                }
            }

            // Called by the SDK when either startImpliedConsentFlow or startExplicitConsentFlow method is called, except when the SDK state meets one or more of the following conditions:
            //   - The Implied Consent screen:
            //     1) Has already been displayed the number of times specified by the parameter to the SDK's startImpliedConsentFlow method.
            //        0: Displays on first start and every notice ID change (recommended).
            //        1+: Is the max number of times to display the consent screen on start up in a 30-day period.
            //     2) Has already been displayed ghostery_implied_flow_session_display_max times in the current session.
            //   - The Explicit Consent screen:
            //     1) Consent has already been given.
            @Override
            public void onNoticeSkipped(boolean isAccepted, HashMap<Integer, Boolean> trackerHashMap) {
                manageTrackers(trackerHashMap);
            }

            // Called by the SDK when the app-user is finished managing their privacy preferences on the Manage Preferences screen and navigates back your app
            @Override
            public void onTrackerStateChanged(HashMap<Integer, Boolean> trackerHashMap) {
                manageTrackers(trackerHashMap);
            }

        };

        final String modeImplied = getResources().getString(R.string.mode_implied);
        boolean isImplied = AppData.getString(AppData.APPDATA_CONSENT_FLOW_MODE, modeImplied).equals(modeImplied);
        if (isImplied) {
            // Example of instantiating the App Notice SDK in implied mode.
            // To be in compliance with honoring a user's prior consent, you must start this consent flow
            // before any trackers are started. In this demo, all trackers are only started from within
            // the manageTrackers method, and the manageTrackers method is only called from the App Notice
            // call-back handler. This ensures that trackers are only started with a users prior consent.
            appNotice = new AppNotice(this, GHOSTERY_TOKEN, appNotice_callback);

            // Start the implied-consent flow (recommended)
            //   0: Displays on first start and every notice ID change (recommended).
            //   1+: Is the max number of times to display the consent screen on start up in a 30-day period.
            appNotice.startConsentFlow(0);
        } else {
            // Example of instantiating the App Notice SDK in explicit mode.
            // To be in compliance with honoring a user's prior consent, you must start this consent flow
            // before any trackers are started. In this demo, all trackers are only started from within
            // the manageTrackers method, and the manageTrackers method is only called from the App Notice
            // call-back handler. This ensures that trackers are only started with a users prior consent.
            appNotice = new AppNotice(this, GHOSTERY_TOKEN, appNotice_callback, false);

            // Start the explicit-consent flow:
            appNotice.startConsentFlow();
        }
    }

	@Override
	protected void onPostResume() {
		super.onPostResume();

		// If any trackers have been opted-out of and need an app restart, handle user notification here
		if (appRestartRequired) {
            showMessage(getString(R.string.restartAppDialog_title), getString(R.string.restartAppDialog_message));
			appRestartRequired = false; // Don't notify again until preferences have been changed again
		}
	}

	private void manageTrackers(HashMap<Integer, Boolean> trackerHashMap) {
        appRestartRequired = false;    // Assume the app doesn't need to be restarted to manage opt-outs

        if (trackerHashMap.size() > 0) {
            // == Manage AdMob ======================================
            // This demonstrates how to manage a tracker that can both be enabled and disabled in a
            // single session. The AdMob tracker is turned on and off as directed by a user's
            // privacy preferences.
            Boolean adMobEnabled = trackerHashMap.get(GHOSTERY_TRACKERID_ADMOB) == null? false : trackerHashMap.get(GHOSTERY_TRACKERID_ADMOB);

            if (adMobEnabled) {
                boolean inEmulator = Build.BRAND.toLowerCase().startsWith("generic");

                // Start the AdMob tracker as specified by the user
                // (Note: If there were a way to detect that this tracker were already running, we
                // could avoid restarting the tracker in that case.)
                AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
                if (isTestingAds) {
                    if (inEmulator) {
                        adRequestBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
                    } else {
                        adRequestBuilder.addTestDevice("8E86E615D3646127F9A6DE11B6E8C533");
                    }
                }
                AdRequest adRequest = adRequestBuilder.build();

                adView.setVisibility(View.VISIBLE);
                adView.bringToFront();
                adView.loadAd(adRequest);

                // Toast the AdMob showing message (optional)
                Toast.makeText(this, TOAST_ADMOB_ENABLE, Toast.LENGTH_LONG).show();
            } else {
                // Stop the AdMob tracker as specified by the user:
                // To honor a user's withdrawn consent, if a tracker can be turned off or disabled,
                // that tracker must be turned off in a way that it is no longer tracking the user
                //  in this session and future sessions.
                adView.pause();
                adView.setVisibility(View.GONE);

                // Toast the AdMob disabled message (optional)
                Toast.makeText(this, TOAST_ADMOB_DISABLE, Toast.LENGTH_LONG).show();
            }


            // == Manage Crashlytics ================================
            // This demonstrates how to manage a tracker that can enabled but not disabled in a
            // single session. The Crashlytics tracker is turned on as directed by a user's
            // privacy preferences. But when a user requests that this tracker be turned off in the
            // privacy preferences, this demonstrates one way to notify that user to restart
            // the app.
            Boolean crashlyticsEnabled = trackerHashMap.get(GHOSTERY_TRACKERID_CRASHLYTICS) == null? false : trackerHashMap.get(GHOSTERY_TRACKERID_CRASHLYTICS);
            if (Fabric.isInitialized()) {    // Crashlytics is running in this session
                if (crashlyticsEnabled) {
                    // Toast the Crashlytics is enabled message (optional)
                    Toast.makeText(this, TOAST_CRASHLYTICS_ENABLE, Toast.LENGTH_LONG).show();
                } else {
                    // Remember to notify the user that an app restart is required to disable this tracker:
                    // To honor a user's withdrawn consent, if a tracker can NOT be turned off or
                    // disabled in the current session, you must notify the user that they will
                    // continue to be tracked until the app is restarted. Then when the app is
                    // restarted, don't start that tracker.
                    appRestartRequired = true;
                }
            } else { // Crashlytics has never been started in this session
                if (crashlyticsEnabled) {
                    // Start the Crashlytics tracker as specified by the user
                    Fabric.with(this, new Crashlytics());

                    // Toast the Crashlytics is enabled message (optional)
                    Toast.makeText(this, TOAST_CRASHLYTICS_ENABLE, Toast.LENGTH_LONG).show();
                } else {
                    // Do nothing: Crashlytics is disabled and not running

                    // Toast the Crashlytics is disabled message (optional)
                    Toast.makeText(this, TOAST_CRASHLYTICS_DISABLE, Toast.LENGTH_LONG).show();
                }
            }

        } else {
            Toast.makeText(activity, TOAST_TEXT_NOPREFS, Toast.LENGTH_LONG).show();
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
            final String modeImplied = getResources().getString(R.string.mode_implied);
            final String modeExplicit = getResources().getString(R.string.mode_explicit);
            boolean isImplied = AppData.getString(AppData.APPDATA_CONSENT_FLOW_MODE, modeImplied).equals(modeImplied);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.settings_title)
                    .setMessage(getResources().getString(R.string.settings_current_mode) + " " + (isImplied? modeImplied : modeExplicit))
                    .setIcon(null)
                    .setPositiveButton(modeImplied, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            AppData.setString(AppData.APPDATA_CONSENT_FLOW_MODE, modeImplied);
                        }
                    })
                    .setNegativeButton(modeExplicit, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            AppData.setString(AppData.APPDATA_CONSENT_FLOW_MODE, modeExplicit);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return true;
        } else if (id == R.id.action_privacyPreferences) {
            appNotice.showManagePreferences();
            return true;
        } else if (id == R.id.action_resetAppNoticeSdk) {
            appNotice.resetSDK();
            return true;
//        } else if (id == R.id.action_forceCrash) {
//            throw new RuntimeException(getResources().getString(R.string.action_forceCrash_message));
        }

        return super.onOptionsItemSelected(item);
    }

    public static AppNotice getAppNotice() {
        return appNotice;
    }

    private void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

}

package com.chronosystems.nearbyapp;

import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.chronosystems.nearbyapp.utils.AppActivity;
import com.chronosystems.nearbyapp.utils.AppConstants;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

/**
 * SplashScreen Activity
 *
 * @author andrevaladas
 */
public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppActivity.go(this, MainActivity.class, AppConstants.TIMER.SPLASH_SCREEN_IN_MILLISECONDS, true);
    }


    public void getIdThread() {

        AdvertisingIdClient.Info adInfo = null;
        try {
            adInfo = AdvertisingIdClient.getAdvertisingIdInfo(this);

        } catch (IOException exception) {
            // Unrecoverable error connecting to Google Play services (e.g.,
            // the old version of the service doesn't support getting AdvertisingId).

        } catch (GooglePlayServicesRepairableException exception) {
            // Encountered a recoverable error connecting to Google Play services.

        } catch (GooglePlayServicesNotAvailableException exception) {
            // Google Play services is not available entirely.
        } catch (Exception e) {

        }

        Log.i("@@@@@@@@@@ ID", Build.ID);
        Log.i("@@@@@@@@@@ SERIAL", Build.SERIAL);
        Log.i("@@@@@@@@@@ InstanceID", InstanceID.getInstance(this).getId());
        Log.i("@@@@@@@@@@ SecureID", Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID));

        if (adInfo != null) {
            final String id = adInfo.getId();
            final boolean isLAT = adInfo.isLimitAdTrackingEnabled();
            Log.i("@@@@@@@@@@ ID_CLIENT", id);
        }
    }
}
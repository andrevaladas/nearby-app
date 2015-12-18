package com.chronosystems.nearbyapp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.chronosystems.nearbyapp.services.NearbyBroadcastService;

public class NearbyStartServiceReceiver extends BroadcastReceiver {

    public NearbyStartServiceReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final Intent service = new Intent(context, NearbyBroadcastService.class);
        context.startService(service);
    }
}

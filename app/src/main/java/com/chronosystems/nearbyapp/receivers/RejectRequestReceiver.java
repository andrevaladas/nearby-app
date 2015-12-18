package com.chronosystems.nearbyapp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.chronosystems.nearbyapp.events.RequestNotification;
import com.chronosystems.nearbyapp.utils.AppConstants;

public class RejectRequestReceiver extends BroadcastReceiver {

    private static final String TAG = RejectRequestReceiver.class.getSimpleName();

    public RejectRequestReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w(TAG, "Connection Invitation - Rejecting");
        RequestNotification.cancel(context);

        //TODO enviar cancelamento de requisição?
        final String remoteEndpointId = intent.getStringExtra(AppConstants.EXTRA.DEVICE_ENDPOINT);
        //Nearby.Connections.rejectConnectionRequest(NearbyMessagesClient.getGoogleApiClient(), remoteEndpointId);
    }
}

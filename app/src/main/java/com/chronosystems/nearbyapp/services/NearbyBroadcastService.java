package com.chronosystems.nearbyapp.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.chronosystems.nearbyapp.domain.builders.NearbyDeviceBuilder;
import com.chronosystems.nearbyapp.domain.messages.NearbyDevice;
import com.chronosystems.nearbyapp.domain.messages.NearbyMessage;
import com.chronosystems.nearbyapp.domain.wrappers.NearbyDeviceWrapper;
import com.chronosystems.nearbyapp.events.RequestNotification;
import com.chronosystems.nearbyapp.helper.QuickPrefsHelper;
import com.chronosystems.nearbyapp.utils.AppConstants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.messages.Message;

import static com.chronosystems.nearbyapp.utils.AppConstants.NEARBY.KEY_PUBLICATION_TASK;
import static com.chronosystems.nearbyapp.utils.AppConstants.NEARBY.NO_LONGER_PUBLISHING;
import static com.chronosystems.nearbyapp.utils.AppConstants.NEARBY.TASK_NONE;
import static com.chronosystems.nearbyapp.utils.AppConstants.NEARBY.TASK_PUBLISH;
import static com.chronosystems.nearbyapp.utils.AppConstants.NEARBY.TASK_REPUBLISH;
import static com.chronosystems.nearbyapp.utils.AppConstants.TIMER.BROADCAST_IN_MILLISECONDS;

/**
 * Nearby Service to broadcast nearby messages
 * <p>
 * Created by andrevaladas on 10/10/2015.
 */
public class NearbyBroadcastService extends Service implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = NearbyBroadcastService.class.getSimpleName();

    private NearbyConnectionsClient mNearbyConnectionsClient;
    private NearbyMessagesClient mNearbyMessagesClient;

    private ResetStateHandler mResetStateHandler;

    private NearbyDevice mMyNearbyDevice;
    private Message mMyNearbyMessage;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        Toast.makeText(this, "Nearby Broadcast Service Started", Toast.LENGTH_LONG).show();

        try {
            QuickPrefsHelper.getPreferenceManager(this).registerOnSharedPreferenceChangeListener(this);
            mResetStateHandler = new ResetStateHandler();

            startNearbyClient();

        } catch (Exception e) {
            stopAllClients();
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void startNearbyClient() {
        mNearbyConnectionsClient = new NearbyConnectionsClient(this, new NearbyConnectionsClient.NearbyBroadcastListener() {
            @Override
            public void onServiceConnected() {
                mMyNearbyDevice = NearbyDeviceBuilder.me(NearbyBroadcastService.this);
                mNearbyConnectionsClient.startAdvertising(mMyNearbyDevice);

                mMyNearbyDevice.setDeviceId(mNearbyConnectionsClient.getMyDeviceId());
                mMyNearbyDevice.setEndpointId(mNearbyConnectionsClient.getMyEndpointId());

                // Starts Nearby Messages Client with Nearby Connections endpoint
                startNearbyMessagesClient();
            }

            @Override
            public void onServiceConnectionFailed(ConnectionResult connectionResult) {
                mMyNearbyDevice = NearbyDeviceBuilder.me(NearbyBroadcastService.this);

                // Starts Nearby Messages Client just for Bluetooth Connections
                startNearbyMessagesClient();
            }

            @Override
            public void onConnectionRequest(String remoteEndpointId, String remoteDeviceId, String remoteEndpointName, byte[] payload) {
                //On connection request, show notification
                NearbyDevice removeDevice = NearbyDeviceBuilder.with(NearbyBroadcastService.this)
                        .nearbyConnections()
                        .deviceId(remoteDeviceId)
                        .endpointId(remoteEndpointId)
                        .name(remoteEndpointName)
                        .build();

                RequestNotification.notify(NearbyBroadcastService.this, new NearbyDeviceWrapper(removeDevice));
            }
        });
    }

    private void startNearbyMessagesClient() {
        mNearbyMessagesClient = new NearbyMessagesClient(this, new NearbyMessagesClient.NearbyPublishListener() {
            @Override
            public void onServiceConnected() {
                mMyNearbyMessage = NearbyMessage.newNearbyMessage(mMyNearbyDevice);
                mNearbyMessagesClient.startPublish(mMyNearbyMessage);
            }

            @Override
            public void onPublished() {
                // Send a message to the handler to change state when done publishing.
                sendMessageToHandler(NO_LONGER_PUBLISHING, BROADCAST_IN_MILLISECONDS);
            }

            @Override
            public void onUnpublished() {
                updateTask(KEY_PUBLICATION_TASK, TASK_NONE);
            }

            @Override
            public void onError(Status status) {
                handleUnsuccessfulNearbyResult(status);
            }
        });
    }

    private void stopAllClients() {
        if (mNearbyConnectionsClient != null) {
            mNearbyConnectionsClient.onStop();
        }
        if (mNearbyMessagesClient != null) {
            mNearbyMessagesClient.onStop();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, final String key) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.equals(key, KEY_PUBLICATION_TASK)) {
                    executePendingPublicationTask();
                }
            }
        });
    }

    private void executePendingPublicationTask() {
        String pendingPublicationTask = getPubSubTask(KEY_PUBLICATION_TASK);
        if (TextUtils.equals(pendingPublicationTask, TASK_PUBLISH)) {
            mNearbyMessagesClient.startPublish(mMyNearbyMessage);
        } else if (TextUtils.equals(pendingPublicationTask, TASK_REPUBLISH)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    // verify auto broadcast
                    final Boolean mAutoBroadcast = QuickPrefsHelper.get(getApplicationContext(), AppConstants.PREF.AUTO_BROADCAST_SWITCH, true);
                    if (mAutoBroadcast) {
                        updateTask(KEY_PUBLICATION_TASK, TASK_PUBLISH);
                    } else {
                        // recall execution
                        executePendingPublicationTask();
                    }
                }
            }, AppConstants.TIMER.WAKEUP_IN_MILLISECONDS);
        }
    }

    private String getPubSubTask(String taskKey) {
        return QuickPrefsHelper.get(this, taskKey, TASK_NONE);
    }

    private void updateTask(String key, String value) {
        QuickPrefsHelper.update(this, key, value);
    }

    private void sendMessageToHandler(int messageID, int ttlInMilliseconds) {
        android.os.Message msg = mResetStateHandler.obtainMessage(messageID);
        mResetStateHandler.sendMessageDelayed(msg, ttlInMilliseconds);
    }

    /**
     * Handles errors generated when performing a subscription or publication action. Uses
     * {@link Status#startResolutionForResult} to display an opt-in dialog to handle the case
     * where a device is not opted into using Nearby.
     */
    private void handleUnsuccessfulNearbyResult(Status status) {
        Log.d(TAG, "processing error, status = " + status);

        if (status.getStatusCode() == ConnectionResult.NETWORK_ERROR) {
            Toast.makeText(this,
                    "No connectivity, cannot proceed. Fix in 'Settings' and try again.",
                    Toast.LENGTH_LONG).show();

            updateTask(KEY_PUBLICATION_TASK, TASK_NONE);
        } else {
            // To keep things simple, pop a toast for all other error messages.
            Toast.makeText(this, "Unsuccessful: " + status.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private class ResetStateHandler extends Handler {

        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case NO_LONGER_PUBLISHING:
                    updateTask(KEY_PUBLICATION_TASK, TASK_REPUBLISH);
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Nearby.Messages Service Destroyed", Toast.LENGTH_LONG).show();
        stopAllClients();
        super.onDestroy();
    }
}
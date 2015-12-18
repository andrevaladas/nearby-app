package com.chronosystems.nearbyapp.services;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.chronosystems.nearbyapp.domain.enumerations.ConnectorType;
import com.chronosystems.nearbyapp.domain.messages.NearbyDevice;
import com.chronosystems.nearbyapp.domain.messages.NearbyMessage;
import com.chronosystems.nearbyapp.utils.AppActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.NearbyMessagesStatusCodes;
import com.google.android.gms.nearby.messages.Strategy;

import static com.chronosystems.nearbyapp.utils.AppConstants.TIMER.BROADCAST_IN_SECONDS;
import static com.chronosystems.nearbyapp.utils.AppConstants.TIMER.DISCOVERY_IN_SECONDS;

/**
 * Class Manager Nearby Messages
 * <p/>
 * Created by andrevaladas on 10/10/2015.
 */
public class NearbyMessagesClient extends MessageListener implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public interface NearbyListener {
        void onServiceConnected();
    }

    public interface NearbyPermissionListener extends NearbyListener {
        void onResolvingNearbyPermissionError(Status status);
    }

    public interface NearbyPublishListener extends NearbyListener {
        void onPublished();

        void onUnpublished();

        void onError(Status status);
    }

    public interface NearbyDiscoveryListener extends NearbyListener {
        void onFound(NearbyDevice nearbyDevice);

        void onLost(NearbyDevice nearbyDevice);
    }

    private static final String TAG = NearbyMessagesClient.class.getSimpleName();

    private static final Strategy PUB_STRATEGY = new Strategy.Builder()
            .setTtlSeconds(BROADCAST_IN_SECONDS)
            .build();
    private static final Strategy SUB_STRATEGY = new Strategy.Builder()
            .setTtlSeconds(DISCOVERY_IN_SECONDS)
            .build();

    private static Context mContext;
    private static GoogleApiClient mGoogleApiClient;
    private NearbyListener mListener;
    private NearbyPermissionListener mNearbyPermissionListener;
    private NearbyPublishListener mNearbyPublishListener;
    private NearbyDiscoveryListener mNearbyDiscoveryListener;

    private boolean mResolvingNearbyPermissionError = false;

    public NearbyMessagesClient(Context context, NearbyPermissionListener listener) {
        mListener = listener;
        mNearbyPermissionListener = listener;

        connectServiceInstance(context);
    }

    public NearbyMessagesClient(Context context, NearbyPublishListener listener) {
        mListener = listener;
        mNearbyPublishListener = listener;

        connectServiceInstance(context);
    }

    public NearbyMessagesClient(Context context, NearbyDiscoveryListener listener) {
        mListener = listener;
        mNearbyDiscoveryListener = listener;

        connectServiceInstance(context);
    }

    private void connectServiceInstance(Context context) {
        if (mGoogleApiClient == null) {
            mContext = context;

            mGoogleApiClient = new GoogleApiClient.Builder(mContext, this, this)
                    .addApi(Nearby.MESSAGES_API)
                    .build();
            mGoogleApiClient.connect();
        } else if (mGoogleApiClient.isConnected()) {
            AppActivity.actionWhenConnected(new Runnable() {
                @Override
                public void run() {
                    mListener.onServiceConnected();
                }
            });
        }
    }

    /**
     * Publishes device information to nearby devices.
     */
    public void startPublish(Message me) {
        Log.d(TAG, "startBroadcast");
        Toast.makeText(mContext, "NearbyMessagesClient.startBroadcast", Toast.LENGTH_LONG).show();
        Nearby.Messages.publish(mGoogleApiClient, me, PUB_STRATEGY)
                .setResultCallback(new ResultCallback<Status>() {

                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.d(TAG, "published successfully");
                            mNearbyPublishListener.onPublished();
                        } else {
                            Log.w(TAG, "could not publish");
                            mNearbyPublishListener.onError(status);
                        }
                    }
                });
    }

    /**
     * Stops publishing device information to nearby devices.
     */
    public void stopPublish(Message me) {
        Log.d(TAG, "stopBroadcast");
        Toast.makeText(mContext, "NearbyMessagesClient.stopBroadcast", Toast.LENGTH_LONG).show();

        Nearby.Messages.unpublish(mGoogleApiClient, me)
                .setResultCallback(new ResultCallback<Status>() {

                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.d(TAG, "unpublished successfully");
                            mNearbyPublishListener.onUnpublished();
                        } else {
                            Log.w(TAG, "could not stopBroadcast");
                            mNearbyPublishListener.onError(status);
                        }
                    }
                });
    }

    /**
     * Begin discovering Nearby Connections/Messages.
     */
    public void startDiscovery() {
        Log.d(TAG, "Starts Nearby discovery service");

        Nearby.Messages.subscribe(mGoogleApiClient, this, SUB_STRATEGY)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.d(TAG, "subscribed successfully");
                        } else {
                            Log.w(TAG, "could not subscribe");
                        }
                    }
                });

    }

    /**
     * Stop discovering Nearby Connections/Messages.
     */
    public void stopDiscovery() {
        Log.d(TAG, "Stops Nearby discovery service");

        Nearby.Messages.unsubscribe(mGoogleApiClient, this)
                .setResultCallback(new ResultCallback<Status>() {

                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.d(TAG, "unsubscribed successfully");
                        } else {
                            Log.w(TAG, "could not unsubscribe");
                        }
                    }
                });
    }

    public void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
            mGoogleApiClient = null;
        }
    }

    /**
     * A simple ResultCallback that displays a toast when errors occur.
     * It also displays the Nearby opt-in dialog when necessary.
     */
    class ErrorCheckingCallback implements ResultCallback<Status> {

        private Runnable runOnSuccess;

        ErrorCheckingCallback(Runnable run) {
            this.runOnSuccess = run;
        }

        @Override
        public void onResult(@NonNull Status status) {
            if (status.isSuccess()) {
                if (runOnSuccess != null) {
                    runOnSuccess.run();
                }
            } else {
                if (status.getStatusCode() == NearbyMessagesStatusCodes.APP_NOT_OPTED_IN) {
                    if (!mResolvingNearbyPermissionError && mNearbyPermissionListener != null) {
                        mResolvingNearbyPermissionError = true;
                        mNearbyPermissionListener.onResolvingNearbyPermissionError(status);
                    }
                } else {
                    if (status.getStatusCode() == ConnectionResult.NETWORK_ERROR) {
                        Toast.makeText(mContext, "No connectivity, cannot proceed. Fix in 'Settings' and try again.", Toast.LENGTH_LONG).show();
                    } else {
                        // To keep things simple, pop a toast for all other error messages.
                        Toast.makeText(mContext, "Unsuccessful: " + status.getStatusMessage(), Toast.LENGTH_LONG).show();
                    }

                }
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");
        if (mContext == null) {
            return;
        }

        // Verify permission
        Nearby.Messages.getPermissionStatus(mGoogleApiClient).setResultCallback(
                new ErrorCheckingCallback(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onServiceConnected();
                    }
                })
        );
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "GoogleApiClient connection suspended: " + connectionSuspendedCauseToString(cause));
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    private String connectionSuspendedCauseToString(int cause) {
        switch (cause) {
            case GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST:
                return "CAUSE_NETWORK_LOST";
            case GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED:
                return "CAUSE_SERVICE_DISCONNECTED";
            default:
                return "CAUSE_UNKNOWN: " + cause;
        }
    }

    public void finishedResolvingNearbyPermissionError() {
        mResolvingNearbyPermissionError = false;
    }

    @Override
    public void onFound(Message message) {
        Log.d(TAG, "onFound:" + message);

        final NearbyDevice nearbyDevice = NearbyMessage.fromNearbyMessage(message);
        if (TextUtils.isEmpty(nearbyDevice.getEndpointId())) {
            nearbyDevice.setConnectorType(ConnectorType.BLUETOOTH);
        } else {
            nearbyDevice.setConnectorType(ConnectorType.CONNECTIONS);
        }
        mNearbyDiscoveryListener.onFound(nearbyDevice);
    }

    @Override
    public void onLost(Message message) {
        Log.d(TAG, "onLost:" + message);

        mNearbyDiscoveryListener.onLost(NearbyMessage.fromNearbyMessage(message));
    }
}
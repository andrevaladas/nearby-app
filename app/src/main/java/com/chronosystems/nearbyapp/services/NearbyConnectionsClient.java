/*
 * Copyright (C) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chronosystems.nearbyapp.services;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.chronosystems.nearbyapp.R;
import com.chronosystems.nearbyapp.conversation.ConnectionsMessageListener;
import com.chronosystems.nearbyapp.domain.builders.NearbyDeviceBuilder;
import com.chronosystems.nearbyapp.domain.messages.NearbyDevice;
import com.chronosystems.nearbyapp.helper.QuickPrefsHelper;
import com.chronosystems.nearbyapp.interfaces.AppChatMessage;
import com.chronosystems.nearbyapp.utils.AppActivity;
import com.chronosystems.nearbyapp.utils.AppConstants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AppIdentifier;
import com.google.android.gms.nearby.connection.AppMetadata;
import com.google.android.gms.nearby.connection.Connections;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A wrapper for a GoogleApiClient that communicates with the Nearby Connections API.
 */
public class NearbyConnectionsClient implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        Connections.ConnectionRequestListener,
        Connections.EndpointDiscoveryListener,
        AppChatMessage {

    interface NearbyListener {
        /**
         * GoogleApiClient is connected, safe to start using the Nearby API through the client.
         */
        void onServiceConnected();

        void onServiceConnectionFailed(ConnectionResult connectionResult);
    }

    interface NearbyBroadcastListener extends NearbyListener {
        void onConnectionRequest(final String remoteEndpointId, final String remoteDeviceId,
                                 final String remoteEndpointName, byte[] payload);
    }

    public interface NearbyRequestListener extends NearbyListener {

        /**
         * Connected to a remote endpoint.
         *
         * @param endpointId   the endpoint id of the remote endpoint.
         * @param deviceId     the device id of the remote endpoint.
         * @param endpointName the display name of the remote endpoint.
         */
        void onConnectedToEndpoint(String endpointId, String deviceId, String endpointName);

        /**
         * Failed to connect to a remote endpoint.
         *
         * @param endpointId   the endpoint id of the remote endpoint.
         * @param deviceId     the device id of the remote endpoint.
         * @param endpointName the display name of the remote endpoint.
         */
        void onFailedToConnectToEndpoint(String endpointId, String deviceId, String endpointName);
    }

    public interface NearbyDiscoveryListener extends NearbyRequestListener {

        void onEndpointFound(final String endpointId, final String deviceId,
                             String serviceId, final String endpointName);

        void onEndpointLost(String remoteEndpointId);
    }

    private static final String TAG = NearbyConnectionsClient.class.getSimpleName();

    public static final int STATE_IDLE = 8000;
    public static final int STATE_DISCOVERING = 8001;
    public static final int STATE_ADVERTISING = 8002;

    // The Google API Client for connecting to the Nearby Connections API
    private static GoogleApiClient mGoogleApiClient;

    // The Context for displaying Toasts and Dialogs
    private static Context mContext;

    // A listener to receive events such as messages and connect/disconnect events.
    private NearbyListener mListener;
    private NearbyBroadcastListener mBroadcastListener;
    private NearbyDiscoveryListener mDiscoveryListener;
    private NearbyRequestListener mRequestListener;

    private ConnectionsMessageListener mConnectionsMessageListener = new ConnectionsMessageListener();

    // The state of the NearbyConnectionsClient (one of STATE_IDLE, STATE_DISCOVERING, or STATE_ADVERTISING)
    private int mState;

    {
        Log.d(TAG, "CREATE >>> NearbyConnectionsClient");
    }

    /**
     * Create a new NearbyConnectionsClient.
     *
     * @param context  the creating context, generally an Activity or Fragment.
     * @param listener a NearbyConversationListener to be notified of events.
     */
    public NearbyConnectionsClient(Context context, NearbyRequestListener listener) {
        mListener = listener;
        mRequestListener = listener;

        connectServiceInstance(context);
    }

    public NearbyConnectionsClient(Context context, NearbyBroadcastListener listener) {
        mListener = listener;
        mBroadcastListener = listener;

        connectServiceInstance(context);
    }

    public NearbyConnectionsClient(Context context, NearbyDiscoveryListener listener) {
        mListener = listener;
        mRequestListener = listener;
        mDiscoveryListener = listener;

        connectServiceInstance(context);
    }

    private void connectServiceInstance(Context context) {
        if (mGoogleApiClient == null) {

            mContext = context;
            mState = STATE_IDLE;

            mGoogleApiClient = new GoogleApiClient.Builder(mContext, this, this)
                    .addApi(Nearby.CONNECTIONS_API)
                    .build();
            mGoogleApiClient.connect();

        } else if (mGoogleApiClient.isConnected()) {
            AppActivity.actionWhenConnected(new Runnable() {
                @Override
                public void run() {
                    mListener.onServiceConnected();
                }
            });
        } else if (!mGoogleApiClient.isConnecting()) {
            AppActivity.actionWhenConnected(new Runnable() {
                @Override
                public void run() {
                    mListener.onServiceConnectionFailed(new ConnectionResult(ConnectionResult.NETWORK_ERROR, null));
                }
            });
        }
    }

    public ConnectionsMessageListener getConnectionsMessageListener() {
        return mConnectionsMessageListener;
    }

    /**
     * Begin advertising for Nearby Connections.
     */
    public void startAdvertising(NearbyDevice nearbyDevice) {

        // Advertising with an AppIdentifer lets other devices on the
        // network discover this application and prompt the user to
        // install the application.
        List<AppIdentifier> appIdentifierList = new ArrayList<>();
        appIdentifierList.add(new AppIdentifier(mContext.getPackageName()));
        AppMetadata appMetadata = new AppMetadata(appIdentifierList);

        String myName = nearbyDevice.getName();
        long NO_TIMEOUT = 0L;
        Nearby.Connections.startAdvertising(mGoogleApiClient, myName, appMetadata, NO_TIMEOUT, this)
                .setResultCallback(new ResultCallback<Connections.StartAdvertisingResult>() {
                    @Override
                    public void onResult(Connections.StartAdvertisingResult result) {
                        if (result.getStatus().isSuccess()) {
                            Log.d(TAG, "Advertising as " + result.getLocalEndpointName());
                            mState = STATE_ADVERTISING;
                        } else {
                            Log.w(TAG, "Failed to start advertising: " + result.getStatus());
                            mState = STATE_IDLE;
                        }
                    }
                });
    }

    /**
     * Stop advertising for Nearby connections.
     */
    public void stopAdvertising() {
        mState = STATE_IDLE;
        Nearby.Connections.stopAdvertising(mGoogleApiClient);
    }

    /**
     * Begin discovering Nearby Connections.
     */
    public void startDiscovery() {
        long NO_TIMEOUT = 0L;
        String mDiscoveringServiceId = mContext.getString(R.string.service_id);
        Nearby.Connections.startDiscovery(mGoogleApiClient, mDiscoveringServiceId, NO_TIMEOUT, this)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.d(TAG, "Started discovery.");
                            mState = STATE_DISCOVERING;
                        } else {
                            Log.w(TAG, "Failed to start discovery: " + status);
                            mState = STATE_IDLE;
                        }
                    }
                });
    }

    /**
     * Stop discovering Nearby Connections.
     */
    public void stopDiscovery() {
        mState = STATE_IDLE;
        String mDiscoveringServiceId = mContext.getString(R.string.service_id);
        Nearby.Connections.stopDiscovery(mGoogleApiClient, mDiscoveringServiceId);
    }

    /**
     * Disconnect the Google API client, disconnect from all connected endpoints and stop
     * discovery/advertising when applicable.
     */
    public void onStop() {
        Log.d(TAG, "onStop");
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            // Stop advertising or discovering, disconnect from all endpoints.
            mGoogleApiClient.disconnect();
            mState = STATE_IDLE;
        }
    }

    /**
     * Send a message to a specific device.
     *
     * @param payload byte array to send as payload.
     */
    private void sendMessage(final String removeEndpointId, byte[] payload) {
        Nearby.Connections.sendReliableMessage(mGoogleApiClient, removeEndpointId, payload);
    }

    @Override
    public void sendMessage(String removeEndpointId, String message) {
        sendMessage(removeEndpointId, message.getBytes(Charset.forName("UTF-8")));
    }

    public String getMyEndpointId() {
        final String localEndpointId = Nearby.Connections.getLocalEndpointId(mGoogleApiClient);
        final String lastTime = AppConstants.MESSAGE_TIME_FORMAT.format(new Date()).replace(":", "h");

        Log.d(TAG, ">>>>>>>>>>>>>>>>>>> getMyEndpointId: " + localEndpointId);

        QuickPrefsHelper.update(mContext, AppConstants.NEARBY.MY_ENDPOINT_ID, localEndpointId);
        QuickPrefsHelper.update(mContext, AppConstants.NEARBY.MY_ENDPOINT_ID_LAST_TIME, lastTime);
        return localEndpointId;
    }

    public String getMyDeviceId() {
        return Nearby.Connections.getLocalDeviceId(mGoogleApiClient);
    }

    public int getState() {
        return mState;
    }

    /**
     * Interface ConnectionCallbacks
     **/
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");
        Log.d(TAG, "@@@@@@@@@@@@@@ NEARBY.CONNECTIONS CONFIG | MyDeviceId:" + getMyDeviceId() + "| MyEndpointId:" + getMyEndpointId());
        mListener.onServiceConnected();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended:" + i);
    }

    /**
     * Interface OnConnectionFailedListener
     **/
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed:" + connectionResult);
        mListener.onServiceConnectionFailed(connectionResult);
    }

    /**
     * Send a connection request to a remote endpoint. If the request is successful, notify the
     * listener and add the connection to the Set. Otherwise, show an error Toast.
     *
     * @param endpointId   the endpointID to connect to.
     * @param deviceId     the device ID of the endpoint to connect to.
     * @param endpointName the name of the endpoint to connect to.
     */
    public void connectTo(final String endpointId, final String deviceId,
                          final String endpointName) {

        Log.d(TAG, "Nearby.Connections connectTo:" + endpointId + ":" + endpointName);
        //TODO get local user name
        String myName = NearbyDeviceBuilder.me(mContext).getName();
        byte[] myPayload = null;
        Nearby.Connections.sendConnectionRequest(mGoogleApiClient, myName, endpointId, myPayload,
                new Connections.ConnectionResponseCallback() {
                    @Override
                    public void onConnectionResponse(String remoteEndpointId, Status status, byte[] payload) {
                        Log.d(TAG, "onConnectionResponse:" + remoteEndpointId + ":" + status);
                        if (status.isSuccess()) {

                            // Connection successful, notify listener
                            Toast.makeText(mContext, "Connected to: " + endpointName, Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onConnectionResponse:" + remoteEndpointId + ":" + status);

                            mRequestListener.onConnectedToEndpoint(remoteEndpointId, deviceId, endpointName);
                        } else {
                            // Connection not successful, show error
                            Toast.makeText(mContext, "Error: failed to connect! Status: " + status.getStatusCode(), Toast.LENGTH_SHORT).show();
                            mRequestListener.onFailedToConnectToEndpoint(remoteEndpointId, deviceId, endpointName);
                        }
                    }
                }, mConnectionsMessageListener);
    }

    /**
     * Interface ConnectionRequestListener
     **/
    @Override
    public void onConnectionRequest(final String remoteEndpointId, final String remoteDeviceId,
                                    final String remoteEndpointName, byte[] payload) {

        Log.d(TAG, "onConnectionRequest:" + remoteEndpointId + ":" + remoteDeviceId + ":" + remoteEndpointName);

        if (mBroadcastListener != null) {
            //TODO OPEN NOTIFICATION FOR USER CONFIRMATION
            mBroadcastListener.onConnectionRequest(remoteEndpointId, remoteDeviceId, remoteEndpointName, payload);

        } else {

            //TODO AUTO ACCEPT - CONVERSATION ON CHAT
            // The host accepts all connection requests it gets.
            onAcceptConnectionRequest(remoteEndpointId, remoteDeviceId, remoteEndpointName);
        }
    }

    public void onAcceptConnectionRequest(final String remoteEndpointId, final String remoteDeviceId, final String remoteEndpointName) {

        byte[] myPayload = null;
        Nearby.Connections.acceptConnectionRequest(mGoogleApiClient, remoteEndpointId, myPayload, mConnectionsMessageListener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.d(TAG, "acceptConnectionRequest:" + status + ":" + remoteEndpointId);
                        if (status.isSuccess()) {
                            Toast.makeText(mContext, "Connected to " + remoteEndpointName, Toast.LENGTH_SHORT).show();

                            // Notify listener
                            mRequestListener.onConnectedToEndpoint(remoteEndpointId, remoteDeviceId, remoteEndpointName);
                        } else {
                            Toast.makeText(mContext, "Failed to connect to: " + remoteEndpointName, Toast.LENGTH_SHORT).show();

                            mRequestListener.onFailedToConnectToEndpoint(remoteEndpointId, remoteDeviceId, remoteEndpointName);
                        }
                    }
                });
    }

    @Override
    public void onEndpointFound(final String endpointId, final String deviceId,
                                String serviceId, final String endpointName) {
        Log.d(TAG, "onEndpointFound:" + endpointId + ":" + deviceId + ":" + serviceId + ":" + endpointName);
        mDiscoveryListener.onEndpointFound(endpointId, deviceId, serviceId, endpointName);
    }

    @Override
    public void onEndpointLost(String remoteEndpointId) {
        Log.d(TAG, "onEndpointLost:" + remoteEndpointId);
        mDiscoveryListener.onEndpointLost(remoteEndpointId);
    }
}
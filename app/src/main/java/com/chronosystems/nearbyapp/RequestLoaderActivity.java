package com.chronosystems.nearbyapp;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.chronosystems.nearbyapp.components.loader.ProgressView;
import com.chronosystems.nearbyapp.conversation.ConversationManager;
import com.chronosystems.nearbyapp.domain.builders.NearbyDeviceBuilder;
import com.chronosystems.nearbyapp.domain.messages.NearbyDevice;
import com.chronosystems.nearbyapp.services.NearbyConnectionsClient;
import com.chronosystems.nearbyapp.utils.AppActivity;
import com.chronosystems.nearbyapp.utils.AppConstants;
import com.google.android.gms.common.ConnectionResult;

/**
 * Loader Activity
 *
 * @author andrevaladas
 */
public class RequestLoaderActivity extends AppCompatActivity {

    private static final String TAG = RequestLoaderActivity.class.getSimpleName();

    private NearbyDevice mNearbyDevice;
    private Bitmap mRemoteProfile;

    private NearbyConnectionsClient mNearbyConnectionsClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loader);

        String mRemoteDeviceId = getIntent().getStringExtra(AppConstants.EXTRA.DEVICE_ID);
        String mRemoteEndpointId = getIntent().getStringExtra(AppConstants.EXTRA.DEVICE_ENDPOINT);
        String mRemoteBluetooth = getIntent().getStringExtra(AppConstants.EXTRA.DEVICE_BLUETOOTH);
        String mRemoteName = getIntent().getStringExtra(AppConstants.EXTRA.DEVICE_NAME);
        Bitmap mRemoteProfile = getIntent().getParcelableExtra(AppConstants.EXTRA.DEVICE_PROFILE);

        mNearbyDevice = NearbyDeviceBuilder.with(this)
                .deviceId(mRemoteDeviceId)
                .endpointId(mRemoteEndpointId)
                .bluetooth(mRemoteBluetooth)
                .name(mRemoteName)
                .build();

        // load components
        final ImageView mLoaderProfile = (ImageView) findViewById(R.id.iv_loader_profile);
        mLoaderProfile.setImageBitmap(mRemoteProfile);

        final ProgressView mProgressBar = (ProgressView) findViewById(R.id.pv_loader);
        mProgressBar.setRefreshing(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!TextUtils.isEmpty(mNearbyDevice.getEndpointId())) {
            sendConnectionsRequestByHostDiscovery();
        } else {
            sendBluetoothRequest();
        }
    }

    private void sendConnectionsRequestByHostDiscovery() {
        mNearbyConnectionsClient = new NearbyConnectionsClient(this, new NearbyConnectionsClient.NearbyDiscoveryListener() {
            @Override
            public void onServiceConnected() {
                Log.d(TAG, "############# sendConnectionsRequestByHostDiscovery");
                mNearbyConnectionsClient.startDiscovery();
            }

            @Override
            public void onServiceConnectionFailed(ConnectionResult connectionResult) {
                // TODO try connect by Bluetooth
                Log.w(TAG, "onServiceConnectionFailed: Error code " + connectionResult.getErrorCode());
                Log.w(TAG, "try connect by Bluetooth...");
                sendBluetoothRequest();
            }

            @Override
            public void onEndpointFound(String endpointId, String deviceId, String serviceId, String endpointName) {
                Log.d(TAG, "############# onEndpointFound: " + endpointId);
                if (TextUtils.equals(endpointId, mNearbyDevice.getEndpointId())) {
                    mNearbyConnectionsClient.connectTo(endpointId, deviceId, endpointName);
                }
            }

            @Override
            public void onEndpointLost(String remoteEndpointId) {

            }

            @Override
            public void onConnectedToEndpoint(String endpointId, String deviceId, String endpointName) {

                ConversationManager.register(mNearbyDevice, mNearbyConnectionsClient.getConnectionsMessageListener());

                // Connection successful
                setResult(RESULT_OK);
                final Bundle extras = getIntent().getExtras();
                AppActivity.go(RequestLoaderActivity.this, ChatActivity.class, extras, 1, true);
            }

            @Override
            public void onFailedToConnectToEndpoint(String endpointId, String deviceId, String endpointName) {
                // Connection not successful, TODO show error
                Log.w(TAG, "onFailedToConnectToEndpoint: " + endpointId + ":" + deviceId + ":" + endpointName);
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    private void sendBluetoothRequest() {
        // Connection successful, notify listener
        Toast.makeText(this, "sendBluetoothRequest: not yet implemented : " + mNearbyDevice.getBluetooth(), Toast.LENGTH_SHORT).show();
        setResult(RESULT_CANCELED);
        finish();
    }
}
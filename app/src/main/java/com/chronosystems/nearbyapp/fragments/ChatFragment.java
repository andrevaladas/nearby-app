package com.chronosystems.nearbyapp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.chronosystems.nearbyapp.ChatActivity;
import com.chronosystems.nearbyapp.R;
import com.chronosystems.nearbyapp.adapters.ChatMessageAdapter;
import com.chronosystems.nearbyapp.conversation.AppMessageListener;
import com.chronosystems.nearbyapp.conversation.ConnectionsConnector;
import com.chronosystems.nearbyapp.conversation.Conversation;
import com.chronosystems.nearbyapp.conversation.ConversationManager;
import com.chronosystems.nearbyapp.domain.DeviceUser;
import com.chronosystems.nearbyapp.domain.builders.NearbyDeviceBuilder;
import com.chronosystems.nearbyapp.domain.enumerations.ConnectorType;
import com.chronosystems.nearbyapp.domain.messages.ChatMessage;
import com.chronosystems.nearbyapp.domain.messages.NearbyDevice;
import com.chronosystems.nearbyapp.domain.wrappers.NearbyDeviceWrapper;
import com.chronosystems.nearbyapp.events.RequestNotification;
import com.chronosystems.nearbyapp.services.NearbyConnectionsClient;
import com.chronosystems.nearbyapp.utils.AppConstants;
import com.google.android.gms.common.ConnectionResult;

/**
 * Manager Chat Conversation
 *
 * @author andrevaladas
 */
public class ChatFragment extends Fragment implements AppMessageListener {

    private static final String TAG = ChatFragment.class.getSimpleName();

    private NearbyConnectionsClient mNearbyConnectionsClient;
    private Conversation mConversation;

    private DeviceUser mDeviceUser;
    private NearbyDevice mRemoteDevice;
    private ChatMessageAdapter mMessageAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_chat, container, false);
        final NearbyDeviceWrapper deviceWrapper = ((ChatActivity) getActivity()).getRemoteDeviceWrapper();
        mRemoteDevice = deviceWrapper.getParent();
        mDeviceUser = deviceWrapper.getDeviceUser();

        final EditText mEtInputMessage = (EditText) view.findViewById(R.id.et_chat_input_message);
        view.findViewById(R.id.fab_chat_send).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // Sending message to remote device
                sendMessage(mEtInputMessage.getText().toString());

                // Clearing the input filed once message was sent
                mEtInputMessage.setText("");
            }
        });

        mMessageAdapter = new ChatMessageAdapter(getActivity(), mDeviceUser.getMessageList());
        final ListView mLvChatMessage = (ListView) view.findViewById(R.id.lv_chat_message);
        mLvChatMessage.setAdapter(mMessageAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        // verify conversation context
        checkConversationContext();

        // check connection
        if (!mConversation.isConnected()) {

            // choose the type of connection
            if (!TextUtils.isEmpty(mRemoteDevice.getEndpointId())) {
                startConnectionsConversation();
            } else {
                startBluetoothConversation();
            }
        }
    }

    private void checkConversationContext() {
        mConversation = ConversationManager.get(mRemoteDevice.getDeviceId());
        if (mConversation == null) {
            throw new RuntimeException("Conversation does not exist.");
        }
    }

    private void startConnectionsConversation() {
        mNearbyConnectionsClient = new NearbyConnectionsClient(getContext(), new NearbyConnectionsClient.NearbyRequestListener() {
            // NearbyListener
            @Override
            public void onServiceConnected() {
                Toast.makeText(getContext(), "ChatFragment.onServiceConnected", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onServiceConnectionFailed(ConnectionResult connectionResult) {
                Toast.makeText(getContext(), "ChatFragment.onServiceConnectionFailed", Toast.LENGTH_LONG).show();
                mConversation.disconnect();
            }

            // NearbyRequestListener
            @Override
            public void onConnectedToEndpoint(String endpointId, String deviceId, String endpointName) {
                Toast.makeText(getContext(), "ChatFragment.onConnectedToEndpoint", Toast.LENGTH_LONG).show();
                mConversation.connect();
            }

            @Override
            public void onFailedToConnectToEndpoint(String endpointId, String deviceId, String endpointName) {
                Toast.makeText(getContext(), "ChatFragment.onFailedToConnectToEndpoint", Toast.LENGTH_LONG).show();
                mConversation.disconnect();
            }
        });

        // verify client connector
        if (!mConversation.hasConnector()) {
            final ConnectorType type = mConversation.getType();
            if (ConnectorType.CONNECTIONS.equals(type)) {
                ((ConnectionsConnector) mConversation).registerConnectorListener(mNearbyConnectionsClient.getConnectionsMessageListener());
            }
        }

        // register chat controllers
        mConversation.registerMessageListener(this);
        mConversation.registerChatMessage(mNearbyConnectionsClient);

        // verify if an accepted connection
        final boolean accepted = getActivity().getIntent().getBooleanExtra(AppConstants.EXTRA.DEVICE_REQUEST_ACCEPTED, false);
        if (accepted) {

            // cancel the notification event
            RequestNotification.cancel(getContext());

            // auto-accept the connection request
            mNearbyConnectionsClient.onAcceptConnectionRequest(
                    mRemoteDevice.getEndpointId(),
                    mRemoteDevice.getDeviceId(),
                    mRemoteDevice.getName());
        }
    }

    private void startBluetoothConversation() {
        Toast.makeText(getContext(), TAG + ".startBluetoothConversation: not implemented yet: " + mRemoteDevice.getBluetooth(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMessageReceived(byte[] payload) {
        final ChatMessage chatMessage = new ChatMessage(mRemoteDevice.getName(), new String(payload), false);
        mMessageAdapter.add(chatMessage);
        mMessageAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDeviceDisconnected(String remoteEndpointId) {
        Toast.makeText(getContext(), TAG + ".onDeviceDisconnected", Toast.LENGTH_SHORT).show();
    }

    public synchronized void sendMessage(String message) {
        // Sending message to remote device
        mConversation.sendMessage(message);

        // Add message to chat list view
        String userName = NearbyDeviceBuilder.me(getContext()).getName();
        mMessageAdapter.add(new ChatMessage(userName, message, true));
        mMessageAdapter.notifyDataSetChanged();
    }
}
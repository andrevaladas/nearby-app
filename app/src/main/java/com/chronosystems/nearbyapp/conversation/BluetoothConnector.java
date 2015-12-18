/**
 *
 */
package com.chronosystems.nearbyapp.conversation;

import com.chronosystems.nearbyapp.domain.enumerations.ConnectorType;
import com.chronosystems.nearbyapp.domain.messages.NearbyDevice;

/**
 * Connector for Bluetooth Adapter
 *
 * @author andrevaladas
 */
public class BluetoothConnector extends AbstractConversation {

    private static final long serialVersionUID = 1L;

    /**
     * @param nearbyDevice
     */
    public BluetoothConnector(NearbyDevice nearbyDevice, AppListener listener) {
        super();
        nearbyDevice.setConnectorType(ConnectorType.BLUETOOTH);

        mNearbyDevice = nearbyDevice;
        mConnectorMessageListener = (ConnectionsMessageListener) listener;
    }

    @Override
    public ConnectorType getType() {
        return ConnectorType.BLUETOOTH;
    }

    @Override
    public void connect() {
        // TODO Auto-generated method stub
        if (!isConnected() && !isConnecting()) {
            setState(CONNECTING);
            setState(CONNECTED);
        }
    }

    @Override
    public void reconnect() {
        // TODO Auto-generated method stub
        connect();
    }

    @Override
    public void disconnect() {
        // TODO Auto-generated method stub
        if (isConnected()) {
            setState(DISCONNECTED);
        }
    }

    @Override
    public void sendMessage(String message) {
        // TODO Auto-generated method stub
        if (!isConnected()) {
            throw new RuntimeException("You're not connected.");
        }
        if (mChatMessage == null) {
            throw new RuntimeException("Has no chatMessage to send.");
        }

        mChatMessage.sendMessage(mNearbyDevice.getEndpointId(), message);
    }
}
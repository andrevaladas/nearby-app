/**
 *
 */
package com.chronosystems.nearbyapp.conversation;

import com.chronosystems.nearbyapp.domain.enumerations.ConnectorType;
import com.chronosystems.nearbyapp.domain.messages.NearbyDevice;

/**
 * Connector for Nearby.Connections
 *
 * @author andrevaladas
 */
public class ConnectionsConnector extends AbstractConversation {

    private static final long serialVersionUID = 1L;

    /**
     * @param nearbyDevice
     */
    public ConnectionsConnector(NearbyDevice nearbyDevice, AppListener listener) {
        super();
        nearbyDevice.setConnectorType(ConnectorType.CONNECTIONS);

        mNearbyDevice = nearbyDevice;
        mConnectorMessageListener = (ConnectionsMessageListener) listener;
    }

    @Override
    public ConnectorType getType() {
        return ConnectorType.CONNECTIONS;
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
            // Nearby.Connections.disconnectFromEndpoint(mGoogleApiClient, remoteEndpointId);
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
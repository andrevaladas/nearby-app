/**
 *
 */
package com.chronosystems.nearbyapp.conversation;

import com.google.android.gms.nearby.connection.Connections;

/**
 * @author andre.silva
 */
public class ConnectionsMessageListener implements Connections.MessageListener, AppListener {

    private AppMessageListener messageListener;

    public void registerDecorator(AppMessageListener listener) {
        this.messageListener = listener;
    }

    @Override
    public void onMessageReceived(String remoteEndpointId, byte[] payload, boolean isReliable) {
        if (messageListener != null) {
            messageListener.onMessageReceived(payload);
        }
    }

    @Override
    public void onDisconnected(String remoteEndpointId) {
        if (messageListener != null) {
            messageListener.onDeviceDisconnected(remoteEndpointId);
        }
    }
}

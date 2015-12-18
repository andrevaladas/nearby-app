/**
 *
 */
package com.chronosystems.nearbyapp.conversation;

import com.chronosystems.nearbyapp.domain.messages.NearbyDevice;

/**
 * @author andre.silva
 */
public class ConversationFactory {

    public Conversation create(NearbyDevice nearbyDevice, AppListener listener) {

        if (nearbyDevice.getDeviceId() != null) {
            return new ConnectionsConnector(nearbyDevice, listener);
        }
        return new BluetoothConnector(nearbyDevice, listener);
    }
}

/**
 *
 */
package com.chronosystems.nearbyapp.conversation;

import com.chronosystems.nearbyapp.domain.enumerations.ConnectorType;
import com.chronosystems.nearbyapp.domain.messages.NearbyDevice;
import com.chronosystems.nearbyapp.interfaces.AppChatMessage;

import java.io.Serializable;

/**
 * @author andre.silva
 */
public interface Conversation extends Serializable {

    ConnectorType getType();

    NearbyDevice getDevice();

    void setNearbyDevice(NearbyDevice nearbyDevice);

    boolean hasConnector();

    void connect();

    void reconnect();

    void disconnect();

    boolean isConnected();

    void keepAlive();

    void sendMessage(String message);

    void registerMessageListener(AppMessageListener messageListener);

    void registerChatMessage(AppChatMessage chatMessage);

}
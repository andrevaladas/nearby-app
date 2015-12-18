/**
 * 
 */
package com.chronosystems.nearbyapp.conversation;

/**
 * @author andre.silva
 *
 */
public interface AppMessageListener extends AppListener {
	void onMessageReceived(byte[] payload);

	void onDeviceDisconnected(String remoteEndpointId);
}

package com.chronosystems.nearbyapp.conversation;

import com.chronosystems.nearbyapp.domain.messages.NearbyDevice;
import com.chronosystems.nearbyapp.interfaces.AppChatMessage;

public abstract class AbstractConversation implements Conversation {

	private static final long serialVersionUID = 1L;

	protected static final int DISCONNECTED = 0;
	protected static final int CONNECTING = 1;
	protected static final int CONNECTED = 2;

	// five minutes in milliseconds
	private static final int DEFAULT_CONVERSATION_TIMEOUT = 1000 * 60 * 5;
	private int state = DISCONNECTED;
	private int timeout = 0;

	protected NearbyDevice mNearbyDevice;
	protected ConnectionsMessageListener mConnectorMessageListener;
	protected AppChatMessage mChatMessage;

	@Override
	public void keepAlive() {
		timeout = DEFAULT_CONVERSATION_TIMEOUT;
	}

	protected void setState(int state) {
		this.state = state;
	}

	public boolean isConnected() {
		return (state == CONNECTED);
	}

	public boolean isConnecting() {
		return (state == CONNECTING);
	}

	@Override
	public NearbyDevice getDevice() {
		return mNearbyDevice;
	}

	@Override
	public void setNearbyDevice(NearbyDevice nearbyDevice) {
		this.mNearbyDevice = nearbyDevice;
	}

	@Override
	public boolean hasConnector() {
		return this.mConnectorMessageListener != null;
	}

	public void registerConnectorListener(ConnectionsMessageListener listener) {
		this.mConnectorMessageListener = listener;
	}

	public void registerMessageListener(AppMessageListener messageListener) {
		if (!hasConnector()) {
			throw new RuntimeException("Has no connection listener.");
		}
		this.mConnectorMessageListener.registerDecorator(messageListener);
	}

	@Override
	public void registerChatMessage(AppChatMessage chatMessage) {
		this.mChatMessage = chatMessage;
	}
}

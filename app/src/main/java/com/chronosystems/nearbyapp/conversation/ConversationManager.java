/**
 *
 */
package com.chronosystems.nearbyapp.conversation;

import android.support.annotation.NonNull;

import com.chronosystems.nearbyapp.domain.messages.NearbyDevice;

/**
 * @author andre.silva
 */
public class ConversationManager {

    private static ConversationQueue conversations = new ConversationQueue();
    private static ConversationFactory factory = new ConversationFactory();

    public static Conversation register(NearbyDevice nearbyDevice) {
        return register(nearbyDevice, null);
    }

    public static Conversation register(NearbyDevice nearbyDevice, AppListener listener) {

        final Conversation conversation = factory.create(nearbyDevice, listener);
        //conversation.keepAlive();
        conversations.enqueue(conversation);

        return conversation;
    }

    public static Conversation get(@NonNull String deviceId) {
        return conversations.get(deviceId);
    }

    public static Conversation get(@NonNull NearbyDevice nearbyDevice) {

        Conversation conversation = conversations.get(nearbyDevice);
        if (conversation == null) {
            return register(nearbyDevice, null);
        }
        return conversation;
    }

    public static void invalidate(NearbyDevice nearbyDevice) {
        conversations.dequeue();
    }

    public static void registerConnectorListener(Conversation conversation, AppListener listener) {

    }
}

package com.chronosystems.nearbyapp.conversation;

import android.support.annotation.NonNull;

import com.chronosystems.nearbyapp.domain.messages.NearbyDevice;

import java.util.LinkedList;

public class ConversationQueue {

    private LinkedList<Conversation> list = new LinkedList<Conversation>();

    public void enqueue(@NonNull Conversation item) {
        list.addLast(item);
    }

    public Conversation dequeue() {
        return list.poll();
    }

    public Conversation get(@NonNull NearbyDevice nearbyDevice) {
        return get(nearbyDevice.getDeviceId());
    }

    public Conversation get(@NonNull String deviceId) {
        if (hasItems()) {
            for (Conversation conversation : list) {
                if (deviceId.equals(conversation.getDevice().getDeviceId())) {
                    return conversation;
                }

            }
        }
        return null;
    }

    public void invalidate(@NonNull NearbyDevice nearbyDevice) {
        final Conversation conversation = get(nearbyDevice);
        if (conversation != null) {
            list.remove(conversation);
        }
    }

    public boolean hasItems() {
        return !list.isEmpty();
    }

    public int size() {
        return list.size();
    }

    public void addItems(ConversationQueue q) {
        while (q.hasItems())
            list.addLast(q.dequeue());
    }
}
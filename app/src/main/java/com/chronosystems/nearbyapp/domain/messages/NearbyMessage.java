package com.chronosystems.nearbyapp.domain.messages;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.nearby.messages.Message;
import com.google.gson.Gson;

import java.nio.charset.Charset;

/**
 * Nearby Messages
 * <p>
 * <p>
 * Used to prepare the payload for a
 * {@link com.google.android.gms.nearby.messages.Message Nearby Message}. Adds a unique id (an
 * InstanceID) to the Message payload, which helps Nearby distinguish between multiple devices with
 * the same model name.
 * <p>
 * <p>
 * Created by andrevaladas on 09/10/2015.
 */
public class NearbyMessage {

    private static final Gson gson = new Gson();

    public static void verifyContentLength(@NonNull NearbyDevice nearbyDevice) {
        final byte[] content = gson.toJson(nearbyDevice).toString().getBytes(Charset.forName("UTF-8"));
        Log.e("CONTENT LIMIT 102400 | ", "" + content.length);
    }

    /**
     * Builds a new {@link Message} object using a unique identifier.
     */
    public static Message newNearbyMessage(@NonNull NearbyDevice nearbyDevice) {
        final byte[] content = gson.toJson(nearbyDevice).toString().getBytes(Charset.forName("UTF-8"));
        return new Message(content);
    }

    /**
     * Creates a {@code NearbyDevice} object from the string used to construct the payload to a
     * {@code Nearby} {@code Message}.
     */
    public static NearbyDevice fromNearbyMessage(@NonNull Message message) {
        String nearbyMessageString = new String(message.getContent()).trim();
        return gson.fromJson(
                (new String(nearbyMessageString.getBytes(Charset.forName("UTF-8")))),
                NearbyDevice.class);
    }
}
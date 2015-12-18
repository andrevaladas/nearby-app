package com.chronosystems.nearbyapp.events;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.chronosystems.nearbyapp.ChatActivity;
import com.chronosystems.nearbyapp.R;
import com.chronosystems.nearbyapp.domain.messages.NearbyDevice;
import com.chronosystems.nearbyapp.domain.wrappers.NearbyDeviceWrapper;
import com.chronosystems.nearbyapp.receivers.RejectRequestReceiver;
import com.chronosystems.nearbyapp.utils.AppConstants;

/**
 * Helper class for showing and canceling app
 * notifications.
 * <p>
 * This class makes heavy use of the {@link NotificationCompat.Builder} helper
 * class to create notifications in a backward-compatible way.
 */
public class RequestNotification {
    /**
     * The unique identifier for this type of notification.
     */
    private static final String NOTIFICATION_TAG = RequestNotification.class.getSimpleName();

    /**
     * Shows the request notification.
     */
    public static void notify(final Context context,
                              final NearbyDeviceWrapper nearbyDeviceWrapper) {
        final Resources res = context.getResources();
        final Bitmap picture = BitmapFactory.decodeResource(res, R.drawable.ic_nearby_app);

        //TODO ajustar icones/textos e parametros
        final NearbyDevice nearbyDevice = nearbyDeviceWrapper.getParent();
        final String name = nearbyDevice.getName();
        final String title = res.getString(R.string.request_notification_title);
        final String text = res.getString(R.string.request_notification_placeholder_text, name);

        final Bundle extras = new Bundle();
        extras.putBoolean(AppConstants.EXTRA.DEVICE_REQUEST_ACCEPTED, true);
        extras.putString(AppConstants.EXTRA.DEVICE_ID, nearbyDevice.getDeviceId());
        extras.putString(AppConstants.EXTRA.DEVICE_ENDPOINT, nearbyDevice.getEndpointId());
        extras.putString(AppConstants.EXTRA.DEVICE_BLUETOOTH, nearbyDevice.getBluetooth());
        extras.putString(AppConstants.EXTRA.DEVICE_NAME, name);
        extras.putParcelable(AppConstants.EXTRA.DEVICE_PROFILE, nearbyDeviceWrapper.getProfileBitmap());

        final Intent rejectIntent = new Intent(context, RejectRequestReceiver.class);
        rejectIntent.putExtras(extras);

        final Intent acceptIntent = new Intent(context, ChatActivity.class);
        acceptIntent.putExtras(extras);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setDefaults(Notification.DEFAULT_ALL)
                .setCategory(Notification.CATEGORY_CALL)
                .setVisibility(Notification.VISIBILITY_PRIVATE)
                .setSmallIcon(R.drawable.ic_nearby_app)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setLargeIcon(picture)
                .setTicker(name)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(text)
                        .setBigContentTitle(title))
                .setContentIntent(
                        PendingIntent.getActivity(
                                context,
                                0,
                                acceptIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT))

                .addAction(
                        R.drawable.ic_action_stat_reply,
                        res.getString(R.string.action_reject),
                        PendingIntent.getBroadcast(
                                context,
                                0,
                                rejectIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT))
                .addAction(
                        R.drawable.ic_action_stat_reply,
                        res.getString(R.string.action_accept),
                        PendingIntent.getActivity(
                                context,
                                0,
                                acceptIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT))

                .setOnlyAlertOnce(true)
                .setAutoCancel(true);

        notify(context, builder.build());
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private static void notify(final Context context, final Notification notification) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.notify(NOTIFICATION_TAG, 0, notification);
        } else {
            nm.notify(NOTIFICATION_TAG.hashCode(), notification);
        }
    }

    /**
     * Cancels any notifications of this type previously shown using
     * {@link #notify(Context, NearbyDeviceWrapper)}.
     */
    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public static void cancel(final Context context) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.cancel(NOTIFICATION_TAG, 0);
        } else {
            nm.cancel(NOTIFICATION_TAG.hashCode());
        }
    }
}

package com.chronosystems.nearbyapp.utils;

import java.text.SimpleDateFormat;

/**
 * Application NearbyConstants
 * <p/>
 * Created by andrevaladas on 08/10/2015.
 */
public interface AppConstants {

    interface PREF {
        String AUTO_BROADCAST_SWITCH = "broadcast_switch";
    }

    interface TIMER {
        int SPLASH_SCREEN_IN_MILLISECONDS = 3 * 1000; // splash screen delay time

        int BROADCAST_IN_SECONDS = 1 * 91;
        int BROADCAST_IN_MILLISECONDS = BROADCAST_IN_SECONDS * 1000;

        int DISCOVERY_IN_SECONDS = 1 * 91;
        int DISCOVERY_IN_MILLISECONDS = DISCOVERY_IN_SECONDS * 1000;

        int WAKEUP_IN_SECONDS = 1 * 30;
        int WAKEUP_IN_MILLISECONDS = WAKEUP_IN_SECONDS * 1000;

        int DELAY_ACTION_WHEN_CONNECTED_IN_MS = 3 * 1000;
    }

    interface NEARBY {

        // Request code to use when launching the resolution activity.
        int REQUEST_RESOLVE_ERROR = 1001;

        // Keys to get and set the current publication tasks using SharedPreferences.
        String KEY_PUBLICATION_TASK = "publication_task";

        // NearbyConstants for publication tasks.
        String TASK_PUBLISH = "task_publish";
        String TASK_REPUBLISH = "task_republish";
        String TASK_NONE = "task_none";

        // Values used in setting state after publishing.
        int NO_LONGER_PUBLISHING = 56789;

        // Values for Nearby.Connections
        String MY_ENDPOINT_ID = "my_endpoint_id";
        String MY_ENDPOINT_ID_LAST_TIME = "my_endpoint_id_last_time";
    }

    // INTENT EXTRA
    interface EXTRA {
        String DEVICE_ID = "device_id";
        String DEVICE_ENDPOINT = "device_endpoint";
        String DEVICE_BLUETOOTH = "device_bluetooth_address";
        String DEVICE_NAME = "device_name";
        String DEVICE_PROFILE = "device_profile";

        String DEVICE_REQUEST_ACCEPTED = "device_request_accept";
    }

    interface ACTIVITY {
        int REQUEST_RESULT_CODE = 12345;
    }

    interface LOADER {
        int[] DEFAULT_COLORS = new int[]{
                android.R.color.holo_green_dark,
                android.R.color.holo_purple,
                android.R.color.holo_orange_light
        };
    }

    SimpleDateFormat MESSAGE_TIME_FORMAT = new SimpleDateFormat("HH:mm");

}

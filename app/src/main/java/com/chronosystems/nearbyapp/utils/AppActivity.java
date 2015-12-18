package com.chronosystems.nearbyapp.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.chronosystems.nearbyapp.R;

import static com.chronosystems.nearbyapp.utils.AppConstants.TIMER.DELAY_ACTION_WHEN_CONNECTED_IN_MS;

/**
 * Class to manager change between activity views
 *
 * @author andrevaladas on 01/10/2015.
 */
public class AppActivity {

    private static final int DEFAULT_DISPLAY_TIME = 1;

    public static void go(final Activity activity, final Class targetClass) {
        go(activity, targetClass, DEFAULT_DISPLAY_TIME, false);
    }

    public static void go(final Activity parent, final Class targetClass, final int delayTime, final boolean finishParent) {

        go(parent, targetClass, null, delayTime, finishParent);
    }

    public static void go(final Activity parent, final Class targetClass, final Bundle extras, final int delayTime, final boolean finishParent) {
       go(parent, targetClass, extras, -1, delayTime, finishParent);
    }

    public static void go(final Activity parent, final Class targetClass, final Bundle extras, final int requestCode, final int delayTime, final boolean finishParent) {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                final Intent intent = new Intent(parent, targetClass);
                if (extras != null) {
                    intent.putExtras(extras);
                }

                if (requestCode > 0) {
                    parent.startActivityForResult(intent, requestCode);
                } else {
                    parent.startActivity(intent);
                }
                if (finishParent) {
                    parent.finish();
                }

                // transition from splash to main menu
                parent.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            }
        }, delayTime);
    }

    public static void actionWhenConnected(Runnable runnable) {
        new Handler().postDelayed(runnable, DELAY_ACTION_WHEN_CONNECTED_IN_MS);
    }
}

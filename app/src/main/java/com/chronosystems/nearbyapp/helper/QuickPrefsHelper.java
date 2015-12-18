/**
 *
 */
package com.chronosystems.nearbyapp.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * @author Andre Valadas
 */
public class QuickPrefsHelper {

    public static SharedPreferences getPreferenceManager(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Helper for editing entries in SharedPreferences.
     */
    public static void update(final Context context, String key, String value) {
        getPreferenceManager(context)
                .edit()
                .putString(key, value)
                .apply();
    }

    public static void update(final Context context, String key, Boolean value) {
        getPreferenceManager(context)
                .edit()
                .putBoolean(key, value)
                .apply();
    }

    public static String get(final Context context, String key, String defaultKey) {
        return getPreferenceManager(context).getString(key, defaultKey);
    }

    public static Boolean get(final Context context, String key, Boolean defaultKey) {
        return getPreferenceManager(context).getBoolean(key, defaultKey);
    }
}

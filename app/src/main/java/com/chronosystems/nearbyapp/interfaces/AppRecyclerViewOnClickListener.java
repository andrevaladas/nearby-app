package com.chronosystems.nearbyapp.interfaces;

import android.view.View;

/**
 * Custom Recycle View Click Listener
 *
 * @author andrevaladas
 */
public interface AppRecyclerViewOnClickListener {
    void onClickListener(View view, int position);

    void onLongPressClickListener(View view, int position);
}

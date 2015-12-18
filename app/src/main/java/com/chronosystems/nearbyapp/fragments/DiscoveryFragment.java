package com.chronosystems.nearbyapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.chronosystems.nearbyapp.R;
import com.chronosystems.nearbyapp.RequestActivity;
import com.chronosystems.nearbyapp.adapters.DiscoveryAdapter;
import com.chronosystems.nearbyapp.components.loader.ProgressView;
import com.chronosystems.nearbyapp.domain.messages.NearbyDevice;
import com.chronosystems.nearbyapp.domain.wrappers.NearbyDeviceWrapper;
import com.chronosystems.nearbyapp.interfaces.AppRecyclerViewOnClickListener;
import com.chronosystems.nearbyapp.services.NearbyMessagesClient;
import com.chronosystems.nearbyapp.utils.AppActivity;
import com.chronosystems.nearbyapp.utils.AppConstants;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.ArrayList;
import java.util.List;

/**
 * Discovery Fragment
 *
 * @author andrevaladas
 */
public class DiscoveryFragment extends Fragment implements AppRecyclerViewOnClickListener {

    private static final String TAG = DiscoveryFragment.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private List<NearbyDeviceWrapper> mList;
    private DiscoveryAdapter mAdapter;
    private ProgressView mProgressBar;
    private FloatingActionButton mFabDiscovery;
    private TextView mCountDown;
    private CountDownTimer mCountDownTimer;

    private NearbyMessagesClient mNearbyMessagesClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Use a retained fragment to avoid re-publishing or re-subscribing upon orientation changes.
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_discovery, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addOnItemTouchListener(new RecyclerViewTouchListener(getActivity(), mRecyclerView, this));

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);

        mList = new ArrayList<>();
        mAdapter = new DiscoveryAdapter(getActivity(), mList);
        mAdapter.setRecyclerViewOnClickListener(this);
        mRecyclerView.setAdapter(mAdapter);

        mProgressBar = (ProgressView) view.findViewById(R.id.pv_discovery);
        mFabDiscovery = (FloatingActionButton) view.findViewById(R.id.fab_discovery_action);
        mCountDown = (TextView) view.findViewById(R.id.tv_count_down);
        mCountDown.setVisibility(View.INVISIBLE);

        mProgressBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cancel discovery service
                cancelDiscovery();
            }
        });

        mFabDiscovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start discovery service
                startDiscovery();
            }
        });

        mCountDownTimer = new CountDownTimer(AppConstants.TIMER.DISCOVERY_IN_MILLISECONDS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mCountDown.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                Toast.makeText(getActivity(), "Finished", Toast.LENGTH_SHORT).show();
                cancelDiscovery();
            }
        };

        mNearbyMessagesClient = new NearbyMessagesClient(getContext(), new NearbyMessagesClient.NearbyDiscoveryListener() {
            @Override
            public void onServiceConnected() {
                // Auto-start the discovery service
                DiscoveryFragment.this.startDiscovery();
            }

            @Override
            public void onFound(NearbyDevice nearbyDevice) {
                // Add device to Card View
                DiscoveryFragment.this.addNearbyDevice(nearbyDevice);
            }

            @Override
            public void onLost(NearbyDevice nearbyDevice) {
                // Remove device when lost the connection reference
                DiscoveryFragment.this.mAdapter.removeListItem(nearbyDevice);
            }
        });
        return view;
    }

    @Override
    public void onDestroy() {
        cancelDiscovery();
        super.onDestroy();
    }

    private void startDiscovery() {
        if (View.INVISIBLE == mCountDown.getVisibility()) {
            mList.clear();
            mNearbyMessagesClient.startDiscovery();

            mFabDiscovery.setVisibility(View.INVISIBLE);
            try {
                YoYo.with(Techniques.FadeOut)
                        .duration(500)
                        .playOn(mFabDiscovery);
            } catch (Exception e) {
            }

            mProgressBar.setRefreshing(true);
            mCountDown.setVisibility(View.VISIBLE);
            mCountDownTimer.start();
        }
    }

    public void cancelDiscovery() {
        if (View.VISIBLE == mCountDown.getVisibility()) {
            mCountDown.setVisibility(View.INVISIBLE);
            mCountDownTimer.cancel();
            mNearbyMessagesClient.stopDiscovery();

            mProgressBar.setRefreshing(false);

            mFabDiscovery.setVisibility(View.VISIBLE);
            try {
                YoYo.with(Techniques.FadeInLeft)
                        .duration(1000)
                        .playOn(mFabDiscovery);
            } catch (Exception e) {
            }
        }
    }

    private void addNearbyDevice(final NearbyDevice nearbyDevice) {
        synchronized (TAG) {

            final LinearLayoutManager llm = (LinearLayoutManager) mRecyclerView.getLayoutManager();
            final int firstCompletelyVisibleItemPosition = llm.findFirstCompletelyVisibleItemPosition();

            // add device to card view
            mAdapter.addListItem(nearbyDevice, 0);

            //TODO verificar BUG de aceleracao do scroll conforme o aumento do numero de itens
            if (firstCompletelyVisibleItemPosition < 1) {
                mRecyclerView.smoothScrollBy(0, -mRecyclerView.getHeight());
            }
        }
    }

    @Override
    public void onClickListener(final View view, int position) {
        if (position > -1 && position < mList.size()) {
            final NearbyDeviceWrapper mDeviceDiscovery = mList.get(position);

            final Bundle extras = new Bundle();
            final NearbyDevice remoteNearbyDevice = mDeviceDiscovery.getParent();
            extras.putString(AppConstants.EXTRA.DEVICE_ID, remoteNearbyDevice.getDeviceId());
            extras.putString(AppConstants.EXTRA.DEVICE_ENDPOINT, remoteNearbyDevice.getEndpointId());
            extras.putString(AppConstants.EXTRA.DEVICE_BLUETOOTH, remoteNearbyDevice.getBluetooth());

            extras.putString(AppConstants.EXTRA.DEVICE_NAME, remoteNearbyDevice.getName());
            extras.putParcelable(AppConstants.EXTRA.DEVICE_PROFILE, mDeviceDiscovery.getProfileBitmap());

            AppActivity.go(getActivity(), RequestActivity.class, extras, AppConstants.ACTIVITY.REQUEST_RESULT_CODE, 1, false);
        }
    }

    @Override
    public void onLongPressClickListener(View view, int position) {
        Toast.makeText(getActivity(), "onLongPressClickListener(): " + position, Toast.LENGTH_SHORT).show();
        mAdapter.removeListItem(position);
    }

    private static class RecyclerViewTouchListener implements RecyclerView.OnItemTouchListener {
        private Context mContext;
        private GestureDetector mGestureDetector;
        private AppRecyclerViewOnClickListener mAppRecyclerViewOnClickListener;

        public RecyclerViewTouchListener(Context c, final RecyclerView rv, AppRecyclerViewOnClickListener rvocl) {
            mContext = c;
            mAppRecyclerViewOnClickListener = rvocl;

            mGestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public void onLongPress(MotionEvent e) {
                    super.onLongPress(e);

                    View cv = rv.findChildViewUnder(e.getX(), e.getY());
                    if (cv != null && mAppRecyclerViewOnClickListener != null) {
                        mAppRecyclerViewOnClickListener.onLongPressClickListener(cv, rv.getChildPosition(cv));
                    }
                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    View cv = rv.findChildViewUnder(e.getX(), e.getY());
                    if (cv != null && mAppRecyclerViewOnClickListener != null) {
                        // Adapter implements this event
                        //mAppRecyclerViewOnClickListener.onClickListener(cv, rv.getChildPosition(cv));
                    }
                    return (true);
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            mGestureDetector.onTouchEvent(e);
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
}
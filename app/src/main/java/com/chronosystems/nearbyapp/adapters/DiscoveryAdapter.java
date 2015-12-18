package com.chronosystems.nearbyapp.adapters;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chronosystems.nearbyapp.R;
import com.chronosystems.nearbyapp.domain.enumerations.ConnectorType;
import com.chronosystems.nearbyapp.domain.messages.NearbyDevice;
import com.chronosystems.nearbyapp.domain.wrappers.NearbyDeviceWrapper;
import com.chronosystems.nearbyapp.interfaces.AppRecyclerViewOnClickListener;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.List;

/**
 * Adapter for Device Discovery
 *
 * @author andrevaladas on 01/10/15.
 */
public class DiscoveryAdapter extends RecyclerView.Adapter<DiscoveryAdapter.MyViewHolder> {
    private Context mContext;
    private List<NearbyDeviceWrapper> mList;
    private LayoutInflater mLayoutInflater;
    private AppRecyclerViewOnClickListener mAppRecyclerViewOnClickListener;
    private float scale;
    private int width;
    private int height;

    public DiscoveryAdapter(Context c, List<NearbyDeviceWrapper> l) {
        mContext = c;
        mList = l;
        mLayoutInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        scale = mContext.getResources().getDisplayMetrics().density;
        width = mContext.getResources().getDisplayMetrics().widthPixels - (int) (14 * scale + 0.5f);
        height = (width / 16) * 9;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = mLayoutInflater.inflate(R.layout.item_discovery_card, viewGroup, false);
        MyViewHolder mvh = new MyViewHolder(v);
        return mvh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder myViewHolder, final int position) {

        // run in a different thread
        new Handler().postDelayed(new Runnable() {
            public void run() {
                final NearbyDeviceWrapper nearbyDeviceWrapper = mList.get(position);
                myViewHolder.ivCover.setImageBitmap(nearbyDeviceWrapper.getCoverImageBitmap());
                myViewHolder.ivPhoto.setImageBitmap(nearbyDeviceWrapper.getProfileBitmap());

                final NearbyDevice nearbyDevice = nearbyDeviceWrapper.getParent();
                myViewHolder.tvName.setText(nearbyDevice.getName());
                myViewHolder.tvStatus.setText("nearby.connections: " + nearbyDevice.getEndpointId());
                //myViewHolder.tvStatus.setText(nearbyDevice.getStatus());

                try {
                    YoYo.with(Techniques.FadeInLeft)
                            .duration(500)
                            .playOn(myViewHolder.itemView);
                } catch (Exception e) {
                }
                try {
                    YoYo.with(Techniques.FadeIn)
                            .duration(500)
                            .playOn(myViewHolder.ivCover);
                } catch (Exception e) {
                }
                try {
                    YoYo.with(Techniques.FlipInX)
                            .duration(1000)
                            .playOn(myViewHolder.ivPhoto);
                } catch (Exception e) {
                }
            }
        }, 1);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setRecyclerViewOnClickListener(AppRecyclerViewOnClickListener r) {
        mAppRecyclerViewOnClickListener = r;
    }

    public void addListItem(NearbyDevice c, int position) {
        mList.add(position, new NearbyDeviceWrapper(c));
        notifyItemInserted(position);
    }

    public void removeListItem(NearbyDevice nearbyDevice) {
        int positionToRemove = -1;
        for (int i = 0; i < mList.size(); i++) {
            NearbyDeviceWrapper item = mList.get(i);

            final ConnectorType connectorType = nearbyDevice.getConnectorType();
            if (ConnectorType.CONNECTIONS.equals(connectorType)) {
                final String endpointId = nearbyDevice.getEndpointId();
                if (!TextUtils.isEmpty(endpointId)) {
                    if (endpointId.equals(item.getParent().getEndpointId())) {
                        positionToRemove = i;
                        break;
                    }
                }
            } else {
                final String bluetoothAddress = nearbyDevice.getBluetooth();
                if (!TextUtils.isEmpty(bluetoothAddress)) {
                    if (bluetoothAddress.equals(item.getParent().getBluetooth())) {
                        positionToRemove = i;
                        break;
                    }
                }
            }
        }
        if (positionToRemove > -1) {
            removeListItem(positionToRemove);
        }
    }

    public void removeListItem(int position) {
        mList.remove(position);
        notifyItemRemoved(position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView ivCover;
        public ImageView ivPhoto;
        public TextView tvName;
        public TextView tvStatus;

        public MyViewHolder(View itemView) {
            super(itemView);

            ivCover = (ImageView) itemView.findViewById(R.id.iv_cover);
            ivPhoto = (ImageView) itemView.findViewById(R.id.iv_photo);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvStatus = (TextView) itemView.findViewById(R.id.tv_status);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mAppRecyclerViewOnClickListener != null) {
                mAppRecyclerViewOnClickListener.onClickListener(v, getPosition());
            }
        }
    }
}

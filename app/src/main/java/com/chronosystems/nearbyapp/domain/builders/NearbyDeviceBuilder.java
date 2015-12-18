package com.chronosystems.nearbyapp.domain.builders;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.chronosystems.nearbyapp.R;
import com.chronosystems.nearbyapp.domain.enumerations.ConnectorType;
import com.chronosystems.nearbyapp.domain.messages.NearbyDevice;
import com.chronosystems.nearbyapp.helper.ImageHelper;
import com.google.gson.Gson;

import java.nio.charset.Charset;

/**
 * Created by User on 09/10/2015.
 */
public class NearbyDeviceBuilder {

    public static String defaultProfileBitmap;
    public static String defaultCoverImageBitmap;

    public static NearbyDevice me(final Context context) {
        return me(context, null, null);
    }

    public static NearbyDevice me(final Context context, final String deviceId, String endpointId) {
        final BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();

        //final String instanceId = InstanceID.getInstance(context).getId();
        //final String deviceId = NearbyMessagesClient.getMyDeviceId();
        //final String endpointId = NearbyMessagesClient.getMyEndpointId();
        final String bluetooth = defaultAdapter.getAddress();

        //TODO find dada from app database
        boolean isAdmin = Build.SERIAL.equals("136dc19c");
        final String name = isAdmin ? "Andre Valadas" : "Camila Valadas";
        final String status = "Living the live!";
        final int profile = isAdmin ? R.drawable.profile : R.drawable.profile_2;
        final int coverImage = isAdmin ? R.drawable.wallpaper_2 : R.drawable.wallpaper;

        final NearbyDevice nearbyDevice = NearbyDeviceBuilder
                .with(context)
                .deviceId(deviceId)
                .endpointId(endpointId)
                .bluetooth(bluetooth)
                .name(name)
                .status(status)
                .profile(ImageHelper.reduceProfileRequest(context, profile))
                .coverImage(ImageHelper.reduceCoverImageRequest(context, coverImage))
                .build();

        return nearbyDevice;
    }

    public static ResultBuilder with(Context context) {

        if (defaultProfileBitmap == null) {
            defaultProfileBitmap = ImageHelper.reduceProfileRequest(context, R.drawable.default_profile);
        }
        if (defaultCoverImageBitmap == null) {
            defaultCoverImageBitmap = ImageHelper.reduceProfileRequest(context, R.drawable.wallpaper_3);
        }

        return new ResultBuilder();
    }

    public static class ResultBuilder {

        private NearbyDevice result;

        private ResultBuilder() {
            result = new NearbyDevice();
        }

        public ResultBuilder deviceId(String deviceId) {
            result.setDeviceId(deviceId);
            return this;
        }

        public ResultBuilder endpointId(String endpointId) {
            result.setEndpointId(endpointId);
            return this;
        }

        public ResultBuilder name(String name) {
            result.setName(name);
            return this;
        }

        public ResultBuilder status(String status) {
            result.setStatus(status);
            return this;
        }

        public ResultBuilder profile(String profile) {
            result.setProfile(profile);
            return this;
        }

        public ResultBuilder coverImage(String coverImage) {
            result.setCoverImage(coverImage);
            return this;
        }

        public ResultBuilder bluetooth(String address) {
            result.setBluetooth(address);
            return this;
        }

        public ResultBuilder nearbyBluetooth() {
            result.setConnectorType(ConnectorType.BLUETOOTH);
            return this;
        }

        public ResultBuilder nearbyConnections() {
            result.setConnectorType(ConnectorType.CONNECTIONS);
            return this;
        }

        public NearbyDevice build() {
            return result;
        }
    }
}

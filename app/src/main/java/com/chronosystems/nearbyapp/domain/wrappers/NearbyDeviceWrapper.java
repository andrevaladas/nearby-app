package com.chronosystems.nearbyapp.domain.wrappers;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.chronosystems.nearbyapp.domain.DeviceUser;
import com.chronosystems.nearbyapp.domain.builders.NearbyDeviceBuilder;
import com.chronosystems.nearbyapp.domain.messages.NearbyDevice;
import com.chronosystems.nearbyapp.helper.ImageHelper;

import java.io.Serializable;

/**
 * Discovery Domain To List Adapter
 *
 * @author andrevaladas
 */
public class NearbyDeviceWrapper implements Serializable {

    private Bitmap profileBitmap;
    private Bitmap coverImageBitmap;

    private NearbyDevice parent;
    private DeviceUser deviceUser;

    public NearbyDeviceWrapper(NearbyDevice parent) {
        this.parent = parent;
    }

    public Bitmap getProfileBitmap() {
        if (profileBitmap == null) {
            if (!TextUtils.isEmpty(parent.getProfile())) {
                profileBitmap = ImageHelper.decodeFromBase64(parent.getProfile());
            } else {
                profileBitmap = ImageHelper.decodeFromBase64(NearbyDeviceBuilder.defaultProfileBitmap);
            }
        }
        return profileBitmap;
    }

    public void setProfileBitmap(Bitmap profileBitmap) {
        this.profileBitmap = profileBitmap;
    }

    public Bitmap getCoverImageBitmap() {
        if (coverImageBitmap == null) {
            if (!TextUtils.isEmpty(parent.getCoverImage())) {
                coverImageBitmap = ImageHelper.decodeFromBase64(parent.getCoverImage());
            } else {
                coverImageBitmap = ImageHelper.decodeFromBase64(NearbyDeviceBuilder.defaultCoverImageBitmap);
            }
        }
        return coverImageBitmap;
    }

    public void setCoverImageBitmap(Bitmap coverImageBitmap) {
        this.coverImageBitmap = coverImageBitmap;
    }

    public NearbyDevice getParent() {
        return parent;
    }

    public DeviceUser getDeviceUser() {
        return deviceUser;
    }

    public void setDeviceUser(DeviceUser deviceUser) {
        this.deviceUser = deviceUser;
    }
}

package com.chronosystems.nearbyapp.domain.builders;

import android.content.Context;

import com.chronosystems.nearbyapp.R;
import com.chronosystems.nearbyapp.domain.messages.NearbyDevice;
import com.chronosystems.nearbyapp.domain.messages.NearbyMessage;
import com.chronosystems.nearbyapp.helper.ImageHelper;
import com.google.android.gms.iid.InstanceID;

/**
 * Created by User on 10/10/2015.
 */
public class MockBuilder {

    public static NearbyDevice newDeviceMessage(Context context, int rowCount) {

        final String instanceId = InstanceID.getInstance(context).getId();

        String[] names = new String[]{"Andre Valadas", "Camila Valadas", "Julie Valadas"};
        String[] status = new String[]{"Living the life!", "Mary Kay", "A lhasa apso dog"};
        int[] profiles = new int[]{R.drawable.profile, R.drawable.profile_2, R.drawable.profile_3};
        int[] coverImages = new int[]{R.drawable.wallpaper_2, R.drawable.wallpaper, R.drawable.wallpaper_3};

        final int profile = profiles[rowCount % profiles.length];
        final int coverImage = coverImages[rowCount % coverImages.length];
        final String name = names[rowCount % names.length];
        final String s = status[rowCount % status.length];

        final NearbyDevice nearbyDevice = NearbyDeviceBuilder
                .with(context)
                .deviceId(instanceId)
                .endpointId("136dc19c:760582879")
                .bluetooth("9C:D3:5B:59:9C:4B")
                .name(name)
                .status(s)
                .profile(ImageHelper.reduceProfileRequest(context, profile))
                .coverImage(ImageHelper.reduceCoverImageRequest(context, coverImage))
                .build();

        NearbyMessage.verifyContentLength(nearbyDevice);

        return nearbyDevice;
    }
}

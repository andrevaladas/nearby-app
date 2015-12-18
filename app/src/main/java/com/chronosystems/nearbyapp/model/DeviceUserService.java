package com.chronosystems.nearbyapp.model;

import android.content.Context;

import com.chronosystems.nearbyapp.domain.DeviceUser;
import com.chronosystems.nearbyapp.domain.messages.ChatMessage;

import java.util.ArrayList;

/**
 * Device User Service
 * <p>
 * Created by andrevaladas on 08/08/2015.
 */
public class DeviceUserService {

    private DeviceUserService() {
    }

    public static ResultBuilder with(final Context context) {
        return new ResultBuilder(context);
    }

    public static class ResultBuilder {
        private Context context;
        private DeviceUser result;

        private ResultBuilder(final Context context) {
            this.context = context;
            this.result = new DeviceUser();
        }

        public ResultBuilder deviceId(String deviceId) {
            result.setDeviceId(deviceId);
            return this;
        }

        public DeviceUser load() {

            // TODO search data from database
            final ArrayList<ChatMessage> messageList = result.getMessageList();
            /*if (messageList.isEmpty()) {
                messageList.add(new ChatMessage("WinkApp", "Welcome to WinkApp", false));
                messageList.add(new ChatMessage("WinkApp", "Send something and...", false));
                messageList.add(new ChatMessage("WinkApp", "enjoy it! ;)", false));
            }*/

            return result;
        }
    }
}

package com.chronosystems.nearbyapp.domain;

import android.graphics.Bitmap;

import com.chronosystems.nearbyapp.domain.enumerations.ConnectorType;
import com.chronosystems.nearbyapp.domain.messages.ChatMessage;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by User on 08/08/2015.
 */
public class DeviceUser implements Serializable {

    private String deviceId; // the device ID of the endpoint to connect to.
    private String endpointId; // the endpointID to connect to.
    private String bluetooth;
    private String name; //the name of the endpoint to connect to.
    private Bitmap profile;

    private ConnectorType connectorType;
    private ArrayList<ChatMessage> messageList = new ArrayList<>();

    public static ResultBuilder with(final ConnectorType connectorType) {
        return new ResultBuilder(connectorType);
    }

    public static class ResultBuilder {

        private DeviceUser result;

        private ResultBuilder(ConnectorType connectorType) {
            result = new DeviceUser();
            result.connectorType = connectorType;
        }

        public ResultBuilder name(String name) {
            result.name = name;
            return this;
        }

        public ResultBuilder profile(Bitmap profile) {
            result.profile = profile;
            return this;
        }

        public ResultBuilder bluetooth(String address) {
            result.bluetooth = address;
            return this;
        }

        public DeviceUser build() {
            return result;
        }
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getProfile() {
        return profile;
    }

    public void setProfile(Bitmap profile) {
        this.profile = profile;
    }

    public String getBluetooth() {
        return bluetooth;
    }

    public void setBluetooth(String bluetooth) {
        this.bluetooth = bluetooth;
    }

    public ConnectorType getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(ConnectorType connectorType) {
        this.connectorType = connectorType;
    }

    public ArrayList<ChatMessage> getMessageList() {
        return messageList;
    }

    public void setMessageList(ArrayList<ChatMessage> messageList) {
        this.messageList = messageList;
    }
}

package com.chronosystems.nearbyapp.domain.messages;

import com.chronosystems.nearbyapp.domain.enumerations.ConnectorType;

import java.io.Serializable;

/**
 * Discovery Domain
 *
 * @author andrevaladas
 */
public class NearbyDevice implements Serializable {

    private String deviceId; // the device ID of the endpoint to connect to.
    private String endpointId; // the endpointID to connect to.
    private String bluetooth;
    private String name; //the name of the endpoint to connect to.

    private String status;
    private String profile;
    private String coverImage;

    private ConnectorType connectorType;

    public NearbyDevice() {
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

    public void setName(String endpointName) {
        this.name = endpointName;
    }

    public String getBluetooth() {
        return bluetooth;
    }

    public void setBluetooth(String bluetooth) {
        this.bluetooth = bluetooth;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public ConnectorType getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(ConnectorType connectorType) {
        this.connectorType = connectorType;
    }
}

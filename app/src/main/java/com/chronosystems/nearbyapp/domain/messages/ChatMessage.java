package com.chronosystems.nearbyapp.domain.messages;

import java.io.Serializable;
import java.util.Date;

public class ChatMessage implements Serializable {

    private String userName, text;
    private Date time;
    private boolean self;
    private Boolean newSender;

    public ChatMessage(String userName, String text, boolean self) {
        this.userName = userName;
        this.text = text;
        this.self = self;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getTime() { return time; }

    public void setTime(Date time) { this.time = time; }

    public boolean isSelf() {
        return self;
    }

    public void setSelf(boolean self) {
        this.self = self;
    }

    public void setNewSender(boolean newSender) {
        this.newSender = newSender;
    }

    public Boolean isNewSender() {
        return newSender;
    }
}

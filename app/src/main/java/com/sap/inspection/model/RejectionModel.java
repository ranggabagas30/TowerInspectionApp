package com.sap.inspection.model;

import org.parceler.Parcel;

@Parcel(Parcel.Serialization.BEAN)
public class RejectionModel {
    private String title;
    private String messages;

    public RejectionModel() {}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessages() {
        return messages;
    }

    public void setMessages(String messages) {
        this.messages = messages;
    }
}

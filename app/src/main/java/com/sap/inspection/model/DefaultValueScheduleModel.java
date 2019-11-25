package com.sap.inspection.model;

import org.parceler.Parcel;

@Parcel(Parcel.Serialization.BEAN)
public class DefaultValueScheduleModel extends BaseModel {
    private int item_id;
    private int group_id;
    private int form_id;
    private String default_value;

    public DefaultValueScheduleModel() {

    }

    public int getItem_id() {
        return item_id;
    }

    public void setItem_id(int item_id) {
        this.item_id = item_id;
    }

    public int getGroup_id() {
        return group_id;
    }

    public void setGroup_id(int group_id) {
        this.group_id = group_id;
    }

    public int getForm_id() {
        return form_id;
    }

    public void setForm_id(int form_id) {
        this.form_id = form_id;
    }

    public String getDefault_value() {
        return default_value;
    }

    public void setDefault_value(String default_value) {
        this.default_value = default_value;
    }
}

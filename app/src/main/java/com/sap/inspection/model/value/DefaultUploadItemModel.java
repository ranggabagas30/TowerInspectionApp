package com.sap.inspection.model.value;

import com.sap.inspection.model.PictureModel;

public class DefaultUploadItemModel {

    private int id;
    private String value;
    private int schedule_id;
    private int site_id;
    private int operator_id;
    private int work_form_group_item_id;
    private String remark;
    private String photo_status = null;
    private String accuracy = null;
    private String latitude = null;
    private String longitude = null;
    private int warga_id;
    private int barang_id;
    private PictureModel picture = null;
    private String photo_datetime = null;
    private int row_id;


    // Getter Methods

    public int getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public int getSchedule_id() {
        return schedule_id;
    }

    public int getSite_id() {
        return site_id;
    }

    public int getOperator_id() {
        return operator_id;
    }

    public int getWork_form_group_item_id() {
        return work_form_group_item_id;
    }

    public String getRemark() {
        return remark;
    }

    public String getPhoto_status() {
        return photo_status;
    }

    public String getAccuracy() {
        return accuracy;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public int getWarga_id() {
        return warga_id;
    }

    public int getBarang_id() {
        return barang_id;
    }

    public PictureModel getPicture() {
        return picture;
    }

    public String getPhoto_datetime() {
        return photo_datetime;
    }

    public int getRow_id() {
        return row_id;
    }

    // Setter Methods

    public void setId( int id ) {
        this.id = id;
    }

    public void setValue( String value ) {
        this.value = value;
    }

    public void setSchedule_id( int schedule_id ) {
        this.schedule_id = schedule_id;
    }

    public void setSite_id( int site_id ) {
        this.site_id = site_id;
    }

    public void setOperator_id( int operator_id ) {
        this.operator_id = operator_id;
    }

    public void setWork_form_group_item_id( int work_form_group_item_id ) {
        this.work_form_group_item_id = work_form_group_item_id;
    }

    public void setRemark( String remark ) {
        this.remark = remark;
    }

    public void setPhoto_status( String photo_status ) {
        this.photo_status = photo_status;
    }

    public void setAccuracy( String accuracy ) {
        this.accuracy = accuracy;
    }

    public void setLatitude( String latitude ) {
        this.latitude = latitude;
    }

    public void setLongitude( String longitude ) {
        this.longitude = longitude;
    }

    public void setWarga_id(int warga_id) {
        this.warga_id = warga_id;
    }

    public void setBarang_id(int barang_id) {
        this.barang_id = barang_id;
    }

    public void setPicture(PictureModel picture) {
        this.picture = picture;
    }

    public void setPhoto_datetime(String photo_datetime ) {
        this.photo_datetime = photo_datetime;
    }

    public void setRow_id( int row_id ) {
        this.row_id = row_id;
    }
}

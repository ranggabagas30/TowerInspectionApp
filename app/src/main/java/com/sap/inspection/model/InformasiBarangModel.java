package com.sap.inspection.model;

import org.parceler.Parcel;

@Parcel(Parcel.Serialization.BEAN)
public class InformasiBarangModel {

    private int id;
    private int citizen_id;
    private int schedule_id;
    private String nama;
    private String photo_depan = null;
    private String photo_belakang = null;
    private String photo_serial = null;
    private String category_barang = null;
    private String type_barang = null;
    private String brand_barang = null;
    private String ukuran_barang = null;
    private String nilai_claim = null;
    private String harga_baru = null;
    private String selisih = null;
    private String sumber_harga = null;
    private String created_at;
    private String updated_at;

    public InformasiBarangModel() {}

    // Getter Methods

    public int getId() {
        return id;
    }

    public int getCitizenId() {
        return citizen_id;
    }

    public int getScheduleId() {
        return schedule_id;
    }

    public String getNama() {
        return nama;
    }

    public String getPhotoDepan() {
        return photo_depan;
    }

    public String getPhotoBelakang() {
        return photo_belakang;
    }

    public String getPhotoSerial() {
        return photo_serial;
    }

    public String getCategoryBarang() {
        return category_barang;
    }

    public String getTypeBarang() {
        return type_barang;
    }

    public String getBrandBarang() {
        return brand_barang;
    }

    public String getUkuranBarang() {
        return ukuran_barang;
    }

    public String getNilaiClaim() {
        return nilai_claim;
    }

    public String getHargaBaru() {
        return harga_baru;
    }

    public String getSelisih() {
        return selisih;
    }

    public String getSumberHarga() {
        return sumber_harga;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    // Setter Methods

    public void setId( int id ) {
        this.id = id;
    }

    public void setCitizenId( int citizen_id ) {
        this.citizen_id = citizen_id;
    }

    public void setScheduleId( int schedule_id ) {
        this.schedule_id = schedule_id;
    }

    public void setNama( String nama ) {
        this.nama = nama;
    }

    public void setPhotoDepan( String photo_depan ) {
        this.photo_depan = photo_depan;
    }

    public void setPhotoBelakang( String photo_belakang ) {
        this.photo_belakang = photo_belakang;
    }

    public void setPhotoSerial( String photo_serial ) {
        this.photo_serial = photo_serial;
    }

    public void setCategoryBarang( String category_barang ) {
        this.category_barang = category_barang;
    }

    public void setTypeBarang( String type_barang ) {
        this.type_barang = type_barang;
    }

    public void setBrandBarang( String brand_barang ) {
        this.brand_barang = brand_barang;
    }

    public void setUkuranBarang( String ukuran_barang ) {
        this.ukuran_barang = ukuran_barang;
    }

    public void setNilaiClaim( String nilai_claim ) {
        this.nilai_claim = nilai_claim;
    }

    public void setHargaBaru( String harga_baru ) {
        this.harga_baru = harga_baru;
    }

    public void setSelisih( String selisih ) {
        this.selisih = selisih;
    }

    public void setSumberHarga( String sumber_harga ) {
        this.sumber_harga = sumber_harga;
    }

    public void setCreated_at( String created_at ) {
        this.created_at = created_at;
    }

    public void setUpdated_at( String updated_at ) {
        this.updated_at = updated_at;
    }
}

package com.sap.inspection.model;

import org.parceler.Parcel;

@Parcel(Parcel.Serialization.BEAN)
public class InformasiDiriModel {

    private int id;
    private int schedule_id;
    private String no_ktp = null;
    private String nama;
    private String alamat = null;
    private String rt_rw = null;
    private String kelurahan = null;
    private String kecamatan = null;
    private String kabupaten = null;
    private String surat_pernyataan = null;
    private String ktp_kk = null;
    private String created_at;
    private String updated_at;

    public InformasiDiriModel() {}

    // Getter Methods
    public int getId() {
        return id;
    }

    public int getScheduleId() {
        return schedule_id;
    }

    public String getNoKTP() {
        return no_ktp;
    }

    public String getNama() {
        return nama;
    }

    public String getAlamat() {
        return alamat;
    }

    public String getRt_rw() {
        return rt_rw;
    }

    public String getKelurahan() {
        return kelurahan;
    }

    public String getKecamatan() {
        return kecamatan;
    }

    public String getKabupaten() {
        return kabupaten;
    }

    public String getSuratPernyataan() {
        return surat_pernyataan;
    }

    public String getKtp_kk() {
        return ktp_kk;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    // Setter Methods
    public void setId(int id ) {
        this.id = id;
    }

    public void setScheduleId(int schedule_id ) {
        this.schedule_id = schedule_id;
    }

    public void setNoKTP( String no_ktp ) {
        this.no_ktp = no_ktp;
    }

    public void setNama( String nama ) {
        this.nama = nama;
    }

    public void setAlamat( String alamat ) {
        this.alamat = alamat;
    }

    public void setRt_rw( String rt_rw ) {
        this.rt_rw = rt_rw;
    }

    public void setKelurahan( String kelurahan ) {
        this.kelurahan = kelurahan;
    }

    public void setKecamatan( String kecamatan ) {
        this.kecamatan = kecamatan;
    }

    public void setKabupaten( String kabupaten ) {
        this.kabupaten = kabupaten;
    }

    public void setSuratPernyataan( String surat_pernyataan ) {
        this.surat_pernyataan = surat_pernyataan;
    }

    public void setKtp_kk( String ktp_kk ) {
        this.ktp_kk = ktp_kk;
    }

    public void setCreated_at( String created_at ) {
        this.created_at = created_at;
    }

    public void setUpdated_at( String updated_at ) {
        this.updated_at = updated_at;
    }
}

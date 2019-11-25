package com.sap.inspection.model.config.formimbaspetir;

public class Barang
{
    private String barangid;
    private String barangke;
    private boolean registered = false;

    public String getBarangid ()
    {
        return barangid;
    }

    public void setBarangId (String barangid)
    {
        this.barangid = barangid;
    }

    public String getBarangke() {
        return barangke;
    }

    public void setBarangke(String barangke) {
        this.barangke = barangke;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [barangid = "+barangid+"]";
    }
}

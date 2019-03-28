package com.sap.inspection.model.config.formimbaspetir;

public class Barang
{
    private String barangid;

    public String getBarangid ()
    {
        return barangid;
    }

    public void setBarangid (String barangid)
    {
        this.barangid = barangid;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [barangid = "+barangid+"]";
    }
}

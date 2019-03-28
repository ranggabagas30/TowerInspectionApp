package com.sap.inspection.model.config.formimbaspetir;

import java.util.ArrayList;

public class ImbasPetirData
{
    private String scheduleid;
    private ArrayList<Warga> warga;

    public ArrayList<Warga> getWarga ()
    {
        return warga;
    }

    public void setWarga (ArrayList<Warga> warga)
    {
        this.warga = warga;
    }

    public String getScheduleid ()
    {
        return scheduleid;
    }

    public void setScheduleid (String scheduleid)
    {
        this.scheduleid = scheduleid;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [warga = "+warga+", scheduleid = "+scheduleid+"]";
    }
}

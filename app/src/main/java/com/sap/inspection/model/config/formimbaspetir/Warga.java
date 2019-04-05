package com.sap.inspection.model.config.formimbaspetir;

import java.util.ArrayList;

public class Warga
{
    private String wargaid;
    private boolean registered;
    private ArrayList<Barang> barang;

    public ArrayList<Barang> getBarang ()
    {
        return barang;
    }

    public void setBarang (ArrayList<Barang> barang)
    {
        this.barang = barang;
    }

    public String getWargaid ()
    {
        return wargaid;
    }

    public void setWargaid (String wargaid)
    {
        this.wargaid = wargaid;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public boolean isRegistered() {
        return registered;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [barang = "+barang+", wargaid = "+wargaid+"]";
    }
}

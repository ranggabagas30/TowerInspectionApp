package com.sap.inspection.model.config.formimbaspetir;

import java.util.ArrayList;

public class Warga
{
    private String wargaid;
    private int wargake;
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

    public int getWargake() {
        return wargake;
    }

    public void setWargake(int wargake) {
        this.wargake = wargake;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [barang = "+barang+", wargaid = "+wargaid+", wargake = " + wargake + "]";
    }
}

package com.sap.inspection.model.config.formimbaspetir;

import java.util.ArrayList;

public class Warga
{
    private String wargaid;
    private String wargake;
    private boolean registered = false;
    private ArrayList<Barang> barang;
    private int countaddbarang = 0;

    public String getWargaid ()
    {
        return wargaid;
    }

    public void setWargaId (String wargaid)
    {
        this.wargaid = wargaid;
    }

    public String getWargake() {
        return wargake;
    }

    public void setWargake(String wargake) {
        this.wargake = wargake;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public boolean isRegistered() {
        return registered;
    }

    public ArrayList<Barang> getBarang ()
    {
        return barang;
    }

    public void setBarang (ArrayList<Barang> barang)
    {
        this.barang = barang;
    }

    public int getCountaddbarang() {
        return countaddbarang;
    }

    public void setCountaddbarang(int countaddbarang) {
        this.countaddbarang = countaddbarang;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [barang = "+barang+", wargaid = "+wargaid+"]";
    }
}

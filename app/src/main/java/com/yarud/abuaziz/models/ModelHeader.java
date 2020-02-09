package com.yarud.abuaziz.models;

import com.yarud.abuaziz.utils.RecyclerViewItem;

public class ModelHeader extends RecyclerViewItem {
    private String NAMAPROFILE;
    private String EMAILPROFILE;
    private String IMAGEURL;

    public ModelHeader(String NAMAPROFILE, String EMAILPROFILE, String IMAGEURL) {
        this.NAMAPROFILE = NAMAPROFILE;
        this.EMAILPROFILE = EMAILPROFILE;
        this.IMAGEURL = IMAGEURL;
    }

    public String getNAMAPROFILE() {
        return NAMAPROFILE;
    }

    public void setNAMAPROFILE(String NAMAPROFILE) {
        this.NAMAPROFILE = NAMAPROFILE;
    }

    public String getEMAILPROFILE() {
        return EMAILPROFILE;
    }

    public void setEMAILPROFILE(String EMAILPROFILE) {
        this.EMAILPROFILE = EMAILPROFILE;
    }

    public String getIMAGEURL() {
        return IMAGEURL;
    }

    public void setIMAGEURL(String IMAGEURL) {
        this.IMAGEURL = IMAGEURL;
    }
}

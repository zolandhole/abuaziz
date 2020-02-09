package com.yarud.abuaziz.models;

import com.yarud.abuaziz.utils.RecyclerViewItem;

public class ModelFooter extends RecyclerViewItem {

    private String QUOTE;
    private String AUTHOR;
    private String IMAGEURL;

    public ModelFooter(String QUOTE, String AUTHOR, String IMAGEURL) {
        this.QUOTE = QUOTE;
        this.AUTHOR = AUTHOR;
        this.IMAGEURL = IMAGEURL;
    }

    public String getQUOTE() {
        return QUOTE;
    }

    public void setQUOTE(String QUOTE) {
        this.QUOTE = QUOTE;
    }

    public String getAUTHOR() {
        return AUTHOR;
    }

    public void setAUTHOR(String AUTHOR) {
        this.AUTHOR = AUTHOR;
    }

    public String getIMAGEURL() {
        return IMAGEURL;
    }

    public void setIMAGEURL(String IMAGEURL) {
        this.IMAGEURL = IMAGEURL;
    }
}

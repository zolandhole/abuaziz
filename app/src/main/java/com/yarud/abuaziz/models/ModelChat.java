package com.yarud.abuaziz.models;

import com.yarud.abuaziz.utils.RecyclerViewItem;

public class ModelChat extends RecyclerViewItem {
    private String pesan, waktu, jam, id_login, photo, uniq_id;

    public ModelChat(String pesan, String waktu, String jam, String id_login, String photo, String uniq_id) {
        this.pesan = pesan;
        this.waktu = waktu;
        this.jam = jam;
        this.id_login = id_login;
        this.photo = photo;
        this.uniq_id = uniq_id;
    }

    public String getPesan() {
        return pesan;
    }

    public String getWaktu() {
        return waktu;
    }

    public String getJam() {
        return jam;
    }

    public String getId_login() {
        return id_login;
    }

    public String getPhoto() {
        return photo;
    }

    public String getUniq_id() {
        return uniq_id;
    }
}
